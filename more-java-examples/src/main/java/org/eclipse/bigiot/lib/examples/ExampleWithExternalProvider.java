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
import java.util.Scanner;

import org.eclipse.bigiot.lib.Provider;
import org.eclipse.bigiot.lib.exceptions.IncompleteOfferingDescriptionException;
import org.eclipse.bigiot.lib.exceptions.InvalidOfferingException;
import org.eclipse.bigiot.lib.exceptions.NotRegisteredException;
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
import org.joda.time.DateTime;

/**
 * Example for using BIG IoT API as a provider.
 */
public class ExampleWithExternalProvider {

    public static void main(String[] args) throws IncompleteOfferingDescriptionException, IOException,
            NotRegisteredException, InvalidOfferingException {

        // Load example properties file
        BridgeIotProperties prop = BridgeIotProperties.load("example.properties");

        // Initialize provider with provider id and Marketplace URI
        Provider provider = Provider.create(prop.PROVIDER_ID, prop.MARKETPLACE_URI).authenticate(prop.PROVIDER_SECRET);

        // Construct Offering Description of your Offering incrementally
        RegistrableOfferingDescription offeringDescription =
                // provider.createOfferingDescriptionFromOfferingId("TestOrganization-TestProvider-DemoParkingOffering");
                OfferingDescription.createOfferingDescription("DemoParkingOfferingWithExternalProvider")
                        .withName("Demo Parking Offering With External Provider")
                        .withCategory("urn:big-iot:ParkingSpaceCategory")
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
                        .withLicenseType(LicenseType.PROJECT_INTERNAL_USE_ONLY);

        Endpoints endpoints = Endpoints.create(offeringDescription).withEndpointUri("https://127.0.0.1:9443/test");

        provider.register(offeringDescription, endpoints);

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
