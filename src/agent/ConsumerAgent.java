package agent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import Other.ProducerInfo;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

/** This class defines a Consumer
 *  They request the list of producers and their information to the market and can then
 *  ask the producers if they accept a transaction for booking. */
public class ConsumerAgent extends Agent {
	private static final long serialVersionUID = 1L;
	
	private HashMap<String,ProducerInfo> producerInfo;
	
	private String producerChosen;
	private int timeSlotChosen;
	private boolean found;

	protected void setup() {
		System.out.println("[" + getLocalName() + "] Consumer Agent created");
		
		found = false;
		producerInfo = null;
		boolean continue_ = true;
		
		try {Thread.sleep(1000);}catch(Exception e) {} // debug
		
		while (continue_) {
			ACLMessage msg = this.blockingReceive(3000);
			if (msg != null) {
				System.out.println("[" + getLocalName() + "] Message received from " + msg.getSender().getLocalName() + " : " + msg.getContent());
				if (msg.getContent().equals("delete")) break; // debug
				else if (msg.getContent().equals("getInfo")) requestInfo(); // debug
				else if (msg.getContent().equals("ask")) calcUtilityAndAsk(); // debug
				else {
					ByteArrayInputStream bis = new ByteArrayInputStream(msg.getContent().getBytes());
					try {
						ObjectInputStream ois = new ObjectInputStream(bis);
						String type = (String)ois.readObject();
						if (type.equals("marketSendsInfo")) marketSendsInfo(ois);
						else if (type.equals("producerAcceptsTransaction")) found = true;
						else if (type.equals("producerRejectsTransaction")) recalcUtilityAndAsk(ois);
					}catch(Exception e) {
						System.out.println("[" + getLocalName() + "] Error");
					}
				}
			}else {
				if (producerInfo == null) requestInfo();
				else if (found == false) {
					calcUtilityAndAsk();
				}else {
					
				}
			}
		}
		
		this.doDelete();
	}
	
	/** finds the best producer and time slot according to price */
	private void calcBestUtility() {
		float min_ = 100.0f;
		producerChosen = null;
		timeSlotChosen = 0;
		for (Map.Entry<String,ProducerInfo> p : producerInfo.entrySet()) {
			for (int i=0;i<24;i++) {
				if (p.getValue().isAvailable[i] && p.getValue().priceTable[i] < min_) {
					producerChosen = p.getKey();
					timeSlotChosen = i;
					min_ = p.getValue().priceTable[i];
				}
			}
		}
		
		System.out.println("[" + getLocalName() + "] Best producer chosen : " + producerChosen + " (" + timeSlotChosen + ") " +  " : " + min_);
	}
	
	/** request producers' info to the market */
	private void requestInfo() {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(new AID("Market", AID.ISLOCALNAME));
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject("consumerRequestsInfo");
			
			msg.setContent(bos.toString());
			this.send(msg);
		}catch(Exception e) {}
	}
	
	/** Handle the market sending producers' info */
	private void marketSendsInfo(ObjectInputStream ois) throws Exception {
		producerInfo = (HashMap<String,ProducerInfo>)ois.readObject();
		System.out.println(producerInfo.toString());
	}
	
	/** send a transaction request to the producer which provides the best utility */
	private void calcUtilityAndAsk() {
		if (producerInfo != null) {
			calcBestUtility();
			
			if (producerChosen != null) {
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(new AID(producerChosen, AID.ISLOCALNAME));
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				try {
					ObjectOutputStream oos = new ObjectOutputStream(bos);
					oos.writeObject("consumerAsksTransaction");
					oos.writeObject(new Integer(timeSlotChosen).toString());
	
					msg.setContent(bos.toString());
					this.send(msg);
				}catch(Exception e) {}
			}
		}
	}
	
	/** Handle refusal from a producer */
	private void recalcUtilityAndAsk(ObjectInputStream ois) throws Exception {
		String name = (String)ois.readObject();
		int index = Integer.parseInt((String)ois.readObject());
		ProducerInfo p = producerInfo.get(name);
		// label the refused time slot as unavailable and retry
		p.isAvailable[index] = false;
		calcUtilityAndAsk();
	}
}
