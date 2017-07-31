# BIG-IoT Java Lib - Example Provider

This project illustrates how to create a simple example provider using the BIG-IoT Java Lib.

It is setup as a gradle project, where the build.gradle files pulls in the required BIG-IoT libraries from our Project Maven repository.

The provider uses the BIG-IoT Embedded Spark Library that allows a provider to offer easy access to resources based on an embedded web server. 


## What does the program do?

*Note: You can find the java code under: src/main/java/org/bigiot/examples/ExampleProvider.java*

The main method creates an new BIG-IoT Provider instance: 

	ProviderSpark provider = new ProviderSpark(PROVIDER_ID, MARKETPLACE_URI, "localhost", 9020);
		
Here it passes the BIG-IoT Marketplace URI. In this case we use the public BIG-IoT Marketplace running at:

	https://market.big-iot.org
		
The call also includes the *PROVIDER_ID*. A dedicated Provider ID for your instance can be obtained from the [Marketplace Web Portal](https://market.big-iot.org/). Finally, the call defines under which IP address or DNS name and port the offered resource can be accessed. In this example, we use 'localhost' and port 9020. You can also use your public IP address or DNS name and a port you choose. 

Once the provider instance has been created, we authenticate the provider with the marketplace:
		
	    provider.authenticate(PROVIDER_SECRET);
	 	    
If you create your own Provider via the Marketplace Portal, you can obtain the *PROVIDER_SECRET* from the Web portal.
	 	    
In a next step, we create an example offering. Here we make an offering for a random value.

	    //Construct Offering Description of your Offering incrementally
	    RegistrableOfferingDescription offeringDescription = provider.createOfferingDescription("RandomNumberOffer")
	    		.withInformation(new Information ("Random Number", new RDFType("bigiot:RandomNumber")))
	    		.addOutputDataElement("value", new RDFType("schema:random"), ValueType.NUMBER)
	    		.inRegion(Region.city("Stuttgart"))
	    		.withPrice(Euros.amount(0.001))
	    		.withAccountingType(AccountingType.PER_ACCESS)
	    		.withLicenseType(LicenseType.OPEN_DATA_LICENSE)
	    		//Below is actually Offering specific	
	    		.withRoute("randomvalue") //implies mode 2 or mode 1
	    		.withAccessRequestHandler(accessCallback);
	  
The offering description also gets a *route*, via which this offering can be eventually accessed by a consumer on the embedded web server, and an access callback (here 'accessCallback'), which is called whenever a consumer accesses the offering.

*Note: Further details about the BIG IoT API and the Offering Descriptions can be found [here](https://big-iot.github.io/).*

In a final step, we register the offering on the marketplace:
	    
	    RegisteredOffering offering = offeringDescription.register();

To make make the provider fully operational, you also need to implement the 'accessCallback' method, which returns with the registered resources of the offering. In this case with a random number in JSON format.


## How do you build and run it?

You can build and run the provider either from the command line using gradle:

	gradle build run
	
Alternatively, you can also import the project into your IDE (e.g. Eclipse) as a gradle project, and then build and run it from the IDE.


## How can you test it? 

Once the provider is running, the can login on the marketplace and see the [registered offering](https://market.big-iot.org/offering/TestOrganization-TestProvider-RandomNumberOffering) at:

	https://market.big-iot.org/offering/TestOrganization-TestProvider-RandomNumberOffering	

*Note: If you use your own organization and provider instance (see the WebPortal [marketplace](https://market.big-iot.org/)), you need to update the Offering ID accordingly. 

You can also access the offering by following the [EndPoint URI](https://localhost:9020/bigiot/access/randomvalue):

	https://localhost:9020/bigiot/access/randomvalue

	
## How can you use it?

With this Example Provider service running, you provide a random number service on the Marketplace. 

You can now also run the corresponding Example Consumer java program, which automatically discovers your Example Provider via the Marketplace and accesses the offering at run-time.

