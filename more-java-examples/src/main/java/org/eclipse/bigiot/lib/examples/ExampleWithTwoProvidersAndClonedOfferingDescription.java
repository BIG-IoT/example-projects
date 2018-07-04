/**
 * Copyright (c) 2016-2017 in alphabetical order:
 * Bosch Software Innovations GmbH, Robert Bosch GmbH, Siemens AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Denis Kramer     (Bosch Software Innovations GmbH)
 *    Stefan Schmid    (Robert Bosch GmbH)
 *    Andreas Ziller   (Siemens AG)
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
import org.eclipse.bigiot.lib.misc.BridgeIotProperties;
import org.eclipse.bigiot.lib.model.BigIotTypes.LicenseType;
import org.eclipse.bigiot.lib.model.BigIotTypes.PricingModel;
import org.eclipse.bigiot.lib.model.BigIotTypes.ValueType;
import org.eclipse.bigiot.lib.model.BoundingBox;
import org.eclipse.bigiot.lib.model.Location;
import org.eclipse.bigiot.lib.model.Price.Euros;
import org.eclipse.bigiot.lib.offering.Endpoints;
import org.eclipse.bigiot.lib.offering.OfferingDescription;
import org.eclipse.bigiot.lib.offering.RegistrableOfferingDescription;
import org.eclipse.bigiot.lib.serverwrapper.BigIotHttpResponse;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Example for using BIG IoT API as a provider.
 */
public class ExampleWithTwoProvidersAndClonedOfferingDescription {

    private static AccessRequestHandler accessCallback = new AccessRequestHandler() {
        @Override
        public BigIotHttpResponse processRequestHandler(OfferingDescription offeringDescription,
                Map<String, Object> inputData, String subscriptionId, String consumerInfo) {

            double longitude = 41.0;
            if (inputData.containsKey("longitude"))
                longitude = Double.parseDouble((String) inputData.get("longitude"));

            double latitude = 9.0;
            if (inputData.containsKey("latitude"))
                latitude = Double.parseDouble((String) inputData.get("latitude"));

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

            // return BigIotHttpResponse errorResponse = BigIotHttpResponse.error()
            // .withBody("{\"status\":\"error\"}")
            // .withStatus(422).asJsonType();
        }
    };

    public static void main(String[] args)
            throws IncompleteOfferingDescriptionException, IOException, NotRegisteredException {

        // Load example properties file
        BridgeIotProperties prop = BridgeIotProperties.load("example.properties");

        // Initialize provider with provider id and Marketplace URI
        ProviderSpark provider1 = ProviderSpark
                .create(prop.PROVIDER_ID, prop.MARKETPLACE_URI, prop.PROVIDER_DNS_NAME, prop.PROVIDER_PORT)
                .authenticate(prop.PROVIDER_SECRET);

        // Initialize provider with provider id and Marketplace URI
        ProviderSpark provider2 = ProviderSpark
                .create(prop.PROVIDER_ID, prop.MARKETPLACE_URI, prop.PROVIDER_DNS_NAME, prop.PROVIDER_PORT)
                .authenticate(prop.PROVIDER_SECRET);

        // Construct Offering Description of your Offering incrementally
        RegistrableOfferingDescription offeringDescription1 =
                // provider.createOfferingDescriptionFromOfferingId("TestOrganization-TestProvider-Manual_Offering_Test")
                OfferingDescription.createOfferingDescription("DemoParkingOfferingWithClone1")
                        .withName("Demo Parking Offering With Clone").withCategory("urn:big-iot:ParkingSpaceCategory")
                        .withTimePeriod(new DateTime(2017, 1, 1, 0, 0, 0), new DateTime())
                        .inRegion(BoundingBox.create(Location.create(42.1, 9.0), Location.create(43.2, 10.0)))
                        // .inCity("Barcelona")
                        .addInputData("longitude", "schema:longitude", ValueType.NUMBER)
                        .addInputData("latitude", "schema:latitude", ValueType.NUMBER)
                        .addInputData("radius", "schema:geoRadius", ValueType.NUMBER)
                        .addOutputData("lon", "schema:longitude", ValueType.NUMBER)
                        .addOutputData("lat", "schema:latitude", ValueType.NUMBER)
                        .addOutputData("dist", "datex:distanceFromParkingSpace", ValueType.NUMBER)
                        .addOutputData("status", "datex:parkingSpaceStatus", ValueType.TEXT)
                        .withPrice(Euros.amount(0.02)).withPricingModel(PricingModel.PER_ACCESS)
                        .withLicenseType(LicenseType.CREATIVE_COMMONS);

        Endpoints endpoints1 = Endpoints.create(offeringDescription1).withRoute("1st-route")
                .withAccessRequestHandler(accessCallback);

        provider1.register(offeringDescription1, endpoints1);

        // 2nd Offering Description, based on initial one

        RegistrableOfferingDescription offeringDescription2 = offeringDescription1
                .cloneForOtherProvider("DemoParkingOfferingWithClone2");

        Endpoints endpoints2 = Endpoints.create(offeringDescription2).withRoute("2nd-route")
                .withAccessRequestHandler(accessCallback);

        provider2.register(offeringDescription2, endpoints2);

        // Run until user input is obtained
        System.out.println(">>>>>>  Terminate ExampleProvider by pressing ENTER  <<<<<<");
        Scanner keyboard = new Scanner(System.in);
        keyboard.nextLine();
        keyboard.close();

        // Deregister your offering form Marketplace
        provider1.deregister(offeringDescription1);
        provider2.deregister(offeringDescription2);

        // Terminate provider instance
        provider1.terminate();
        provider2.terminate();

    }

}
