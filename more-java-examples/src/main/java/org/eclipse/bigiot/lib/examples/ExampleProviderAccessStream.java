/**
 *      Copyright (c) 2017 by Contributors of the BIG IoT Project Consortium (see below).
 *      All rights reserved.
 *
 *      This source code is licensed under the MIT license found in the
 *      LICENSE file in the root directory of this source tree.
 *
 */
package org.eclipse.bigiot.lib.examples;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import org.eclipse.bigiot.lib.ProviderSpark;
import org.eclipse.bigiot.lib.exceptions.IncompleteOfferingDescriptionException;
import org.eclipse.bigiot.lib.exceptions.NotRegisteredException;
import org.eclipse.bigiot.lib.handlers.AccessStreamFilterHandler;
import org.eclipse.bigiot.lib.misc.BridgeIotProperties;
import org.eclipse.bigiot.lib.model.BigIotTypes.LicenseType;
import org.eclipse.bigiot.lib.model.BigIotTypes.PricingModel;
import org.eclipse.bigiot.lib.model.Price.Euros;
import org.eclipse.bigiot.lib.model.RDFType;
import org.eclipse.bigiot.lib.model.ValueType;
import org.eclipse.bigiot.lib.offering.OfferingDescription;
import org.eclipse.bigiot.lib.offering.RegisteredOffering;
import org.eclipse.bigiot.lib.offering.RegistrableOfferingDescription;
import org.json.JSONObject;

/**
 * Example for using BIG IoT API as a provider. This example corresponds with ExampleConsumerNew.java
 */
public class ExampleProviderAccessStream {

    private static AccessStreamFilterHandler accessStreamFilterCallback = new AccessStreamFilterHandler() {
        @Override
        public boolean processRequestHandler(OfferingDescription offeringDescription, JSONObject jsonObj,
                Map<String, Object> inputData, String subscriberId, String consumerInfo) {

            double longitude = 9.0;
            if (inputData.containsKey("longitude"))
                longitude = new Double((String) inputData.get("longitude"));

            double latitude = 42.0;
            if (inputData.containsKey("latitude"))
                latitude = new Double((String) inputData.get("latitude"));

            double lon = jsonObj.getDouble("longitude");
            double lat = jsonObj.getDouble("latitude");

            if ((Math.abs(latitude - lat) < 0.005) || (Math.abs(longitude - lon) < 0.005))
                return true;

            return false;
        };
    };

    public static void main(String[] args)
            throws InterruptedException, IncompleteOfferingDescriptionException, IOException, NotRegisteredException {
        
        // Load example properties file
        BridgeIotProperties prop = BridgeIotProperties.load("example.properties");

        // Initialize provider with provider id and Marketplace URI
        ProviderSpark provider = new ProviderSpark(prop.PROVIDER_ID, prop.MARKETPLACE_URI, prop.PROVIDER_DNS_NAME, prop.PROVIDER_PORT);

        // provider.setProxy("127.0.0.1", 3128); //Enable this line if you are behind a proxy
        // provider.addProxyBypass("172.17.17.100"); //Enable this line and the addresses for internal hosts

        // Authenticate provider on the marketplace
        provider.authenticate(prop.PROVIDER_SECRET);

        // Construct Offering Description of your Offering incrementally
        RegistrableOfferingDescription offeringDescription =
                // provider.createOfferingDescriptionFromOfferingId("TestOrganization-TestProvider-Manual_Offering_Test")
                provider.createOfferingDescription("ParkingSpotProvider2")
                        .withInformation("Demo Parking Offering with Access Stream", new RDFType("bigiot:Parking"))
                        .addInputData("longitude", new RDFType("schema:longitude"), ValueType.NUMBER)
                        .addInputData("latitude", new RDFType("schema:latitude"), ValueType.NUMBER)
                        .addInputData("radius", new RDFType("schema:geoRadius"), ValueType.NUMBER)
                        .addOutputData("longitude", new RDFType("schema:longitude"), ValueType.NUMBER)
                        .addOutputData("latitude", new RDFType("schema:latitude"), ValueType.NUMBER)
                        .addOutputData("status", new RDFType("datex:parkingSpaceStatus"), ValueType.TEXT)
                        .inCity("Barcelona").withPrice(Euros.amount(0.001)).withPricingModel(PricingModel.PER_ACCESS)
                        .withLicenseType(LicenseType.OPEN_DATA_LICENSE)
                        // Below is actually Offering specific
                        .withAccessStreamFilterHandler(accessStreamFilterCallback); // Optional if no filtering is
                                                                                    // needed

        RegisteredOffering offering = provider.register(offeringDescription);

        // Run until user input is obtained
        System.out.println(">>>>>>  Terminate ExampleProvider by pressing ENTER  <<<<<<");

        int i = 0;
        Random r = new Random();
        while (System.in.available() == 0) {

            JSONObject jsonObject = new JSONObject().put("latitude", 42.0 + r.nextFloat() * 0.01)
                    .put("longitude", 9.0 + r.nextFloat() * 0.01)
                    .put("status", r.nextBoolean() ? "available" : "occupied");

            // add new Output Data element to the Offering Access Stream=
            offering.queue(jsonObject);

            System.out.println("Add output data element: " + jsonObject.toString());

            Thread.sleep(4000);

            // Optional
            if (i++ % 100 == 0) {
                offering.flush(); // To flush old elements in the Offering Access Stream
                System.out.println("Flushed offering access stream");
            }

        }

        // Deregister your offering form Marketplace
        offering.deregister();

        // Terminate provider instance
        provider.terminate();

    }

}
