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
