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

import org.eclipse.bigiot.lib.Provider;
import org.eclipse.bigiot.lib.exceptions.IncompleteOfferingDescriptionException;
import org.eclipse.bigiot.lib.exceptions.NotRegisteredException;
import org.eclipse.bigiot.lib.misc.Helper;
import org.eclipse.bigiot.lib.model.Information;
import org.eclipse.bigiot.lib.model.RDFType;
import org.eclipse.bigiot.lib.model.ValueType;
import org.eclipse.bigiot.lib.model.BigIotTypes.LicenseType;
import org.eclipse.bigiot.lib.model.BigIotTypes.PricingModel;
import org.eclipse.bigiot.lib.model.Price.Euros;
import org.eclipse.bigiot.lib.offering.RegisteredOffering;
import org.eclipse.bigiot.lib.offering.RegistrableOfferingDescription;
import org.eclipse.bigiot.lib.query.elements.RegionFilter;

public class StarWarsRegistryProvider {

	private static final String MARKETPLACE_URI = "https://market.big-iot.org";
	private static final String PROVIDER_ID 	= "Null_Island-Happy_Parkings";
	private static final String PROVIDER_SECRET = "kETI8TK1QjC5whzgNDG9gw==";
	
	public static void main(String[] args) throws IOException, IncompleteOfferingDescriptionException, NotRegisteredException {
		
		Provider provider = new Provider(PROVIDER_ID, MARKETPLACE_URI);
		
//		provider.setProxy("127.0.0.1", 3128); //Enable this line if you are behind a proxy
//		provider.addProxyBypass("172.17.17.100"); //Enable this line and the addresses for internal hosts
		
		provider.setProxy("194.145.60.1",9400); //Enable this line if you are behind a proxy
		provider.addProxyBypass("172.17.17.100"); //Enable this line and the addresses for internal hosts
		
		provider.authenticate(PROVIDER_SECRET);
		
		RegistrableOfferingDescription offeringDescription = provider
				.createOfferingDescription("starWarsOffering")	    		
				.withInformation(new Information ("Star Wars Universe Registry", "http://schema.org/environmental"))
				.inRegion(RegionFilter.city("Knossa"))
				.withPrice(Euros.amount(0.0001))
				.withPricingModel(PricingModel.PER_ACCESS)
				.withLicenseType(LicenseType.OPEN_DATA_LICENSE)
				.addInputDataInUrl("resourceType", new RDFType("schema:resourceType"), ValueType.TEXT)
				.addInputDataInUrl("resourceId", new RDFType("schema:resourceId"), ValueType.NUMBER)		    		
				.asHttpGet()
				.acceptsJson()
				.producesJson()
				.onExternalEndpoint("http://swapi.co/api/@@resourceType@@/@@resourceId@@")
				;
		
		System.out.println(Helper.getPojoAsJson(offeringDescription));
		
	    RegisteredOffering registeredOffering =  offeringDescription.register();
	    
	    provider.terminate();
	    
	    System.exit(0);
	}

}
