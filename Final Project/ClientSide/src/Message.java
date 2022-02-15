/*
 * EE422C Final Project submission by
 * Anuj
 * aaj2447
 * 17805
 * Fall 2021
 * Slip days used: 2
 */

public class Message {
    String type;
    boolean loginSuccess;
    String username;
    String passHash;
    String input;
    String history;
    double currPrice;
    double bid;

    protected Message() {
        this.type = "";
        this.loginSuccess = false;
        this.username = "guest";
        this.passHash = "";
        this.input = "";
        this.currPrice= 0.0;
        this.bid = 0.0;
        this.history = "";
        System.out.println("client-side message created");
    }

    protected Message(String type, boolean login, String user, String pass, String input, double number, double bid) {
        this.type = type;
        this.loginSuccess = login;
        this.username = user;
        this.passHash = pass;
        this.input = input;
        this.currPrice = number;
        this.bid = bid;
        this.history = "";
        System.out.println("client-side message created");
    }
}