package com.soham;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.StringJoiner;

import org.json.JSONObject;

/**
 * This class represents a simple Block.
 *
 * author soham chakraborti
 * andrewId: sohamc
 */
public class Block {

    // the default nonce value.
    private static final String DEFAULT_NONCE = "1";

    // the position of the block on the chain.
    private int index;

    //  it is an int that specifies the exact number of left most hex digits needed by a proper hash.
    private int difficulty;

    // a Java Timestamp object.
    private Timestamp timestamp;

    // a String holding the block's single transaction details.
    private String data;

    // the SHA256 hash of a block's parent.
    private String previousHash;

    // a BigInteger value determined by a proof of work routine.
    private BigInteger nonce;

    /**
     * SHA-256 Holder.
     */
    private static final String SHA_256 = "SHA-256";


    /**
     * Constructor.
     * @param index
     * @param timestamp
     * @param data
     * @param difficulty
     */
    Block(int index, Timestamp timestamp, String data, int difficulty) {
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.difficulty = difficulty;
        this.nonce = new BigInteger(DEFAULT_NONCE);
        this.previousHash = "0";
    }

    /**
     * Converts given hash to hex.
     * https://www.baeldung.com/sha-256-hashing-java
     *
     * @param hash to convert.
     * @return string - converted to hex format.
     */
    private static String convertToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * This method computes a hash of the concatenation of the index, timestamp,
     * data, previousHash, nonce, and difficulty.
     * @return String - hex format of SHA265 hash.
     */
    public String calculateHash() {
        StringJoiner strHashToCalculate = new StringJoiner("");
        // create the concatenated string, StringJoiner is efficient than '+'
        strHashToCalculate.add(Integer.toString(index))
                .add(timestamp.toString())
                .add(data)
                .add(previousHash)
                .add(nonce.toString())
                .add(Integer.toString(difficulty));

        String hexEncodedBytes = "";
        String concatString = strHashToCalculate.toString();
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] encodedHashData = digest.digest(
                    concatString.getBytes(StandardCharsets.UTF_8));
            // making sure it's positive
            // https://stackoverflow.com/questions/6357234/sha-hash-function-gives-a-negative-output
            // BigInteger bigInteger = new BigInteger(1, encodedHashData);
            // encode the bytes into hex format.
            hexEncodedBytes = convertToHex(encodedHashData);
        } catch (NoSuchAlgorithmException nsa) {
            System.out.println("No such algorithm exception thrown " + nsa);
        }
        return hexEncodedBytes;
    }


    /**
     * The proof of work methods finds a good hash. It increments the nonce until it produces a good hash.
     * stops when it meets the difficulty.
     * @return String - Hex SHA256 hash with correct difficulty level.
     */
    public String proofOfWork() {
        // Adds the correct amount of zeros to String depending on difficulty.
        String leadingHexZeros = "0".repeat(difficulty);
        // Loop executes till the hash has the same number of leading zeroes as difficulty.
        while (true) {
            String generatedHash = calculateHash();
            String leadingZerosInGeneratedHash = generatedHash.substring(0, difficulty);
            if (leadingHexZeros.equals(leadingZerosInGeneratedHash)) {
                // difficulty match found, great job !!
                return generatedHash;
            } else {
                // If difficulty is not matched.
                // Increase nonce by 1 and try again.
                nonce = nonce.add(new BigInteger("1"));
            }
        }
    }


    /**
     * getter for the nonce field.
     * @return
     */
    public BigInteger getNonce() {
        return nonce;
    }

    /**
     * getter for the difficulty field.
     * @return
     */
    public int getDifficulty() {
        return difficulty;
    }

    /**
     * setter for difficulty field.
     * @param difficulty
     */
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * getter for previous hash field.
     * @return
     */
    public String getPreviousHash() {
        return previousHash;
    }

    /**
     * setter for previous hash field.
     * @param previousHash
     */
    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    /**
     * getter for index field.
     * @return
     */
    public int getIndex() {
        return index;
    }

    /**
     * setter for index field.
     * @param index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * getter for timestamp field.
     * @return
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * setter for timestamp field.
     * @param timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * getter for data field.
     * @return
     */
    public String getData() {
        return data;
    }

    /**
     * set the data field.
     * @param data
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Returns a JSON string.
     * @return String - in JSON format.
     */
    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("index", index);
        json.put("time stamp", timestamp);
        json.put("data", data);
        json.put("previous hash", previousHash);
        json.put("nonce", nonce);
        json.put("difficulty", difficulty);
        return json.toString();
    }
}
