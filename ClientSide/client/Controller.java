/*
 * EE422C Final Project submission by
 * Jason Lin
 * jjl3777
 * 17625
 * Slip day used: 1
 * Fall 2022
 */

package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

public class Controller {

	public static Client client;

	@FXML
	private BorderPane pane;

	@FXML
	private PasswordField password;

	@FXML
	private TextField user;

	@FXML
	private Text error;
	
	@FXML
	void login(ActionEvent event) {
		error.setVisible(false);
		if (user.getText().contains(" ") || password.getText().contains(" ") || user.getText().contains("~") || password.getText().contains("~")) {
			error.setText("Incorrect username and/or password");
			error.setVisible(true);
		} else if (user.getText().equals("") || password.getText().equals("")) {
			error.setText("Fill out both fields");
			error.setVisible(true);
		} else {
			
			client.sendToServer("LOGIN~" + user.getText().trim() + "~" + Client.passwordHash(password.getText()));
		}
			
		
	}

	@FXML
	void signup(ActionEvent event) {
		client.signup();
	}

	@FXML
	void createAcct(ActionEvent event) {
		error.setVisible(false);
		if (user.getText().contains(" ") || password.getText().contains(" ")) {
			error.setText("No spaces allowed");
			error.setVisible(true);
		} else if (user.getText().contains("~") || password.getText().contains("~")) {
			error.setText("'~' not allowed");
			error.setVisible(true);
		} else if (user.getText().equals("") || password.getText().equals("")) {
			error.setText("Fill out both fields");
			error.setVisible(true);
		} else 
			client.sendToServer("NEW_USER~" + user.getText().trim() + "~" + Client.passwordHash(password.getText()));
	}

	@FXML
	void backLog(ActionEvent event) {
		client.login();
	}

	@FXML
	void guest(ActionEvent event) {
		client.setUser("NEW_GUEST");
	}

	@FXML
	void exit(ActionEvent event) {
		Client.stage.close();
	}
	
	public void invalidUser() {
		error.setText("Username already taken");
		error.setVisible(true);
	}
	
	public void logFail() {
		error.setText("Incorrect username and/or password");
		error.setVisible(true);
	}
}
