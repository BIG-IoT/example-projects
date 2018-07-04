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
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.bigiot.lib.Consumer;
import org.eclipse.bigiot.lib.examples.types.MyParkingResultPojoAnnotated;
import org.eclipse.bigiot.lib.misc.BridgeIotProperties;
import org.eclipse.bigiot.lib.offering.AccessResponse;
import org.eclipse.bigiot.lib.offering.Offering;
import org.eclipse.bigiot.lib.offering.parameters.AccessParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example for using BIG IoT API as a consumer. This example corresponds with ExampleProviderNew.java
 * 
 * 
 */
public class ExampleConsumerSubscriptionById {

    private static final Logger logger = LoggerFactory.getLogger(ExampleConsumerSubscriptionById.class);

    /*
     * Main Routine
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {

        // Load example properties file
        BridgeIotProperties prop = BridgeIotProperties.load("example.properties");

        // Initialize Consumer with Consumer ID and marketplace URL
        Consumer consumer = new Consumer(prop.CONSUMER_ID, prop.MARKETPLACE_URI).authenticate(prop.CONSUMER_SECRET);

        // Subscribe to Offering by OfferingId
        Offering offering = consumer.subscribeByOfferingId("TestOrganization-TestProvider-DemoParkingOffering").get();

        // Define Input Data as access parameters
        AccessParameters accessParameters = AccessParameters.create();
        // .addRdfTypeValue("schema:latitude", 42.0)
        // .addRdfTypeValue("schema:longitude", 9.0);

        // Access Offering one-time with Access Parameters (input data)
        AccessResponse response = offering.accessOneTime(accessParameters).get();

        logger.info("One time Offering access: {} elements received", response.asJsonNode().size());

        // Mapping the response automatically to your pojo
        List<MyParkingResultPojoAnnotated> parkingResult = response.map(MyParkingResultPojoAnnotated.class);
        parkingResult.forEach(t -> logger.info("Record: " + t.toString()));

        // Unsubscribe Offering
        offering.unsubscribe();

        // Terminate consumer session
        consumer.terminate();

    }
}
