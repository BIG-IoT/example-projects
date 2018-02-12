/*
 *	Copyright (c) 2017 by Contributors of the BIG IoT Project Consortium (see below).
 *	All rights reserved.
 *
 *	This source code is licensed under the MIT license found in the
 * 	LICENSE file in the root directory of this source tree.
 *
 *	Contributor:
 *	- Robert Bosch GmbH
 *	    > Stefan Schmid (stefan.schmid@bosch.com)
 */
package android.bigiot.org.androidexampleconsumer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.bigiot.lib.android.Consumer;
import org.eclipse.bigiot.lib.android.IAuthenticationHandler;
import org.eclipse.bigiot.lib.android.IDiscoveryHandler;
import org.eclipse.bigiot.lib.android.IResponseHandler;
import org.eclipse.bigiot.lib.android.ISubscriptionHandler;
import org.eclipse.bigiot.lib.exceptions.IncompleteOfferingQueryException;
import org.eclipse.bigiot.lib.model.BigIotTypes;
import org.eclipse.bigiot.lib.model.Price;
import org.eclipse.bigiot.lib.model.RDFType;
import org.eclipse.bigiot.lib.model.ValueType;
import org.eclipse.bigiot.lib.offering.AccessParameters;
import org.eclipse.bigiot.lib.offering.AccessResponse;
import org.eclipse.bigiot.lib.offering.OfferingCore;
import org.eclipse.bigiot.lib.offering.OfferingDescription;
import org.eclipse.bigiot.lib.query.OfferingQuery;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IDiscoveryHandler, IResponseHandler, ISubscriptionHandler, IAuthenticationHandler {

    private static final String MARKETPLACE_URI = "https://market.big-iot.org";
    private static final String CONSUMER_ID     = "TestOrganization-AndroidConsumer";
    private static final String CONSUMER_SECRET = "bdKorgCSRcWVLZvo2DrjQQ==";

    private Consumer consumer = null;
    private OfferingCore offering = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onAccessOffering(View view) throws Exception {

        if (consumer == null) {

            consumer = new Consumer(CONSUMER_ID, MARKETPLACE_URI);
            consumer.authenticateByTask(CONSUMER_SECRET, this);

        } else {

            if (this.offering != null) {
                AccessParameters accessParameters = AccessParameters.create();
                        //  .addRdfTypeValue(new RDFType("schema:longitude"), 41.4)
                        //  .addRdfTypeValue(new RDFType("schema:latitude"), 2.17)
                        //  .addRdfTypeValue(new RDFType("schema:geoRadius"), 1000);
                consumer.accessByTask(this.offering, accessParameters, this);
            }

        }

        return true;

    }

    @Override
    public void onAuthenticate(String result) {

        if (result.equals(IAuthenticationHandler.AUTHENTICATION_OK)) {

            Toast.makeText(getApplicationContext(), "Authentication Successful!", Toast.LENGTH_SHORT).show();

            try {

                // CASE 1: Offering is already known - i.e. know discovery is needed and offering can be directly subscribed
                consumer.subscribeByTask("TestOrganization-TestProvider-RandomNumberOffering", this);
                // END of CASE 1

                // CASE 2: Offering not yet known, i.e. OfferingQuery is used to discovery matching offerings on Marketplace
                OfferingQuery query = Consumer.createOfferingQuery("Example_RandomNumber_Query")
                        .withName("Example Random Number Query")
                        .withCategory("urn:proposed:RandomValues")
                        .addOutputData(new RDFType("proposed:randomValue"), ValueType.NUMBER)
                        .withPricingModel(BigIotTypes.PricingModel.PER_ACCESS)
                        .withMaxPrice(Price.Euros.amount(0.02))
                        .withLicenseType(BigIotTypes.LicenseType.OPEN_DATA_LICENSE);

                consumer.discoverByTask(query, this);
                // END OF CASE 2

            } catch (IncompleteOfferingQueryException e) {
                Toast.makeText(getApplicationContext(), "ERROR: Offering Query invalid!", Toast.LENGTH_LONG).show();
            }

        }
        else {
            Toast.makeText(getApplicationContext(), "ERROR: Authentication on Marketplace failed - check secret", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onDiscoveryResponse(OfferingQuery query, List<OfferingDescription> offeringDescriptions) {

        if ((offeringDescriptions != null) && !offeringDescriptions.isEmpty()) {

            OfferingDescription selectedOfferingDescription = offeringDescriptions.get(0);
            consumer.subscribeByTask(selectedOfferingDescription, this);
            Toast.makeText(getApplicationContext(), "Offering found: " + selectedOfferingDescription.getId(), Toast.LENGTH_SHORT).show();

        }
        else {
            Toast.makeText(getApplicationContext(), "No Offerings found", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSubscriptionResponse(OfferingDescription offeringDescription, OfferingCore offering) {

        if (offering != null) {

            this.offering = offering;
            Toast.makeText(getApplicationContext(), "Subscription successful!", Toast.LENGTH_SHORT).show();

            AccessParameters accessParameters = AccessParameters.create();
            consumer.accessByTask(this.offering, accessParameters, this);

        }
        else {
            Toast.makeText(getApplicationContext(), "Subscription failed!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onAccessResponse(OfferingCore offeringCore, AccessResponse accessResponse) {

        if (accessResponse != null) {

            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setText(accessResponse.getBody());
        }

    }

}