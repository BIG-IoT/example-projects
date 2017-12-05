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
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.bigiot.lib.Consumer;
import org.eclipse.bigiot.lib.examples.types.MyParkingResultPojo;
import org.eclipse.bigiot.lib.examples.types.MyParkingResultPojoAnnotated;
import org.eclipse.bigiot.lib.exceptions.AccessToNonActivatedOfferingException;
import org.eclipse.bigiot.lib.exceptions.AccessToNonSubscribedOfferingException;
import org.eclipse.bigiot.lib.exceptions.IncompleteOfferingQueryException;
import org.eclipse.bigiot.lib.feed.AccessFeed;
import org.eclipse.bigiot.lib.handlers.DiscoverFailureException;
import org.eclipse.bigiot.lib.handlers.DiscoverResponseErrorHandler;
import org.eclipse.bigiot.lib.handlers.DiscoverResponseHandler;
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
import org.eclipse.bigiot.lib.query.IOfferingQuery;
import org.eclipse.bigiot.lib.query.OfferingQuery;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example for using BIG IoT API as a consumer. This example corresponds with ExampleProviderNew.java
 * 
 * 
 */
public class ExampleConsumerDiscoverContinuous {

    private static final String MARKETPLACE_URI = "https://market.big-iot.org";

    private static final String CONSUMER_ID = "Null_Island-Parking_App";
    private static final String CONSUMER_SECRET = "-9DLobRfRx63EwL_OJYj-w==";

    private static final Logger logger = LoggerFactory.getLogger(ExampleConsumerDiscoverContinuous.class);

    private static DiscoverResponseHandler discoverResponseHandler = new DiscoverResponseHandler() {
			@Override
			public void processResponse(IOfferingQuery reference, List offeringDescriptions) {
				
				logger.info("Discovered {} offerings", offeringDescriptions.size());
				
				if (offeringDescriptions.size() == 0) 
					return;
				
				SubscribableOfferingDescription offeringDescription = (SubscribableOfferingDescription) offeringDescriptions.get(0);
				
				try {
					Offering offering = offeringDescription.subscribe().get();
					
					AccessParameters accessParameters = AccessParameters.create()
							.addRdfTypeValue("schema:latitude", 42.0)
			                .addRdfTypeValue("schema:longitude", 9.0)
			                .addRdfTypeValue("schema:geoRadius", 777);
	
			        AccessResponse response = offering.accessOneTime(accessParameters).get();
			        
			        logger.info("One time Offering access: " + response.asJsonNode().size() + " elements received.");
			        
			        // Mapping the response automatically to your pojo
			        List<MyParkingResultPojoAnnotated> parkingResult = response.map(MyParkingResultPojoAnnotated.class);
			        // parkingResult.forEach(t -> logger.info("Record: " + t.toString()));
			        
			        offering.unsubscribe();
		        
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
    
    private static DiscoverResponseErrorHandler discoverResponseErrorHandler = new DiscoverResponseErrorHandler() {
			@Override
			public void processResponse(IOfferingQuery reference, DiscoverFailureException failure) {
				logger.error("Discovery error");
			}
		};

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
        
        consumer.discoverContinous(query, discoverResponseHandler, discoverResponseErrorHandler, 10);

        // Run until user input is obtained
        System.out.println(">>>>>>  Terminate ExampleConsumer by pressing ENTER  <<<<<<");
        Scanner keyboard = new Scanner(System.in);
        keyboard.nextLine();
        keyboard.close();

        // Stop continuous discovery
        consumer.stopDiscoverContinuous(query);
        
        // Terminate consumer session (unsubscribe from marketplace)
        consumer.terminate();

    }
}
