# BIG-IoT Java Lib - Example Provider

This project illustrates how to create a simple example provider using the BIG-IoT Java Lib.

It is setup as a gradle project, where the build.gradle files pulls in the required BIG-IoT libraries from our Project Maven repository.

The provider uses the BIG-IoT Embedded Spark Library that allows a provider to offer easy access to resources based on an embedded web server. 


## What does the program do?

*Note: You can find the java code under: src/main/java/org/bigiot/examples/ExampleProvider.java*

The main method first loads the properties file:

     BridgeIotProperties prop = BridgeIotProperties.load("example.properties");
	
It then create a BIG-IoT Provider instance of type ProviderSpark. Note this Provider instance comes with an embedded Web server, that is able to directly serve the offered data: 

	ProviderSpark provider = ProviderSpark.create(prop.PROVIDER_ID, prop.MARKETPLACE_URI, prop.PROVIDER_DNS_NAME, prop.PROVIDER_PORT);

Here it passes the BIG-IoT Marketplace URI. In this case we use the public BIG-IoT Marketplace running at:

	https://market.big-iot.org
		
The call also includes the *PROVIDER_ID*. A dedicated Provider ID for your instance can be obtained from the [Marketplace Web Portal](https://market.big-iot.org/). Finally, the call defines under which IP address or DNS name and port the offered resource can be accessed. In this example, we use 'localhost' and port 9123. You can also use your public IP address or DNS name and a port you choose. 

Once the provider instance has been created, we authenticate the provider with the marketplace:
		
	provider.authenticate(prop.PROVIDER_SECRET);
	 	    
If you create your own Provider via the Marketplace Portal, you can obtain the *PROVIDER_SECRET* from the Web portal.
	 	    
In a next step, we create an example offering. Here we make an offering for a random value. We give the offering an ID ('"RandomNumberOffering"'), a name, a semantic type or category ('"proposed:RandomValue"'), and then we define the actual output data that this offering providers, namely a value and a timestamp. We can also define a price for accessing the offering and a license. 

	 //Construct Offering Description of your Offering incrementally
	 RegistrableOfferingDescription offeringDescription = 
	     OfferingDescription.createOfferingDescription("RandomNumberOffering")
    	    		.withName("Random Number Offering")
    	    		.withCategory("proposed:RandomValues")
    	    		.addOutputData("value", new RDFType("proposed:randomValue"), ValueType.NUMBER)
    	         .addOutputData("timestamp", new RDFType("schema:datePublished"), ValueType.NUMBER)
    	    		.withPrice(Euros.amount(0.001))
    	    		.withPricingModel(PricingModel.PER_ACCESS)
    	    		.withLicenseType(LicenseType.OPEN_DATA_LICENSE);
	  
***Note: A full list of already defined and supported semantic categories is available [here](https://big-iot.github.io/categories/). Via the Marketplace user interface, you can also create new categories during creation of an offering. Those '"proposed"' categories can then also be used in your code.***

***Note: New semantic types for input and output data can be directly created via the code. Just use the keyword '"proposed"' in front of your new type (e.g. '"proposed:randomValue"').***
	  
Before we can register this offering on the Marketplace, we have to define the Endpoint with a callback function (here 'accessCallback'), via which this offering can be eventually accessed by a consumer on the embedded web server. 

	Endpoints endpoints = Endpoints.create(offeringDescription)
              .withAccessRequestHandler(accessCallback);

*Note: Further details about the BIG IoT API and the Offering Descriptions can be found [here](https://big-iot.github.io/).*

In a final step, we register the offering on the marketplace:
	    
	provider.register(offeringDescription, endpoints);

To make make the provider fully operational, you also need to implement the 'accessCallback' method, which returns with the registered resources of the offering. In this case with a random number ('value' property) and a timestamnp ('timestamp' property) in JSON format.


## How do you build and run it?

You can build and run the provider either from the command line using gradle:

	gradle build run
	
Alternatively, you can also import the project into your IDE (e.g. Eclipse) as a gradle project, and then build and run it from the IDE.


## How can you test it? 

Once the provider is running, the can login on the marketplace and see the [registered offering](https://market.big-iot.org/offering/TestOrganization-TestProvider-RandomNumberOffering) at:

	https://market.big-iot.org/offering/TestOrganization-TestProvider-RandomNumberOffering	

*Note: If you use your own organization and provider instance (see the WebPortal [marketplace](https://market.big-iot.org/)), you need to update the Offering ID accordingly. 

You can also access the offering by following the [EndPoint URI](https://localhost:9123/bigiot/access/RandomNumberOffering):

	https://localhost:9123/bigiot/access/RandomNumberOffering

	
## How can you use it?

With this Example Provider running, you provide a random number service on the Marketplace. 

You can now also run the corresponding Example Consumer java program, which automatically discovers your Example Provider via the Marketplace and accesses the offering at run-time.

