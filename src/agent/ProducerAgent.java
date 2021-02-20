package agent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import Other.ProducerInfo;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

/** This class defines an Energy Producer
 *  They can communicate their price table and Energy type to the Market Agent
 *  and respond to a transaction demand from a Consumer. */
public class ProducerAgent extends Agent {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private ProducerInfo info;
	private boolean[] isAvailableReal;

	protected void setup() {
		System.out.println("[" + getLocalName() + "] Producer Agent created");

		name = getLocalName();
		info = new ProducerInfo(new float[24],true);
		Random r = new Random();
		for (int i=0;i<24;i++) info.priceTable[i] = r.nextFloat();
		isAvailableReal = new boolean[24];
		for (int i=0;i<24;i++) isAvailableReal[i] = false;
		isAvailableReal[15] = true;
		isAvailableReal[16] = true;
		isAvailableReal[17] = true;
		
		try {Thread.sleep(1000);}catch(Exception e) {} // debug
		sendInfoToMarket();
		
		while (true) {
			ACLMessage msg = this.blockingReceive();
			System.out.println("[" + getLocalName() + "] Message received from [" + msg.getSender().getLocalName() + "] : " + msg.getContent());
			if (msg.getContent().equals("delete")) break; // debug
			else if (msg.getContent().equals("send")) sendInfoToMarket(); // debug
			else {
				ByteArrayInputStream bis = new ByteArrayInputStream(msg.getContent().getBytes());
				try {
					ObjectInputStream ois = new ObjectInputStream(bis);
					String type = (String)ois.readObject();
					if (type.equals("consumerAsksTransaction")) {
						consAsksTransaction(ois,msg);
					}
				}catch(Exception e) {
					System.out.println("[" + getLocalName() + "] Error");
				}
			}
		}
		
		this.doDelete();
	}
	
	/** Sends a message to the Market Agent with its info to be registered or updated */
	private void sendInfoToMarket() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(new AID("Market", AID.ISLOCALNAME));
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject("producerSendsInfo");
			oos.writeObject(name);
			oos.writeObject(info);
			
			msg.setContent(bos.toString());
			this.send(msg);
		}catch(Exception e) {}
	}
	
	/** Handles a request from a consumer to confirm or reject a transaction */
	private void consAsksTransaction(ObjectInputStream ois,ACLMessage msgReceived) throws Exception {
		int index = Integer.parseInt((String)ois.readObject());
		
		ACLMessage msg = msgReceived.createReply();//new ACLMessage(ACLMessage.INFORM);
		//msg.addReceiver(msgReceived.getSender());
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			
			if (isAvailableReal[index] == true) {
				oos.writeObject("producerAcceptsTransaction");
				msg.setPerformative(ACLMessage.CONFIRM);
			}else {
				oos.writeObject("producerRejectsTransaction");
				msg.setPerformative(ACLMessage.REFUSE);
			}
			oos.writeObject(name);
			oos.writeObject(new Integer(index).toString());
			
			msg.setContent(bos.toString());
			this.send(msg);
		}catch(Exception e) {}
		
		
		
	}
	
}
