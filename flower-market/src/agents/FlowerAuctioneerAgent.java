package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

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
	  	catch (FIPAException exeption) {
	  		exeption.printStackTrace();
	  	}
	  	
	  	//searh flower buyer agents on df
	  	DFAgentDescription template = new DFAgentDescription();
	  	ServiceDescription serviceTemplate = new ServiceDescription();
	  	serviceTemplate.setType("flower-buyer");
	  	template.addServices(serviceTemplate);
	  	DFAgentDescription[] resultSearch = null; //agents found
	  	try{
	  		resultSearch = DFService.search(this, template); //find agents that match with template
	  		//print founders agents
	  		for(int i = 0; i < resultSearch.length; i++){
		  		System.out.println("Agent:" + resultSearch[i].getName().getLocalName());
		  	}
	  	}catch(FIPAException fe){
	  		fe.printStackTrace();
	  	}
	  	
	  	//Initializing protocol
	  	ACLMessage inform = new ACLMessage(ACLMessage.INFORM); //mesage that initialize protocol
	  	//add receivers that were found on df
	  	for(int i = 0; i < resultSearch.length; i++){
	  		inform.addReceiver(resultSearch[0].getName());
	  	}
	  	inform.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION); //define protocol for interaction
	  	inform.setContent("the-auction-going-to-start");
	  	addBehaviour(new DutchInitiator(this, inform)); ////add role of the auctioneer
	  	
	}
	
	protected void takeDown(){
		try{
			DFService.deregister(this);
		}catch(FIPAException exeption){
			exeption.printStackTrace();
		}
	}
	
}

//implements the role of the auctioneer in the auction
class DutchInitiator extends AchieveREInitiator{
	private static final long serialVersionUID = -2591356594585592411L;

	public DutchInitiator(Agent agent, ACLMessage mensage){
		super(agent, mensage);
	}
}