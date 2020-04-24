package com.soham;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringJoiner;

/**
 * This class represents a simple BlockChain.
 *
 * author soham chakraborti
 * andrewId: sohamc
 */
public class BlockChain {

    /**
     * SHA-256 Holder.
     */
    private static final String SHA_256 = "SHA-256";


    //  an ArrayList to hold Blocks
    private ArrayList<Block> blockChainList;

    // hash of the most recently added Block.
    private String hashValueOfLastBlock;

    /**
     * Constructor.
     */
    public BlockChain() {
        blockChainList = new ArrayList<>();
        hashValueOfLastBlock = "";
    }

    /**
     * Returns the current system time as a TimeStamp.
     * @return TimeStamp - Current system time.
     */
    public Timestamp getTime() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * Returns a reference to the most recently added Block.
     * @return a Block object that is the latest block in the Blockchain.
     */
    public Block getLatestBlock() {
      try {
          return blockChainList.get(blockChainList.size() - 1);
      } catch (Exception e) {
          throw new NullPointerException("Block chain is Empty !!");
      }
    }

    /**
     * Returns the size of the blockchain.
     * @return int size of the arraylist.
     */
    public int getChainSize() {
        return blockChainList.size();
    }

    /**
     * Measures hashes per second of the computer holding this chain.
     * It uses a simple string - "00000000" to hash.
     * @return int hashPerSec - amount of times the loop ran.
     */
    public int hashesPerSecond() {
        String stringToHash = "00000000";
        int hashPerSec = 0;
        Instant currTime = Instant.now();
        Instant endTime = currTime.plusSeconds(1); // within 1 sec
        // check till current time is less than endTime
        while ( currTime.compareTo(endTime) != 1) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance(SHA_256);
                byte[] hash = messageDigest.digest(stringToHash.getBytes(StandardCharsets.UTF_8));
                hashPerSec++;
                currTime = Instant.now();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return hashPerSec;
    }

    /**
     * Adds a block to the chain, sets previous hash to hashValueOfLastBlock,
     * and sets the new hashValueOfLastBlock to hash of most recent block.
     * @param block
     */
    public void addBlock(Block block) {
        // add the block to the list
        blockChainList.add(block);
        // set the blocks prev hash value to the prev last hash value
        block.setPreviousHash(hashValueOfLastBlock);
        // set the new hash value of last block
        hashValueOfLastBlock = block.proofOfWork();;
    }

    /**
     * Method to get a string representation of the blockchain.
     * @return a String representation of the entire chain is returned.
     */
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject()
                        .put("ds_chain", blockChainList)
                        .put("chainHash", hashValueOfLastBlock);
        return jsonObject.toString();
    }

    private boolean checkIfGenesisBlockValid() {
        Block genesis = blockChainList.get(0);
        String genesisHash = hashValueOfLastBlock;
        String computedGenesisHash = genesis.proofOfWork();
        // this is the number of leading zeros expected.
        String leadingHexZeros = "0".repeat(genesis.getDifficulty());
        // computed hash and extract the leading zeros.
        String calculatedLeadingHexZeros = computedGenesisHash.substring(0, genesis.getDifficulty());
        if (calculatedLeadingHexZeros.equals(leadingHexZeros)
                && genesisHash.equals(computedGenesisHash)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Verifies whether the the hashes of each block are correct.
     * @return boolean - false if the hashes are not correct - true if hashes are correct.
     */
    public boolean isChainValid() {
        if (blockChainList.size() == 1) {
            // Block chain has only the genesis block.
            checkIfGenesisBlockValid();
        } else {
            // If block chain has more blocks.
            for (int i = 0; i < blockChainList.size(); i++) {
                Block currentBlock = blockChainList.get(i);
                // expected number of zero
                String leadingHexZerosExpected = "0".repeat(currentBlock.getDifficulty());
                // current block hash
                String currentBlockHash = currentBlock.proofOfWork();
                // current block leading zero
                String currentBlockLeadingZeros = currentBlockHash.substring(0, currentBlock.getDifficulty());
                if (i == blockChainList.size() - 1) {
                    // if it's the last block, check the hash with the hashValueOfLastBlock
                    if (!currentBlockHash.equals(hashValueOfLastBlock)) {
                        return false;
                    }
                } else {
                    // if not the last block match with next block's prev hash and also check the leading zero's
                    Block nextBlock = blockChainList.get(i + 1);
                    String nextBlockPrevHash = nextBlock.getPreviousHash();
                    if (!currentBlockHash.equals(nextBlockPrevHash)) {
                        if (currentBlockLeadingZeros.equals(leadingHexZerosExpected))
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * This routine repairs the chain. It checks the hashes
     * of each block and ensures that any illegal hashes are recomputed.
     * After this routine is run, the chain will be valid.
     */
    public void repairChain() {
        for (int i = 0; i < blockChainList.size(); i++) {
            // for the last block.
            if (i == blockChainList.size() - 1) {
                String prevHash = blockChainList.get(i-1).proofOfWork();
                blockChainList.get(i).setPreviousHash(prevHash);
                hashValueOfLastBlock = blockChainList.get(i).proofOfWork();
            } else {
                // for each block the previous hash value should be set.
                String prevHash = blockChainList.get(i).proofOfWork();
                blockChainList.get(i + 1).setPreviousHash(prevHash);
            }
        }
    }

    /**
     * Menu driven user inputs are taken.
     * @return user input as a string.
     */
    private static String getUserInput() {
        System.out.println("0. View basic blockchain status.");
        System.out.println("1. Add a transaction to the blockchain.");
        System.out.println("2. Verify the blockchain.");
        System.out.println("3. View the blockchain.");
        System.out.println("4. Corrupt the chain.");
        System.out.println("5. Hide the corruption by recomputing hashes.");
        System.out.println("6. Exit");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String operation = bufferedReader.readLine();
            return operation;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "None";
    }

    /**
     * Option zero chosen by user.
     * @param currentBlockChain the current block chain.
     */
    private static void optionZero(BlockChain currentBlockChain) {
        System.out.println("Current chain size: " + currentBlockChain.getChainSize());
        System.out.println("Current hashes per second by this machine: " + currentBlockChain.hashesPerSecond());
        System.out.println("Difficulty of most recent block: " + currentBlockChain.getLatestBlock().getDifficulty());
        System.out.println("Nonce for most recent block: " + currentBlockChain.getLatestBlock().getNonce());
        System.out.println("Chain hash: " + currentBlockChain.hashValueOfLastBlock);
    }

    /**
     * Option one chosen by user.
     * @param currentBlockChain the current block chain.
     */
    private static void optionOne(BlockChain currentBlockChain) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter difficulty > 0");
        String difficulty =  bufferedReader.readLine();
        System.out.println("Enter transaction: ");
        String data =  bufferedReader.readLine();
        long startTime = System.currentTimeMillis();
        Block newBlock = new Block(currentBlockChain.getChainSize(), currentBlockChain.getTime(), data, Integer.parseInt(difficulty));
        currentBlockChain.addBlock(newBlock);
        long endTime = System.currentTimeMillis();
        System.out.println("Total time to add new block was " + (endTime - startTime) + " milliseconds.");
    }

    /**
     * Option Two chosen by user.
     * @param currentBlockChain the current block chain.
     */
    private static void optionTwo(BlockChain currentBlockChain) {
        System.out.println("Verifying entire chain...");
        long startTime = System.currentTimeMillis();
        boolean validChain = currentBlockChain.isChainValid();
        long endTime = System.currentTimeMillis();
        System.out.println("Chain verification: " + validChain);
        System.out.println("Total time required for verification: " + (endTime - startTime) + " milliseconds.");
    }

    /**
     * Option Four chosen by user.
     * @param currentBlockChain the current block chain.
     */
    private static void optionFour(BlockChain currentBlockChain) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter block ID of block to Corrupt : ");
        int corruptBlock = Integer.parseInt( bufferedReader.readLine());

        while (corruptBlock < 0 || corruptBlock > currentBlockChain.getChainSize()) {
            System.out.print("Please enter a valid block id to corrupt: ");
            corruptBlock = Integer.parseInt( bufferedReader.readLine());
        }

        System.out.print("Enter new data for block " + corruptBlock + ": ");
        String corruptData = bufferedReader.readLine();
        currentBlockChain.blockChainList.get(corruptBlock - 1).setData(corruptData);
        System.out.println("Block " + corruptBlock + " now holds " + corruptData);
    }

    /**
     * Option Five chosen by user.
     * @param currentBlockChain the current block chain.
     */
    private static void optionFive(BlockChain currentBlockChain) throws IOException {
        long startTime = System.currentTimeMillis();
        currentBlockChain.repairChain();
        long endTime = System.currentTimeMillis();
        System.out.println("Chain repaired.");
        System.out.println("Total time required for repair: " + (endTime - startTime) + " milliseconds.");
    }


    /**
     * Method Processes user input calls appropriate routine based on choice.
     */
    private static void processInputRequest() {
        // Creates new blockchain.
        BlockChain newBlockChain = new BlockChain();
        // Creates genesis block.
        Block genesis = new Block(0, newBlockChain.getTime(), "", 2);
        newBlockChain.addBlock(genesis);
        while (true) {
            String userInput = getUserInput();
            try {
                switch (userInput) {
                    case "0":
                        optionZero(newBlockChain);
                        break;
                    case "1":
                        optionOne(newBlockChain);
                        break;
                    case "2":
                        optionTwo(newBlockChain);
                        break;
                    case "3":
                        System.out.println(newBlockChain.toString());
                        break;
                    case "4":
                        optionFour(newBlockChain);
                        break;
                    case "5":
                        optionFive(newBlockChain);
                        break;
                    case "6":
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Not supported option!!");
                }
            } catch (Exception e) {
                System.out.println("Something wrong !!");
            }
        }
    }


    /**
     * This routine acts as a test driver for your Blockchain.
     * It will begin by creating a BlockChain object and then adding the Genesis block to the chain.
     * The Genesis block will be created with an empty string as the pervious hash and a difficulty of 2.
     * @param args
     */
    public static void main(String[] args) {
        /*
        Difficulty lvl 4: Avg time to add is 50 milliseconds.
        Difficulty lvl 5: Avg time to add is 950 milliseconds.
        */
        processInputRequest();
    }

}
