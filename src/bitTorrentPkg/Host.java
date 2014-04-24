package bitTorrentPkg;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;

import bitTorrentPkg.Messages.Piece;

public class Host {
	//--------------------VARIABLES--------------------
	//Common.cfg variables
	private int numOfPrefNeighbors; //Number of neighbors this peer will share with
	private long unchokingInterval; //Time between each normal unchoke of an existing peer (in seconds)
	private long optUnchokingInterval; //Time between each optimistic unchoke of a new peer (in seconds)
	private String fileName; 
	private long fileSize;	//in bytes
	private int pieceSize;	//Size of each piece file will be broken up into (in bytes)
	private int numOfPieces; //number of pieces in a file (fileSize divided by pieceSize, rounded up)
	private long requestTTL;
	
	//PeerInfo.cfg variables
	private int peerID; //the peerID of THIS peer (inputted into command line)
	private String hostName; //host name of THIS peer
	private int listeningPort; 	//listening port for THIS peer
	private boolean hasFile;
	private boolean isFirstPeer; //if it's the first peer, just wait and listen
								 //if not, initiate tcp connections with others
	
	private Bitfield bitfield;
	private ArrayList<RequestedPiece> requests;

	public HashMap<Integer,Peer> peerInfo; //This will contain the list of ALL peers listed in PeerInfo, not just the ones this host is connected to. 
	public ArrayList<Integer> targetPeers; //This will contain the peer IDs of all of the peers above this host in the PeerInfo file. 
	
	/*--------------------CONSTRUCTORS--------------------
	 * All Peer class constructors are located here
	 */
	
	public Host(){
		this(-1);
	}
	
	public Host(int peerID){
		this.peerID = peerID;
		peerInfo = new HashMap<Integer, Peer>();
		targetPeers = new ArrayList<Integer>();
	}
	
	public void init() throws IOException{
		readPeerInfo();
		readCommon();
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
	
	public long getUnchokingInterval(){
		return unchokingInterval;
	}
	
	public void setUnchokingInterval(long unchokingInterval){
		this.unchokingInterval = unchokingInterval;
	}
	
	public long getOptUnchokingInterval(){
		return optUnchokingInterval;
	}
	
	public void setOptUnchokingInterval(long optUnchokingInterval){
		this.optUnchokingInterval = optUnchokingInterval;
	}
	
	public String getFileName(){
		return fileName; //this cannot be changed
	}
	
	public long getFileSize(){
		return fileSize; //this cannot be changed
	}
	
	public int pieceSize(){
		return pieceSize; //this cannot be changed
	}
	
	public int getNumOfPieces(){
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
		this.unchokingInterval = Long.parseLong(parts[1]);
		currLine = config.readLine();
		parts = currLine.split("\\s+");
		this.optUnchokingInterval = Long.parseLong(parts[1]);
		currLine = config.readLine();
		parts = currLine.split("\\s+");
		this.fileName = parts[1];
		currLine = config.readLine();
		parts = currLine.split("\\s+");
		this.fileSize = Long.parseLong(parts[1]);
		currLine = config.readLine();
		parts = currLine.split("\\s+");
		this.pieceSize = Integer.parseInt(parts[1]);
		config.close(); //close the config file
		
		//reading from Common.cfg is complete, now finish calculations
		this.readShareFileInfo();
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
			Tools.debug("[Host.readPeerInfo] %s",currLine);
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
			
			//Tools.debug("Comparing config id (%d) with host id (%d).",currPeerID,peerID);
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
				}else{
					//if it does not have the file, set all of bitfield to be 0
					//at the begining of the program, either the peer has the complete file 
					//or the peer does not have a single piece of the file
					//therefore, we set all of the bitfield to be 0, since it has no pieces
					hasFile = false;
				}					
			}else{
				//Tools.debug("[Host.readPeerInfo] Adding peer %d to peer list.",currPeerID);
				peerInfo.put(currPeerID, new Peer(currPeerID, currHostName, currListeningPort, currHasFile, currIsFirstPeer, 
							this.pieceSize, this.numOfPieces, System.currentTimeMillis()));
				
				if(!foundOwnPeerID){	//If this is above the host in peerInfo (and thus we haven't yet found own own peerID), 
										//then it is a peer we will be trying to connect to
					targetPeers.add(currPeerID); //So we'll add it to the list of peers we will connect to. 
				}
			}
			peerCount++;
		}
		peerInfoReader.close();
		if(!foundOwnPeerID){
			Tools.debug("[Host.readPeerInfo] WARNING: This machine not found in tracker.  Terminating...");
			System.exit(0);
		}
		
	}
	

	
	public void initiateTCPConnections() throws IOException{
		if(this.targetPeers.size() > 0 ){
			Peer nextPeer;
			for(int nextPeerId : this.targetPeers){
				if(this.peerInfo.containsKey(nextPeerId)){
					nextPeer = this.peerInfo.get(nextPeerId);
					if(nextPeer == null){
						continue;
					}
					if(nextPeer.getPeerID() != this.peerID){ //This check shouldn't be necessary, but lets just be safe.
						Tools.debug("[Host.intitiateTCPConnections] Initiating TCP connection with peer %d: %s:%d...",nextPeerId,nextPeer.getHostName(),nextPeer.getListeningPort());
						
						nextPeer.createEdgeConnection();
						nextPeer.getConnection().sendHandshake();
						NeighborController.addPeer(nextPeer);
						
						Logger.logTCPConnectionTo(nextPeer.getPeerID());
						Tools.debug("[Host.intitiateTCPConnections] BAM! Connection established with %s!",nextPeer.getHostName());
					}
				}
			}
		}
		else{
			Tools.debug("[Host.intitiateTCPConnections] No peers to establish connection with!");
		}
		//TODO: I now have readPeerInfo actually creating all the peers
		//This method now needs to loop through those peers and initiate TCP connections
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
	
	private void readShareFileInfo() throws IOException{
		if(this.hasFile){
			Tools.debug("[Host.readShareFileInfo] Reading file info for %s...",this.fileName);
			
			FileManager.openSharedFile(this.fileName);
			FileManager.FileInfo info = FileManager.getFileInfo();
			if(info.getByteLength() != this.fileSize){
				throw new IOException(String.format("File size of %s is %d; expected %d from config file!",this.fileName,info.getByteLength(),this.fileSize));
			}
		}
		else{
			Tools.debug("[Host.readShareFileInfo] Not in possestion of file, skipping read attempt.");
		}
		
		this.numOfPieces = (int) Math.ceil(fileSize / pieceSize); 
		bitfield = new Bitfield(numOfPieces,this.hasFile);
	}
	
	public boolean hasInterestIn(Peer other){
		return this.bitfield.checkForInterest(other.getBitfield());
	}
	
	public int getPieceIdToRequestFrom(Peer other){
		//return this.bitfield.getRandomIndex(true);
		Bitfield possiblePieces = this.bitfield.not().and(other.getBitfield());
		if(possiblePieces.isAll(false)){
			return -1; //Other has no pieces that we do not already have. 
		}
		return possiblePieces.getRandomIndex(true);
	}

	public boolean hasPiece(int id){
		return this.bitfield.getValue(id);
	}
	
	public void setRequestTTL(long TTL){
		this.requestTTL = TTL;
	}
	
	public long getRequestTTL(){
		return requestTTL;
	}
	
	public void addRequest(int peerID, int pieceIndex){
		RequestedPiece request = new RequestedPiece(peerID, pieceIndex);
		requests.add(request);
	}
	
	public RequestedPiece getRequest(int pieceIndex){
		for(int i = 0; i < requests.size(); i++){
			if(requests.get(i).getPieceIndex() == pieceIndex){
				return requests.get(i);
			}
		}
		Tools.debug("[Host] Piece index " + pieceIndex + " not found in outstanding requests!");
		return null;
	}
	
	public int getRequestIndex(int pieceIndex){
		//every time you get a request, make sure the requests are clean
		cleanRequests();
		for(int i = 0; i < requests.size(); i++){
			if(requests.get(i).getPieceIndex() == pieceIndex){
				return i;
			}
		}
		Tools.debug("[Host] Piece index " + pieceIndex + " not found in outstanding requests!");
		return -1; 
	}
	
	public void removeRequest(int pieceIndex){
		if(getRequestIndex(pieceIndex) != -1){
			requests.remove(getRequestIndex(pieceIndex));
		}else{
			Tools.debug("[Host] Piece index " + pieceIndex + " could not be removed, because it was not found in oustanding requests!");
		}
	}
	
	public void cleanRequests(){
		for(int i = 0; i < requests.size(); i++){
			if(System.currentTimeMillis() - requests.get(i).getTimestamp() > requestTTL){
				//if the request is greater than the request TTL
				//delete the request so that it has a chance of being resent
				requests.remove(i);
				Tools.debug("[Host] Request of piece index " + requests.get(i).pieceIndex + " from peer " 
						+ requests.get(i).getPeerID() + " has exceeded it's time to live, and has been removed.");
				
			}
		}
	}
	
	public boolean hasRequestedPiece(Peer other,int pieceIndex){
		int requestIndex = this.getRequestIndex(pieceIndex);
		if(requestIndex != -1){
			RequestedPiece request = this.getRequest(pieceIndex);
			if(request.getPeerID() != other.getPeerID()){
				return false;
			}
		}
		return true;
	}
	
	public byte[] getPiece(int pieceIndex) throws IOException{
		return FileManager.getFilePiece(pieceIndex, this.pieceSize);
	}
	
	public void savePiece(Piece piece){
		try {
			FileManager.writeFilePiece(piece.getIndex(), piece.getData());
			this.bitfield.setValue(piece.getIndex(), true);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean downloadIsComplete(){
		return this.bitfield.isAll(true);
	}
}




