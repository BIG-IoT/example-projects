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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.bigiot.lib.Consumer;
import org.eclipse.bigiot.lib.examples.types.AlternativeParkingPojo;
import org.eclipse.bigiot.lib.examples.types.MyComplexParkingResultPojo;
import org.eclipse.bigiot.lib.examples.types.MyComplexParkingResultPojoAnnotated;
import org.eclipse.bigiot.lib.exceptions.AccessToNonActivatedOfferingException;
import org.eclipse.bigiot.lib.exceptions.AccessToNonSubscribedOfferingException;
import org.eclipse.bigiot.lib.exceptions.IncompleteOfferingQueryException;
import org.eclipse.bigiot.lib.feed.AccessFeed;
import org.eclipse.bigiot.lib.misc.BridgeIotProperties;
import org.eclipse.bigiot.lib.misc.Helper;
import org.eclipse.bigiot.lib.model.BigIotTypes;
import org.eclipse.bigiot.lib.model.BigIotTypes.LicenseType;
import org.eclipse.bigiot.lib.model.BigIotTypes.ValueType;
import org.eclipse.bigiot.lib.model.BoundingBox;
import org.eclipse.bigiot.lib.model.Location;
import org.eclipse.bigiot.lib.model.Price.Euros;
import org.eclipse.bigiot.lib.model.TimePeriod;
import org.eclipse.bigiot.lib.offering.AccessResponse;
import org.eclipse.bigiot.lib.offering.Offering;
import org.eclipse.bigiot.lib.offering.OfferingSelector;
import org.eclipse.bigiot.lib.offering.SubscribableOfferingDescription;
import org.eclipse.bigiot.lib.offering.mapping.OutputMapping;
import org.eclipse.bigiot.lib.offering.parameters.AccessParameters;
import org.eclipse.bigiot.lib.offering.parameters.NumberParameter;
import org.eclipse.bigiot.lib.offering.parameters.ObjectParameter;
import org.eclipse.bigiot.lib.query.OfferingQuery;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example for using BIG IoT API as a consumer. This example corresponds with ExampleProviderNew.java
 * 
 * 
 */
public class ComplexExampleConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ComplexExampleConsumer.class);

    /*
     * Main Routine
     */
    public static void log(Object s) {
        if (logger.isInfoEnabled()) {
            logger.info(s.toString());
        }
    }

    public static void main(String[] args)
            throws InterruptedException, ExecutionException, IncompleteOfferingQueryException, IOException,
            AccessToNonSubscribedOfferingException, AccessToNonActivatedOfferingException {

        // Load example properties file
        BridgeIotProperties prop = BridgeIotProperties.load("example.properties");

        // Initialize Consumer with Consumer ID and marketplace URL
        Consumer consumer = Consumer.create(prop.CONSUMER_ID, prop.MARKETPLACE_URI);

        // consumer.setProxy(prop.PROXY, prop.PROXY_PORT); //Enable this line if you are behind a proxy
        // consumer.addProxyBypass("172.17.17.100"); //Enable this line and the addresses for internal hosts

        // Authenticate provider on the marketplace
        consumer.authenticate(prop.CONSUMER_SECRET);

        // Construct Offering search query incrementally
        OfferingQuery query = OfferingQuery.create("ParkingQuery")
                .withName("Parking Query")
                .withCategory("urn:proposed:Miscellaneous")
                .withTimePeriod(TimePeriod.create(new DateTime(1999, 1, 1, 0, 0, 0), new DateTime()))
                .inRegion(BoundingBox.create(Location.create(48.07, 11.65), Location.create(48.13, 11.67)))
                .addInputData("schema:geoMidpoint")
                .addOutputData("datex:parkingSpaceStatus", ValueType.TEXT)
                .addOutputData("datex:distanceFromParkingSpace", ValueType.UNDEFINED)
                .withPricingModel(BigIotTypes.PricingModel.PER_ACCESS).withMaxPrice(Euros.amount(0.5))
                .withLicenseType(LicenseType.CREATIVE_COMMONS);

        CompletableFuture<SubscribableOfferingDescription> offeringDescriptionFuture = consumer.discover(query)
                .thenApply(SubscribableOfferingDescription::showOfferingDescriptions)
                .thenApply(l -> OfferingSelector.create().onlyLocalhost().cheapest().mostPermissive().select(l));

        SubscribableOfferingDescription offeringDescription = offeringDescriptionFuture.get();
        if (offeringDescription == null) {
            logger.error("Couldn't find any offering. Are sure that one is registered? It could be expired meanwhile");
            System.exit(1);
        }

        // Instantiation of Offering Access objects via subscribe
        CompletableFuture<Offering> offeringFuture = offeringDescription.subscribe();
        Offering offering = offeringFuture.get();

        // Prepare access parameters
        AccessParameters accessParameters = AccessParameters.create().addNameValue("radius", 500)
                .addNameValue("center",
                        AccessParameters.create().addNameValue("latitude", 48.10).addNameValue("longitude", 11.23));

        CompletableFuture<AccessResponse> response = offering.accessOneTime(accessParameters);

        if (response.get().getBody().contains("error")) {
            throw new RuntimeException(response.get().getBody());
        } else {
            log("One time Offering access: " + response.get().asJsonNode().size() + " elements received. ");
        }

        // Mapping the response automatically to your pojo
        List<MyComplexParkingResultPojoAnnotated> parkingResult = response.get()
                .map(MyComplexParkingResultPojoAnnotated.class);
        
        // Alternatively you can manually map your response
        List parkingResult2 = response.get().map(MyComplexParkingResultPojo.class,
                OutputMapping.create().addTypeMapping("schema:geoCoordinates", "myCoordinate")
                        .addTypeMapping("datex:distanceFromParkingSpace", "myDistance")
                        .addTypeMapping("datex:parkingSpaceStatus", "myStatus"));

        // Or you can do your own mapping cherry-picking your favorite fields
        List parkingResult3 = response.get().map(AlternativeParkingPojo.class,
                OutputMapping.create().addNameMapping("geoCoordinates.latitude", "coordinates.latitude")
                        .addNameMapping("geoCoordinates.longitude", "coordinates.longitude")
                        .addNameMapping("distance", "meters"));

        Thread.sleep(5L * Helper.Second);

        Duration feedDuration = Duration.standardHours(1);

        // Create a data feed using callbacks for the received results
        AccessFeed accessFeed = offering.accessContinuous(accessParameters, feedDuration.getMillis(),
                (f, r) -> log("Incoming feed data: " + r.asJsonNode().size() + " elements received. "),
                (f, r) -> log("Feed operation failed"));

        Thread.sleep(23L * Helper.Second);

        // Pausing Feed
        accessFeed.stop();

        // Printing feed status
        log(accessFeed.getStatus());

        Thread.sleep(10L * Helper.Second);

        // Resuming Feed
        accessFeed.resume();

        Thread.sleep(10L * Helper.Second);

        // Setting a new lifetime for the feed
        accessFeed.setLifetimeSeconds(5000);

        Thread.sleep(10L * Helper.Second);

        accessFeed.stop();

        // Unsubscribe Offering
        offering.unsubscribe();

        // Terminate consumer session (unsubscribe from marketplace)
        consumer.terminate();

    }
}
