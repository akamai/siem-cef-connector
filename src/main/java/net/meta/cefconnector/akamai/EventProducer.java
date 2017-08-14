package net.meta.cefconnector.akamai;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventProducer {

	private BlockingQueue<Message> queue = null;
	private AtomicInteger counter = new AtomicInteger(0);
	private static final Logger log = LogManager.getLogger(EventConsumer.class);

	public EventProducer(BlockingQueue<Message> q) {
		this.queue = q;
	}

	public void produce(Message message) {
		try {
			queue.put(message);
			counter.getAndIncrement();
		} catch (InterruptedException e) {
			log.error("Producer thread interrupted, error processing " + message.getEvent(), e);
		}
	}

	public int getProcessedRecord() {
		return counter.get();
	}
}
