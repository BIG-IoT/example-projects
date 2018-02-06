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
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.eclipse.bigiot.lib.ProviderSpark;
import org.eclipse.bigiot.lib.exceptions.IncompleteOfferingDescriptionException;
import org.eclipse.bigiot.lib.exceptions.NotRegisteredException;
import org.eclipse.bigiot.lib.handlers.AccessRequestHandler;
import org.eclipse.bigiot.lib.misc.BridgeIotProperties;
import org.eclipse.bigiot.lib.model.BigIotTypes.LicenseType;
import org.eclipse.bigiot.lib.model.BigIotTypes.PricingModel;
import org.eclipse.bigiot.lib.model.BoundingBox;
import org.eclipse.bigiot.lib.model.Location;
import org.eclipse.bigiot.lib.model.Price.Euros;
import org.eclipse.bigiot.lib.model.RDFType;
import org.eclipse.bigiot.lib.model.ValueType;
import org.eclipse.bigiot.lib.offering.Endpoints;
import org.eclipse.bigiot.lib.offering.OfferingDescription;
import org.eclipse.bigiot.lib.offering.RegistrableOfferingDescription;
import org.eclipse.bigiot.lib.serverwrapper.BigIotHttpResponse;
import org.joda.time.DateTime;
import org.json.JSONObject;

/** 
 * Example for using BIG IoT API as a Provider. This example corresponds with Example Consumer project * 
 */
public class ExampleProvider {
	
	private static Random rand = new Random();

	private static AccessRequestHandler accessCallback = new AccessRequestHandler() {
		@Override
		public BigIotHttpResponse processRequestHandler (
	           OfferingDescription offeringDescription, Map<String,Object> inputData, String subscriberId, String consumerInfo) {
			
			/*
			double longitude = 41.0;
            if (inputData.containsKey("longitude"))
                longitude = Double.parseDouble((String) inputData.get("longitude"));

            double latitude = 9.0;
            if (inputData.containsKey("latitude"))
                latitude = Double.parseDouble((String) inputData.get("latitude"));

			*/
			
		    // Prepare the offering response as a JSONObject/Array - according to the Output Data defined in the Offering Description
		    JSONObject number = new JSONObject();
		    number.put("value", rand.nextFloat());
		    number.put("timestamp", new Date().getTime()); 
		   
	        // Send the response as JSON in the form: { [ { "value" : 0.XXX, "timestamp" : YYYYYYY } ] }
			return BigIotHttpResponse.okay().withBody(number).asJsonType();
		}
	};

	public static void main(String args[]) throws InterruptedException, IncompleteOfferingDescriptionException, IOException, NotRegisteredException {
				
	    // Load example properties file
        BridgeIotProperties prop = BridgeIotProperties.load("example.properties");

		// Initialize a provider with Provider ID and Marketplace URI, the local IP/DNS, etc., and authenticate it on the Marketplace
		ProviderSpark provider = ProviderSpark.create(prop.PROVIDER_ID, prop.MARKETPLACE_URI, prop.PROVIDER_DNS_NAME, prop.PROVIDER_PORT)
		                                .authenticate(prop.PROVIDER_SECRET);
		
		// provider.setProxy(prop.PROXY, prop.PROXY_PORT); //Enable this line if you are behind a proxy
		// provider.addProxyBypass(prop.PROXY_BYPASS); //Enable this line and the addresses for internal hosts
			    
	    // Construct Offering Description of your Offering incrementally
	    RegistrableOfferingDescription offeringDescription = 
	            OfferingDescription.createOfferingDescription("RandomNumberOffering")
    	    		.withName("Random Number Offering")
    	    		.withCategory("urn:proposed:RandomValues")
    	    		//.addInputData("longitude", new RDFType("schema:longitude"))
    	    		//.addInputData("latitude", new RDFType("schema:latitude"))
    	    		.addOutputData("value", new RDFType("proposed:randomValue"), ValueType.NUMBER)
    	            .addOutputData("timestamp", new RDFType("schema:datePublished"), ValueType.NUMBER)
    	            //.inRegion(BoundingBox.create(Location.create(42.1, 9.0), Location.create(43.2, 10.0)))
                    //.withTimePeriod(new DateTime(2017, 1, 1, 0, 0, 0), new DateTime())
    	    		.withPrice(Euros.amount(0.001))
    	    		.withPricingModel(PricingModel.PER_ACCESS)
    	    		.withLicenseType(LicenseType.OPEN_DATA_LICENSE);
	    
	    Endpoints endpoints = Endpoints.create(offeringDescription)
                    .withAccessRequestHandler(accessCallback);

	    // Register OfferingDescription on Marketplace - this will create a local endpoint based on the embedded Spark Web server
	    provider.register(offeringDescription, endpoints);
	    
		// Run until user presses the ENTER key
		System.out.println(">>>>>>  Terminate ExampleProvider by pressing ENTER  <<<<<<");
		Scanner keyboard = new Scanner(System.in);
		keyboard.nextLine();
	 
		System.out.println("Deregister Offering");

		// Deregister the Offering from the Marketplace
	    provider.deregister(offeringDescription);
	    
	    // Terminate the Provider instance 
	    provider.terminate();
	   
	}

}

