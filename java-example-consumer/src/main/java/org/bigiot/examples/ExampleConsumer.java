/*
 *	Copyright (c) 2017 by Contributors of the BIG IoT Project Consortium (see below).
 *	All rights reserved. 
 *		
 *	This source code is licensed under the MIT license found in the
 * 	LICENSE file in the root directory of this source tree.
 *		
 *	Contributor:
 *	- Robert Bosch GmbH 
 *	    > Stefan Schmid (stefan.schmid@bosch.com)
 */

package org.bigiot.examples;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.eclipse.bigiot.lib.Consumer;
import org.eclipse.bigiot.lib.exceptions.AccessToNonActivatedOfferingException;
import org.eclipse.bigiot.lib.exceptions.AccessToNonSubscribedOfferingException;
import org.eclipse.bigiot.lib.exceptions.IncompleteOfferingQueryException;
import org.eclipse.bigiot.lib.feed.AccessFeed;
import org.eclipse.bigiot.lib.misc.BridgeIotProperties;
import org.eclipse.bigiot.lib.model.BigIotTypes.LicenseType;
import org.eclipse.bigiot.lib.model.BigIotTypes.PricingModel;
import org.eclipse.bigiot.lib.model.BigIotTypes.ValueType;
import org.eclipse.bigiot.lib.model.Price.Euros;
import org.eclipse.bigiot.lib.offering.AccessResponse;
import org.eclipse.bigiot.lib.offering.Offering;
import org.eclipse.bigiot.lib.offering.SubscribableOfferingDescription;
import org.eclipse.bigiot.lib.offering.parameters.AccessParameters;
import org.eclipse.bigiot.lib.query.OfferingQuery;

import org.joda.time.Duration;

public class ExampleConsumer {
		
	public static void main(String args[]) throws InterruptedException, ExecutionException, IncompleteOfferingQueryException, IOException, AccessToNonSubscribedOfferingException, AccessToNonActivatedOfferingException {
	    
	    // Load example properties file
        BridgeIotProperties prop = BridgeIotProperties.load("example.properties");

		// Initialize consumer with Consumer ID and Marketplace URL
		Consumer consumer = Consumer.create(prop.CONSUMER_ID, prop.MARKETPLACE_URI)
		                            .authenticate(prop.CONSUMER_SECRET);
		
	    // Construct Offering Query incrementally
		OfferingQuery query = OfferingQuery.create("RandomNumberQuery")
				.withName("Random Number Query")
                .withCategory("urn:proposed:RandomValues")
                // .addInputData("http://schema.org/latitude", ValueType.NUMBER)
                .addOutputData("proposed:randomValue", ValueType.NUMBER)
                //.inRegion(BoundingBox.create(Location.create(42.1, 9.0), Location.create(43.2, 10.0)))
                //.withTimePeriod(new DateTime(2017, 1, 1, 0, 0, 0), new DateTime())
				.withPricingModel(PricingModel.PER_ACCESS)
				.withMaxPrice(Euros.amount(0.002))             
				.withLicenseType(LicenseType.OPEN_DATA_LICENSE);

		// Discover available offerings based on Offering Query		
		List<SubscribableOfferingDescription> list = consumer.discover(query).get();	
		
		// Select Offering that has been offered by a local provider instance 
		SubscribableOfferingDescription selectedOfferingDescription = list.get(0); 
		
		if (selectedOfferingDescription != null) { 
			
			// Subscribe to a selected OfferingDescription (if successful, returns accessible Offering instance)		
			Offering offering = selectedOfferingDescription.subscribe().get();
	
			// Prepare Access Parameters
			AccessParameters accessParameters = AccessParameters.create();
			        // .addRdfTypeValue("http://schema.org/latitude", 41);
			
			// EXAMPLE 1: ONE-TIME ACCESS to the Offering
			AccessResponse response = offering.accessOneTime(accessParameters).get();
            System.out.println("Received data: " + response.asJsonNode().toString());
            
            // EXAMPLE 2: CONTINUOUS ACCESS to the Offering 
			// Create an Access Feed with callbacks for the received results		
			Duration feedDuration = Duration.standardHours(2);
			Duration feedInterval = Duration.standardSeconds(2);
			AccessFeed accessFeed = offering.accessContinuous(accessParameters, 
										feedDuration.getMillis(), 
										feedInterval.getMillis(), 
										(f,r) -> {
											System.out.println("Received data: " + r.asJsonNode().toString());
										},
										(f,r) -> {
											System.out.println("Feed operation failed");
											f.stop();
										});
			
			// Run until user presses the ENTER key
			System.out.println(">>>>>>  Terminate ExampleConsumer by pressing ENTER  <<<<<<");
			Scanner keyboard = new Scanner(System.in);
			keyboard.nextLine();
	
			// Stop Access Feed
			accessFeed.stop();
			
			// Unsubscribe the Offering
			offering.unsubscribe();
	
		}
		else {
			// No active Offerings could be discovered 
			System.out.println(">>>>>>  No matching offering found  <<<<<<");
		}
		
		// Terminate consumer instance
		consumer.terminate();
			
	}

}
