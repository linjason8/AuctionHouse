/*
 * EE422C Final Project submission by
 * Jason Lin
 * jjl3777
 * 17625
 * Slip day used: 1
 * Fall 2022
 */

package serverReset;

import java.util.Iterator;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;

public class ServerReset {
	public static void main(String[] args) {
		ConnectionString connectionString = new ConnectionString(
				"mongodb+srv://jason:JasonPassword123@cluster0.8njyfao.mongodb.net/?retryWrites=true&w=majority");
		MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
				.serverApi(ServerApi.builder().version(ServerApiVersion.V1).build()).build();
		MongoClient mongoClient = MongoClients.create(settings);
		MongoDatabase db = mongoClient.getDatabase("ItemsDB");
		MongoCollection<Document> backup = db.getCollection("Backup");
		MongoCollection<Document> items = db.getCollection("Items");
		MongoCollection<Document> users = db.getCollection("Users");
		
		users.deleteMany(new Document());
		items.deleteMany(new Document());
		Iterator<Document> it = backup.find().iterator();
		while(it.hasNext()) {
			items.insertOne(it.next());
		}
		
		System.out.println("Server reset complete");
	}
}
