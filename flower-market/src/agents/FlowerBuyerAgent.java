package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

/**
 * Flower buyer agent at auction
 */
public class FlowerBuyerAgent extends Agent{
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
	  	
	  	//create message template to respond to the protocol
	  	MessageTemplate protocol = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
	  	MessageTemplate perform = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
	  	MessageTemplate template = MessageTemplate.and(protocol, perform);
	  	addBehaviour(new DutchResponder(this, template)); //add role of the buyer

	}
	
	protected void takeDown(){
		try{
			DFService.deregister(this);
		}catch(FIPAException fe){
			fe.printStackTrace();
		}
	}
	
}
//implements the role of the buyer in the auction
class DutchResponder extends AchieveREResponder{
	private static final long serialVersionUID = 9061459035293633648L;

	public DutchResponder(Agent agent, MessageTemplate template){
		super(agent, template);
	}
	
	//Wait for informs and responds them
	protected ACLMessage prepareResponse(ACLMessage inform){
		ACLMessage info = inform.createReply();
		info.setContent("ok-i-going-to-participate");
		info.setPerformative(ACLMessage.INFORM);
		//System.out.println("ok-i-going-to-participate");
		return info;
	}
	
}