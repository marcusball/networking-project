package bitTorrentPkg;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;

/* CLASS: Peer
 * 		-Peer is a stripped down version of class Host
 * 		-It is made to represent "other" peers with respect to the Host (the machine executing the program)
 * 		-It only contains some configuration variables (int numOfPieces)
 * 		-It does not contain methods for reading configuration files
 * 		-It does not contain methods for listening or any other communication whatsoever (besides numOfPieces
 * 		-This class tracks what other peers are interested in and what other peers are requesting
 * 		
 */


public class Peer {
	//--------------------VARIABLES--------------------
	
	//PeerInfo.cfg variables
	private int peerID; //the peerID of THIS peer (inputted into command line)
	private String hostName; //host name of THIS peer
	private int listeningPort; 	//listening port for THIS peer
	private boolean hasFile;
	private boolean isFirstPeer; //if it's the first peer, just wait and listen
								 //if not, initiate tcp connections with others
	
	int numOfPieces;
	private Bitfield bitfield;

	
	/*--------------------CONSTRUCTORS--------------------
	 * All Peer class constructors are located here
	 */
	
	public Peer(int peerID, String hostName, int listeningPort, boolean hasFile, boolean isFirstPeer, int numOfPieces){
		//this constructor is used to keep track of OTHER peers
		//when keeping track of other peers, this info is all that is necessary
		this.peerID = peerID;
		this.hostName = hostName;
		this.listeningPort = listeningPort;
		this.hasFile = hasFile;
		this.isFirstPeer = isFirstPeer;
		this.numOfPieces = numOfPieces;
	}
	
	
	/*--------------------GET/SET METHODS--------------------
	 * All Peer class get/set methods are located here
	 */
	
	
	public int getPeerID(){
		return peerID;	//this cannot be changed
	}
	
	public String getHostName(){
		return hostName; //this cannot be changed
	}
	
	public int getListeningPort(){
		return listeningPort;
	}
	
	public void setListeningPort(int listeningPort){
		this.listeningPort = listeningPort;
	}
	
	public boolean hasFile(){
		return hasFile;
	}
	
	public void setHasFile(boolean hasFile){
		this.hasFile = hasFile;
	}
	
	public boolean isFirstPeer(){
		return isFirstPeer;
	}
	
	public void setIsFirstPeer(boolean isFirstPeer){
		this.isFirstPeer = isFirstPeer;
	}
	
	public int getNumOfPieces(){
		return numOfPieces; 	//this cannot be changed
	}
	
	public Bitfield getBitfield(){
		return bitfield;		//this cannot be changed manually
	}
	
	public String toString(){
		//mainly for debugging- this can be converted into a log later maybe?
		return "PeerID " + peerID + "\nHostName " + hostName + 
				"\nListeningPort " + listeningPort + "\nHasFile " + hasFile + "\n";		
	}

}


