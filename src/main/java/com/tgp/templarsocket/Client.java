package com.tgp.templarsocket;

import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;

import org.apache.commons.lang3.SerializationUtils;
import org.jasypt.util.binary.AES256BinaryEncryptor;
import org.java_websocket.WebSocket;

import com.tgp.templarsocket.Message;

public class Client {

	private final WebSocket conn;

	private AES256BinaryEncryptor encryptor = new AES256BinaryEncryptor();
	
	private final String encryptionKey;

	public Client(WebSocket conn) {
		this.conn = conn;
		encryptionKey = gerarKey();
	}

	public void privateKeyReceived(PublicKey publicKey) throws Exception{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		conn.send(Base64.getEncoder().encodeToString(cipher.doFinal(encryptionKey.getBytes())));
		encryptor.setPassword(encryptionKey);
	}
	
	public Message<?, ?> decryptMessage(byte[] msg) {
		return (Message<?, ?>) SerializationUtils.deserialize(encryptor.decrypt(msg));
	}

	public void sendMessage(Message<?, ?> msg) {
		conn.send(ByteBuffer.wrap(encryptor.encrypt(SerializationUtils.serialize(msg))));
	}

	private String gerarKey() {
		SecureRandom secureRandom = new SecureRandom();
		Base64.Encoder base64Encoder = Base64.getUrlEncoder();
		byte[] randomBytes = new byte[64];
		secureRandom.nextBytes(randomBytes);
		return base64Encoder.encodeToString(randomBytes);
	}

	public WebSocket getConn() {
		return conn;
	}
}
