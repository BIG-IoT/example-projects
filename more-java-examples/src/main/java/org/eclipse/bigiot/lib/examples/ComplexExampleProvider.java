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
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.eclipse.bigiot.lib.ProviderSpark;
import org.eclipse.bigiot.lib.exceptions.IncompleteOfferingDescriptionException;
import org.eclipse.bigiot.lib.exceptions.NotRegisteredException;
import org.eclipse.bigiot.lib.handlers.AccessRequestHandler;
import org.eclipse.bigiot.lib.model.BigIotTypes.LicenseType;
import org.eclipse.bigiot.lib.model.BigIotTypes.PricingModel;
import org.eclipse.bigiot.lib.model.IOData;
import org.eclipse.bigiot.lib.model.Price.Euros;
import org.eclipse.bigiot.lib.model.RDFType;
import org.eclipse.bigiot.lib.model.ValueType;
import org.eclipse.bigiot.lib.offering.OfferingDescription;
import org.eclipse.bigiot.lib.offering.RegisteredOffering;
import org.eclipse.bigiot.lib.offering.RegistrableOfferingDescription;
import org.eclipse.bigiot.lib.serverwrapper.BigIotHttpResponse;


/** Example for using BIG IoT API as a provider. This example corresponds with ExampleConsumerNew.java
 * 
 * 
 */
public class ComplexExampleProvider {


	private static final String MARKETPLACE_URI = "https://market.big-iot.org";
//	private static final String MARKETPLACE_URI = "https://market-int.big-iot.org";
//	private static final String MARKETPLACE_URI = "https://market-dev.big-iot.org";
	
	private static final String PROVIDER_ID 	= "Null_Island-Happy_Parkings";
	private static final String PROVIDER_SECRET = "kETI8TK1QjC5whzgNDG9gw==";

	private static AccessRequestHandler accessCallbackDummy = new AccessRequestHandler(){
		@Override
		public BigIotHttpResponse processRequestHandler (
				OfferingDescription offeringDescription, Map<String,Object> inputData) {

			double longitude=0, latitude=0, radius=0;

			BigIotHttpResponse errorResponse = BigIotHttpResponse.error().withBody("{\"status\":\"error\"}").withStatus(422).asJsonType();
			if(!inputData.containsKey("areaSpecification")) return errorResponse;
			Map areaSpecification = (Map) inputData.get("areaSpecification");

			if(!areaSpecification.containsKey("geoCoordinates")) return errorResponse;
			Map geoCoordinates = (Map) areaSpecification.get("geoCoordinates");

			if (!geoCoordinates.containsKey("longitude")) return errorResponse;
			longitude = new Double((String)geoCoordinates.get("longitude"));

			if (!geoCoordinates.containsKey("latitude")) return errorResponse;
			latitude = new Double((String)geoCoordinates.get("latitude"));


			if (!areaSpecification.containsKey("radius")) return errorResponse;
			radius = new Double((String)areaSpecification.get("radius"));

			Random r = new Random();
			int n = (int) (r.nextFloat()*10.0 + 10.0);
			String s= "[";

			for (int i = 0; i < n; i++) {
				if(i>0)s+=",\n";
				s+=String.format(Locale.US, "{\"geoCoordinates\":{\n\"latitude\": %.4f,\n\"longitude\": %.4f},\n\"distance\": %.2f,\n\"status\":\"available\"\n}", r.nextFloat()*0.01+latitude, r.nextFloat()*0.01+longitude, r.nextFloat()*radius);
			}
			s+="]";
			
			
			return BigIotHttpResponse.okay().withBody(s).asJsonType();

		};	
	};

	public static void main(String args[]) throws InterruptedException, IncompleteOfferingDescriptionException, IOException, NotRegisteredException {

		boolean run = true;

		// Initialize provider with provider id and marketpalce URL
		ProviderSpark provider = new ProviderSpark(PROVIDER_ID, MARKETPLACE_URI, "localhost", 9002);

		//		provider.setProxy("127.0.0.1", 3128); //Enable this line if you are behind a proxy
		//		provider.addProxyBypass("172.17.17.100"); //Enable this line and the addresses for internal hosts
		
		provider.setProxy("194.145.60.1",9400); //Enable this line if you are behind a proxy
		provider.addProxyBypass("172.17.17.100"); //Enable this line and the addresses for internal hosts
		
		// Authenticate provider on the marketplace 
		provider.authenticate(PROVIDER_SECRET);

		//Construct Offering Description of your Offering incrementally
		RegistrableOfferingDescription offeringDescription = 
				// provider.createOfferingDescriptionFromOfferingId("TestOrganization-TestProvider-Manual_Offering_Test")
				provider.createOfferingDescription("parkingSpotFinder")
				.withInformation("Demo Parking Offering", new RDFType("bigiot:Parking"))
				.addInputData("areaSpecification", new RDFType("schema:GeoCircle"), IOData.createMembers()
						.addInputData("geoCoordinates", new RDFType("schema:geoCoordinates"), IOData.createMembers()
								.addInputData("longitude", new RDFType("schema:longitude"), ValueType.NUMBER)
								.addInputData("latitude", new RDFType("schema:latitude"), ValueType.NUMBER))
						.addInputData("radius", new RDFType("schema:geoRadius"), ValueType.NUMBER))
				.addOutputData("geoCoordinates", new RDFType("schema:geoCoordinates"), IOData.createMembers()
						.addOutputData("longitude", new RDFType("schema:longitude"), ValueType.NUMBER)
						.addOutputData("latitude", new RDFType("schema:latitude"), ValueType.NUMBER))
				.addOutputData("distance", new RDFType("datex:distanceFromParkingSpace"), ValueType.NUMBER)
				.addOutputData("status", new RDFType("datex:parkingSpaceStatus"), ValueType.TEXT)
				.inCity("Barcelona")
				.withPrice(Euros.amount(0.001))
				.withPricingModel(PricingModel.PER_ACCESS)
				.withLicenseType(LicenseType.OPEN_DATA_LICENSE) 
				//Below is actually Offering specific	
				.withAccessRequestHandler(accessCallbackDummy);

		RegisteredOffering offering = offeringDescription.register();

		// Run until user input is obtained
		System.out.println(">>>>>>  Terminate ExampleProvider by pressing ENTER  <<<<<<");
		Scanner keyboard = new Scanner(System.in);
		keyboard.nextLine();

		offering.deregister();

		// Terminate provider instance 
		provider.terminate();

	}

}
