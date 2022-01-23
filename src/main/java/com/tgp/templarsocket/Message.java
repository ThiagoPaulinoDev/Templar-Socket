package com.tgp.templarsocket;

import java.io.Serializable;
import java.util.UUID;

/**
 * <p>
 * The <Message> class represents a message that can be send by the server and
 * by the client. It contains two important parts, the header that contains an
 * object of type <T> and that serves to both sides know what make with this
 * message, and the message body, that contains an object of type
 * <Q>that is the content of the message.
 * 
 * A message can requires a response that will another message. Because this, an
 * object <Message> contains parameters to indicates if it requires a response
 * or if it is a response. Also it contains a {@link UUID} where this parameter
 * of the response must be the same of the first message.
 * 
 * @author Thiago G. Paulino <thiagogpaulino@gmail.com>
 *
 * @version 1.0
 *
 * @param <T> type of message header.
 * @param <Q> type of message body.
 */
public class Message<T, Q> implements Serializable {

	/**
	 * The class fingerprint that is set to indicate serialization compatibility
	 * with a previous version of the class.
	 */
	private static final long serialVersionUID = -1279730581313569293L;

	/**
	 * Object that contains the message body.
	 */
	private final Q messageBody;

	/**
	 * Object that contains the message header.
	 */
	private final T messageHeader;

	/**
	 * Boolean variable that indicates if this message requires a callback.
	 */
	private boolean requiresCallback;

	/**
	 * Boolean variable that indicates if this message is a callback.
	 */
	private boolean isCallback;

	/**
	 * A unique identifier to this message.
	 */
	private UUID id;

	/**
	 * Constructor for message.
	 * 
	 * @param messageHeader
	 * @param body
	 */
	public Message(T messageHeader, Q body) {
		messageBody = body;
		this.messageHeader = messageHeader;
	}

	/**
	 * Generate a unique id for this message.
	 */
	public void generateId() {
		this.id = UUID.randomUUID();
	}

	/**
	 * Returns the message body.
	 * 
	 * @return <Q>message body
	 */
	public Q getMessageBody() {
		return messageBody;
	}

	/**
	 * Returns the message header
	 * 
	 * @return <T> message header
	 */
	public T getMessageHeader() {
		return messageHeader;
	}

	/**
	 * Verify if the message requires a response.
	 * 
	 * @return boolean requiresCallback
	 */
	public boolean isRequiresCallback() {
		return requiresCallback;
	}

	/**
	 * Set if this message requires a response.
	 * 
	 * @param requiresCallback
	 */
	public void setRequiresCallback(boolean requiresCallback) {
		this.requiresCallback = requiresCallback;
	}

	/**
	 * Verify if this message is a response.
	 * 
	 * @return boolean isCallback
	 */
	public boolean isCallback() {
		return isCallback;
	}

	/**
	 * Set if this message is a response;
	 * 
	 * @param isCallback
	 */
	public void setCallback(boolean isCallback) {
		this.isCallback = isCallback;
	}

	/**
	 * Returns the id.
	 * 
	 * @return UUID id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Set the id.
	 * 
	 * @param id
	 */
	public void setId(UUID id) {
		this.id = id;
	}

	@Override
	public String toString() {
		if (id != null)
			return "[Message] - id: " + id.toString() + "\nheader: " + messageHeader.toString() + "\nbody: "
					+ messageBody.toString();
		else
			return "[Message] - header: " + messageHeader.toString() + "\nbody: " + messageBody.toString();
	}
}
