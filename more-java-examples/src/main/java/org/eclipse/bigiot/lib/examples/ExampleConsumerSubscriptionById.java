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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.bigiot.lib.Consumer;
import org.eclipse.bigiot.lib.examples.types.MyParkingResultPojo;
import org.eclipse.bigiot.lib.examples.types.MyParkingResultPojoAnnotated;
import org.eclipse.bigiot.lib.exceptions.AccessToNonActivatedOfferingException;
import org.eclipse.bigiot.lib.exceptions.AccessToNonSubscribedOfferingException;
import org.eclipse.bigiot.lib.exceptions.IllegalEndpointException;
import org.eclipse.bigiot.lib.exceptions.IncompleteOfferingDescriptionException;
import org.eclipse.bigiot.lib.exceptions.IncompleteOfferingQueryException;
import org.eclipse.bigiot.lib.exceptions.InvalidOfferingException;
import org.eclipse.bigiot.lib.feed.AccessFeed;
import org.eclipse.bigiot.lib.misc.BridgeIotProperties;
import org.eclipse.bigiot.lib.misc.Helper;
import org.eclipse.bigiot.lib.model.BigIotTypes;
import org.eclipse.bigiot.lib.model.BigIotTypes.LicenseType;
import org.eclipse.bigiot.lib.model.Information;
import org.eclipse.bigiot.lib.model.RDFType;
import org.eclipse.bigiot.lib.model.ValueType;
import org.eclipse.bigiot.lib.model.Price.Euros;
import org.eclipse.bigiot.lib.offering.AccessParameters;
import org.eclipse.bigiot.lib.offering.AccessResponse;
import org.eclipse.bigiot.lib.offering.Offering;
import org.eclipse.bigiot.lib.offering.OfferingSelector;
import org.eclipse.bigiot.lib.offering.SubscribableOfferingDescription;
import org.eclipse.bigiot.lib.offering.mapping.OutputMapping;
import org.eclipse.bigiot.lib.query.OfferingQuery;
import org.joda.time.Duration;
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
    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException,
            IllegalEndpointException, IncompleteOfferingDescriptionException, InvalidOfferingException {
        
        // Load example properties file
        BridgeIotProperties prop = BridgeIotProperties.load("example.properties");

        // Initialize Consumer with Consumer ID and marketplace URL
        Consumer consumer = new Consumer(prop.CONSUMER_ID, prop.MARKETPLACE_URI)
                                    .authenticate(prop.CONSUMER_SECRET);

        // Subscribe to Offering by OfferingId
        Offering offering = consumer.subscribeByOfferingId("TestOrganization-TestProvider-ParkingSpotProvider").get();

        // Access Offering one-time with Access Parameters (input data)
        AccessResponse response = offering
                .accessOneTime(AccessParameters.create().addRdfTypeValue("schema:latitude", 42.0)
                        .addRdfTypeValue("schema:longitude", 9.0).addRdfTypeValue("schema:geoRadius", 777))
                .get();

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
