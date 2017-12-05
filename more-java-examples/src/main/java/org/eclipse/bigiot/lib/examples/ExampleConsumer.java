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
import org.eclipse.bigiot.lib.exceptions.IncompleteOfferingQueryException;
import org.eclipse.bigiot.lib.feed.AccessFeed;
import org.eclipse.bigiot.lib.misc.Helper;
import org.eclipse.bigiot.lib.model.BigIotTypes;
import org.eclipse.bigiot.lib.model.BigIotTypes.LicenseType;
import org.eclipse.bigiot.lib.model.Information;
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
public class ExampleConsumer {

    private static final String MARKETPLACE_URI = "https://market.big-iot.org";

    private static final String CONSUMER_ID = "Null_Island-Parking_App";
    private static final String CONSUMER_SECRET = "-9DLobRfRx63EwL_OJYj-w==";

    private static final Logger logger = LoggerFactory.getLogger(ExampleConsumer.class);

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

        // Initialize Consumer with Consumer ID and marketplace URL
        Consumer consumer = new Consumer(CONSUMER_ID, MARKETPLACE_URI);

        // consumer.setProxy("127.0.0.1", 3128); //Enable this line if you are behind a proxy
        // consumer.addProxyBypass("172.17.17.100"); //Enable this line and the addresses for internal hosts

        // Authenticate provider on the marketplace
        consumer.authenticate(CONSUMER_SECRET);

        // Construct Offering search query incrementally
        OfferingQuery query = OfferingQuery.create("ParkingQuery")
                .withInformation(new Information("Parking Query", "bigiot:Parking"))
                // .inCity("Barcelona")
                .withPricingModel(BigIotTypes.PricingModel.PER_ACCESS).withMaxPrice(Euros.amount(0.1))
                .withLicenseType(LicenseType.OPEN_DATA_LICENSE);

        CompletableFuture<SubscribableOfferingDescription> offeringDescriptionFuture = consumer.discover(query)
                .thenApply(SubscribableOfferingDescription::showOfferingDescriptions)
                .thenApply(l -> OfferingSelector.create().onlyLocalhost().cheapest().mostPermissive().select(l));

        // Alternatively you can use discover with callbacks
        // consumer.discover(query,(q,l)-> {
        // log("Discovery with callback");
        // SubscribableOfferingDescription.showOfferingDescriptions(list);
        // });

        SubscribableOfferingDescription offeringDescription = offeringDescriptionFuture.get();
        if (offeringDescription == null) {
            logger.error("Couldn't find any offering. Are sure that one is registered? It could be expired meanwhile");
            System.exit(1);
        }

        // Instantiation of Offering Access objects via subscribe
        CompletableFuture<Offering> offeringFuture = offeringDescription.subscribe();
        Offering offering = offeringFuture.get();

        // Prepare access parameters

        // Supported in the future as soon marketplace supports complex data
        // types
        // AccessParameters accessParameters= AccessParameters.create()
        // .addRdfTypeValue("schema:GeoCircle", AccessParameters.create()
        // .addRdfTypeValue("schema:GeoCoordinates", AccessParameters.create()
        // .addRdfTypeValue("schema:latitude", 50.22)
        // .addRdfTypeValue("schema:longitude", 8.11))
        // .addRdfTypeValue("schema:geoRadius", 777));

        AccessParameters accessParameters = AccessParameters.create().addRdfTypeValue("schema:latitude", 42.0)
                .addRdfTypeValue("schema:longitude", 9.0).addRdfTypeValue("schema:geoRadius", 777);
        // .addNameValue("accessSessionId", 123456789);

        CompletableFuture<AccessResponse> response = offering.accessOneTime(accessParameters);

        if (response.get().getBody().contains("error")) {
            throw new RuntimeException(response.get().getBody());
        } else {
            log("One time Offering access: " + response.get().asJsonNode().size() + " elements received. ");
        }

        // Mapping the response automatically to your pojo
        List<MyParkingResultPojoAnnotated> parkingResult = response.get().map(MyParkingResultPojoAnnotated.class);
        parkingResult.forEach(t -> log("Record: " + t.toString()));

        // Alternatively you can manually map your response
        List parkingResult2 = response.get().map(MyParkingResultPojo.class,
                OutputMapping.create().addTypeMapping("schema:longitude", "longitude")
                        .addTypeMapping("schema:latitude", "latitude")
                        .addTypeMapping("datex:distanceFromParkingSpace", "distance")
                        .addTypeMapping("datex:parkingSpaceStatus", "status"));

        Thread.sleep(5L * Helper.Second);

        Duration feedDuration = Duration.standardHours(1);
        Duration feedInterval = Duration.standardSeconds(2);

        // Create a data feed using callbacks for the received results
        AccessFeed accessFeed = offering.accessContinuous(accessParameters, feedDuration.getMillis(),
                feedInterval.getMillis(),
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
