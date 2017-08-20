package agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

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
	  		
	  		addBehaviour(new FlowerAuctioneerBehaviour());
	  	} else {
	  		//not enough arguments informed, kill agent
  			System.out.println("Not enough arguments informed! Please type in the number of flowers" +
  					" the auctioneer agent has, \nthen, after a comma (,), the price of each one -" +
  					" both in decimal form.");
			doDelete();
	  	}
	}
	
	protected void takeDown(){
		try {
			DFService.deregister(this);
		} catch(FIPAException exception) {
			exception.printStackTrace();
		}
	}
	
	private class FlowerAuctioneerBehaviour extends FSMBehaviour {
		private static final long serialVersionUID = -6043784535465943180L;
		
		// Constants for state names
		private static final String SEARCH_AGENTS = "searching for buyer agents";
		private static final String START_AUCTION = "starting auction";
		private static final String SEND_CFP = "sending call for proposal";
		private static final String END_AUCTION = "ending auction";
		
		private List<AID> buyers;
		private ACLMessage cfp;
		
		public FlowerAuctioneerBehaviour() {
			buyers = new ArrayList<>();
			
			registerFirstState(new SearchAgentsBehaviour(), SEARCH_AGENTS);
			registerState(new StartAuctionBehaviour(), START_AUCTION);
			registerState(new AuctionInteractionBehaviour(myAgent, cfp), SEND_CFP);
			
			/* Empty state only to simulate transition. 
  			 * Will be removed when proper end states are implemented.
 			 */
			registerLastState(new OneShotBehaviour() {
				private static final long serialVersionUID = 236173643728064163L;

				@Override
				public void action() {					
				}
			}, END_AUCTION);
			
			registerDefaultTransition(SEARCH_AGENTS, START_AUCTION);
			registerDefaultTransition(START_AUCTION, SEND_CFP);
			registerDefaultTransition(SEND_CFP, END_AUCTION);
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
		
		/**
		 * This behaviour informs all agents saved in the buyers list that the auction is 
		 * going to start. It creates an ACLMessage, adds the agents found on the DF 
		 * as its receivers, sets its protocol as the Dutch Auction one and its content,
		 * then sends it.
		 */
		private class StartAuctionBehaviour extends OneShotBehaviour {
			private static final long serialVersionUID = -4704356444598692779L;

			@Override
			public void action() {
				ACLMessage informStartAuction = new ACLMessage(ACLMessage.INFORM); //message that initialize protocol
				informStartAuction.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION); //define protocol for interaction
			  	informStartAuction.setContent("the-auction-is-going-to-start");
			  	
				for (AID buyer : buyers) {
					informStartAuction.addReceiver(buyer); //add receivers that were found on df
				}
			  	myAgent.send(informStartAuction);
			  	
			  	System.out.println("The auction is about to begin...");
			}
		}
		
		private class AuctionInteractionBehaviour extends ContractNetInitiator {
			private static final long serialVersionUID = 5831124461005530439L;
			
			public AuctionInteractionBehaviour(Agent a, ACLMessage cfp) {
				super(a, cfp);
			}
			
			@Override
			protected Vector<ACLMessage> prepareCfps(ACLMessage cfp) {
				cfp = new ACLMessage(ACLMessage.CFP);
		  		cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
			  	cfp.setConversationId("CFP" + System.currentTimeMillis());
			  	cfp.setContent(numberOfFlowers + "," + flowerPrice);
		  		
		  		for (AID buyer : buyers) {
					cfp.addReceiver(buyer);
					System.out.println("Sending CFP to [" + buyer.getLocalName() +"]");
				}
		  		
				Vector<ACLMessage> messages = new Vector<>();
				messages.addElement(cfp);
				return messages;
 			}
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			protected void handleAllResponses(Vector responses, Vector acceptances) {
				AID roundWinner = null;
				List<AID> roundLosers = new ArrayList<>();
				ACLMessage acceptMessage = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				ACLMessage rejectMessage = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
				
				for (Object response : responses) {
					if (((ACLMessage) response).getPerformative() == ACLMessage.PROPOSE) {
						if (roundWinner == null) {
							roundWinner = ((ACLMessage) response).getSender();
						} else {
							roundLosers.add(((ACLMessage) response).getSender());
						}
					}
					
					System.out.println("Buyer [" + ((ACLMessage) response).getSender().getName() + "] answered with "
							+ ACLMessage.getPerformative(((ACLMessage) response).getPerformative()));
				}
				
				acceptMessage.addReceiver(roundWinner);
				for (AID loser : roundLosers) {
					rejectMessage.addReceiver(loser);
				}
				
				acceptances.add(acceptMessage);
				acceptances.add(rejectMessage);
			}
		}
	}
}