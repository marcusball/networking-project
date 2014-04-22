package bitTorrentPkg;

//io related
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;

//Socket related
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Scanner;


//Messages
import bitTorrentPkg.Messages.*;

import java.util.Arrays;

public class ServerEdge extends Thread {
	private ServerSocket server;
	
	public ServerEdge() throws IOException{
		createServerListener();
		//sendHandshake();
	}
	
	private void createServerListener() throws IOException{
		this.server = new ServerSocket(NeighborController.host.getListeningPort());
	}
	
	public void run(){
		try{
			Socket socket;
			while(true){
				Tools.debug("Waiting for connection...");
				socket = this.server.accept(); 
				Tools.debug("Connection accepted from %s.",socket.getInetAddress().getHostAddress());
				if(!this.isConnectedTo(socket)){ //Make sure we're not already connected to this peer
					Edge newEdge = new Edge();
					newEdge.setClientSocket(socket);
					newEdge.start();
					
					int peerId = newEdge.blockForHandshake();
					if(peerId == -1){ //never received handshake
						Tools.debug("No handshake received!");
						socket.close();
					}
					else if(NeighborController.hasPeer(peerId)){ //If we're already connected to the peer with this ID
						Tools.debug("Connection rejected from %s:%d; already connected to peer %d.",socket.getInetAddress().getHostAddress(),socket.getPort(),peerId);
					}
					else if(NeighborController.host.isExpectingPeerId(peerId)){ //We've received a handshake from a valid peer
						Tools.debug("Received valid handshake. Accepting peer %d.",peerId);
						
						Peer newPeer = NeighborController.host.getPeer(peerId);
						newPeer.setConnection(newEdge);
						NeighborController.addPeer(newPeer);
					}
					else{
						Tools.debug("Rejecting handshake connection from unknown peer %d.",peerId);
						socket.close();
					}
				}
				else{
					Tools.debug("Connection rejected from %s; already connected.",socket.getInetAddress().getHostAddress());
				}
			}
		}
		catch(IOException ioe){
			Tools.debug("Error while checking for incoming messages! IOException: \"%s\".",ioe.getMessage());
			Tools.debug(ioe.toString());
		}
	}
	
	private boolean isConnectedTo(Socket connection){
		Peer peer = null;
		for(Iterator<Peer> i = NeighborController.getPeers().iterator(); i.hasNext(); peer = i.next()){
			if(peer == null){
				continue;
			}
			Edge conn = peer.getConnection();
			if(conn != null){
				Socket connSocket = conn.getSocket();
				if(connSocket != null){
					if(connection.equals(connSocket)){
						return true;
					}
				}
				else{
					Tools.debug("isConnectedTo: Socket is null!");
				}
			}
			else{
				Tools.debug("isConnectedTo: Edge is null!");
			}
			
		}
		return false;
	}

	
	
}
