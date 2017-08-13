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
		// log.log(Level.forName("syslog", 50), message);
		LogManager.getLogger("cefsyslog").log(Level.getLevel("syslog"), message);
		if (log.isDebugEnabled()) {
			log.debug(message);
		}
	}
}
