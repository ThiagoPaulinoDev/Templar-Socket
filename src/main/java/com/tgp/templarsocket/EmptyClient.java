package com.tgp.templarsocket;

import java.net.URI;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.lang3.SerializationUtils;
import org.jasypt.util.binary.AES256BinaryEncryptor;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import com.tgp.templarsocket.Message;

public class EmptyClient extends WebSocketClient {

	private Cipher cipher;

	private final AES256BinaryEncryptor encryptor = new AES256BinaryEncryptor();
	
	private Map<UUID, Callback> requisitionsWithCallback = new HashMap<>();

	public EmptyClient(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	public EmptyClient(URI serverURI) {
		super(serverURI);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024);
			KeyPair pair = keyGen.generateKeyPair();

			String privateKey = Base64.getEncoder().encodeToString(SerializationUtils.serialize(pair.getPublic()));
			send(privateKey);

			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		System.out.println("closed with exit code " + code + " additional info: " + reason);
	}

	@Override
	public void onMessage(String message) {
		try {
			String encryptionKey = new String(cipher.doFinal(Base64.getDecoder().decode(message)));
			encryptor.setPassword(encryptionKey);
			cl.onConnected();
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(ByteBuffer message) {
		Message<?, ?> msg = (Message<?, ?>) SerializationUtils.deserialize(encryptor.decrypt(message.array()));
		if(msg.isCallback()) {
			UUID id = msg.getId();
			requisitionsWithCallback.get(id).onCallback(msg);
			requisitionsWithCallback.remove(id);
		} else {
			mrl.onMessageReceived(msg);
		}
	}

	@Override
	public void onError(Exception ex) {
		System.err.println("an error occurred:" + ex);
	}

	public void sendMessage(Message<?, ?> message) {
		send(ByteBuffer.wrap(encryptor.encrypt(SerializationUtils.serialize(message))));
	}
	
	public void sendMessageWithCallback(Message<?, ?> message, Callback callback) {
		message.setRequiresCallback(true);
		message.generateId();
		requisitionsWithCallback.put(message.getId(), callback);
		send(ByteBuffer.wrap(encryptor.encrypt(SerializationUtils.serialize(message))));
	}
	
	public interface MessageReceivedListenner{
		void onMessageReceived(Message<?, ?> message);
	}
	
	private MessageReceivedListenner mrl = msg -> {};
	
	public void setOnMessageReceived(MessageReceivedListenner mrl) {
		this.mrl = mrl;
	}

	public interface Callback{
		void onCallback(Message<?, ?> message);
	}
	
	public interface ConnectedListenner {
		void onConnected();
	}
	
	private ConnectedListenner cl = () -> {};

	public void setOnConnected(ConnectedListenner cl) {
		this.cl = cl;
	}
}