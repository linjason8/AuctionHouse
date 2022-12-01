/*
 * EE422C Final Project submission by
 * Jason Lin
 * jjl3777
 * 17625
 * Slip day used: 1
 * Fall 2022
 */

package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Client extends Application {

	private static String host = "127.0.0.1";
	private static Socket socket;
	private static BufferedReader fromServer;
	private static PrintWriter toServer;

	private boolean login = true;

	private String user = "";
	private String password = "";
	public ArrayList<Item> items = new ArrayList<>();
	public ArrayList<String> notifsLog;
	public double balance = 0;

	public static Stage stage;
	private static Controller signup;
	private static Controller loginCon;
	private static Controller2 auction;

	public static void main(String[] args) {
		Client client = new Client();
		Controller.client = Controller2.client = client;
		try {
			client.setUpNetworking();
			launch(args);
		} catch (Exception e) {
			System.exit(0);
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Screen screen = Screen.getPrimary();

		stage = primaryStage;
		primaryStage.setTitle("Client GUI");

		FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
		stage.setScene(new Scene(loader.load()));
		loginCon = (Controller) loader.getController();

		primaryStage.setX(screen.getBounds().getMinX());
		primaryStage.setY(screen.getBounds().getMinY());
		primaryStage.setWidth(screen.getBounds().getWidth());
		primaryStage.setHeight(screen.getBounds().getHeight());
		primaryStage.show();

		primaryStage.setResizable(false);
		primaryStage.setFullScreen(true);

		primaryStage.setOnHidden(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				try {
					sendToServer("CLOSE_CONNECTION");
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("Closing connection");
				System.exit(0);
			}
		});
	}

	private void changeStage() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					FXMLLoader loader = new FXMLLoader(getClass().getResource("Auction.fxml"));
					stage.getScene().setRoot(loader.load());
					auction = (Controller2) loader.getController();
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Failed to join auction");
				}
				stage.setResizable(false);
				stage.setFullScreen(true);
			}
		});
	}

	private void setUpNetworking() throws Exception {
		socket = new Socket(host, 4242);

		fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		toServer = new PrintWriter(socket.getOutputStream());

		Thread readerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				String input;
				try {
					readItems();

					input = fromServer.readLine();
					String[] temp = input.split(", ");
					temp[0] = temp[0].substring(1);
					temp[temp.length - 1] = temp[temp.length - 1].substring(0, temp[temp.length - 1].length() - 1);
					notifsLog = new ArrayList<>(Arrays.asList(temp));

					while ((input = fromServer.readLine()) != null) {
						String[] in = input.split("~");
						if (in[0].equals("INVALID_USER")) {
							signup.invalidUser();
						} else if (in[0].equals("BAD_LOGIN")) {
							loginCon.logFail();
						} else if (in[0].equals("USER_ADDED")) {
							login = false;
							user = in[1];
							password = in[2];
							balance = Double.parseDouble(in[3]);
							changeStage();
						} else if (in[0].equals("INVALID_BID")) {
							auction.invalid();
						} else if (in[0].equals("UPDATE_BID")) {
							notifsLog.add(in[3] + " has placed a bid on " + in[1] + " for $"
									+ String.format("%,.2f", Double.parseDouble(in[2])));
							if (login) {
								int index = -1;
								for (int i = 0; i < items.size(); i++) {
									if (items.get(i).getName().equals(in[1])) {
										index = i;
										break;
									}
								}
								items.get(index).history
										.add(in[3] + " has bid $" + String.format("%,.2f", Double.parseDouble(in[2])));

								items.get(index).setBid(Double.parseDouble(in[2]));
								items.get(index).setBidder(in[3]);
							} else {
								auction.update(in);
							}
						} else if (in[0].equals("NEWITEM")) {
							JsonElement item = JsonParser.parseString(in[1]).getAsJsonObject();
							String name = item.getAsJsonObject().get("Name").toString().replaceAll("\"", "");
							String desc = item.getAsJsonObject().get("Description").toString().replaceAll("\"", "");
							Double bid = Double.parseDouble(item.getAsJsonObject().get("Bid").toString());
							Double buyNow = Double.parseDouble(item.getAsJsonObject().get("Buyout").toString());
							String bidder = item.getAsJsonObject().get("Bidder").toString().replaceAll("\"", "");
							LocalDateTime time = LocalDateTime
									.parse(item.getAsJsonObject().get("Time").toString().replaceAll("\"", ""));
							items.add(new Item(name, desc, bid, buyNow, bidder, time));
							JsonArray jHistory = item.getAsJsonObject().get("History").getAsJsonArray();
							for (int i = 0; i < jHistory.size(); i++) {
								items.get(items.size() - 1).history
										.add(jHistory.get(i).toString().replaceAll("\"", ""));
							}
							String imgURL = item.getAsJsonObject().get("Image").toString().replaceAll("\"", "");
							if (!imgURL.equals("")) {
								try {
									items.get(items.size() - 1).setImg(new Image(imgURL));
								} catch (Exception e) {

								}
							}
							notifsLog.add("NOTICE: New listing added! " + name + " starting at $"
									+ String.format("%,.2f", bid) + "!");
							if (!login)
								auction.addedItem();
						} else if (in[0].equals("DUPLICATE")) {
							auction.duplicate();
						}
						System.out.println("From server: " + input);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			public void readItems() throws IOException {
				String input;
				boolean readingItems = true;
				JsonArray arr = new JsonArray();
				while (readingItems && (input = fromServer.readLine()) != null) {
					if (input.equals("END_ITEMS")) {
						readingItems = false;
					} else {
						JsonElement item = JsonParser.parseString(input).getAsJsonObject();
						arr.add(item);
						String name = item.getAsJsonObject().get("Name").toString().replaceAll("\"", "");
						String desc = item.getAsJsonObject().get("Description").toString().replaceAll("\"", "");
						Double bid = Double.parseDouble(item.getAsJsonObject().get("Bid").toString());
						Double buyNow = Double.parseDouble(item.getAsJsonObject().get("Buyout").toString());
						String bidder = item.getAsJsonObject().get("Bidder").toString().replaceAll("\"", "");
						LocalDateTime time = LocalDateTime
								.parse(item.getAsJsonObject().get("Time").toString().replaceAll("\"", ""));
						items.add(new Item(name, desc, bid, buyNow, bidder, time));
						JsonArray jHistory = item.getAsJsonObject().get("History").getAsJsonArray();
						for (int i = 0; i < jHistory.size(); i++) {
							items.get(items.size() - 1).history.add(jHistory.get(i).toString().replaceAll("\"", ""));
						}
					}
				}

				initImages(arr);
			}
		});

		readerThread.start();
	}

	protected void processRequest(String input) {
		return;
	}

	protected void sendToServer(String string) {
		System.out.println("send: " + string);
		toServer.println(string);
		toServer.flush();
	}

	public String getUser() {
		return user;
	}

	public void setUser(String string) {
		sendToServer(string);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String string) {
		password = string;
	}

	public void signup() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					FXMLLoader loader = new FXMLLoader(getClass().getResource("Signup.fxml"));
					stage.getScene().setRoot(loader.load());
					signup = (Controller) loader.getController();
				} catch (IOException e) {
					e.printStackTrace();
				}
				stage.setResizable(false);
				stage.setFullScreen(true);
			}
		});
	}

	public void login() {
		login = true;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
					stage.getScene().setRoot(loader.load());
					loginCon = (Controller) loader.getController();
				} catch (IOException e) {
					e.printStackTrace();
				}
				stage.setResizable(false);
				stage.setFullScreen(true);
			}
		});
	}

	private void initImages(JsonArray arr) {
		(new Thread(new Runnable() {
			public void run() {
				while (stage == null)
					;
				for (int i = 0; i < arr.size(); i++)
					if (!arr.get(i).getAsJsonObject().get("Image").toString().equals("")) {
						try {
							items.get(i).setImg(new Image(
									arr.get(i).getAsJsonObject().get("Image").toString().replaceAll("\"", "")));
						} catch (Exception e) {

						}
					}
			}
		})).start();
	}

	public static String passwordHash(String string) {
		String out = "0fA1fwh8AW41nfAW1" + string + "0Af2ubfofDWDwfaj31";
		double sum = 0;
		for (int i = 0; i < out.length(); i++) {
			sum += out.charAt(i);
		}
		sum /= 577;
		sum *= 937241;
		out = String.valueOf(sum);
		out = out.substring(0, out.indexOf('.')) + out.substring(out.indexOf('.') + 1);

		StringBuilder hashed = new StringBuilder();
		for (int i = 0; i < out.length(); i++) {
			if (i % 2 == 0) {
				hashed.insert(0, out.charAt(i));
			} else {
				hashed.insert(0, (char) (out.charAt(i) - '0' + 'a'));
			}
		}
		return hashed.toString();
	}
}
