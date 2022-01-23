package com.tgp.templarsocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;

import org.apache.commons.lang3.SerializationUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.tgp.templarsocket.Message;
import com.tgp.templarsocket.Requisitions;

public class Server extends WebSocketServer {

	private HashMap<WebSocket, Client> clients = new HashMap<>();

	public Server(InetSocketAddress address) {
		super(address);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		clients.put(conn, new Client(conn));
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		clients.remove(conn);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		try {
			clients.get(conn).privateKeyReceived(
					(PublicKey) SerializationUtils.deserialize(Base64.getDecoder().decode(message)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer message) {
		Client client = clients.get(conn);
		Message<?, ?> msg = client.decryptMessage(message.array());
		if(msg.isRequiresCallback()) {
			Message<?, ?> callback = mwcrl.onMessageReceived(client, msg);
			callback.setCallback(true);
			callback.setId(msg.getId());
			client.sendMessage(callback);
		} else {
			mrl.onMessageReceived(client, msg);
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		System.err.println("an error occurred on connection " + conn.getRemoteSocketAddress() + ":" + ex);
	}

	@Override
	public void onStart() {
		System.out.println("server started successfully");
	}

	public interface MessageReceivedListenner {
		void onMessageReceived(Client client, Message<?, ?> message);
	}

	private MessageReceivedListenner mrl = (client, msg) -> {
	};

	public void setOnMessageReceived(MessageReceivedListenner mrl) {
		this.mrl = mrl;
	}

	public interface MessageWithCallbackReceivedListenner {
		Message<?, ?> onMessageReceived(Client client, Message<?, ?> message);
	}

	private MessageWithCallbackReceivedListenner mwcrl = (client, msg) -> {
		return new Message<Integer, String>(0, "return does not setted");
	};

	public void setOnMessageWithCallbackReceived(MessageWithCallbackReceivedListenner mwcrl) {
		this.mwcrl = mwcrl;
	}
}
