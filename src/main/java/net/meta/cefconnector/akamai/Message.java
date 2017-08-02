package net.meta.cefconnector.akamai;

public class Message {
	private String event;
	private boolean token;

	public Message(String event, boolean token) {
		super();
		this.event = event;
		this.token = token;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public boolean isToken() {
		return token;
	}

	public void setToken(boolean token) {
		this.token = token;
	}

}
