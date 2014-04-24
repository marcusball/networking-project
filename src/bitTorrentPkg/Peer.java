package bitTorrentPkg;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;

import bitTorrentPkg.Messages.*;

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
	protected int peerID; //the peerID of THIS peer (inputted into command line)
	protected String hostName; //host name of THIS peer
	protected int listeningPort; 	//listening port for THIS peer
	protected boolean hasFile;
	protected boolean firstPeer; //if it's the first peer, just wait and listen
								 //if not, initiate tcp connections with others
	
	protected boolean unchoked;
	protected boolean optUnchoked;
	protected boolean interested; 
	
	protected Edge connection;
	
	protected long pieceSize;
	protected int numOfPieces;
	protected Bitfield bitfield;
	
	protected long startTime; //in milliseconds since Jan 1 1970
	protected long totalDLTime; //in milliseconds
	//after every unchoke, lastUnchokeTime is saved the current time
	protected long lastUnchokeTime; //time this peer was last unchoked
	protected int piecesDownloaded;
	protected int piecesSinceUnchoke;
	protected float dlRate; //represents average download rate since last unchoke
	
	protected int lastRequestedPiece; //What piece was last requested by this peer


	
	/*--------------------CONSTRUCTORS--------------------
	 * All Peer class constructors are located here
	 */
	
	public Peer(int peerID, String hostName, int listeningPort, boolean hasFile, boolean isFirstPeer, long pieceSize, int numOfPieces, long startTime){
		//this constructor is used to keep track of OTHER peers
		//when keeping track of other peers, this info is all that is necessary
		this.peerID = peerID;
		this.hostName = hostName;
		this.listeningPort = listeningPort;
		this.hasFile = hasFile;
		this.firstPeer = isFirstPeer;
		this.pieceSize = pieceSize;
		this.numOfPieces = numOfPieces;
		unchoked = false;
		optUnchoked = false;
		this.startTime = startTime;
		
		this.bitfield = new Bitfield(this.numOfPieces);
	}
	
	public Peer(int peerID, String hostName, int listeningPort, boolean hasFile, boolean isFirstPeer, long pieceSize, int numOfPieces, long startTime, Edge connection){
		//this constructor is used to keep track of OTHER peers
		//when keeping track of other peers, this info is all that is necessary
		this.peerID = peerID;
		this.hostName = hostName;
		this.listeningPort = listeningPort;
		this.hasFile = hasFile;
		this.firstPeer = isFirstPeer;
		this.pieceSize = pieceSize;
		this.numOfPieces = numOfPieces;
		unchoked = false;
		optUnchoked = false;
		this.startTime = startTime;
		this.connection = connection;
		
		this.bitfield = new Bitfield(this.numOfPieces);
	}	
	
	public void createEdgeConnection() throws IOException{
		this.connection = new Edge(this);
		this.connection.createClientSocket();
		this.connection.start();
		Tools.debug("Edge connection initiated!");
	}
	
	public void createEdgeConnectionFromSocket(Socket s) throws IOException{
		this.connection = new Edge(this);
		this.connection.setClientSocket(s);
		this.connection.start();
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
		return firstPeer;
	}
	
	public void setIsFirstPeer(boolean isFirstPeer){
		this.firstPeer = isFirstPeer;
	}
	
	public long getNumOfPieces(){
		return numOfPieces; 	//this cannot be changed
	}
	
	public Bitfield getBitfield(){
		return bitfield;		//this cannot be changed manually
	}
	
	public Edge getConnection(){
		return connection;
	}
	
	public void setConnection(Edge connection){
		this.connection = new Edge(this,connection);
	}
	
	public int getPiecesDownloaded(){
		return piecesDownloaded;
	}
	
	public void incrementPiecesDownloaded(){
		piecesDownloaded++;
	}
	
	public void setPiecesDownloaded(int piecesDownloaded){
		this.piecesDownloaded = piecesDownloaded;
	}
	
	public float getDLRate(){
		dlRate = ((float)(piecesDownloaded*pieceSize)) / ((float)((System.currentTimeMillis() - startTime)*1000));
		return dlRate;
	}
	
	public void setBitfield(Bitfield bf){
		this.bitfield = bf;
	}
	
	public void setInterest(boolean isIntr){
		this.interested = isIntr;
	}
	public boolean isInterested(){
		return this.interested;
	}
	
	public void choke(){
		unchoked = false;
		Choke choke = new Choke();
		connection.sendMessage(choke);
	}
	
	public void unchoke(){
		unchoked = true;
		Unchoke unchoke = new Unchoke();
		connection.sendMessage(unchoke);
	}
	
	public boolean isUnchoked(){
		return unchoked;
	}
	
	public void optUnchoke(){
		optUnchoked = true;
		Unchoke unchoke = new Unchoke();
		connection.sendMessage(unchoke);
	}
	
	public void optChoke(){
		optUnchoked = false;
		Choke choke = new Choke();
		connection.sendMessage(choke);
	}
	
	public boolean isOptUnchoked(){
		return optUnchoked;
	}
	
	public String toString(){
		//mainly for debugging- this can be converted into a log later maybe?
		return "PeerID " + peerID + "\nHostName " + hostName + 
				"\nListeningPort " + listeningPort + "\nHasFile " + hasFile + "\n";		
	}
	
	public void setHasPiece(int pieceId, boolean has){
		this.bitfield.setValue(pieceId, has);
	}
	
	public void setLastRequestedPiece(int piece){
		this.lastRequestedPiece = piece;
	}
	public int getLastRequestedPiece(){
		return this.lastRequestedPiece;
	}
}


