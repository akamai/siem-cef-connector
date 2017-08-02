package net.meta.cefconnector.akamai;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class EventProducer {

	private BlockingQueue<Message> queue = null;
	private AtomicInteger counter = new AtomicInteger(0);

	public EventProducer(BlockingQueue<Message> q) {
		this.queue = q;
	}

	public void produce(Message message) {
		try {
			queue.put(message);
			counter.getAndIncrement();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public int getProcessedRecord() {
		return counter.get();
	}
}
