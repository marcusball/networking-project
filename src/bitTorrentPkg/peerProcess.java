package bitTorrentPkg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/* peerProcess.java
 * Authors: Joey Siracusa, Marcus Ball, Anurag
 * 
 * This process is launched on each machine.  It reads Common.cfg and PeerInfo.cfg, initializes
 * the related variables, and has peers connect and share with each other. 
 * 
 * 
 */


public class peerProcess {

	int numOfPrefNeighbors; //Number of neighbors this peer will share with
	int unchokingInterval; //Time between each normal unchoke of an existing peer (in seconds)
	int optUnchokingInterval; //Time between each optimistic unchoke of a new peer (in seconds)
	String fileName; 
	int fileSize;	//in bytes
	int pieceSize;	//Size of each piece file will be broken into (in bytes)
	int numOfPieces; //number of pieces in a file (filesize divided by piecesize, rounded up)
	
	
	public void readCommon() throws IOException{
		//this method parses Common.cfg
		BufferedReader config = new BufferedReader(new FileReader("Common.cfg"));
		config.skip(27); //NumberOfPreferences_ is 27 chars
		numOfPrefNeighbors = config.read();
		config.skip(19); //newline + UnchokingInterval_ is 19 chars
		unchokingInterval = config.read();
		config.skip(29); //newline + OptimisticUnchokingInterval_
		optUnchokingInterval = config.read();
		config.skip(10); //newline + FileName_
		fileName = config.readLine();
		config.skip(9); //FileSize_ (don't count newline)
		fileSize = config.read();
		config.skip(11); //newline + PieceSize_
		pieceSize = config.read();
		config.close();
		
		//reading from Common.cfg is complete, now finish calculations
		numOfPieces = (fileSize + pieceSize - 1) / pieceSize; //integer division which rounds up
		
	}
	
	public void readPeerInfo() throws IOException{
		//this method 
	}
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
