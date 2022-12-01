/*
 * EE422C Final Project submission by
 * Jason Lin
 * jjl3777
 * 17625
 * Slip day used: 1
 * Fall 2022
 */

package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Observer;

import org.bson.BsonDocument;
import org.bson.Document;

import com.mongodb.client.FindIterable;

import java.util.Iterator;
import java.util.Observable;

public class ClientHandler implements Runnable, Observer {

	private Server server;
	private Socket clientSocket;
	private BufferedReader fromClient;
	private PrintWriter toClient;

	protected ClientHandler(Server server, Socket clientSocket) {
		this.server = server;
		this.clientSocket = clientSocket;
		try {
			fromClient = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			toClient = new PrintWriter(this.clientSocket.getOutputStream());
		} catch (IOException e) {
		}
	}

	protected void sendToClient(String string) {
		toClient.println(string);
		toClient.flush();
	}

	@Override
	public void run() {
		FindIterable<Document> iterDoc = server.getCollection().find();
		Iterator<Document> it = iterDoc.iterator();
		synchronized (server) {
			while (it.hasNext()) {
				BsonDocument doc = it.next().toBsonDocument();
				sendToClient(doc.toString());
			}
			sendToClient("END_ITEMS");
			sendToClient(server.notifs.toString());
		}

		String input;
		try {
			while ((input = fromClient.readLine()) != null) {
				server.processRequest(input, this);
			}
		} catch (IOException e) {

		}
	}

	@Override
	public void update(Observable o, Object arg) {
		System.out.println((String) arg);
		this.sendToClient((String) arg);
	}

	public void closeSocket() {
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Socket getSocket() {
		return clientSocket;
	}
}