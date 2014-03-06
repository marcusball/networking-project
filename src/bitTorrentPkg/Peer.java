package bitTorrentPkg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Peer {
	private int numOfPrefNeighbors; //Number of neighbors this peer will share with
	private int unchokingInterval; //Time between each normal unchoke of an existing peer (in seconds)
	private int optUnchokingInterval; //Time between each optimistic unchoke of a new peer (in seconds)
	private String fileName; 
	private int fileSize;	//in bytes
	private int pieceSize;	//Size of each piece file will be broken into (in bytes)
	private int numOfPieces; //number of pieces in a file (filesize divided by piecesize, rounded up)
	private int peerID; //the peerID of THIS peer
	private String hostName; //host name of THIS peer
	private int listeningPort; 	//listening port for THIS peer
	private boolean hasFile;

	public Peer() throws IOException{
		peerID = -1;
		readCommon();
		readPeerInfo();
	}
	
	public Peer(int peerID) throws IOException{
		this.peerID = peerID;
		readCommon();
		readPeerInfo();
	}
	
	private void readCommon() throws IOException{
		//this method parses Common.cfg
		String currLine = null;
		String parts[] = null;
		BufferedReader config = new BufferedReader(new FileReader("/Users/joeysiracusa/Development/networking-project/src/bitTorrentPkg/Common.cfg"));
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
		String currLine = null;
		String parts[] = null;
		BufferedReader peerInfo = new BufferedReader(new FileReader("/Users/joeysiracusa/Development/networking-project/src/bitTorrentPkg/PeerInfo.cfg"));
		boolean foundOwnPeerID = false;
		currLine = peerInfo.readLine();
		while(!foundOwnPeerID && currLine != null){
			parts = currLine.split(" ");
			if(Integer.parseInt(parts[0]) == peerID){
				foundOwnPeerID = true;
				this.hostName = parts[1];
				this.listeningPort = Integer.parseInt(parts[2]);
				if(Integer.parseInt(parts[3]) == 1){
					hasFile = true;
					//TODO: Set bitfield to be all 1's
					
				}else{
					hasFile = false;
					//TODO: Set bitfield to be all 0's
					
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
	
	public String toString(){
		//mainly for debugging- this can be converted into a log later maybe?
		return "NumberOfPrefferedNeighbors " + numOfPrefNeighbors + "\nUnchokingInterval " + unchokingInterval + 
				"\nOptimisticUnchokingInterval " + optUnchokingInterval + "\nFileName " + fileName + 
				"\nFileSize " + fileSize + "\nPeerID " + peerID + "\nHostName " + hostName + 
				"\nListeningPort " + listeningPort + "\nHasFile " + hasFile + "\n";
		
	}



}


