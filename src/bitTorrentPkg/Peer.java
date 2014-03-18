package bitTorrentPkg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Peer {
	//--------------------VARIABLES--------------------
	//Common.cfg variables
	private int numOfPrefNeighbors; //Number of neighbors this peer will share with
	private int unchokingInterval; //Time between each normal unchoke of an existing peer (in seconds)
	private int optUnchokingInterval; //Time between each optimistic unchoke of a new peer (in seconds)
	private String fileName; 
	private int fileSize;	//in bytes
	private int pieceSize;	//Size of each piece file will be broken up into (in bytes)
	private int numOfPieces; //number of pieces in a file (fileSize divided by pieceSize, rounded up)
	
	//PeerInfo.cfg variables
	private int peerID; //the peerID of THIS peer (inputted into command line)
	private String hostName; //host name of THIS peer
	private int listeningPort; 	//listening port for THIS peer
	private boolean hasFile;
	private boolean isFirstPeer; //if it's the first peer, just wait and listen
								 //if not, initiate tcp connections with others
	
	//File related variables
	private boolean bitfield[]; //if bitfield[i] is true, the peer has piece i
								//keeps track of which pieces the peer has
								

	/*--------------------CONSTRUCTORS--------------------
	 * All Peer class constructors are located here
	 */
	
	public Peer() throws IOException{
		peerID = -1; //if for some reason you don't have a peerID but want to test
		readCommon();
		bitfield = new boolean[numOfPieces];
		readPeerInfo();
	}
	
	public Peer(int peerID) throws IOException{
		this.peerID = peerID;
		readCommon();
		bitfield = new boolean[numOfPieces];
		readPeerInfo();
	}
	
	/*--------------------GET/SET METHODS--------------------
	 * All Peer class get/set methods are located here
	 */
	public int getNumOfPrefNeighbors(){
		return numOfPrefNeighbors;
	}
	
	public void setNumOfPrefNeighbors(int numOfPrefNeighbors){
		this.numOfPrefNeighbors = numOfPrefNeighbors;
	}
	
	public int getUnchokingInterval(){
		return unchokingInterval;
	}
	
	public void setUnchokingInterval(int unchokingInterval){
		this.unchokingInterval = unchokingInterval;
	}
	
	public int getOptUnchokingInterval(){
		return optUnchokingInterval;
	}
	
	public void setOptUnchokingInterval(int optUnchokingInterval){
		this.optUnchokingInterval = optUnchokingInterval;
	}
	
	public String getFileName(){
		return fileName; //this cannot be changed
	}
	
	public int getFileSize(){
		return fileSize; //this cannot be changed
	}
	
	public int pieceSize(){
		return pieceSize; //this cannot be changed
	}
	
	public int numOfPieces(){
		return numOfPieces; //this cannot be changed
	}
	
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
	
	public boolean[] getBitfield(){
		return bitfield;		//this cannot be changed
	}
	
	/*--------------------READING CONFIG FILES--------------------
	 * Methods to read Common.cfg and PeerInfo.cfg are located here
	 */
	
	
	private void readCommon() throws IOException{
		//this method parses Common.cfg
		String currLine = null;
		String parts[] = null;
		//Joey's path: /Users/joeysiracusa/Development/networking-project/src/bitTorrentPkg/Common.cfg
		//Anurag's path: /Users/anurag/My Documents/GitHub/networking-project/src/bitTorrentPkg/Common.cfg
		BufferedReader config = new BufferedReader(new FileReader("/Users/anurag/My Documents/GitHub/networking-project/src/bitTorrentPkg/Common.cfg"));
		currLine = config.readLine(); //gets the line in a string
		parts = currLine.split(" "); //splits the line at the space
		this.numOfPrefNeighbors = Integer.parseInt(parts[1]); //reads the value after the space
		currLine = config.readLine(); //repeat for all variables in config file
		parts = currLine.split(" ");
		this.unchokingInterval = Integer.parseInt(parts[1]);
		currLine = config.readLine();
		parts = currLine.split(" ");
		this.optUnchokingInterval = Integer.parseInt(parts[1]);
		currLine = config.readLine();
		parts = currLine.split(" ");
		this.fileName = parts[1];
		currLine = config.readLine();
		parts = currLine.split(" ");
		this.fileSize = Integer.parseInt(parts[1]);
		currLine = config.readLine();
		parts = currLine.split(" ");
		this.pieceSize = Integer.parseInt(parts[1]);
		config.close(); //close the config file
		
		//reading from Common.cfg is complete, now finish calculations
		numOfPieces = (fileSize + pieceSize - 1) / pieceSize; //integer division which rounds up
		
	}
	

	private void readPeerInfo() throws IOException{
		//searches PeerInfo for THIS peer ID
		String currLine = null;
		String parts[] = null;
		//Joey's path: /Users/joeysiracusa/Development/networking-project/src/bitTorrentPkg/PeerInfo.cfg
		//Anurag's path: /Users/anurag/My Documents/GitHub/networking-project/src/bitTorrentPkg/PeerInfo.cfg
		BufferedReader peerInfo = new BufferedReader(new FileReader("/Users/anurag/My Documents/GitHub/networking-project/src/bitTorrentPkg/PeerInfo.cfg"));
		boolean foundOwnPeerID = false;
		currLine = peerInfo.readLine();
		while(!foundOwnPeerID && currLine != null){
			parts = currLine.split(" "); //split each line into peerID, hostname, listening port, has file
			if(Integer.parseInt(parts[0]) == peerID){
				foundOwnPeerID = true;
				this.hostName = parts[1]; //save the host name
				this.listeningPort = Integer.parseInt(parts[2]); //save the listening port
				if(Integer.parseInt(parts[3]) == 1){
					//if it has the file, set all of the bitfield to be true
					//this means is has every piece of the file
					hasFile = true;
					for(int i = 0; i < numOfPieces; i++){
						bitfield[i] = true;
					}
					
				}else{
					//if it does not have the file, set all of bitfield to be false
					//at the beggining of the program, either the peer has the complete file 
					//or the peer does not have a single piece of the file
					//therefore, we set all of the bitfield to be false, since it has no pieces
					hasFile = false;
					for(int i = 0; i < numOfPieces; i++){
						bitfield[i] = true;
					}
					
					
				}
					
				break;
			}
			currLine = peerInfo.readLine();
		}
		peerInfo.close();
		if(!foundOwnPeerID){
			System.out.println("WARNING: This machine not found in tracker.  Terminating...");
			System.exit(0);
		}
		
	}
	
	public void listen(){
		
	}
	
	public String toString(){
		//mainly for debugging- this can be converted into a log later maybe?
		return "NumberOfPrefferedNeighbors " + numOfPrefNeighbors + "\nUnchokingInterval " + unchokingInterval + 
				"\nOptimisticUnchokingInterval " + optUnchokingInterval + "\nFileName " + fileName + 
				"\nFileSize " + fileSize + "\nPeerID " + peerID + "\nHostName " + hostName + 
				"\nListeningPort " + listeningPort + "\nHasFile " + hasFile + "\n";
		
	}



}


