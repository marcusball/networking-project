package bitTorrentPkg;

import java.io.IOException;

/* peerProcess.java
 * Authors: Joey Siracusa, Marcus Ball, Anurag Komaravelli
 * 
 * This process is launched on each machine.  It reads Common.cfg and PeerInfo.cfg, initializes
 * the related variables, and has peers connect and share with each other. 
 * 
 * 
 */


public class peerProcess {
	
	
	public static void main(String[] args) throws IOException {
		int peerID0 = 1000; //normally this would be inputted from the command line
		Peer peer0 = new Peer(peerID0);
		
		if(peer0.isFirstPeer()){
			//if it is the first peer, listen for other peers attempting to connect
			peer0.listen();
		}
		
	}

}
