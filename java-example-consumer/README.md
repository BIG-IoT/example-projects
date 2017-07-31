# BIG-IoT Java Lib - Example Consumer

This project illustrates how to create a simple example consumer using the BIG-IoT Java Lib.

It is setup as a gradle project, where the build.gradle files pulls in the required BIG-IoT libraries from our Project Maven repository.
 
 
## What does the program do?

*Note: You can find the java code under: src/main/java/org/bigiot/examples/ExampleConsumer.java*

The main method creates an new BIG-IoT Consumer instance: 

	Consumer consumer = new Consumer(CONSUMER_ID, MARKETPLACE_URI); 
	
Here it passes the BIG-IoT Marketplace URI. In this case we use the public BIG-IoT Marketplace running at:

	https://market.big-iot.org
		
The call also includes the *Consumer_ID*. A dedicated Consumer_ID for your instance can be obtained from the [Marketplace Web Portal](https://market.big-iot.org/). 

Once the consumer instance has been created, we authenticate the consumer with the marketplace:
		
	    consumer.authenticate(CONSUMER_SECRET);
	 	    
If you create your own Consumer via the Marketplace, you can obtain the *ConsumerSecret* from the Web portal.
	 	    
In a next step, we create an Offering Query. Here we make an query for a random value offering.

	    OfferingQuery query = OfferingQuery.create("RandomNumberQuery")
				.withInformation(new Information("Random Number Query", "bigiot:RandomNumber"))
				.inRegion(Region.city("Stuttgart"))
				.withAccountingType(BigIotTypes.AccountingType.PER_ACCESS)
				.withMaxPrice(Euros.amount(0.002))             
				.withLicenseType(LicenseType.OPEN_DATA_LICENSE);
	  
*Note: Further details about the BIG IoT API and the Offering Query can be found [here](https://big-iot.github.io/).*

In a next step, we call the discovery method with the *OfferingQuery* to find out what matching offerings have been registered on the marketplace:
	    
	    // Discover available offerings based on Offering Query
	    CompletableFuture<List<SubscribableOfferingDescription>> listFuture = consumer.discover(query);			
	    List<SubscribableOfferingDescription> list = listFuture.get();	

The discover method returns a list of matching offering descriptions of the type SubscribableOfferingDescription

Once an offering has been selected, we call the subscribe method.

		// Subscribe to a selected OfferingDescription (if successful, returns accessible Offering instance)		
		CompletableFuture<Offering> offeringFuture = selectedOfferingDescription.subscribe();
		Offering offering = offeringFuture.get();
	
If the subscribe is successful, we obtain an instance of the type Offering. We can then initiate the access to the resources, by creating an instance for the *AccessParameters* and and calling the *accessOneTime* method:

		// Prepare Access Parameters
		AccessParameters accessParameters = AccessParameters.create();
		
		// Access the resources of the offering
		CompletableFuture<AccessResponse> futureResponse =  offering.accessOneTime(accessParameters);
		AccessResponse response = futureResponse.get();
		
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

	https://market.big-iot.org/offering/TestOrganization_TestConsumer-RandomNumberQuery

*Note: If you use your own organization and consumer instance (see the WebPortal [marketplace](https://market.big-iot.org/)), you need to update the Query ID accordingly. 
 
In case someone runs a Provider with a matching RandomNumber Offering, your Example Consumer application will automatically discover the Provider instance via the Marketplace and attempt to access the service. If everything works correct, the application will regularly fetch a new random number and print it.

*Note: To avoid access problems with stale offerings on the Marketplace, the consumer application selects only offerings from providers that run on "localhost". I.e. if you run the corresponding Example Provider application on your localhost, everything should work.



