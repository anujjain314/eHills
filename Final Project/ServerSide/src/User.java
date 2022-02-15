/*
 * EE422C Final Project submission by
 * Anuj
 * aaj2447
 * 17805
 * Fall 2021
 * Slip days used: 2
 */

public class User {
    private final String username;
    private byte[] passHash;
    private byte[] salt;

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public byte[] getPassHash() { return this.passHash; }

    public byte[] getSalt() { return this.salt; }

    public void setPass(byte[] hash) { this.passHash = hash; }

    public void setSalt(byte[] salt) { this.salt = salt; }




}