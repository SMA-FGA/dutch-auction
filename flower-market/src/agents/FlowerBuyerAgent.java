package agents;

import java.util.Arrays;
import java.util.List;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

/**
 * Flower buyer agent at auction
 */
public class FlowerBuyerAgent extends Agent{
	private static final long serialVersionUID = 7193161857298710501L;
	private int numberOfFlowers = 0; //number of flowers the buyer wants
	private int flowerPrice = 0; //price they are willing to pay for them

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
	  	
	  	
	  	Object args[] = getArguments();
	  	//checks if enough arguments were informed and assigns them to their variables
	  	if (args != null && args.length >= 2){
	  		try {
	  			numberOfFlowers = Integer.parseInt(args[0].toString());
	  			flowerPrice = Integer.parseInt(args[1].toString());
	  		} catch (NumberFormatException exception){
	  			System.out.println("Invalid arguments! Please type in the number of flowers the" +
	  					" buyer agent wants, \nthen, after a comma (,), the price they are willing" +
	  					" to pay for them - both in decimal form.");
	  		}
	  		
	  		addBehaviour(new FlowerBuyerBehaviour());
	  		
	  		
	  		/* TODO Change this Behaviour from Cyclic to Ticker (or REResponder, maybe)
	  		 * and move it to the FSM.
	  		 * addBehaviour(new CyclicBehaviour() {
				private static final long serialVersionUID = -3436481342609766781L;

				@Override
				public void action() {
					MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CFP);
					ACLMessage message = myAgent.receive(template);
					if (message != null) {
						myAgent.send(handleCFP(message));
					} else {
						block();
					}
				}
			});*/
	  	} else {
	  		//not enough arguments informed, kill agent
  			System.out.println("Not enough arguments informed! Please type in the number of flowers" +
  					" the buyer agent wants, \nthen, after a comma (,), the price they are willing" +
  					" to pay for them - both in decimal form.");
			doDelete();
	  	}
	}
	
	protected void takeDown(){
		try{
			DFService.deregister(this);
		}catch(FIPAException fe){
			fe.printStackTrace();
		}
	}
	
	// TODO move this method to the CFP receiver after it has been implemented in the FSM.
	private ACLMessage handleCFP(ACLMessage cfp) {
		// CFP received. Process it
		if (cfp != null) {
			ACLMessage reply = cfp.createReply();
			
			/* CFP comes in format 'amountOfFlowers, price'
			 * We need to split it and convert the values back to integers. 
			 * */
			String content = cfp.getContent();
			List<String> splitContent = Arrays.asList(content.split(","));
			
			System.out.println(splitContent.get(0));
			System.out.println(splitContent.get(1));
			
			int flowersAvailable =  Integer.parseInt(splitContent.get(0));
			int price =  Integer.parseInt(splitContent.get(1));
			
			if (flowersAvailable >= numberOfFlowers && price <= flowerPrice) {
				reply.setPerformative(ACLMessage.PROPOSE);
				reply.setContent(numberOfFlowers + "," + flowerPrice);
				return reply;
			} else {
				reply.setPerformative(ACLMessage.REFUSE);
				return reply;
			}
		}
		return null;
	}
	
	private class FlowerBuyerBehaviour extends FSMBehaviour {
		private static final long serialVersionUID = -7923578069458706584L;	
		private static final long TICKER_TIME = 5000;
		
		// Constants for state names
		private static final String WAIT_AUCTION = "waiting for auction";
		private static final String AUCTION_ENDED = "auction ended";
		
		public FlowerBuyerBehaviour() {
			registerFirstState(new WaitAuctionBehaviour(myAgent, TICKER_TIME), WAIT_AUCTION);
			
			/* Empty state only to simulate transition. 
  			 * Will be removed when proper end states are implemented.
 			 */
			registerLastState(new OneShotBehaviour() {
				private static final long serialVersionUID = 236173643728064163L;

				@Override
				public void action() {					
				}
			}, AUCTION_ENDED);
			
			registerDefaultTransition(WAIT_AUCTION, AUCTION_ENDED);
		}
		
		/*
		 * This behaviour executes waiting for an Inform from the Auctioneer
		 * that the auction is about to start.
		 */
		private class WaitAuctionBehaviour extends TickerBehaviour {
			private MessageTemplate informTemplate;
			
			public WaitAuctionBehaviour(Agent agent, long period) {
				super(agent, period);
				informTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			}

			private static final long serialVersionUID = 7652201915378925035L;

			@Override
			protected void onTick() {
				ACLMessage inform = myAgent.receive(informTemplate);
				
				if (inform != null) {
					System.out.println("Buyer [" + getAID().getLocalName() + "] was informed.");
					stop();
				} 
			}
		}
	}
}
