package bitTorrentPkg;

import java.io.IOException;
import java.util.*;

/* peerProcess.java
 * Authors: Joey Siracusa, Marcus Ball, Anurag Komaravelli
 * 
 * This process is launched on each machine.  It reads Common.cfg and PeerInfo.cfg, initializes
 * the related variables, and has peers connect and share with each other. 
 * 
 */


public class peerProcess {
	
	
	public static void main(String[] args) throws IOException {		
		/*
		Scanner input = new Scanner(System.in);
		System.out.println("Enter a peer ID:");
		int peerID = input.nextInt();
		System.out.println(peerID);
		Host host = new Host(peerID);
		System.out.println(host.isFirstPeer());

		if(peer.isFirstPeer()){
			
		}
		*/
		Scanner input = new Scanner(System.in);
		System.out.println("Enter a peer ID:");
		int peerID = input.nextInt();
		
		System.out.println(peerID);
		Host host = new Host(peerID);
		System.out.println(host.isFirstPeer());
		
		NeighborController.setHost(host);
		NeighborController.init();
		
		ServerEdge server = new ServerEdge();
		server.start();
		if(!host.isFirstPeer()){
			host.initiateTCPConnections(); //else, initiate tcp connections with previous peers
		}
		
	}

}
