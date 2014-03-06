package bitTorrentPkg;

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
	
	
	public static void main(String[] args) throws IOException {
		int peerID0 = 1000; //normally this would be inputted from the command line
		Peer thisPeer0 = new Peer(peerID0);
		System.out.println("FIRST PEER: \n" + thisPeer0.toString());
		int peerID1 = 1001;
		Peer thisPeer1 = new Peer(peerID1);
		System.out.println("SECOND PEER: \n" + thisPeer1.toString());
	}

}
