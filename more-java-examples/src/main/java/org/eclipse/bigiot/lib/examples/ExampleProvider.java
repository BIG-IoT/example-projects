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
import java.util.Scanner;

import org.eclipse.bigiot.lib.ProviderSpark;
import org.eclipse.bigiot.lib.exceptions.IncompleteOfferingDescriptionException;
import org.eclipse.bigiot.lib.exceptions.NotRegisteredException;
import org.eclipse.bigiot.lib.handlers.AccessRequestHandler;
import org.eclipse.bigiot.lib.model.BigIotTypes.LicenseType;
import org.eclipse.bigiot.lib.model.BigIotTypes.PricingModel;
import org.eclipse.bigiot.lib.model.Price.Euros;
import org.eclipse.bigiot.lib.model.RDFType;
import org.eclipse.bigiot.lib.model.ValueType;
import org.eclipse.bigiot.lib.offering.OfferingDescription;
import org.eclipse.bigiot.lib.offering.RegistrableOfferingDescription;
import org.eclipse.bigiot.lib.serverwrapper.BigIotHttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Example for using BIG IoT API as a provider.
 */
public class ExampleProvider {

    private static final String MARKETPLACE_URI = "https://market.big-iot.org";
    // private static final String MARKETPLACE_URI = "https://market-int.big-iot.org";
    // private static final String MARKETPLACE_URI = "https://market-dev.big-iot.org";

    private static final String PROVIDER_ID = "Null_Island-Happy_Parkings";
    private static final String PROVIDER_SECRET = "kETI8TK1QjC5whzgNDG9gw==";

    private static AccessRequestHandler accessCallbackDummy = new AccessRequestHandler() {
        @Override
        public BigIotHttpResponse processRequestHandler(OfferingDescription offeringDescription,
                Map<String, Object> inputData, String subscriberId, String consumerInfo) {

            double longitude;
            double latitude;

            BigIotHttpResponse errorResponse = BigIotHttpResponse.error().withBody("{\"status\":\"error\"}")
                    .withStatus(422).asJsonType();

            if (!inputData.containsKey("longitude"))
                return errorResponse;
            longitude = new Double((String) inputData.get("longitude"));

            if (!inputData.containsKey("latitude"))
                return errorResponse;
            latitude = new Double((String) inputData.get("latitude"));

            Random r = new Random();
            JSONArray jsonArray = new JSONArray();
            int n = Math.round(r.nextFloat() * 10 + 10);
            for (int i = 0; i < n; i++) {
                JSONObject jsonObject = new JSONObject().put("lat", latitude + r.nextFloat() * 0.01)
                        .put("lon", longitude + r.nextFloat() * 0.01)
                        .put("status", r.nextBoolean() ? "available" : "occupied");
                jsonArray.put(jsonObject);
            }

            return BigIotHttpResponse.okay().withBody(jsonArray);
        }
    };

    public static void main(String[] args)
            throws IncompleteOfferingDescriptionException, IOException, NotRegisteredException {

        // Initialize provider with provider id and Marketplace URI
        ProviderSpark provider = new ProviderSpark(PROVIDER_ID, MARKETPLACE_URI, "localhost", 9003);

        // provider.setProxy("127.0.0.1", 3128); //Enable this line if you are behind a proxy
        // provider.addProxyBypass("172.17.17.100"); //Enable this line and the addresses for internal hosts

        // Authenticate provider on the marketplace
        provider.authenticate(PROVIDER_SECRET);

        // Construct Offering Description of your Offering incrementally
        RegistrableOfferingDescription offeringDescription =
                // provider.createOfferingDescriptionFromOfferingId("TestOrganization-TestProvider-Manual_Offering_Test")
                provider.createOfferingDescription("ParkingSpotProvider")
                        .withInformation("Demo Parking Offering", new RDFType("bigiot:Parking"))
                        .addInputData("longitude", new RDFType("schema:longitude"), ValueType.NUMBER)
                        .addInputData("latitude", new RDFType("schema:latitude"), ValueType.NUMBER)
                        .addInputData("radius", new RDFType("schema:geoRadius"), ValueType.NUMBER)
                        .addOutputData("lon", new RDFType("schema:longitude"), ValueType.NUMBER)
                        .addOutputData("lat", new RDFType("schema:latitude"), ValueType.NUMBER)
                        .addOutputData("dist", new RDFType("datex:distanceFromParkingSpace"), ValueType.NUMBER)
                        .addOutputData("status", new RDFType("datex:parkingSpaceStatus"), ValueType.TEXT)
                        // .inCity("Barcelona")
                        .withPrice(Euros.amount(0.001)).withPricingModel(PricingModel.PER_ACCESS)
                        .withLicenseType(LicenseType.OPEN_DATA_LICENSE)
                        // Below is actually Offering specific
                        .withAccessRequestHandler(accessCallbackDummy);

        provider.register(offeringDescription);

        // Run until user input is obtained
        System.out.println(">>>>>>  Terminate ExampleProvider by pressing ENTER  <<<<<<");
        Scanner keyboard = new Scanner(System.in);
        keyboard.nextLine();
        keyboard.close();

        // Deregister your offering form Marketplace
        provider.deregister(offeringDescription);

        // Terminate provider instance
        provider.terminate();

    }

}
