# BIG-IoT Java Lib - Example Consumer

This project illustrates how to create a simple example consumer using the BIG-IoT Java Lib.

It is setup as a gradle project, where the build.gradle files pulls in the required BIG-IoT libraries from our Project Maven repository.
 
 
## What does the program do?

*Note: You can find the java code under: src/main/java/org/bigiot/examples/ExampleConsumer.java*

The main method first loads the properties file:

     BridgeIotProperties prop = BridgeIotProperties.load("example.properties");
	
It then creates a new BIG-IoT Consumer instance: 

	Consumer consumer = Consumer.create(prop.CONSUMER_ID, prop.MARKETPLACE_URI) 
	
Here it passes the BIG-IoT Marketplace URI. In this case we use the public BIG-IoT Marketplace running at:

	https://market.big-iot.org
		
The call also includes the *Consumer_ID*. A dedicated Consumer_ID for your instance can be obtained from the [Marketplace Web Portal](https://market.big-iot.org/). 

Once the consumer instance has been created, we authenticate the consumer with the marketplace:
		
	consumer.authenticate(prop.CONSUMER_SECRET);
	 	    
If you create your own Consumer via the Marketplace, you can obtain the *ConsumerSecret* from the Web portal.
	 	    
In a next step, we create an Offering Query. Here we make an query for offerings of semantic type/category: '"proposed:RandomValue"'. We can also define what output data should be provided by matching offerings (here: '"proposed:randomValue"') and a maximum price for the access as well as the accepted license. 

	OfferingQuery query = OfferingQuery.create("RandomNumberQuery")
			.withName("Random Number Query")
             	.withCategory("proposed:RandomValues")
            	.addOutputData(new RDFType("proposed:randomValue"), ValueType.NUMBER)
			.withPricingModel(PricingModel.PER_ACCESS)
			.withMaxPrice(Euros.amount(0.002))             
			.withLicenseType(LicenseType.OPEN_DATA_LICENSE);
	  
*Note: Further details about the BIG IoT API and the Offering Query can be found [here](https://big-iot.github.io/).*

In a next step, we call the discovery method with the *OfferingQuery* to find out what matching offerings have been registered on the marketplace:
	    
	    // Discover available offerings based on Offering Query
	    List<SubscribableOfferingDescription> list = consumer.discover(query).get();	

The discover method returns a list of matching offering descriptions of the type SubscribableOfferingDescription

Once an offering has been selected, we call the subscribe method.

		// Subscribe to a selected OfferingDescription (if successful, returns accessible Offering instance)		
		Offering offering = selectedOfferingDescription.subscribe().get();
	
If the subscribe is successful, we obtain an instance of the type Offering. We can then initiate the access to the resources, by creating an instance for the *AccessParameters* (this is normally used to provide input data during the access) and and calling the *accessOneTime* method:

		// Prepare Access Parameters
		AccessParameters accessParameters = AccessParameters.create();
		AccessResponse response = offering.accessOneTime(accessParameters).get();
		
In addition to a one-time access, we can also create an Access Feed, which accesses the resources regularly or continuously (depending on the communication protocol used). In the *accessContinues* call, we define callback functions that are called when additional information are retrieved, or an error occurs: 
		
		// Create an Access Feed with callbacks for the received results		
		Duration feedDuration = Duration.standardHours(1);
		Duration feedInterval = Duration.standardSeconds(2);
		AccessFeed accessFeed = offering.accessContinuous(accessParameters, 
									feedDuration.getMillis(), 
									feedInterval.getMillis(), 
								(f,r) -> {  
									System.out.println("Received data: " + r.asJsonNode().get("results").toString());
								},
								(f,r) -> {
									System.out.println("Feed operation failed");
								});
			
								
## How do you build and run it?

You can build and run the provider either from the command line using gradle:

	gradle build run
	
Alternatively, you can also import the project into your IDE (e.g. Eclipse) as a gradle project, and then build and run it from the IDE.


## How can you test it? 

Once the consumer is running, you can login on the marketplace and see the [offering query](https://market.big-iot.org/offering/TestOrganization_TestConsumer-RandomNumberQuery) at:

	https://market.big-iot.org/offeringQuery/TestOrganization-TestConsumer-RandomNumberQuery

*Note: If you use your own organization and consumer instance (see the WebPortal [marketplace](https://market.big-iot.org/)), you need to update the Query ID accordingly. 
 
In case someone runs a Provider with a matching RandomNumber Offering, your Example Consumer application will automatically discover the Provider instance via the Marketplace and attempt to access the service. If everything works correct, the application will regularly fetch a new random number and print it.




