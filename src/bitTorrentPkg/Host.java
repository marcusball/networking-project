package bitTorrentPkg;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;

public class Host {
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
	
	private Bitfield bitfield;

	public HashMap<Integer,Peer> peerInfo; //This will contain the list of ALL peers listed in PeerInfo, not just the ones this host is connected to. 
	
	/*--------------------CONSTRUCTORS--------------------
	 * All Peer class constructors are located here
	 */
	
	public Host() throws IOException{
		this(-1);
	}
	
	public Host(int peerID) throws IOException{
		this.peerID = peerID;
		peerInfo = new HashMap<Integer, Peer>();
		readCommon();
		bitfield = new Bitfield(numOfPieces);
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
	
	public Bitfield getBitfield(){
		return bitfield;		//this cannot be changed manually
	}
	
	
	
	/*--------------------READING CONFIG FILES--------------------
	 * Methods to read Common.cfg and PeerInfo.cfg are located here
	 */
	
	
	private void readCommon() throws IOException{
		//this method parses Common.cfg
		String currLine = null;
		String parts[] = null;
		BufferedReader config = new BufferedReader(new FileReader("Common.cfg"));
		currLine = config.readLine(); //gets the line in a string
		parts = currLine.split("\\s+"); //splits the line at the space
		this.numOfPrefNeighbors = Integer.parseInt(parts[1]); //reads the value after the space
		currLine = config.readLine(); //repeat for all variables in config file
		parts = currLine.split("\\s+");
		this.unchokingInterval = Integer.parseInt(parts[1]);
		currLine = config.readLine();
		parts = currLine.split("\\s+");
		this.optUnchokingInterval = Integer.parseInt(parts[1]);
		currLine = config.readLine();
		parts = currLine.split("\\s+");
		this.fileName = parts[1];
		currLine = config.readLine();
		parts = currLine.split("\\s+");
		this.fileSize = Integer.parseInt(parts[1]);
		currLine = config.readLine();
		parts = currLine.split("\\s+");
		this.pieceSize = Integer.parseInt(parts[1]);
		config.close(); //close the config file
		
		//reading from Common.cfg is complete, now finish calculations
		numOfPieces = (fileSize + pieceSize - 1) / pieceSize; //integer division which rounds up
		
	}
	

	private void readPeerInfo() throws IOException{
		//read PeerInfo.cfg and fills creates list of peers from it (and fills in host info)
		int peerCount = 0;
		String currLine = null;
		String parts[] = null;
		int currPeerID;
		String currHostName;
		int currListeningPort;
		boolean currHasFile;
		boolean currIsFirstPeer;

		BufferedReader peerInfoReader = new BufferedReader(new FileReader("PeerInfo.cfg"));
		boolean foundOwnPeerID = false;
		while((currLine = peerInfoReader.readLine()) != null){
			Tools.debug(currLine);
			parts = currLine.split("\\s+"); //split each line into peerID, hostname, listening port, has file
			
			//get the peer variables from the string array parts
			currPeerID = Integer.parseInt(parts[0]);
			currHostName = parts[1];
			currListeningPort = Integer.parseInt(parts[2]);
			if(Integer.parseInt(parts[3]) == 1){
				currHasFile = true;
			}else{
				currHasFile = false;
			}
			if(peerCount == 0){
				currIsFirstPeer = true;
			}else{
				currIsFirstPeer = false;
			}
			
			Tools.debug("Comparing config id (%d) with host id (%d).",currPeerID,peerID);
			//either save the info to the host or add a new peer with it
			if(currPeerID == peerID){
				foundOwnPeerID = true;
				//if this line is representing the host
				this.isFirstPeer = currIsFirstPeer;
				this.hostName = currHostName; //save the host name
				this.listeningPort = currListeningPort; //save the listening port
				if(currHasFile){
					//if it has the file, set all of the bitfield to be 1
					//this means is has every piece of the file
					hasFile = true;
					this.bitfield.setValueAll(true);
				}else{
					//if it does not have the file, set all of bitfield to be 0
					//at the begining of the program, either the peer has the complete file 
					//or the peer does not have a single piece of the file
					//therefore, we set all of the bitfield to be 0, since it has no pieces
					hasFile = false;
					this.bitfield.setValueAll(false);
				}					
			}else{
				peerInfo.put(currPeerID, new Peer(currPeerID, currHostName, currListeningPort, currHasFile, currIsFirstPeer, 
							this.pieceSize, this.numOfPieces, System.currentTimeMillis()));
			}
			peerCount++;
		}
		peerInfoReader.close();
		if(!foundOwnPeerID){
			System.out.println("WARNING: This machine not found in tracker.  Terminating...");
			System.exit(0);
		}
		
	}
	

	
	public void initiateTCPConnections() throws IOException{
		if(this.peerInfo.size() > 0 && this.peerInfo.get(0) != null){
			Peer nextPeer;
			for(int x=0; (nextPeer = this.peerInfo.get(x)).getPeerID() != this.peerID; x += 1){ //get the next connection, as long as it's not to this host
				nextPeer.createEdgeConnection();
				nextPeer.getConnection().sendHandshake();
				NeighborController.addPeer(nextPeer);
			}
		}
		else{
			Tools.debug("No peers to establish connection with!");
		}
		//TODO: I now have readPeerInfo actually creating all the peers
		//This method now needs to loop through those peers and initiate TCP connections
	}
	
	public void listen() throws IOException{
		//TODO: After changing Peer to represent OTHER PEERS ONLY, change this
		
	}
	
	
	public boolean isExpectingPeerId(int id){ //Checks whether PeerInfo had connection configured for this ID
		return this.peerInfo.containsKey(id);
	}
	
	public Peer getPeer(int id){
		if(this.peerInfo.containsKey(id)){
			return this.peerInfo.get(id);
		}
		return null;
	}
	
	public String toString(){
		//mainly for debugging- this can be converted into a log later maybe?
		return "NumberOfPrefferedNeighbors " + numOfPrefNeighbors + "\nUnchokingInterval " + unchokingInterval + 
				"\nOptimisticUnchokingInterval " + optUnchokingInterval + "\nFileName " + fileName + 
				"\nFileSize " + fileSize + "\nPeerID " + peerID + "\nHostName " + hostName + 
				"\nListeningPort " + listeningPort + "\nHasFile " + hasFile + "\n";		
	}

}




