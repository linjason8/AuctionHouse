/*
 * EE422C Final Project submission by
 * Jason Lin
 * jjl3777
 * 17625
 * Slip day used: 1
 * Fall 2022
 */

package server;

import static com.mongodb.client.model.Filters.eq;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;

public class Server extends Observable {

	private MongoCollection<Document> users;
	// private Map<String, String> users = new HashMap<>();
	// private Map<String, Double> bals = new HashMap<>();
	private MongoCollection<Document> collection;
	private int numGuest = 1;
	public ArrayList<String> notifs = new ArrayList<>(
			Arrays.asList("Server started... Auction begins! (or continues)"));
	private ServerSocket serverSock;

	public static void main(String[] args) {
		Server server = new Server();
		server.runServer();
	}

	private void runServer() {
		try {
			setUpNetworking();
		} catch (Exception e) {
		}
	}

	private void setUpNetworking() throws Exception {
		ConnectionString connectionString = new ConnectionString(
				"mongodb+srv://jason:JasonPassword123@cluster0.8njyfao.mongodb.net/?retryWrites=true&w=majority");
		MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
				.serverApi(ServerApi.builder().version(ServerApiVersion.V1).build()).build();
		MongoClient mongoClient = MongoClients.create(settings);
		MongoDatabase db = mongoClient.getDatabase("ItemsDB");
		collection = db.getCollection("Items");
		users = db.getCollection("Users");

		serverSock = new ServerSocket(4242);
		System.out.println("Server online at IP: " + serverSock.getInetAddress().getLocalHost());
		while (true) {
			Socket clientSocket = serverSock.accept();
			ClientHandler ch = new ClientHandler(this, clientSocket);
			this.addObserver(ch);
			new Thread(ch).start();
			System.out.println(clientSocket.getInetAddress() + " Connected");
		}
	}

	protected void processRequest(String input, ClientHandler ch) {
		synchronized (this) {
			if (input.equals("CLOSE_CONNECTION")) {
				ch.closeSocket();
				return;
			} else if (input.equals("CLOSE_SERVER")) {
				try {
					serverSock.close();
				} catch (IOException e) {
				}
				System.exit(0);
			} else {
				String[] msg = input.split("~");
				System.out.println(Arrays.toString(msg));
				if (msg[0].equals("BID")) {
					String item = msg[1];
					Double bid = Double.parseDouble(msg[2]);
					Document query = collection.find(eq("Name", item)).limit(1).first();
					if ((Double) query.get("Bid") >= bid) {
						ch.sendToClient("INVALID_BID");
					} else {
						setChanged();
						notifyObservers("UPDATE_BID~" + item + "~" + msg[2] + "~" + msg[3]);

						if (!query.get("Bidder").equals("Starting Price")) {
							System.out.println(query.get("Bidder"));
							Document doc = users.find(eq("user", query.get("Bidder"))).limit(1).first();
							doc.replace("balance", ((Double) doc.get("balance") + (Double) query.get("Bid")));
							users.replaceOne(eq("user", query.get("Bidder")), doc);
						}

						query.replace("Bid", bid);
						query.replace("Bidder", msg[3]);
						ArrayList<String> hist = (ArrayList<String>) query.get("History");
						notifs.add(msg[3] + " has placed a bid on " + item + " for $" + String.format("%,.2f", bid));
						hist.add(msg[3] + " has bid $" + String.format("%,.2f", bid));
						query.replace("History", hist);
						collection.replaceOne(eq("Name", item), query);

						Document doc = users.find(eq("user", msg[3])).limit(1).first();
						doc.replace("balance", (Double) doc.get("balance") - (Double) query.get("Bid"));
						users.replaceOne(eq("user", msg[3]), doc);
					}
				} else if (msg[0].equals("NEW_GUEST")) {
					String user = "Guest" + ((int) (Math.random() * 10000 + 1));
					String pass = String.valueOf(Math.random());
					Document newUser = new Document().append("user", user).append("pass", pass).append("balance",
							1000.0);
					if (users.countDocuments(eq("Name", user)) == 0)
						users.insertOne(newUser);
					else
						users.updateOne(eq("Name", user), newUser);
					ch.sendToClient("USER_ADDED~"+user + "~" + pass + "~1000");
				} else if (msg[0].equals("NEW_USER")) {
					String user = msg[1];
					if (users.countDocuments(eq("user", user)) != 0) {
						ch.sendToClient("INVALID_USER");
					} else {
						String pass = msg[2];
						Document newUser = new Document().append("user", user).append("pass", pass).append("balance",
								1000.0);
						users.insertOne(newUser);
						ch.sendToClient("USER_ADDED~"+user + "~" + pass + "~1000");
					}
				} else if (msg[0].equals("LOGIN")) {
					String user = msg[1];
					String pass = msg[2];
					if (users.countDocuments(eq("user", user)) == 0
							|| !users.find(eq("user", user)).limit(1).first().get("pass").equals(pass)) {
						ch.sendToClient("BAD_LOGIN");
					} else {
						ch.sendToClient(
								"USER_ADDED~"+user + "~" + pass + "~" + users.find(eq("user", user)).limit(1).first().get("balance"));
					}
				} else if (msg[0].equals("UPDATE_BAL")) {
					Document query = users.find(eq("user", msg[1])).limit(1).first();
					query.replace("balance", Double.parseDouble(msg[2]));
					users.replaceOne(eq("user", msg[1]), query);
				} else if (msg[0].equals("ADD_BAL")) {
					Document query = users.find(eq("user", msg[1])).limit(1).first();
					query.replace("balance", (Double) query.get("balance") + Double.parseDouble(msg[2]));
					users.replaceOne(eq("user", msg[1]), query);
				} else if (msg[0].equals("UPDATE_PASS")) {
					Document query = users.find(eq("user", msg[1])).limit(1).first();
					query.replace("pass", msg[2]);
					users.replaceOne(eq("user", msg[1]), query);
				} else if (msg[0].equals("NEWITEM")) {
					Document newDoc = Document.parse(msg[1]);
					newDoc.replace("Bid", (Double) Double.parseDouble(newDoc.get("Bid").toString()));
					newDoc.replace("Buyout", Double.parseDouble(newDoc.get("Buyout").toString()));
					String[] hist = {msg[2] + " has listed " + newDoc.getString("Name"),newDoc.get("History").toString()};
					newDoc.replace("History", new ArrayList<String>(Arrays.asList(hist)));
					if (collection.countDocuments(eq("Name", newDoc.get("Name"))) != 0) {
						ch.sendToClient("DUPLICATE");
					} else {
						notifs.add("NOTICE: New listing added! " + newDoc.getString("Name") + " starting at $"
								+ String.format("%,.2f", newDoc.getDouble("Bid")) + "!");
						collection.insertOne(newDoc);
						setChanged();
						notifyObservers("NEWITEM~" + newDoc.toBsonDocument());
					}
				}
			}
		}
	}

	public MongoCollection<Document> getCollection() {
		return collection;
	}

	public int getNumGuest() {
		return numGuest;
	}

	public void incNumGuest() {
		numGuest++;
	}

}
