package launch;

import agent.*;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

public class Launcher {
	public static void main(String[] args) {
		// disp args
		for (int i=0;i<args.length;i++) System.out.println("Arg[" + i + "] = " + args[i].toString());
		
		// set the nb of consumers and producers from args
		int nbConsumers = (args.length >= 1) ? Integer.parseInt(args[0]) : 1;
		int nbProducers = (args.length >= 2) ? Integer.parseInt(args[1]) : 1;
		
		//init
		Runtime runtime = Runtime.instance();
		Profile config = new ProfileImpl("localhost",8888,null);
		config.setParameter("gui", "true");
		AgentContainer mc = runtime.createMainContainer(config);
		
		// create the agents
		try {
			mc.createNewAgent("Market", MarketAgent.class.getName(), null).start();
			for (int i=0;i<nbConsumers;i++) mc.createNewAgent("Consumer_"+i, ConsumerAgent.class.getName(), null).start();
			for (int i=0;i<nbProducers;i++) mc.createNewAgent("Producer_"+i, ProducerAgent.class.getName(), null).start();
		} catch (StaleProxyException e) {
			
		}
		
	}
}
