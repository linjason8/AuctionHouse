/*
 * EE422C Final Project submission by
 * Jason Lin
 * jjl3777
 * 17625
 * Slip day used: 1
 * Fall 2022
 */

package client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.bson.Document;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Controller2 {

	public static Client client;

	@FXML
	private Pane bidPane, rightPane, homePane, fundsPane, acctPane, successPane, listingPane;

	@FXML
	private Text displayUser, name, status, status2, bid, buyNow, error, closed, passError, passSuc, balance, balance1,
			cardError, newBal, listingError;

	@FXML
	private TextField bidAmt, search, cardNo, exp, cvc, depo, itemName, itemPrice, itemBN, itemEnd, itemImg;

	@FXML
	private PasswordField oldPass, newPass1, newPass2;

	@FXML
	private TableView<Item> table;

	@FXML
	private ListView<String> hist;

	@FXML
	private TableColumn<Item, String> nameCol, statusCol, descCol, bidderCol, bidCol, buyCol, endCol;

	@FXML
	private CheckBox save;

	@FXML
	private TextArea notifs, desc, itemDesc;

	@FXML
	private Rectangle userRect;

	@FXML
	private ImageView image;

	@FXML
	private Hyperlink sign, quit, fund, acct;

	private ArrayList<Item> items = new ArrayList<>();
	private ObservableList<Item> itemsO;

	private boolean open = false;
	private boolean hide = false;
	private int currentItem = -1;
	private int logInd = 1;

	@FXML
	public void initialize() {
		try {
			while (client.items.size() < 6)
				Thread.sleep(50);
		} catch (Exception e) {
		}

		items = client.items;

		userRect.setVisible(false);
		sign.setVisible(false);
		quit.setVisible(false);
		fund.setVisible(false);
		acct.setVisible(false);
		status.setVisible(false);
		status2.setVisible(false);
		bidPane.setVisible(false);
		error.setVisible(false);
		closed.setVisible(false);
		cardError.setVisible(false);
		listingError.setVisible(false);
		
		desc.setEditable(false);

		fundsPane.setVisible(false);
		acctPane.setVisible(false);
		successPane.setVisible(false);
		listingPane.setVisible(false);

		rightPane.setPickOnBounds(false);

		displayUser.setText(client.getUser());
		updateBal();

		itemsO = FXCollections.observableArrayList();
		for (int i = 0; i < items.size(); i++) {
			itemsO.add(items.get(i));
		}

		// https://docs.oracle.com/javafx/2/ui_controls/table-view.htm
		nameCol.setCellValueFactory(new PropertyValueFactory<Item, String>("name"));
		statusCol.setCellValueFactory(new PropertyValueFactory<Item, String>("status"));
		descCol.setCellValueFactory(new PropertyValueFactory<Item, String>("desc"));
		bidderCol.setCellValueFactory(new PropertyValueFactory<Item, String>("bidder"));
		bidCol.setCellValueFactory(new PropertyValueFactory<Item, String>("bid"));
		buyCol.setCellValueFactory(new PropertyValueFactory<Item, String>("buyNow"));
		endCol.setCellValueFactory(new PropertyValueFactory<Item, String>("time"));
		table.setItems(itemsO);
		table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (items.indexOf(newSelection) != -1) {
				displayItem(items.indexOf(newSelection));
			}
		});

		notifs.setEditable(false);
		notifs.setText(client.notifsLog.get(0));
		for (int i = 1; i < client.notifsLog.size(); i++) {
			updateChat();
		}
	}

	private ObservableList<String> getHist() {
		ObservableList<String> itemsO = FXCollections.observableArrayList();
		for (int i = 0; i < items.get(currentItem).history.size(); i++) {
			itemsO.add(items.get(currentItem).history.get(i));
		}
		return itemsO;
	}

	@FXML
	void placeBid() {
		error.setVisible(false);
		String in = bidAmt.getText().trim();
		double _balance = client.balance;
		if (items.get(currentItem).getBidder().equals(client.getUser())) {
			_balance += items.get(currentItem).getBidD();
		}

		try {
			double bid = Double.parseDouble(in);
			if (in.contains(".") && in.substring(in.indexOf(".")).length() > 3) {
				error.setText("Enter a valid $ value");
				error.setVisible(true);
			} else if (bid > _balance) {
				error.setText("Insufficient funds!");
				error.setVisible(true);
			} else {
				client.sendToServer("BID~" + items.get(currentItem).getName() + "~" + String.format("%.2f", bid) + "~"
						+ client.getUser());
			}

		} catch (Exception e) {
			error.setText("Enter a valid $ value");
			error.setVisible(true);
		}
	}

	@FXML
	void usermenu() {
		open = !open;
		userRect.setVisible(open);
		sign.setVisible(open);
		quit.setVisible(open);
		fund.setVisible(open);
		acct.setVisible(open);
	}

	@FXML
	void addBtn() {
		cardError.setVisible(false);

		try {
			Integer i = Integer.parseInt(cardNo.getText().substring(0, 6));
			i = Integer.parseInt(cardNo.getText().substring(6));
			if (cardNo.getText().length() != 12)
				throw new Exception();
			if (cardNo.getText().contains("-"))
				throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
			cardError.setText("Invalid card number! Should be 12 digits");
			cardError.setVisible(true);
			return;
		}

		try {
			if (exp.getText().length() != 5)
				throw new Exception();
			Integer m = Integer.parseInt(exp.getText().substring(0, 2));
			if (m < 1 || m > 12)
				throw new Exception();
			if (exp.getText().charAt(2) != '/')
				throw new Exception();
			Integer y = Integer.parseInt(exp.getText().substring(3)) + 2000;
			if (y < LocalDateTime.now().getYear()
					|| (y == LocalDateTime.now().getYear() && m < LocalDateTime.now().getMonth().getValue())) {
				cardError.setText("Card expired");
				cardError.setVisible(true);
				return;
			}
		} catch (Exception e) {
			cardError.setText("Invalid date! Should be in MM/YY format");
			cardError.setVisible(true);
			return;
		}

		try {
			Integer i = Integer.parseInt(cvc.getText());
			if (cvc.getText().length() != 3)
				throw new Exception();
		} catch (Exception e) {
			cardError.setText("Invalid security code! Should be 3 digits");
			cardError.setVisible(true);
			return;
		}

		String in = depo.getText();
		try {
			double deposit = Double.parseDouble(in);
			if (in.contains(".") && in.substring(in.indexOf(".")).length() > 3)
				throw new Exception();
			else if (deposit < 0) {
				throw new Exception();
			} else {
				client.sendToServer("ADD_BAL~" + client.getUser() + "~" + String.format("%.2f", deposit));
				client.balance += deposit;
				updateBal();
			}
		} catch (Exception e) {
			cardError.setText("Enter a valid $ value");
			cardError.setVisible(true);
			return;
		}

		if (!save.isSelected()) {
			cardNo.setText("");
			cvc.setText("");
			exp.setText("");
		}

		depo.setText("");

		newBal.setText("New Balance: $" + String.format("%,.2f", client.balance));

		fundsPane.setVisible(false);
		successPane.setVisible(true);
	}

	@FXML
	void addFunds() {
		fundsPane.setVisible(true);
		acctPane.setVisible(false);
		homePane.setVisible(false);
		successPane.setVisible(false);
		listingPane.setVisible(false);
	}

	@FXML
	void openSettings() {
		acctPane.setVisible(true);
		fundsPane.setVisible(false);
		homePane.setVisible(false);
		successPane.setVisible(false);
		listingPane.setVisible(false);

		oldPass.setText("");
		newPass1.setText("");
		newPass2.setText("");
		passError.setVisible(false);
		passSuc.setVisible(false);
	}

	@FXML
	void goHome() {
		homePane.setVisible(true);
		acctPane.setVisible(false);
		fundsPane.setVisible(false);
		successPane.setVisible(false);
		listingPane.setVisible(false);
	}

	@FXML
	void exit() {
		Client.stage.close();
	}

	@FXML
	void reset() {
		table.setItems(itemsO);
		search.setText("");
	}

	@FXML
	void signout() {
		client.login();
	}

	@FXML
	void updateTable() {
		table.setItems(itemsO.filtered(i -> i.getName().toLowerCase().contains(search.getText().toLowerCase())));
	}

	@FXML
	void toggleHide() {
		if (!hide) {
			table.setItems(itemsO
					.filtered(i -> i.getName().toLowerCase().contains(search.getText().toLowerCase()) && i.isOpen()));
			hide = true;
		} else {
			updateTable();
			hide = false;
		}
	}

	@FXML
	void updatePass() {
		passError.setVisible(false);
		passSuc.setVisible(false);
		String oldPassH = Client.passwordHash(oldPass.getText());
		if (!oldPassH.equals(client.getPassword())) {
			passError.setText("Incorrect old password");
			passError.setVisible(true);
		} else if (!newPass1.getText().equals(newPass2.getText())) {
			passError.setText("New passwords must match");
			passError.setVisible(true);
		} else {
			String newPass = Client.passwordHash(newPass1.getText());
			client.sendToServer("UPDATE_PASS~" + client.getUser() + "~" + newPass);
			client.setPassword(newPass);
			passSuc.setVisible(true);
		}
	}

	@FXML
	void newListing() {
		listingPane.setVisible(true);
		homePane.setVisible(false);
		acctPane.setVisible(false);
		fundsPane.setVisible(false);
		successPane.setVisible(false);
	}

	@FXML
	void createListing() {
		listingError.setVisible(false);

		if (itemName.getText().trim().equals("")) {
			listingError.setText("Enter a Name");
			listingError.setVisible(true);
			return;
		}

		try {
			String in = itemPrice.getText();
			double price = Double.parseDouble(in);
			if (in.contains(".") && in.substring(in.indexOf(".")).length() > 3)
				throw new Exception();
			else if (price < 0) {
				throw new Exception();
			}

			String in2 = itemBN.getText();
			double bn = Double.parseDouble(in2);
			if (in2.contains(".") && in2.substring(in2.indexOf(".")).length() > 3)
				throw new Exception();
			else if (bn < 0) {
				throw new Exception();
			} else if (bn <= price) {
				listingError.setText("Buy Now price must be higher than starting price");
				listingError.setVisible(true);
				return;
			}
		} catch (Exception e) {
			listingError.setText("Enter a valid $ value");
			listingError.setVisible(true);
			return;
		}

		try {
			DateTimeFormatter format = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
			LocalDateTime dateTime = LocalDateTime.parse(itemEnd.getText(), format);
		} catch (Exception e) {
			listingError.setText("Enter a valid date and time in the given format");
			listingError.setVisible(true);
			return;
		}

		String time = itemEnd.getText().substring(6, 10) + "-" + itemEnd.getText().substring(0, 5) + "T"
				+ itemEnd.getText().substring(11) + ":00";
		error.setVisible(false);
		Document doc = new Document().append("Name", itemName.getText());
		doc.append("Bid", itemPrice.getText());
		doc.append("Bidder", "Starting Price");
		doc.append("Time", time);
		doc.append("Description", itemDesc.getText());
		doc.append("Image", itemImg.getText());
		doc.append("Buyout", itemBN.getText());
		doc.append("History", "Bid starts at $" + String.format("%,.2f", Double.parseDouble(itemPrice.getText())));

		client.sendToServer("NEWITEM~" + doc.toJson() + "~" + client.getUser());
	}

	@FXML
	void cancelListing() {
		itemName.setText("");
		itemDesc.setText("");
		itemBN.setText("");
		itemPrice.setText("");
		itemEnd.setText("");
		itemImg.setText("");
		goHome();
	}

	private void updateBal() {
		balance.setText("Balance: $" + String.format("%,.2f", client.balance));
		balance1.setText("Current Balance: $" + String.format("%,.2f", client.balance));
	}

	private void displayItem(int itemNum) {
		currentItem = itemNum;
		name.setText(items.get(itemNum).getName());
		desc.setText("Item Description:\n\n" + items.get(itemNum).getDesc());

		bid.setText("Current Bid: $" + items.get(itemNum).getBid() + " (" + items.get(itemNum).getBidder() + ")");
		buyNow.setText("\"Buy Now\" Price: $" + items.get(itemNum).getBuyNow());
		statusUpdate(itemNum);

		new Thread(new Runnable() {
			public void run() {
				image.setImage(items.get(itemNum).getImg());
			}
		}).start();

		Platform.runLater(new Runnable() {
			public void run() {
				hist.setItems(getHist());
			}
		});
		hist.scrollTo(items.get(itemNum).history.size() - 1);
	}

	public void addedItem() {
		itemsO.add(items.get(items.size() - 1));
		table.refresh();
		
		updateChat();
		
		newBal.setText("Successfully listed item");
		listingPane.setVisible(false);
		homePane.setVisible(false);
		acctPane.setVisible(false);
		fundsPane.setVisible(false);
		successPane.setVisible(true);
	}

	public void update(String[] in) {
		updateChat();

		int index = -1;
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getName().equals(in[1])) {
				index = i;
				break;
			}
		}

		items.get(index).history.add(in[3] + " has bid $" + String.format("%,.2f", Double.parseDouble(in[2])));
		hist.refresh();
		hist.scrollTo(items.get(index).history.size() - 1);

		if (items.get(index).getBidder().equals(client.getUser())) {
			client.balance += items.get(index).getBidD();
		}
		items.get(index).setBid(Double.parseDouble(in[2]));
		items.get(index).setBidder(in[3]);

		if (in[3].equals(client.getUser())) {
			client.balance -= items.get(index).getBidD();
		}

		updateBal();
		if (currentItem == index)
			displayItem(index);

		items.get(index).update();
		statusUpdate(index);
		table.refresh();
	}

	public void updateChat() {
		notifs.appendText("\n" + client.notifsLog.get(logInd++));
		notifs.setScrollTop(Double.MAX_VALUE);
	}

	public void statusUpdate(int itemNum) {
		boolean isOpen = items.get(itemNum).isOpen();
		if (isOpen) {
			status2.setVisible(false);
			status.setVisible(true);
		} else {
			status.setVisible(false);
			status2.setVisible(true);
		}

		if (isOpen) {
			bidPane.setVisible(true);
			closed.setVisible(false);
		} else {
			bidPane.setVisible(false);
			if (items.get(itemNum).isSold()) {
				closed.setText("Auction Closed! Sold to " + items.get(itemNum).getBidder() + " for $"
						+ items.get(itemNum).getBid());
			} else {
				closed.setText("Auction Closed! Time Expired");
			}
			closed.setVisible(true);
		}
	}

	public void invalid() {
		error.setText("Bid too low!");
		error.setVisible(true);
	}
	
	public void duplicate() {
		listingError.setText("Cannot list a duplicate item!");
		listingError.setVisible(true);
	}
}
