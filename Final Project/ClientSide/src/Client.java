/*
 * EE422C Final Project submission by
 * Anuj
 * aaj2447
 * 17805
 * Fall 2021
 * Slip days used: 2
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Signature;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.crypto.Cipher;

public class Client extends Application {

	private PrintWriter toServer = null;
	private BufferedReader fromServer = null;

	private Socket socket;
	private Thread receiveThread;

	public boolean loginSuccess;
	public boolean loginReceived;
	public String username;

	public ObservableList<AuctionItem> marketItems;
	public Property<ObservableList<AuctionItem>> marketProperty;

	public ObservableList<String> itemHistory;
	public Property<ObservableList<String>> itemHistoryProperty;

	public Client() {
		this.loginSuccess = false;
	}

	@Override
	public void start(Stage primaryStage) {
		Client client = new Client();
		try {
			client.connectToServer();
		} catch (Exception e) {
			System.err.println("error: client connection");
			e.printStackTrace();
		}
		ClientGUI window = new ClientGUI();

		window.showLogin(primaryStage, client);
	}

	// Connects client to server
	private void connectToServer() throws Exception {
		int port = 4243;

		socket = new Socket("10.147.251.198", port);
		fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		toServer = new PrintWriter(socket.getOutputStream());

		receiveThread = new Thread(() -> {
			String input;
			try {
				input = fromServer.readLine();
				while (input != null) {
					System.out.println("Received from server: " + input);
					processRequest(input);
					input = fromServer.readLine();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		receiveThread.start();
	}

	// Inspiration: https://www.tutorialspoint.com/java_cryptography/java_cryptography_encrypting_data.htm
	// Encrypt strings
	protected String encrypt(String s) throws Exception {
		//Creating a Signature object
		Signature sign = Signature.getInstance("SHA256withRSA");

		//Creating KeyPair generator object
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

		//Initializing the key pair generator
		keyPairGen.initialize(2048);

		//Generating the pair of keys
		KeyPair pair = keyPairGen.generateKeyPair();

		//Creating a Cipher object
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

		//Initializing a Cipher object
		cipher.init(Cipher.ENCRYPT_MODE, pair.getPublic());

		//Adding data to the cipher
		byte[] input = s.getBytes();
		cipher.update(input);

		//encrypting the data
		byte[] cipherText = cipher.doFinal();
		String encryptedString = new String(cipherText, StandardCharsets.UTF_8);
		System.out.println(encryptedString);
		return encryptedString;
	}

	// Source: https://www.tutorialspoint.com/java_cryptography/java_cryptography_decrypting_data.htm
	// Decrypt strings
	protected String decrypt(String s) throws Exception {
		//Creating a Signature object
		Signature sign = Signature.getInstance("SHA256withRSA");

		//Creating KeyPair generator object
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

		//Initializing the key pair generator
		keyPairGen.initialize(2048);

		//Generate the pair of keys
		KeyPair pair = keyPairGen.generateKeyPair();

		//Getting the public key from the key pair
		PublicKey publicKey = pair.getPublic();

		//Creating a Cipher object
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

		//Initializing a Cipher object
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);

		//Add data to the cipher
		byte[] input = s.getBytes();
		cipher.update(input);

		//encrypting the data
		byte[] cipherText = cipher.doFinal();
		System.out.println( new String(cipherText, StandardCharsets.UTF_8));

		//Initializing the same cipher for decryption
		cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());

		//Decrypting the text
		byte[] decipheredText = cipher.doFinal();
		String decryptedString = new String(decipheredText);
		System.out.println(decryptedString);
		return decryptedString;
	}


	// Process messages from server
	protected void processRequest(String input) {

		// Gson and message initialization
		Gson gson = new Gson();
		Message message = gson.fromJson(input, Message.class);
		System.out.println(message.type);

		try {
			switch(message.type) {

				// Verify login
				case "login":
					if(message.loginSuccess) {
						this.username = message.username;
						this.loginSuccess = true;
						this.loginReceived = true;

						sendToServer("{ type: 'items', username: '" + username + "'}");
					}
					else {
						this.loginSuccess = false;
						this.loginReceived = true;
					}
					break;

				// Take items from server and populate client with them
				case "items":

					// convert JSON to ArrayList of AuctionItem and fill in table
					List<AuctionItem> jsonList = new Gson().fromJson(message.input, new TypeToken<List<AuctionItem>>() {}.getType());
					marketItems = FXCollections.observableArrayList(jsonList);
					marketProperty = new SimpleObjectProperty<>(marketItems);
					for(AuctionItem auctionItem : marketItems) System.out.println(auctionItem);

					return;

				// Returns history of bids for an item
				case "history":
					// convert JSON to ArrayList of AuctionItem and fill in table
					List<String> historyJSONList = new Gson().fromJson(message.input, new TypeToken<List<String>>() {}.getType());
					itemHistory = FXCollections.observableArrayList(historyJSONList);
					itemHistoryProperty = new SimpleObjectProperty<>(itemHistory);
					for(String s : itemHistory) System.out.println(s);

					return;

				// An item has been bid on, update the item on client
				case "bid":
					System.out.println("Received a new bid: " + message.currPrice);

					for(AuctionItem item: marketItems) {
						if(item.getItemName().equals(message.input)) {
							int place = marketItems.indexOf(item);
							item.setMinPrice(message.currPrice);
							item.setHighestBidderName(message.username);
							if(item.getBuyNow() <=  item.getMinPrice()) item.setStatus("Sold!");
							else item.setStatus("New Bid!");
							marketItems.set(place, item);
						}
					}
					return;

				// An error has occurred
				case "error":
					System.out.println("Error: " + message.input);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	// Sends message to server
	protected void sendToServer(String string) {
		System.out.println("Sending to server: " + string);
		toServer.println(string);
		toServer.flush();
	}

	// Exits client (used for logout)
	@SuppressWarnings("deprecation")
	public void exitClient() throws IOException {
		receiveThread.stop();
		socket.close();
		System.exit(0);
	}

	public static void main(String[] args) {
		launch(args);
	}
}