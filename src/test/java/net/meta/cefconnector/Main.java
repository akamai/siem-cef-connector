package net.meta.cefconnector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

	private static final Logger log = LogManager.getLogger(Main.class);
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();

        CEFConnectorApp instance = new CEFConnectorApp();
        instance.start();
		long endTime = System.currentTimeMillis();
		
		log.error(" &&&& Total Time :" + (endTime - startTime));

		

	}

}
