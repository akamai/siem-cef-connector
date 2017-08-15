package net.meta.cefconnector.akamai;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.meta.cefconnector.config.CEFContext;
import net.meta.cefconnector.dataaccess.DBUtil;
import net.meta.cefconnector.logger.CEFLogger;

public class EventConsumer implements Runnable {
	private static final Logger log = LogManager.getLogger(EventConsumer.class);
	private BlockingQueue<Message> queue = null;
	private CEFLogger logger = null;
	private CEFContext context = null;
	private volatile boolean done = false;

	public EventConsumer(CEFContext context, CEFLogger logger, BlockingQueue<Message> q) {
		this.queue = q;
		this.logger = logger;
		this.context = context;
	}

	@Override
	public void run() {
		long start = System.currentTimeMillis();
		while (!done) {
			try {
				Message message = queue.poll(5, TimeUnit.MILLISECONDS);
				if (message != null) {
					if (message.isToken()) {
						try {
							String offset = logger.processToken(message.getEvent());

							if (context.isOffsetMode() || (!context.isOffsetMode()
									&& (context.getDateTimeTo() == null || context.getDateTimeTo().isEmpty()))) {

								long oneRecstart = System.currentTimeMillis();
								DBUtil.setLastOffset(offset);
								context.setDataOffset(offset);
								long oneRecend = System.currentTimeMillis();
								log.info("Time Taken to process offset :" + (oneRecend - oneRecstart));

							}
						} catch (SQLException e) {
							log.error("Error persisting offset " + message.getEvent() + "\n Context :"
									+ context.toString(), e);
						} catch (Exception e) {
							log.error("Error processing offset " + message.getEvent() + "\n Context :"
									+ context.toString(), e);
						} finally {
							done = true;
						}
					} else {
						// long oneRecstart = System.currentTimeMillis();
						logger.processLogLine(context, message.getEvent());
						// long oneRecend = System.currentTimeMillis();
						// log.info("Time Taken to process a single record :" +
						// (oneRecend - oneRecstart));
					}
				}
			} catch (InterruptedException e) {
				log.error("Context Info=%s", context.toString());
				log.error("Consumer thread interrupted ", e);
				done = true;
			}
		}
		long end = System.currentTimeMillis();
		log.info("Consumer Time Taken :" + (end - start));
	}

}
