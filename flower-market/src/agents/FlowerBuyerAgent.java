package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;

/**
 * Flower buyer agent at auction
 */
public class FlowerBuyerAgent extends Agent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7193161857298710501L;

	protected void setup(){
		//Creating an agent entry on yellow page
	  	DFAgentDescription description = new DFAgentDescription();
	  	description.setName(getAID());
	  		
	  	//Adding a service
	  	ServiceDescription service = new ServiceDescription();
	  	service.setName("buyer");
	  	service.setType("flower-buyer");
	  	description.addServices(service);
	  	
	  	//agent trying to register on yellow page
	  	try {
	  		DFService.register(this, description);
	  	}
	  	catch (FIPAException fe) {
	  		fe.printStackTrace();
	  	}

	}
	
}
