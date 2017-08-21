package net.meta.cefconnector.logger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Special class for logging messages to filelog and/or syslog
 *
 */
public class LogSaver {

	private static final Logger log = LogManager.getLogger(LogSaver.class);

	private LogSaver() {
	}

	/**
	 * Saves message to filelog and/or syslog (if they are configured)
	 *
	 * @param message
	 *            message to log
	 */
	static void save(String message) {
		// Writing Event to Socket Appender
		// Below line is an error, change it to uppercase SYSLOG
		//LogManager.getLogger("cefsyslog").log(Level.getLevel("syslog"), message);
		LogManager.getLogger("cefsyslog").log(Level.getLevel("SYSLOG"), message);
		if (log.isDebugEnabled()) {
			log.debug(message);
		}
	}
}
