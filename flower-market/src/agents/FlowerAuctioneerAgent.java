package agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
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
	private int numberOfFlowers = 0; //number of flowers the auctioneer has
	private int flowerPrice = 0; //price of each flower
	
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
	  	catch (FIPAException exception) {
	  		exception.printStackTrace();
	  	}
	  	
	  	
	  	Object args[] = getArguments();
	  	//checks if enough arguments were informed and assigns them to their variables
	  	if (args != null && args.length >= 2){
	  		try {
	  			numberOfFlowers = Integer.parseInt(args[0].toString());
	  			flowerPrice = Integer.parseInt(args[1].toString());
	  		} catch (NumberFormatException exception){
	  			System.out.println("Invalid arguments! Please type in the number of flowers the" +
	  					" auctioneer agent has, \nthen, after a comma (,), the price of each one -" +
	  					" both in decimal form.");
	  		}
	  	
	  		// TODO Remove this behaviour from setup and extract as a separate behaviour in the FSM.
	  		/*addBehaviour(new TickerBehaviour(this, 10000) {
	  			private static final long serialVersionUID = 1L;

				@Override
				protected void onTick() {
			  		// if there are flowers, send cfp
			  		if (numberOfFlowers > 0){
				  		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				  		cfp.setContent(numberOfFlowers + "," + flowerPrice);
				  		addReceivers(cfp, searchAgents()); 
				  		addBehaviour(new DutchInitiator(myAgent, cfp)); //add role of the auctioneer
			  		} else {
			  			doDelete();
			  		}
				}
	  		});*/
	  		
	  		addBehaviour(new FlowerAuctioneerBehaviour());
	  	} else {
	  		//not enough arguments informed, kill agent
  			System.out.println("Not enough arguments informed! Please type in the number of flowers" +
  					" the auctioneer agent has, \nthen, after a comma (,), the price of each one -" +
  					" both in decimal form.");
			doDelete();
	  	}
	}
	
	
	/**
	 * This method adds the receivers contained in the DFAgentDescription array
	 * to the ACLMessage received on the parameters.
	 * @param message the ACLMessage which the receivers are going to be added
	 * @param receivers the DFAgentDescription array of the agents who are going
	 * to receive the message.
	 */
	protected void addReceivers(ACLMessage message, DFAgentDescription[] receivers){
		for(int agent = 0; agent < receivers.length; agent++){
	  		message.addReceiver(receivers[agent].getName());
	  	}
	}
	
	//TODO transform this method into a separate behaviour inside the FSM.
	/**
	 * This method informs all agents subscribed to the DF that the auction is 
	 * going to start. It creates an ACLMessage, adds the agents found on the DF 
	 * as its receivers, sets its protocol as the Dutch Auction one and its content,
	 * then sends it.
	 * @param receivers The DFAgentDescription array of all agents that are going 
	 * to receive the message.
	 */
	protected void startAuction(DFAgentDescription[] receivers){
		ACLMessage informStartAuction = new ACLMessage(ACLMessage.INFORM); //message that initialize protocol
		addReceivers(informStartAuction, receivers); //add receivers that were found on df
		informStartAuction.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION); //define protocol for interaction
	  	informStartAuction.setContent("the-auction-is-going-to-start");
	  	this.send(informStartAuction);
	}
	
	
	
	protected void takeDown(){
		try{
			DFService.deregister(this);
		}catch(FIPAException exception){
			exception.printStackTrace();
		}
	}
	
	private class FlowerAuctioneerBehaviour extends FSMBehaviour {
		private static final long serialVersionUID = -6043784535465943180L;
		
		// Constants for state names
		private static final String SEARCH_AGENTS = "searching for buyer agents";
		private static final String END_AUCTION = "ending auction";
		
		private List<AID> buyers;
		
		public FlowerAuctioneerBehaviour() {
			buyers = new ArrayList<>();
			
			registerFirstState(new SearchAgentsBehaviour(), SEARCH_AGENTS);
			
			/* Empty state only to simulate transition. 
  			 * Will be removed when proper end states are implemented.
 			 */
			registerLastState(new OneShotBehaviour() {
				private static final long serialVersionUID = 236173643728064163L;

				@Override
				public void action() {					
				}
			}, END_AUCTION);
			
			registerDefaultTransition(SEARCH_AGENTS, END_AUCTION);
		}
		
		/**
		 * This behaviour searches for buyer agents registered on the DF.
		 * It then stores the found buyers' AIDs in a List for future use.
		 */
		private class SearchAgentsBehaviour extends OneShotBehaviour {
			private static final long serialVersionUID = -8880846336869507985L;

			@Override
			public void action() {
				DFAgentDescription template = new DFAgentDescription();
			  	ServiceDescription serviceTemplate = new ServiceDescription();
			  	serviceTemplate.setType("flower-buyer");
			  	template.addServices(serviceTemplate);
				DFAgentDescription[] resultSearch = null;
				
				try {
			  		resultSearch = DFService.search(myAgent, template); //find agents that match with template
			  		//print found agents
			  		for(int i = 0; i < resultSearch.length; i++){
			  			buyers.add(resultSearch[i].getName());
				  		System.out.println("Agent:" + resultSearch[i].getName().getLocalName());
				  	}
			  	} catch(FIPAException fe){
			  		fe.printStackTrace();
			  	}
			}
		}
		
	}
}

//implements the role of the auctioneer in the auction
class DutchInitiator extends AchieveREInitiator{
	private static final long serialVersionUID = -2591356594585592411L;

	public DutchInitiator(Agent agent, ACLMessage message){
		super(agent, message);
	}
	
	@Override
	protected Vector<ACLMessage> prepareRequests(ACLMessage cfp) {
		Vector<ACLMessage> requests = new Vector<ACLMessage>(1);
	  	cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
	  	cfp.setConversationId("CFP" + System.currentTimeMillis());
		requests.addElement(cfp);
		return requests;
	}
}