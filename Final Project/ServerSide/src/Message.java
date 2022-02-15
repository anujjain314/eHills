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
    String username;
    String passHash;
    String input;
    String history;
    double currPrice;
    double bid;

    protected Message() {
        this.type = "";
        this.username = "guest";
        this.passHash = "";
        this.input = "";
        this.currPrice = 0.0;
        this.bid = 0.0;
        this.history = "";
        System.out.println("server-side message created");
    }

    protected Message(String type, String user, String pass, String input, double number, double bid) {
        this.type = type;
        this.username = user;
        this.passHash = pass;
        this.input = input;
        this.currPrice = number;
        this.bid = bid;
        this.history = "";
        System.out.println("server-side message created");
    }
}