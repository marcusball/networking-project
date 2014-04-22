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
import java.util.Scanner;










//Messages
import bitTorrentPkg.Messages.*;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class Edge extends Thread {
	private Peer destination;
	
	private Socket client;
	private InputStream in;
	private OutputStream out;
	
	private final AtomicBoolean hasReceivedHandshake = new AtomicBoolean(false);
	private final AtomicBoolean hasReceivedMessage = new AtomicBoolean(false);
	private Message lastMessage;
	private boolean hasSentHandshake = false;
	
	private int edgeState = 0;
	
	private final int EDGE_RECV_HANDSHAKE = 1;
	private final int EDGE_SENT_HANDSHAKE = 2;
	private final int EDGE_RECV_BITFIELD  = 4;
	private final int EDGE_SENT_BITFIELD  = 8;
	private final int EDGE_RECV_CHOKE = 16;
	private final int EDGE_SENT_CHOKE = 32;
	private final int EDGE_RECV_UNCHOKE = 64;
	private final int EDGE_SENT_UNCHOKE = 128;
	private final int EDGE_RECV_INTERESTED = 256;
	private final int EDGE_SENT_INTERESTED = 512;
	private final int EDGE_RECV_NOTINTERESTED = 1024;
	private final int EDGE_SENT_NOTINTERESTED = 2048;
	private final int EDGE_RECV_HAVE = 4096;
	private final int EDGE_SENT_HAVE = 8192;
	private final int EDGE_RECV_PIECE = 16384;
	private final int EDGE_SENT_PIECE = 32768;
	
	
	public Edge() throws IOException{
		this(null);
	}
	public Edge(Peer destinationPeer) throws IOException{
		this.destination = destinationPeer;
		this.lastMessage = null;
		this.edgeState = 0;
	}
	
	public void setDestination(Peer dest){
		this.destination = dest;
	}
	
	public void setClientSocket(Socket s) throws IOException{
		this.client = s;
		this.in = this.client.getInputStream();
		this.out = this.client.getOutputStream();
		
		Tools.debug("Edge socket set for %s.",s.getRemoteSocketAddress().toString());
	}
	public void createClientSocket() throws UnknownHostException, IOException, NullPointerException{
		if(this.destination == null){
			throw new NullPointerException("Cannot create client socket with null destination peer!");
		}
		Tools.debug("Creating socket to host: %s:%d",this.destination.getHostName(), this.destination.getListeningPort());
		Socket s = new Socket(this.destination.getHostName(), this.destination.getListeningPort());
		Tools.debug("Socket created!");
		
		this.setClientSocket(s);
	}
	
	public boolean sendMessage(Message message){
		try {
			this.out.write(message.toBytes());
			this.out.flush();
			return true;
		} 
		catch (IOException e) {
			Tools.debug("Unable to send message to %s! IOException occurred: \"%s\".",this.destination.getHostName(),e.getMessage());
		}
		return false;
	}
	
	public void sendHandshake(){
		this.edgeState |= EDGE_SENT_HANDSHAKE;
		
		Handshake handshake = new Handshake(NeighborController.host.getPeerID());
		this.sendMessage(handshake);
	}
	public void sendBitfield(){
		this.edgeState |= EDGE_SENT_BITFIELD;
		
		BitfieldMessage bitfield = new BitfieldMessage(NeighborController.host.getBitfield());
		this.sendMessage(bitfield);
	}
	
	public void run(){
		try{
			byte[] buffer;
			int bytesRead;
			Tools.debug("Edge: Now listening for responses...");
			while(true){
				if(!this.client.isConnected()){
					Tools.debug("Edge: connection terminated!");
					break;
				}
				
				if(this.in.available() > 0){
					buffer = new byte[(int) (5 + NeighborController.host.pieceSize())]; //This is the maximum length any message will ever take.
					bytesRead = this.in.read(buffer);
	
					buffer = Arrays.copyOfRange(buffer, 0, bytesRead); //Trim off the excess buffer space.
					
					System.out.print("Received: ");
					for(byte b : buffer){
						System.out.printf("%2x ",b);
					}
					System.out.println();
					
					Message received = MessageReceiver.OpenMessageBytes(buffer);
					this.handleMessage(received);
				}
			}
		}
		catch(IOException ioe){
			Tools.debug("Edge exception checking for incoming messages! IOException: \"%s\".",ioe.getMessage());
			ioe.printStackTrace();
		}
		catch(Exception e){
			Tools.debug("Edge exception while checking for incoming messages! Exception: \"%s\".",e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void handleMessage(Message received){
		this.lastMessage = received;
				
		if(received instanceof Handshake){
			Tools.debug("RECEIVED HANDSHAKE!");
			this.edgeState |= EDGE_RECV_HANDSHAKE;
			
			if((this.edgeState & EDGE_SENT_HANDSHAKE) == 0){
				this.sendHandshake();
			}
			else if((this.edgeState & EDGE_SENT_BITFIELD) == 0){
				this.sendBitfield();
			}
		}else if(received instanceof Choke){
			Tools.debug("RECEIVED CHOKE!");
			this.edgeState |= EDGE_RECV_CHOKE;
			
			

		}else if(received instanceof Unchoke){
			Tools.debug("RECEIVED UNCHOKE!");
			this.edgeState |= EDGE_RECV_UNCHOKE;
			
			
			
		}
		else if(received instanceof BitfieldMessage){
			Tools.debug("RECEIVED BITFIELD");
			this.edgeState |= EDGE_RECV_BITFIELD;
			
			BitfieldMessage bfMessage = (BitfieldMessage)received;
			if(this.destination == null){
				Tools.debug("UGGGHH FIX ME!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"); //TODO: fix
			}
			else{
				this.destination.setBitfield(bfMessage.getBitfield());
			}
			
			Tools.debug("Bitfield assigned to peer object.");
		}
	}
	
	public int blockForHandshake(){
		long startTime = System.currentTimeMillis();
		long currentTime = System.currentTimeMillis();
		while(this.hasReceivedHandshake.get() == false && (currentTime - startTime) <= 10000){
			currentTime = System.currentTimeMillis();
		}
		
		if(this.hasReceivedHandshake.get() == false){ //Timeout
			return -1; 
		}
		
		if(this.lastMessage != null){
			if(this.lastMessage instanceof Handshake){
				return ((Handshake)this.lastMessage).getPeerId();
			}
		}
		return -1;
	}
	
	public Socket getSocket(){
		return this.client;
	}

	
	
	
}
