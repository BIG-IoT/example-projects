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
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import org.bigiot.lib.ProviderSpark;
import org.bigiot.lib.exceptions.IncompleteOfferingDescriptionException;
import org.bigiot.lib.handlers.AccessRequestHandler;
import org.bigiot.lib.model.BigIotTypes.AccountingType;
import org.bigiot.lib.model.BigIotTypes.LicenseType;
import org.bigiot.lib.model.Information;
import org.bigiot.lib.model.Price.Euros;
import org.bigiot.lib.model.RDFType;
import org.bigiot.lib.model.ValueType;
import org.bigiot.lib.offering.OfferingDescription;
import org.bigiot.lib.offering.RegisteredOffering;
import org.bigiot.lib.offering.RegistrableOfferingDescription;
import org.bigiot.lib.query.elements.Region;
import org.bigiot.lib.serverwrapper.BigIotHttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/** 
 * Example for using BIG IoT API as a Provider. This example corresponds with Example Consumer project * 
 */
public class ExampleProvider {
	
	private static final String MARKETPLACE_URI = "https://market.big-iot.org";
	private static final String PROVIDER_ID 	= "TestOrganization-TestProvider";
	private static final String PROVIDER_SECRET = "lvYjYgQXS4GlE2gxEr0SVw==";
	
	private static Random rand = new Random();

	private static AccessRequestHandler accessCallback = new AccessRequestHandler() {
		@Override
		public BigIotHttpResponse processRequestHandler (
	           OfferingDescription offeringDescription, Map<String,Object> inputData) {
			
			/*
			double longitude=0, latitude=0, radius=0;
			if (inputData.containsKey("longitude")) {
				longitude = new Double(inputData.get("longitude"));
			}
			if (inputData.containsKey("latitude")) {
				latitude = new Double(inputData.get("latitude"));
			}
			if (inputData.containsKey("radius")) {
				radius = new Double(inputData.get("radius"));
			}
			*/
			
			// Create the response as a JSON Object of the form: { "results" : [ "value" : 0.XXX ] }
			JSONObject value = new JSONObject();
			value.put("value", rand.nextFloat());
			JSONArray array = new JSONArray();
			array.put(value);
			JSONObject results = new JSONObject();
			results.put("results",  array);
						
			return BigIotHttpResponse.okay().withBody(results.toString()).asJsonType();
		};	
	};

	public static void main(String args[]) throws InterruptedException, IncompleteOfferingDescriptionException, IOException {
				
		// Initialize provider with Provider ID and Marketplace URI
		ProviderSpark provider = new ProviderSpark(PROVIDER_ID, MARKETPLACE_URI, "localhost", 9020);
		
		// Authenticate provider instance on the marketplace 
	    provider.authenticate(PROVIDER_SECRET);
	 	    
	    // Construct Offering Description of your Offering incrementally
	    RegistrableOfferingDescription offeringDescription = provider.createOfferingDescription("RandomNumberOffering")
	    		.withInformation(new Information ("Random Number", new RDFType("bigiot:RandomNumber")))
	    		//.addInputDataElement("longitude", new RDFType("schema:longitude"))
	    		//.addInputDataElement("latitude", new RDFType("schema:latitude"))
	    		//.addInputDataElement("radius", new RDFType("schema:geoRadius"))
	    		.addOutputDataElement("value", new RDFType("schema:random"), ValueType.NUMBER)
	    		.inRegion(Region.city("Stuttgart"))
	    		.withPrice(Euros.amount(0.001))
	    		.withAccountingType(AccountingType.PER_ACCESS)
	    		.withLicenseType(LicenseType.OPEN_DATA_LICENSE)
	    		//Below is actually Offering specific	
	    		.withRoute("randomvalue")
	    		.withAccessRequestHandler(accessCallback);
	    
	    // Register OfferingDescription on Marketplace - this will create a local endpoint based on the embedded Spark Web server
	    RegisteredOffering offering = offeringDescription.register();
	    
		// Run until user presses the ENTER key
		System.out.println(">>>>>>  Terminate ExampleProvider by pressing ENTER  <<<<<<");
		Scanner keyboard = new Scanner(System.in);
		keyboard.nextLine();
	 
		System.out.println("Deregister Offering");

		// Deregister the Offering from the Marketplace
	    offering.deregister();
	    
	    // Terminate the Provider instance 
	    provider.terminate();
	   
	}

}

