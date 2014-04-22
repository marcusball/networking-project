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
import java.util.concurrent.atomic.*;

public class Edge extends Thread {
	protected final Peer destination;
	
	protected Socket client;
	protected InputStream in;
	protected OutputStream out;
	
	protected Message lastMessage;
	
	protected final AtomicInteger edgeState;
	
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
	
	private final int EDGE_GREETING_COMPLETE = 15; 
	
	private final boolean cloned;
	
	/**
	 * Constructors. 
	 * These all have different functionality, so choose wisely. 
	 * Once destination is set, because it's final, it can't be changed to anything new.
	 * So, Edge() is mostly used as a placeholder until the connection can be copied to
	 * a new edge using Edge(edgeToClone). 
	 */
	
	/**
	 * Creates a new edge with no destination set.
	 */
	public Edge(){
		this.destination = null;
		this.lastMessage = null;
		this.edgeState = new AtomicInteger(0);
		this.cloned = false;
	}
	
	/**
	 * Creates a new Edge with destination set,
	 * @param destinationPeer
	 * @throws IOException
	 */
	public Edge(Peer destinationPeer) throws IOException{
		this.destination = destinationPeer;
		this.lastMessage = null;
		this.edgeState = new AtomicInteger(0);
		this.cloned = false;
	}
	
	/**
	 * Creates a new edge by copying the contents of another edge. 
	 * Uses other's destination. 
	 * @param other Edge to clone
	 */
	public Edge(Edge other){
		this.cloned = true;
		this.destination = other.destination;
		
		this.client = other.client;
		this.in = other.in;
		this.out = other.out;
		
		this.lastMessage = other.lastMessage;
		
		this.edgeState = other.edgeState;
		
		if(other.isAlive()){
			this.run();
		}
	}
	
	/**
	 * Creates a new edge by copying the contents of another edge.
	 * Uses the supplied destination and ignores the supplied Edge.
	 * @param destinationPeer
	 * @param other
	 */
	public Edge(Peer destinationPeer, Edge other){
		this.cloned = true;
		this.destination = destinationPeer;
		
		this.client = other.client;
		this.in = other.in;
		this.out = other.out;
		
		this.lastMessage = other.lastMessage;
		this.edgeState = other.edgeState;
		Tools.debug("[Edge] Cloned Edge");
		
		if(this.destination == null){
			Tools.debug("[Edge] Destination is still null.");
		}
		
		if(other.isAlive()){
			Tools.debug("[Edge] Running new Edge...");
			this.start();
		}
	}
	
	
	/**
	 * Let's create a connection based on an already existing socket.
	 * @param s Socket to use for this Edge.
	 * @throws IOException
	 */
	public void setClientSocket(Socket s) throws IOException{
		this.client = s;
		this.in = this.client.getInputStream();
		this.out = this.client.getOutputStream();
		
		Tools.debug("[Edge.setClientEdge] Edge socket set for %s.",s.getRemoteSocketAddress().toString());
	}
	
	/**
	 * Create our own socket for this connection. Requires a destination be set in the constructor. 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws NullPointerException
	 */
	public void createClientSocket() throws UnknownHostException, IOException, NullPointerException{
		if(this.destination == null){
			throw new NullPointerException("[Edge.createClientSocket] Cannot create client socket with null destination peer!");
		}
		Tools.debug("[Edge.createClientSocket] Creating socket to host: %s:%d",this.destination.getHostName(), this.destination.getListeningPort());
		Socket s = new Socket(this.destination.getHostName(), this.destination.getListeningPort());
		Tools.debug("[Edge.createClientSocket] Socket created!");
		
		this.setClientSocket(s);
	}
	
	/**
	 * Send a message through the current edge
	 * @param message
	 * @return
	 */
	public boolean sendMessage(Message message){
		try {
			this.out.write(message.toBytes());
			this.out.flush();
			return true;
		} 
		catch (IOException e) {
			Tools.debug("[Edge.sendMessage] Unable to send message to %s! IOException occurred: \"%s\".",this.destination.getHostName(),e.getMessage());
		}
		return false;
	}
	
	/**
	 * Sends a handshake
	 * @throws IOException
	 */
	public void sendHandshake() throws IOException{
		Tools.debug("[Edge.sendHandshake] blocking for peer...");
		synchronized(this.edgeState){
			if(this.blockForPeer()){
					this.edgeState.set(this.edgeState.get() | EDGE_SENT_HANDSHAKE);
					
					Handshake handshake = new Handshake(NeighborController.host.getPeerID());
					this.sendMessage(handshake);
				}
				else{
					Tools.debug("[Edge.sendHandshake] %s",(this.cloned)?"is clone":"is not clone");
					throw new IOException("[Edge.sendHandshake] Unable to find destination peer!");
				}
			}
		}
	
	/**
	 * Sends the bitfield this host possesses (NeighborController.host.getBitfield()).
	 */
	public void sendBitfield(){
		this.edgeState.set(this.edgeState.get() | EDGE_SENT_BITFIELD);
		
		BitfieldMessage bitfield = new BitfieldMessage(NeighborController.host.getBitfield());
		this.sendMessage(bitfield);
	}
	
	/**
	 * Thread method: Perpetually listens for responses. 
	 */
	public void run(){
		try{
			byte[] buffer;
			int bytesRead;
			Tools.debug("[Edge.run] Now listening for responses...");
			Tools.debug("[Edge.run] State is %s.",Tools.byteToBinString((byte)this.edgeState.get()));
			while(!this.interrupted()){
				if(!this.client.isConnected()){
					Tools.debug("[Edge.run] connection terminated!");
					break;
				}
				
				this.runTasks(); //Do anything that needs to be done now.
				
				if(this.in.available() > 0){ //If there are any new data
					buffer = new byte[(int) (5 + NeighborController.host.pieceSize())]; //This is the maximum length any message will ever take.
					bytesRead = this.in.read(buffer); //Read the data
					buffer = Arrays.copyOfRange(buffer, 0, bytesRead); //Trim off the excess buffer space.
					
					System.out.print("[Edge.run] Received: ");
					for(byte b : buffer){
						System.out.printf("%2x ",b);
					}
					System.out.println();
					
					Message received = MessageReceiver.OpenMessageBytes(buffer);
					this.handleMessage(received);
				}
			}
			Tools.debug("[Edge.run] Loop stopping...");
		}
		catch(IOException ioe){
			Tools.debug("[Edge.run] Edge exception checking for incoming messages! IOException: \"%s\".",ioe.getMessage());
			ioe.printStackTrace();
		}
		catch(Exception e){
			Tools.debug("[Edge.run] Edge exception while checking for incoming messages! Exception: \"%s\".",e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	private void runTasks() throws IOException{
		synchronized(this.edgeState){
			int state = this.edgeState.get();
			if((state & this.EDGE_GREETING_COMPLETE) != this.EDGE_GREETING_COMPLETE){
				if((state & EDGE_RECV_HANDSHAKE) != 0){ //If we've received the handshake
					if((state & EDGE_SENT_HANDSHAKE) == 0){ //If we have not yet sent our handshake
						Tools.debug("[Edge.runTasks] Sending handshake!");
						if(this.destination == null){
							Tools.debug("[Edge.runTasks] destination is null");
						}
						
						this.sendHandshake(); //Send our handshake
					}
					else{ //If we have sent our handshake
						if((state & EDGE_RECV_BITFIELD) != 0){ //If we've received the client's bitfield
							if((state & EDGE_SENT_BITFIELD) == 0){ //but, we haven't sent our bitfield
								if(this.destination != null){
									this.sendBitfield();
								}
							}
						}
					}
				}
			}
			else{
				
			}
		}
	}
	
	/**
	 * Process and perform events relevant to the message that has been received.
	 * @param received
	 * @throws IOException
	 */
	private void handleMessage(Message received) throws IOException{
		this.lastMessage = received;
			
		synchronized(this.edgeState){
			if(received instanceof Handshake){
				Tools.debug("[Edge.handleMessage] RECEIVED HANDSHAKE!");
				this.edgeState.set(this.edgeState.get() | EDGE_RECV_HANDSHAKE);
	//			if((this.edgeState.get() & EDGE_SENT_HANDSHAKE) == 0){
	//				this.sendHandshake();
	//			}
	//			else if((this.edgeState.get() & EDGE_SENT_BITFIELD) == 0){
	//				this.sendBitfield();
	//			}
			}
			else if(received instanceof BitfieldMessage){
				Tools.debug("[Edge.handleMessage] RECEIVED BITFIELD");
				this.edgeState.set(this.edgeState.get() | EDGE_RECV_BITFIELD);
				
				BitfieldMessage bfMessage = (BitfieldMessage)received;
				if(this.destination == null){
					Tools.debug("[Edge.handleMessage] UGGGHH FIX ME!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"); //TODO: fix
				}
				else{
					this.destination.setBitfield(bfMessage.getBitfield());
				}
				
				Tools.debug("[Edge.handleMessage] Bitfield assigned to peer object.");
			}
			else if(received instanceof Choke){
				Tools.debug("RECEIVED CHOKE!");
				this.edgeState.set(this.edgeState.get() | EDGE_RECV_CHOKE);
			}
			else if(received instanceof Unchoke){
				Tools.debug("RECEIVED UNCHOKE!");
				this.edgeState.set(this.edgeState.get() | EDGE_RECV_UNCHOKE);
			}
		}
	}
	
	/**
	 * Pause this thread until we receive a handshake from the client. 
	 * @return Peer ID of the client, taken from the handshake. 
	 */
	public int blockForHandshake(){
		long startTime = System.currentTimeMillis();
		long currentTime = System.currentTimeMillis();
		while((this.edgeState.get() & EDGE_RECV_HANDSHAKE) == 0 && (currentTime - startTime) <= 10000){
			currentTime = System.currentTimeMillis();
		}
		
		Tools.debug("[BlockForHandshake] edgeState = %s.",Tools.byteToBinString((byte)this.edgeState.get()));
		if((this.edgeState.get() & EDGE_RECV_HANDSHAKE) == 0){ //Timeout
			return -1; 
		}
		
		if(this.lastMessage != null){
			Tools.debug("[BlockForHandshake] last message is NOT null");
			if(this.lastMessage instanceof Handshake){
				return ((Handshake)this.lastMessage).getPeerId();
			}
		}
		Tools.debug("[BlockForHandshake] reached final return.");
		return -1;
	}
	
	/**
	 * Wait until this.destination is no longer null. 
	 * This is mostly to solve bugs with NullPointerExceptions on dependencies of this.destination. 
	 * @return
	 */
	public boolean blockForPeer(){
		long startTime = System.currentTimeMillis();
		long currentTime = System.currentTimeMillis();
		while(this.destination == null && (currentTime - startTime) <= 10000){ //Wait until destination is not null, or the loop times out. 
			currentTime = System.currentTimeMillis();
		}
		
		return !(this.destination == null);
	}
	
	public Socket getSocket(){
		return this.client;
	}

	
	
	
}
