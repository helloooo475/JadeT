package agent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import Other.ProducerInfo;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

/** This class defines the Marketplace
 *  Its purpose is to allow the Consumer Agents to discover the Producer Agents registered.
 *  Producer Agents send their information to the Marketplace which the Consumer Agents can then retrieve. */
public class MarketAgent extends Agent {
	private static final long serialVersionUID = 1L;
	
	private HashMap<String,ProducerInfo> producerInfo;

	protected void setup() {
		System.out.println("[" + getLocalName() + "] Market Agent created");
		
		producerInfo = new HashMap<String,ProducerInfo>();
		
		while (true) {
			ACLMessage msg = this.blockingReceive();
			System.out.println("[" + getLocalName() + "] Message received from " + msg.getSender().getLocalName() + " : " + msg.getContent());
			if (msg.getContent().equals("delete")) break; // debug
			else {
				ByteArrayInputStream bis = new ByteArrayInputStream(msg.getContent().getBytes());
				try {
					ObjectInputStream ois = new ObjectInputStream(bis);
					String type = (String)ois.readObject();
					if (type.equals("producerSendsInfo")) prodSendsInfo(ois);
					else if (type.equals("consumerRequestsInfo")) consRequestsInfo(msg);
				}catch(Exception e) {
					System.out.println("[" + getLocalName() + "] Error");
				}
			}
		}
		
		this.doDelete();
	}
	
	/** Handle a producer sending its info */
	private void prodSendsInfo(ObjectInputStream ois) throws Exception {
		String name = (String)ois.readObject(); 
		ProducerInfo p = (ProducerInfo)ois.readObject();
		producerInfo.put(name, p);
		//System.out.println("[" + getLocalName() + "] Added (" + p.toString() + ") in the database.");
	}
	
	/** Handle a consumer requesting registered producers' info */
	private void consRequestsInfo(ACLMessage msg) {
		ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
		msg2.addReceiver(msg.getSender());
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject("marketSendsInfo");
			oos.writeObject(producerInfo);
			
			msg2.setContent(bos.toString());
			this.send(msg2);
		}catch(Exception e) {}
	}
}
