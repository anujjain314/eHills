/*
 * EE422C Final Project submission by
 * Anuj
 * aaj2447
 * 17805
 * Fall 2021
 * Slip days used: 2
 */

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.util.*;


public class Server extends Observable {

    static Server server;

    private Integer clientNum = 0; //number of clients
    private List<AuctionItem> auctionList = new ArrayList<>();
    private List<User> userList = new ArrayList<>();


    public static void main(String[] args) throws Exception {
        server = new Server();
        server.populateItems();
        server.SetupNetworking();
    }

    // Adds items to auctionList and userList from JSON files
    private void populateItems() {
        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get("items.json"));
            auctionList = gson.fromJson(reader, new TypeToken<List<AuctionItem>>() {}.getType());
            auctionList.forEach(System.out::println);
            reader = Files.newBufferedReader(Paths.get("users.json"));
            userList = gson.fromJson(reader, new TypeToken<List<User>>() {}.getType());
            userList.forEach(System.out::println);
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Sets up networking
    private void SetupNetworking() {
        int port = 4243;
        try {
            ServerSocket ss = new ServerSocket(port);
            while (true) {
                Socket clientSocket = ss.accept();
                System.out.println("Client #" + clientNum + " is connecting to the server by " + clientSocket);

                ClientHandler handler = new ClientHandler(this, clientSocket, clientNum);
                this.addObserver(handler);
                clientNum++;

                Thread t = new Thread(handler);
                t.start();
                System.out.println("got a connection");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inspiration: https://www.javainterviewpoint.com/java-salted-password-hashing/
    // Salts and hashes passwords
    private void saltAndHash(User user, String password) {
        MessageDigest md;
        try
        {
            // Select the message digest for the hash computation -> SHA-256
            md = MessageDigest.getInstance("SHA-256");

            // Generate the random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            // Passing the salt to the digest for the computation
            md.update(salt);

            // Generate the salted hash
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword)
                sb.append(String.format("%02x", b));

            user.setPass(hashedPassword);
            user.setSalt(salt);

            System.out.println(sb);
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }

    // Inspiration: https://www.javainterviewpoint.com/java-salted-password-hashing/
    // Checks if an inputted password is correct for a given user
    private boolean checkPassword(User user, String password) {
        byte[] salt = user.getSalt();
        byte[] hash = user.getPassHash();

        MessageDigest md;
        try
        {
            // Select the message digest for the hash computation -> SHA-256
            md = MessageDigest.getInstance("SHA-256");

            // Passing the salt to the digest for the computation
            md.update(salt);

            // Generate the salted hash
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword)
                sb.append(String.format("%02x", b));

            System.out.println(sb);

            return Arrays.equals(hash, hashedPassword);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    // Source: https://www.tutorialspoint.com/java_cryptography/java_cryptography_encrypting_data.htm
    // Encrypts strings
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
    // Decrypts strings
    protected String decrypt(String s) throws Exception {
        //Creating a Signature object
        Signature sign = Signature.getInstance("SHA256withRSA");

        //Creating KeyPair generator object
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

        //Initializing the key pair generator
        keyPairGen.initialize(4096);

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
        byte[] decipheredText = cipher.doFinal(cipherText);
        String decryptedString = new String(decipheredText);
        System.out.println(decryptedString);
        return decryptedString;
    }

    // Process messages from client
    protected synchronized String processRequest(String input) {
        Gson gson = new Gson();
        Message message;
        boolean loginSuccess = false;

        try {
            message = gson.fromJson(input, Message.class);
        } catch (Exception e) {
            return ("{ type: 'error', input: 'invalid message type' }");
        }

        try {
            switch (message.type) {

                // Client is attempting to create account
                case "createAccount":
                    try {
                        for (User user : userList) {
                            if (user.getUsername().equals(message.username)) {
                                return "{ type: 'login', loginSuccess: false, input: 'username exists }";
                            }
                        }
                        User newUser = new User(message.username);
                        saltAndHash(newUser, message.passHash);
                        userList.add(newUser);
                        return "{ type: 'login', loginSuccess: true, username: '" + message.username + "'}";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                // Client is attempting a login
                case "login":
                    try {
                        for (User user : userList) {
                            if (message.username.equals("guest")) {
                                loginSuccess = true;
                                break;
                            }
                            if (user.getUsername().equals(message.username)) {
                                if (checkPassword(user, message.passHash)) {
                                    loginSuccess = true;
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (loginSuccess) return "{ type: 'login', loginSuccess: true, username: '" + message.username + "'}";
                    else return "{ type: 'login', loginSuccess: false, username: '" + message.username + "'}";

                // Client is requesting list of all item for auction
                case "items":
                    String itemsToJSON = new Gson().toJson(auctionList);
                    return  "{ type: 'items', input: '" + itemsToJSON + "'}";

                // Client is requesting to see their bid/purchase history
                case "history":
                    AuctionItem itemHistory = null;
                    for (AuctionItem ai : auctionList) {
                        if (ai.getItemName().equals(message.input)) {
                            itemHistory = ai;
                            break;
                        }
                    }
                    String itemHistoryToJSON = new Gson().toJson(itemHistory.getHistory());
                    return "{ type: 'history', input: '" + itemHistoryToJSON + "'}";

                // Client is attempting to place bid on item
                case "bid":

                    // Check if bid is valid
                    for (AuctionItem item : auctionList) {
                        if (item.getItemName().equals(message.input)) {
                            // Check if item is for sale
                            if (item.getStatus().equals("Sold!")) {
                                item.setHighestBidderName(message.username);
                                return "{ type: 'error', input: 'Item has been sold' }";
                            }
                            // Check if bid is high enough
                            if (item.getMinPrice() <= message.bid) {
                                item.setMinPrice(message.bid);
                                item.setHighestBidderName(message.username);
                                if (item.getMinPrice() >= item.getBuyNow()) {
                                    item.setStatus("Sold!");
                                    this.setChanged();
                                }
                                item.getHistory().add(message.username + ": $" + message.bid);
                                String newBid = "{ type: 'bid', input: '" + item.getItemName() + "', currPrice: '" +
                                        String.format("%.2f", item.getMinPrice()) + "', username:  '" + item.getHighestBidderName() + "' }";
                                this.setChanged();
                                this.notifyObservers(newBid);
                                return newBid;
                            }
                            else {
                                return "{ type: 'error', input: 'Bid needs to be greater than current price' }";
                            }
                        }
                    }
                    return "{ type: 'error', input: 'Invalid bid value' }";
            }
            return "No cases matched, something went wrong";
        } catch(Exception e) {
            e.printStackTrace();
            return "Server error";
        }
    }
}
