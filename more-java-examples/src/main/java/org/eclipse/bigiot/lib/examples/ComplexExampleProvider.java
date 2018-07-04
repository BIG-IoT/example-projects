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
import java.util.Locale;
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
import org.eclipse.bigiot.lib.offering.RegisteredOffering;
import org.eclipse.bigiot.lib.offering.RegistrableOfferingDescription;
import org.eclipse.bigiot.lib.offering.parameters.ArrayParameter;
import org.eclipse.bigiot.lib.offering.parameters.NumberParameter;
import org.eclipse.bigiot.lib.offering.parameters.ObjectParameter;
import org.eclipse.bigiot.lib.serverwrapper.BigIotHttpResponse;
import org.joda.time.DateTime;

/**
 * Example for using BIG IoT API as a provider. This example corresponds with ExampleConsumerNew.java
 */
public class ComplexExampleProvider {

    private static AccessRequestHandler accessCallbackDummy = new AccessRequestHandler() {
        @Override
        public BigIotHttpResponse processRequestHandler(OfferingDescription offeringDescription,
                Map<String, Object> inputData, String subscriptionId, String consumerInfo) {

            double longitude = 0, latitude = 0, radius = 0;

            BigIotHttpResponse errorResponse = BigIotHttpResponse.error().withBody("{\"status\":\"error\"}")
                    .withStatus(422).asJsonType();
            if (!inputData.containsKey("center"))
                return errorResponse;
            Map<String, String> center = (Map<String, String>) inputData.get("center");

            if (!center.containsKey("longitude"))
                return errorResponse;
            longitude = new Double((String) center.get("longitude"));
            if (!center.containsKey("latitude"))
                return errorResponse;
            latitude = new Double((String) center.get("latitude"));

            if (!inputData.containsKey("radius"))
                return errorResponse;
            radius = new Double((String) inputData.get("radius"));

            Random r = new Random();
            int n = Math.round(r.nextFloat() * 10 + 10);
            String s = "[";

            for (int i = 0; i < n; i++) {
                if (i > 0)
                    s += ",\n";
                s += String.format(Locale.US,
                        "{\"geoCoordinates\":{\n\"latitude\": %.4f,\n\"longitude\": %.4f},\n\"distance\": %.2f,\n\"status\":\"available\"\n}",
                        r.nextFloat() * 0.01 + latitude, r.nextFloat() * 0.01 + longitude, r.nextFloat() * radius);
            }
            s += "]";

            return BigIotHttpResponse.okay().withBody(s).asJsonType();

        }
    };

    public static void main(String[] args)
            throws IncompleteOfferingDescriptionException, IOException, NotRegisteredException {

        // Load example properties file
        BridgeIotProperties prop = BridgeIotProperties.load("example.properties");

        // Initialize provider with provider id and marketplace URL
        ProviderSpark provider = ProviderSpark.create(prop.PROVIDER_ID, prop.MARKETPLACE_URI, prop.PROVIDER_DNS_NAME,
                8081);

        // provider.setProxy(prop.PROXY, prop.PROXY_PORT); //Enable this line if you are behind a proxy
        // provider.addProxyBypass("172.17.17.100"); //Enable this line and the addresses for internal hosts

        // Authenticate provider on the marketplace
        provider.authenticate(prop.PROVIDER_SECRET);

        // Construct Offering Description of your Offering incrementally
        RegistrableOfferingDescription offeringDescription = provider
                .createOfferingDescription("ComplexParkingSpotProvider").withName("Complex Demo Parking Offering")
                .withTimePeriod(new DateTime(2017, 1, 1, 0, 0, 0), new DateTime())
                .inRegion(BoundingBox.create(Location.create(48.05, 11.6), Location.create(48.15, 11.7)))
                .withCategory("urn:proposed:Miscellaneous")
                .addInputData("radius", "schema:geoRadius", ValueType.NUMBER)
                .addInputData("center", "schema:geoMidpoint",
                        ObjectParameter.create()
                                .addMember("latitude", "schema:latitude", NumberParameter.create(-90.0, 90.0), true)
                                .addMember("longitude", "schema:longitude", NumberParameter.create(-180.0, 180.0), true),
                        true)
                .addOutputData("distance", "datex:distanceFromParkingSpace", ValueType.NUMBER)
                .addOutputData("status", "datex:parkingSpaceStatus", ValueType.TEXT)
                .withPrice(Euros.amount(0.001))
                .withPricingModel(PricingModel.PER_ACCESS).withLicenseType(LicenseType.CREATIVE_COMMONS);
        
        Endpoints endpoints = Endpoints.create(offeringDescription).withAccessRequestHandler(accessCallbackDummy);

        RegisteredOffering offering = provider.register(offeringDescription, endpoints);

        // Run until user input is obtained
        System.out.println(">>>>>>  Terminate ExampleProvider by pressing ENTER  <<<<<<");
        Scanner keyboard = new Scanner(System.in);
        keyboard.nextLine();
        keyboard.close();

        // Deregister your offering form Marketplace
        offering.deregister();

        // Terminate provider instance
        provider.terminate();

    }

}
