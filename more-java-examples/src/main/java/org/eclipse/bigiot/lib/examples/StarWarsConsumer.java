/**
 * Copyright (c) 2016-2017 BIG IoT Project Consortium and others (see below).
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * Contributors in alphabetical order
 * (for individual contributions, please refer to git log):
 *
 * - Bosch Software Innovations GmbH
 *     > Denis Kramer
 * - Robert Bosch GmbH
 *     > Stefan Schmid (stefan.schmid@bosch.com)
 * - Siemens AG
 *     > Andreas Ziller (andreas.ziller@siemens.com)
 *
 */
package org.eclipse.bigiot.lib.examples;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.bigiot.lib.Consumer;
import org.eclipse.bigiot.lib.exceptions.IncompleteOfferingQueryException;
import org.eclipse.bigiot.lib.misc.Helper;
import org.eclipse.bigiot.lib.model.BigIotTypes;
import org.eclipse.bigiot.lib.model.Information;
import org.eclipse.bigiot.lib.model.BigIotTypes.LicenseType;
import org.eclipse.bigiot.lib.model.BigIotTypes.PricingModel;
import org.eclipse.bigiot.lib.model.Price.Euros;
import org.eclipse.bigiot.lib.offering.AccessParameters;
import org.eclipse.bigiot.lib.offering.AccessResponse;
import org.eclipse.bigiot.lib.offering.Offering;
import org.eclipse.bigiot.lib.offering.OfferingDescription;
import org.eclipse.bigiot.lib.offering.OfferingSelector;
import org.eclipse.bigiot.lib.offering.SubscribableOfferingDescription;
import org.eclipse.bigiot.lib.query.OfferingQuery;
import org.eclipse.bigiot.lib.query.elements.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StarWarsConsumer {

	private static final String MARKETPLACE_URI = "https://market.big-iot.org";
	private static final String CONSUMER_ID	    = "Null_Island-Parking_App";
	private static final String CONSUMER_SECRET = "-9DLobRfRx63EwL_OJYj-w==";

	final static Logger logger = LoggerFactory.getLogger(StarWarsConsumer.class);
	
	public static void main(String[] args) throws IOException, IncompleteOfferingQueryException, InterruptedException, ExecutionException {
		// Initialize Consumer with Consumer ID and marketplace URL
		Consumer consumer = new Consumer(CONSUMER_ID, MARKETPLACE_URI); 

//		consumer.setProxy("127.0.0.1", 3128); //Enable this line if you are behind a proxy
//		consumer.addProxyBypass("172.17.17.100"); //Enable this line and the addresses for internal hosts
		
		// Authenticate provider on the marketplace (not yet supported)
		consumer.authenticate(CONSUMER_SECRET);


		// Construct Offering search query incrementally
		OfferingQuery query = OfferingQuery
				.create("starWarsQuery")
				.withInformation(new Information ("Star Wars Universe Registry", "http://schema.org/environmental"))	
				.withMaxPrice(Euros.amount(0.1))
				.withPricingModel(PricingModel.PER_ACCESS)
				.withLicenseType(LicenseType.OPEN_DATA_LICENSE);


		CompletableFuture<SubscribableOfferingDescription> offeringDescriptionFuture  = consumer.discover(query)
				.thenApply(SubscribableOfferingDescription::showOfferingDescriptions)
				.thenApply((l) -> OfferingSelector.create().cheapest().mostPermissive().select(l));
		
		
		SubscribableOfferingDescription offeringDescription= offeringDescriptionFuture.get();
		
		if(offeringDescription== null) {
			logger.error("Couldn't find any matching offering description. Are sure that one is registered? It could be also expired meanwhile!");
			System.exit(1);
		}
		Offering offering = offeringDescription.subscribe().get();
				
		// Select your favorite Star Wars information
		//
		// resourceType = 
		//		people
		//		planets
		//		films
		//		species
		//		vehicles
		//		starships
		//
		// resourceId is an integer. null shows all
		
		
		AccessParameters accessParameters= AccessParameters.create()
				.addNameValue("resourceType", "species")
				.addNameValue("resourceId",3)
				;
		
		AccessResponse response = offering.accessOneTime(accessParameters).get();
		
		System.out.println("Receiving:\n\n"+ response.getPrettyPrint());
	}

}
