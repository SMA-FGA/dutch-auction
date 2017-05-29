package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;

/**
 * Agent responsible for initiating and conducting the auction
 */
public class FlowerAuctioneerAgent extends Agent{
	private static final long serialVersionUID = 555268633939588671L;

	protected void setup(){
		//Creating an agent entry on yellow page
	  	DFAgentDescription agentDescription = new DFAgentDescription();
	  	agentDescription.setName(getAID());
	  		
	  	//Adding a service
	  	ServiceDescription service = new ServiceDescription();
	  	service.setName("auctioneer");
	  	service.setType("flower-auctioneer");
	  	agentDescription.addServices(service);
	  	
	  	//agent trying to register on yellow page
	  	try {
	  		DFService.register(this, agentDescription);
	  	}
	  	catch (FIPAException fe) {
	  		fe.printStackTrace();
	  	}
	  	
	  	//searh flower buyer agents on df
	  	DFAgentDescription template = new DFAgentDescription();
	  	ServiceDescription serviceTemplate = new ServiceDescription();
	  	serviceTemplate.setType("flower-buyer");
	  	template.addServices(serviceTemplate);
	  	try{
	  		DFAgentDescription[] resultSearch = DFService.search(this, template); //find agents that match with template
	  		//print founders agents
	  		for(int i = 0; i < resultSearch.length; i++){
		  		System.out.println("Agent:" + resultSearch[i].getName().getLocalName());
		  	}
	  	}catch(FIPAException fe){
	  		fe.printStackTrace();
	  	}
	  	
	}
	
	protected void takeDown(){
		try{
			DFService.deregister(this);
		}catch(FIPAException fe){
			fe.printStackTrace();
		}
	}
	
}
