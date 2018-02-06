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

import org.eclipse.bigiot.lib.ProviderSpark;
import org.eclipse.bigiot.lib.exceptions.IncompleteOfferingDescriptionException;
import org.eclipse.bigiot.lib.exceptions.NotRegisteredException;
import org.eclipse.bigiot.lib.handlers.AccessStreamFilterHandler;
import org.eclipse.bigiot.lib.misc.BridgeIotProperties;
import org.eclipse.bigiot.lib.model.BigIotTypes.LicenseType;
import org.eclipse.bigiot.lib.model.BigIotTypes.PricingModel;
import org.eclipse.bigiot.lib.model.Price.Euros;
import org.eclipse.bigiot.lib.model.BoundingBox;
import org.eclipse.bigiot.lib.model.Location;
import org.eclipse.bigiot.lib.model.RDFType;
import org.eclipse.bigiot.lib.model.ValueType;
import org.eclipse.bigiot.lib.offering.Endpoints;
import org.eclipse.bigiot.lib.offering.OfferingDescription;
import org.eclipse.bigiot.lib.offering.RegisteredOffering;
import org.eclipse.bigiot.lib.offering.RegistrableOfferingDescription;
import org.joda.time.DateTime;
import org.json.JSONObject;

/**
 * Example for using BIG IoT API as a provider. This example corresponds with ExampleConsumerNew.java
 */
public class ExampleProviderAccessStream {

    private static AccessStreamFilterHandler accessStreamFilterCallback = new AccessStreamFilterHandler() {
        @Override
        public boolean processRequestHandler(OfferingDescription offeringDescription, JSONObject jsonObj,
                Map<String, Object> inputData, String subscriptionId, String consumerInfo) {

            double longitude = 9.0;
            if (inputData.containsKey("longitude"))
                longitude = new Double((String) inputData.get("longitude"));

            double latitude = 42.0;
            if (inputData.containsKey("latitude"))
                latitude = new Double((String) inputData.get("latitude"));

            double lon = jsonObj.getDouble("lon");
            double lat = jsonObj.getDouble("lat");

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
        ProviderSpark provider = new ProviderSpark(prop.PROVIDER_ID, prop.MARKETPLACE_URI, prop.PROVIDER_DNS_NAME,
                prop.PROVIDER_PORT);

        // provider.setProxy("127.0.0.1", 3128); //Enable this line if you are behind a proxy
        // provider.addProxyBypass("172.17.17.100"); //Enable this line and the addresses for internal hosts

        // Authenticate provider on the marketplace
        provider.authenticate(prop.PROVIDER_SECRET);

        // Construct Offering Description of your Offering incrementally
        RegistrableOfferingDescription offeringDescription =
                // provider.createOfferingDescriptionFromOfferingId("TestOrganization-TestProvider-Manual_Offering_Test")
                OfferingDescription.createOfferingDescription("DemoParkingOffering_WithAccessStream")
                        .withName("Demo Parking Offering with Access Stream")
                        .withCategory("urn:big-iot:ParkingSpaceCategory")
                        .withTimePeriod(new DateTime(2017, 1, 1, 0, 0, 0), new DateTime())
                        .inRegion(BoundingBox.create(Location.create(42.1, 9.0), Location.create(43.2, 10.0)))
                        // .inCity("Barcelona")
                        .addInputData("longitude", new RDFType("schema:longitude"), ValueType.NUMBER)
                        .addInputData("latitude", new RDFType("schema:latitude"), ValueType.NUMBER)
                        .addInputData("radius", new RDFType("schema:geoRadius"), ValueType.NUMBER)
                        .addOutputData("lon", new RDFType("schema:longitude"), ValueType.NUMBER)
                        .addOutputData("lat", new RDFType("schema:latitude"), ValueType.NUMBER)
                        .addOutputData("status", new RDFType("datex:parkingSpaceStatus"), ValueType.TEXT)
                        .withPrice(Euros.amount(0.02)).withPricingModel(PricingModel.PER_ACCESS)
                        .withLicenseType(LicenseType.CREATIVE_COMMONS);

        Endpoints endpoints = Endpoints.create(offeringDescription)
                   .withAccessStreamFilterHandler(accessStreamFilterCallback); // Optional only if filtering needed

        RegisteredOffering offering = provider.register(offeringDescription, endpoints);

        // Run until user input is obtained
        System.out.println(">>>>>>  Terminate ExampleProvider by pressing ENTER  <<<<<<");

        int i = 0;
        Random r = new Random();
        while (System.in.available() == 0) {

            JSONObject jsonObject = new JSONObject().put("lat", 42.0 + r.nextFloat() * 0.01)
                    .put("lon", 9.0 + r.nextFloat() * 0.01)
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
