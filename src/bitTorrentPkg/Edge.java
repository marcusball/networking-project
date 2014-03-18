package bitTorrentPkg;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class Edge {
	private Peer origin;
	private Peer destination;
	
	boolean isServerSocket;
	boolean isNormalSocket;
	
	Socket sender;
	ServerSocket receiver;
	
	public Edge(Peer origin, Peer destination, boolean isServerSocket) throws IOException{
		this.origin = origin;
		this.destination = destination;
		this.isServerSocket = isServerSocket;
		this.isNormalSocket = !isServerSocket;
		if(isNormalSocket){
			sender = new Socket(destination.getHostName(), destination.getListeningPort());
		}
		else{
			receiver = new ServerSocket(origin.getListeningPort());
		}
		
		//this is a useless comment for testing
		
	}
	
	public void sendHandshake() {
		char[] handshake = new char[32];
		
		// First five elements in char array is HELLO
		handshake[0] = 'H';
		handshake[1] = 'E';
		handshake[2] = 'L';
		handshake[3] = 'L';
		handshake[4] = 'O';
		
		// 23 zeros
		for (int i = 0; i < handshake.length-4;i++) {
			// 23 zeros
			handshake[i] = '0';
		}
		
		for (int j = handshake.length-1; j >= handshake.length-4; j--) {
			// Last 4 in handshake message 
			handshake[j] = (char) (origin.getPeerID()%10);
		}
	}

}
