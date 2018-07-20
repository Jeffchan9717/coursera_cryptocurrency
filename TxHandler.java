//package ori;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TxHandler {

    public UTXOPool ledger;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
    	ledger = new UTXOPool(utxoPool); 
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
    	// first
        for (int i = 0; i < tx.numInputs(); i++) {
        	UTXO utxo = new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex);
    		if (ledger.contains(utxo) == false) return false;
        }
        // second
        for (int i = 0; i < tx.numInputs(); i++){
        	UTXO utxo = new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex);
        	PublicKey pubKey = ledger.getTxOutput(utxo).address;
        	byte[] message = tx.getRawDataToSign(i);
        	byte[] signature = tx.getInput(i).signature;
        	if (Crypto.verifySignature(pubKey, message, signature) == false) return false;
        }
        // third
        UTXOPool temp = new UTXOPool();
        for (int i = 0; i < tx.numInputs(); i++) {
        	UTXO utxo = new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex);
    		if (temp.contains(utxo)) return false;
    		temp.addUTXO(utxo, ledger.getTxOutput(utxo));
        }
        temp = null;
        // fourth & fifth
        double txInflow = 0;
        double txOutflow = 0;
        for (int i = 0; i < tx.numOutputs(); i++) {
        	if (tx.getOutput(i).value < 0) return false;
        	txOutflow += tx.getOutput(i).value;
        }
        for (int i = 0; i < tx.numInputs(); i++) {
        	UTXO utxo = new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex);
        	txInflow += ledger.getTxOutput(utxo).value;
        }
        if (txInflow < txOutflow) return false;
    	return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        Set<Transaction> vTx = new HashSet<Transaction>();
        for (Transaction tx : possibleTxs){
        	if (isValidTx(tx)) {
        		vTx.add(tx);
        		for(int i = 0; i < tx.numInputs(); i++){
        			UTXO utxo = new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex);
        			ledger.removeUTXO(utxo);
        		}
        		for(int i = 0; i < tx.numOutputs(); i++){
        			UTXO utxo = new UTXO(tx.getHash(), i);
        			ledger.addUTXO(utxo, tx.getOutput(i));
        		}
        	}
        }
        Transaction[] vTxArray = new Transaction[vTx.size()];
        return vTx.toArray(vTxArray);
    }

}
