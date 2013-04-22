package ru.pinkponies.app;

public class AppMessage {

	private final node sender;
	private final node receiver;
	private final Object message;

	enum node {
		LOGIN_ACTIVITY, MAIN_ACTIVITY, NETWORKING_THREAD
	}

	AppMessage(final node sender, final node receiver, final Object message) {
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
	}

	public node getSender() {
		return this.sender;
	}

	public node getReceiver() {
		return this.receiver;
	}

	public Object getMessage() {
		return this.message;
	}

	// TODO: add autocomplete sender
}
