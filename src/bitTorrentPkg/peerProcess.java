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

	static int numOfPrefNeighbors; //Number of neighbors this peer will share with
	static int unchokingInterval; //Time between each normal unchoke of an existing peer (in seconds)
	static int optUnchokingInterval; //Time between each optimistic unchoke of a new peer (in seconds)
	static String fileName; 
	static int fileSize;	//in bytes
	static int pieceSize;	//Size of each piece file will be broken into (in bytes)
	static int numOfPieces; //number of pieces in a file (filesize divided by piecesize, rounded up)
	
	
	public static void readCommon() throws IOException{
		//this method parses Common.cfg
		String currLine = null;
		String parts[] = null;
		BufferedReader config = new BufferedReader(new FileReader("/Users/joeysiracusa/Development/networking-project/src/bitTorrentPkg/Common.cfg"));
		currLine = config.readLine(); //gets the line in a string
		parts = currLine.split(" "); //splits the line at the space
		numOfPrefNeighbors = Integer.parseInt(parts[1]); //reads the value after the space
		currLine = config.readLine(); //repeat for all variables in config file
		parts = currLine.split(" ");
		unchokingInterval = Integer.parseInt(parts[1]);
		currLine = config.readLine();
		parts = currLine.split(" ");
		optUnchokingInterval = Integer.parseInt(parts[1]);
		currLine = config.readLine();
		parts = currLine.split(" ");
		fileName = parts[1];
		currLine = config.readLine();
		parts = currLine.split(" ");
		fileSize = Integer.parseInt(parts[1]);
		currLine = config.readLine();
		parts = currLine.split(" ");
		pieceSize = Integer.parseInt(parts[1]);
		config.close(); //close the config file
		
		//reading from Common.cfg is complete, now finish calculations
		numOfPieces = (fileSize + pieceSize - 1) / pieceSize; //integer division which rounds up
		
	}
	
	public void readPeerInfo() throws IOException{
		
	}
	
	public static void printConfig(){
		//mainly for debugging- this can be converted into a log later maybe?
		System.out.println("NumberOfPrefferedNeighbors " + numOfPrefNeighbors);
		System.out.println("UnchokingInterval " + unchokingInterval);
		System.out.println("OptimisticUnchokingInterval " + optUnchokingInterval);
		System.out.println("FileName " + fileName);
		System.out.println("FileSize " + fileSize);
	}
	
	
	
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		readCommon();
		printConfig();

	}

}
