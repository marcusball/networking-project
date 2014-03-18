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
			
		
	}

}
