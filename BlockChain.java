//package ori;
// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.ArrayList;
import java.util.HashMap;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    private TransactionPool txPool;

    public class blockNode {
    	public Block block;
    	public int height;
    	public UTXOPool up;
    	public blockNode parent;
    	public ArrayList<blockNode> child;
    }
    
    private blockNode root;
    private blockNode maxHeightNode;
    /** A HashMap from {block.HashCode} to {blockNode} */
    private HashMap<byte[], blockNode>bl2Node;
    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
    	root = new blockNode();
    	root.block = genesisBlock;
    	root.height = 1;
    	root.up = new UTXOPool();
    	root.parent = null;
    	root.child = new ArrayList<>();
    	// get genesisBlock UTXOPool
    	Transaction coinbase = genesisBlock.getCoinbase();
    	for (int i = 0; i < coinbase.numOutputs(); i++){
	    	UTXO utxo = new UTXO(coinbase.getHash(), i);
	    	Transaction.Output txOut = coinbase.getOutput(i);
	    	root.up.addUTXO(utxo, txOut);
    	}
    	maxHeightNode = root;
    	txPool = new TransactionPool();
    	bl2Node = new HashMap<>();
    	bl2Node.put(genesisBlock.getHash(), root);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
    	return maxHeightNode.block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
    	return new UTXOPool(maxHeightNode.up);
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
    	return txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
    	// check block PrevBlockHash
    	if (block.getPrevBlockHash() == null) return false;
    	if (bl2Node.get(block.getPrevBlockHash()) == null) return false;
    	
    	blockNode parent = bl2Node.get(block.getPrevBlockHash());
    	blockNode newNode = new blockNode();
    	newNode.block = block;
    	newNode.height = parent.height + 1;
    	
    	// check Height
    	if (newNode.height <= maxHeightNode.height - CUT_OFF_AGE) return false;
    	if (newNode.height > maxHeightNode.height) {
    		maxHeightNode = newNode;
    	}
    	
    	newNode.parent = parent;
    	parent.child.add(newNode);
    	newNode.child = new ArrayList<>();
    	
    	// Validate Transactions of new block
    	UTXOPool cup = new UTXOPool(parent.up);
    	TxHandler handler = new TxHandler(cup);
    	ArrayList<Transaction> possibleTxsAList = block.getTransactions();
    	Transaction[] possibleTxs = possibleTxsAList.toArray(new Transaction[possibleTxsAList.size()]);
    	Transaction[] validTxs = handler.handleTxs(possibleTxs);
    	if (possibleTxs.length != validTxs.length) return false;
    	
    	UTXOPool newup = handler.getUTXOPool();
    	Transaction coinbase = block.getCoinbase();
    	for (int i = 0 ; i < coinbase.numOutputs(); i++){
    		UTXO utxo = new UTXO(coinbase.getHash(), i);
    		Transaction.Output txOut = coinbase.getOutput(i);
    		newup.addUTXO(utxo, txOut);
    	}
    	newNode.up = new UTXOPool(newup);
    	
    	bl2Node.put(block.getHash(), newNode);
    	return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
    	txPool.addTransaction(tx);
    }
}
