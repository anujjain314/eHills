/*
 * EE422C Final Project submission by
 * Anuj
 * aaj2447
 * 17805
 * Fall 2021
 * Slip days used: 2
 */

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ClientGUI {

    private Stage stage;
    private Client client;

    public TextField usernameField;
    public TextField passwordField;
    private final Text loginError = new Text("");

    private static MediaPlayer mediaPlayer;

    public void showLogin(Stage stage, Client client) {
        this.client = client;
        this.stage = stage;

        Text userText = new Text("Username: ");
        Text passText = new Text("Password : ");
        Button loginBtn = new Button("Login");
        Button guestBtn = new Button("Login as Guest");
        Button createBtn = new Button("Create Account");
        usernameField = new TextField();
        passwordField = new TextField();

        // Log in as guest
        guestBtn.setOnAction(event ->{
            try {
                this.client.sendToServer("{ type: 'login', username: 'guest'}");
                this.client.loginReceived = false;

                // Waiting for sever to respond
                while(!this.client.loginReceived) System.out.print("waiting ");

                if(this.client.loginSuccess) showAuction();
                else {
                    loginError.setText("Guest account is down");
                    loginError();
                }
            } catch(Exception ex) {
                System.out.println("Something went wrong (guestBtn)");
                ex.printStackTrace();
            }
        });

        // Attempt to log in with username and password entered
        loginBtn.setOnAction(e-> {
            try {
                this.client.sendToServer("{ type: 'login', username: '" + usernameField.getText() + "', passHash: '" + passwordField.getText() + "' }");
                this.client.loginReceived = false;

                // Wait for server to respond
                while(!this.client.loginReceived) System.out.print("waiting ");

                if(this.client.loginSuccess) showAuction();
                else {
                    loginError.setText("Wrong username or password");
                    loginError();
                }
            } catch(Exception ex) {
                System.out.println("Login failure");
                ex.printStackTrace();
            }
        });

        // Create account with credentials inputted
        createBtn.setOnAction(e-> {
            try {
                client.sendToServer("{ type: 'createAccount', username: '" + usernameField.getText() + "', passHash: '" + passwordField.getText() + "' }");
                client.loginReceived = false;

                // Wait for server to respond
                while(!client.loginReceived) System.out.print("waiting ");

                if(client.loginSuccess) showAuction();
                else {
                    loginError.setText("Username exists already");
                    loginError();
                }

            } catch(Exception ex) {
                System.out.println("Account creation failure");
                ex.printStackTrace();
            }
        });

        // Login GridPane
        GridPane loginPane = new GridPane();
        loginPane.setBackground(new Background(new BackgroundFill(Color.CORAL, CornerRadii.EMPTY, Insets.EMPTY)));
        loginPane.setMinSize(500,200);
        loginPane.setAlignment(Pos.CENTER);
        loginPane.setVgap(10);
        loginPane.setHgap(10);;

        // Set button colors
        loginBtn.setBackground(new Background(new BackgroundFill(Color.TEAL, CornerRadii.EMPTY, Insets.EMPTY)));
        createBtn.setBackground(new Background(new BackgroundFill(Color.TEAL, CornerRadii.EMPTY, Insets.EMPTY)));
        guestBtn.setBackground(new Background(new BackgroundFill(Color.TEAL, CornerRadii.EMPTY, Insets.EMPTY)));


        // Login ButtonPane for loginBtn and createAccountBtn
        GridPane loginButtonPane = new GridPane();
        loginButtonPane.setHgap(10);

        // Place elements inside GUI
        loginPane.add(userText, 0, 0);
        loginPane.add(usernameField, 1, 0);
        loginPane.add(passText, 0, 1);
        loginPane.add(passwordField, 1, 1);
        loginButtonPane.add(loginBtn, 0, 0);
        loginPane.add(loginError, 1, 2);
        loginPane.add(guestBtn, 0, 3);
        loginButtonPane.add(createBtn, 1, 0);
        loginPane.add(loginButtonPane, 1, 3);

        // Create scene and stage it
        Scene scene = new Scene(loginPane);
        stage.setScene(scene);
        stage.setTitle("eHills Login");
        stage.show();
    }

    // Displays login error
    private void loginError() {
        loginError.setFill(Color.RED);
        loginError.setVisible(true);
    }

    // Plays audio files
    public void playMusic (String filepath) {
        try {
            Media audio = new Media(Paths.get(filepath).toUri().toString());
            mediaPlayer = new MediaPlayer(audio);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Search function to find items
    private boolean searchFindsItem(AuctionItem item, String searchText) {
        return (item.getItemName().toLowerCase().contains(searchText.toLowerCase()));
    }

    // Returns items given by searchFindsItem
    private Predicate<AuctionItem> createPredicate(String searchText) {
        return item -> {
            if (searchText == null || searchText.isEmpty()) return true;
            return searchFindsItem(item, searchText);
        };
    }

    // Shows the auction window
    @SuppressWarnings("rawtypes")
    public void showAuction() {

        // Creates the GridPane that will house all the elements
        GridPane background = new GridPane();
        background.setBackground(new Background(new BackgroundFill(Color.LIGHTSKYBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        background.setPadding(new Insets(10, 10, 10, 10));
        background.setVgap(5);
        background.setHgap(5);
        background.setAlignment(Pos.CENTER);

        // Setup table view
        TableView<AuctionItem> table = new TableView<>();
        table.setEditable(true);
        table.setMinSize(600, 300);

        // Sets up all the table columns
        TableColumn<AuctionItem, String> itemNameCol = new TableColumn<>("Item");
        itemNameCol.setCellValueFactory(
                new PropertyValueFactory<>("itemName"));

        TableColumn<AuctionItem, String> itemDescCol = new TableColumn<>("Description");
        itemDescCol.setCellValueFactory(
                new PropertyValueFactory<>("itemDescription"));
        itemDescCol.setMaxWidth(200);

        TableColumn<AuctionItem, String> itemPriceCol = new TableColumn<>("Price");
        itemPriceCol.setCellValueFactory(
                new PropertyValueFactory<>("minPrice"));

        TableColumn<AuctionItem, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status"));

        TableColumn<AuctionItem, String> highestBidderCol = new TableColumn<>("Highest Bidder");
        highestBidderCol.setCellValueFactory(
                new PropertyValueFactory<>("highestBidderName"));

        table.setItems(client.marketItems);
        table.getColumns().addAll(itemNameCol, itemDescCol, itemPriceCol, statusCol, highestBidderCol);

        // Setup List View
        ListView<String> list = new ListView<>();
        list.setItems(client.itemHistory);

        // Control Panel
        GridPane control = new GridPane();
        GridPane searchSpace = new GridPane();

        // Texts for selected item
        Text selectedItemText = new Text("No Items Selected");
        Text selectedItemPrice = new Text("NA");
        Text selectedItemDescription = new Text("N/A");
        Text itemHistory = new Text("Item Bid History from oldest to most recent");
        Text selectedItemHighestBidder = new Text("No bids");
        Text bidErrorText = new Text("");
        bidErrorText.setFill(Color.RED);

        Text bidVal = new Text("Bid Amount: ");
        TextField bidInput = new TextField();
        Button sendBid = new Button("Bid");

        Text itemName = new Text("Item Name: ");
        Text itemDescription = new Text("Item Description: ");
        Text itemPrice = new Text("Item Price: ");
        Text itemHighestBidder = new Text("Highest Bidder: ");

        Button logout = new Button("Log Out");

        Text search = new Text("Search: ");
        TextField searchBox = new TextField();
        Button searchBtn = new Button("Go");

        Button getHistoryBtn = new Button("Get History");

        // Place elements in GUI
        control.add(bidVal, 0, 0);
        control.add(bidInput, 1, 0);
        control.add(sendBid, 2, 0);

        control.add(itemName, 0, 2);
        control.add(itemDescription, 0, 3);
        control.add(itemPrice, 0, 4);
        control.add(itemHighestBidder, 0, 5);

        control.add(selectedItemText, 1, 2);
        control.add(selectedItemPrice, 1, 4);
        control.add(selectedItemDescription, 1, 3);
        control.add(selectedItemHighestBidder, 1, 5);

        control.add(bidErrorText, 1, 1);
        control.add(logout, 2, 6);

        searchSpace.add(search, 0, 0);
        searchSpace.add(searchBox, 1, 0);
        searchSpace.add(searchBtn, 2, 0);

        background.add(table, 0, 0);
        background.add(searchSpace, 0, 1);
        background.add(control, 1, 0);
        background.add(list, 2, 0);
        background.add(itemHistory, 2, 1);
        background.add(getHistoryBtn, 2, 2);
        
        // Play elevator music in background
        playMusic("elevator_music.wav");

        // Button to close the application
        logout.setOnAction(event -> {
            try {
                client.exitClient();
                Thread.sleep(500);
                System.exit(0);
            } catch (Exception ex) {
                System.err.println("Logout button failure!");
                ex.printStackTrace();
            }
        });

        sendBid.setOnAction(event -> {
            try {
                // Check if item has been sold
                for (AuctionItem item : client.marketItems) {
                    if (selectedItemText.getText().equals(item.getItemName())) {

                        // Send error if item has been sold
                        if (item.getStatus().equals("Sold!")) bidErrorText.setText("Error: this item has been sold!");

                        // Check if bid is valid (high enough)
                        else if (Double.parseDouble(bidInput.getText()) <= item.getMinPrice()) {
                            bidErrorText.setText("Error: bid must be greater than current price");
                        } 
                        else bidErrorText.setText("");
                    }
                }
                client.sendToServer("{ type: 'bid', username: '" + client.username + "', input: '" + selectedItemText.getText() + "', bid: '" + bidInput.getText() + "' }");
                client.sendToServer("{ type: 'history', input: '" + selectedItemText.getText() + "'}");
                bidInput.clear();
            } catch (Exception ex) {
                System.out.println("Error sending bid value");
                ex.printStackTrace();
            }
        });

        //  Create new filtered list
        FilteredList<AuctionItem> filteredData = new FilteredList<>(FXCollections.observableList(client.marketItems));

        // Filter results on click of search button
        searchBtn.setOnAction(event -> {
            filteredData.setPredicate(createPredicate(searchBox.getText()));
            table.setItems(filteredData);
        });


        // Actions when an item in table is selected
        table.getSelectionModel().selectedItemProperty().addListener((ChangeListener<Object>) (observableValue, oldValue, newValue) -> {
            //Check whether item is selected and set value of selected item to Label
            if (table.getSelectionModel().getSelectedItem() != null) {
                TableViewSelectionModel selectionModel = table.getSelectionModel();
                AuctionItem selectedItem = (AuctionItem) selectionModel.getSelectedItem();

                selectedItemText.setText(selectedItem.getItemName());
                selectedItemPrice.setText("$" + String.format("%.2f", selectedItem.getMinPrice()));
                selectedItemDescription.setText(selectedItem.getItemDescription());
                selectedItemHighestBidder.setText(selectedItem.getHighestBidderName());

                getHistoryBtn.setOnAction(event -> {
                    client.sendToServer("{ type: 'history', input: '" + selectedItem.getItemName() + "'}");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    list.setItems(client.itemHistory);
                });

                System.out.println("Updated " + selectedItem.getItemName());
            }
        });
        
        // Create a scene and place it in the stage
        Scene market = new Scene(background, 1400, 500);
        stage.setTitle("eHills Auction");
        stage.setScene(market);
        stage.show();
    }


}