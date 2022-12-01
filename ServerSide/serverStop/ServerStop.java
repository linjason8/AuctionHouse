/*
 * EE422C Final Project submission by
 * Jason Lin
 * jjl3777
 * 17625
 * Slip day used: 1
 * Fall 2022
 */

package serverStop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerStop {
	
	private static String host = "10.165.169.212"; // apt
//	private static String host = "10.147.40.218"; // utexas
	private static Socket socket;
	
	public static void main(String[] args) {
		try {
			socket = new Socket(host, 4242);
			PrintWriter toServer = new PrintWriter(socket.getOutputStream());
			toServer.println("CLOSE_SERVER");
			toServer.flush();
			Thread.sleep(110);
			System.out.println("Server Stopped");
		} catch (Exception e) {
			System.out.println("Already Closed");
		}
	}
}
