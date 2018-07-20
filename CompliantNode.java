//package orio;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
	private double p_graph;
	private double p_malicious;
	private double p_txDistribution;
	private int numRounds;
	private boolean[] followees;
	private Set<Transaction> pendingTransactions;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
    	this.p_graph = p_graph;
    	this.p_malicious = p_malicious;
    	this.p_txDistribution = p_txDistribution;
    	this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
    	this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
    	this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
    	Set<Transaction> temp = new HashSet<Transaction>(pendingTransactions);
    	this.pendingTransactions.clear();
    	return temp;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
    	for (Candidate can : candidates) {
    		if (this.followees[can.sender]) {
    			this.pendingTransactions.add(can.tx);
    		}
    	}
    }
}
