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
	protected Peer destination;
	
	protected Socket client;
	protected InputStream in;
	protected OutputStream out;
	
	protected Message lastMessage;
	
	protected AtomicInteger edgeState;
	protected AtomicBoolean breakAtHandshakeRecv;
	
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
	private final int EDGE_RECV_REQUESTED_PIECE = 16384;
	private final int EDGE_SENT_PIECE = 32768;
	private final int EDGE_SENT_REQUEST = 65536;
	private final int EDGE_RECV_REQUEST = 131072;
	
	private final int EDGE_CLEAR_SENT_INTEREST = Integer.MAX_VALUE & ~(EDGE_SENT_INTERESTED | EDGE_SENT_NOTINTERESTED);
	private final int EDGE_CLEAR_RECV_CHOKE = Integer.MAX_VALUE & ~(EDGE_RECV_CHOKE);
	private final int EDGE_CLEAR_RECV_UNCHOKE = Integer.MAX_VALUE & ~(EDGE_RECV_UNCHOKE);
	private final int EDGE_CLEAR_RECV_HAVE = Integer.MAX_VALUE & ~(EDGE_RECV_HAVE);
	private final int EDGE_CLEAR_RECV_REQUEST = Integer.MAX_VALUE & ~(EDGE_RECV_REQUEST);
	private final int EDGE_CLEAR_RECV_REQUESTED_PIECE = Integer.MAX_VALUE & ~(EDGE_RECV_REQUESTED_PIECE);
	private final int EDGE_CLEAR_SENT_REQUEST = Integer.MAX_VALUE & ~(EDGE_SENT_REQUEST);
	
	private final int EDGE_GREETING_COMPLETE = 15; 
	
	int lastPieceIndex;
	
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
		this.breakAtHandshakeRecv = new AtomicBoolean(false);
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
		this.breakAtHandshakeRecv = new AtomicBoolean(false);
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
		this.breakAtHandshakeRecv = new AtomicBoolean(false);
		
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
		this.breakAtHandshakeRecv = new AtomicBoolean(false);
		Tools.debug("[Edge] Cloned Edge");
		
		if(this.destination == null){
			Tools.debug("[Edge] Destination is still null.");
		}
		
		if(this.edgeState.get() != 0){
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
			
			Thread.sleep(500);
		} 
		catch (IOException e) {
			Tools.debug("[Edge.sendMessage] Unable to send message to %s! IOException occurred: \"%s\".",this.destination.getHostName(),e.getMessage());
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Sends a handshake
	 * @throws IOException
	 */
	public void sendHandshake() throws IOException{
		Tools.debug("[Edge.sendHandshake] blocking for peer... (%s)",(this.cloned)?"Clone":"Not clone");
		synchronized(this.edgeState){
			if(this.blockForPeer()){
				Tools.debug("[Edge.sendHandshake] sending handshake...");
				this.edgeState.set(this.edgeState.get() | EDGE_SENT_HANDSHAKE);
				
				Tools.debug("[Edge.sendHandshake] edge state is now %s.",Tools.byteToBinString((byte)this.edgeState.get()));
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
	
	public void sendInterested(){
		this.edgeState.set(this.edgeState.get() | EDGE_SENT_INTERESTED);
		
		this.sendMessage(new Interested());
	}
	
	public void sendNotInterested(){
		this.edgeState.set(this.edgeState.get() | EDGE_SENT_NOTINTERESTED);
		
		this.sendMessage(new NotInterested());
	}
	
	public void sendRequest(Request message){
		this.edgeState.set(this.edgeState.get() | EDGE_SENT_REQUEST);
		
		this.sendMessage(message);
	}
	
	public void sendPiece(Piece message){
		
		this.sendMessage(message);
	}
	public void sendHaves(int pieceIndex){
		//create a have message for this piece index
		Have have = new Have(pieceIndex);
		//send it to every peer
		for(int i = 0; i < NeighborController.getPeers().size(); i++){
			NeighborController.getPeers().get(i).connection.sendMessage(have);
		}

	}
	
	/**
	 * Thread method: Perpetually listens for responses. 
	 */
	public void run(){
		try{
			if(!NeighborController.isStarted()){
				//if the timers haven't been started already, do it now
				NeighborController.startTimers();
			}
			//byte[] buffer;
			int bytesRead;
			
			final MessageReceiver receiver = new MessageReceiver();
			
			Tools.debug("[Edge.run] Now listening for responses...");
			Tools.debug("[Edge.run] State is %s.",Tools.byteToBinString((byte)this.edgeState.get()));
			while(!this.interrupted()){
				if(!this.client.isConnected()){
					Tools.debug("[Edge.run] connection terminated!");
					break;
				}
				
				if((this.edgeState.get() == this.EDGE_RECV_HANDSHAKE) && this.breakAtHandshakeRecv.get() == true){
					//This if statement requires that EDGE_RECV_HANDSHAKE be 1. 
					Tools.debug("[Edge.run] Handshake received, breaking...");
					break;
				}
				
				if(NeighborController.host.hasFile() && NeighborController.allPeersHaveFile()){
					Tools.debug("[Edge.run] All peers have the file!");
					break;
				}

				
				this.runTasks(); //Do anything that needs to be done now.
				
				if(this.in.available() > 0){ //If there are any new data
					byte[] buffer;
					
					buffer = new byte[(int) (5 + NeighborController.host.pieceSize())]; //This is the maximum length any message will ever take.
					bytesRead = this.in.read(buffer); //Read the data
					buffer = Arrays.copyOfRange(buffer, 0, bytesRead); //Trim off the excess buffer space.
					
					StringBuilder out = new StringBuilder();
					out.append("[Edge.run] Received: ");
					for(byte b : buffer){
						out.append(String.format("%2x ",b));
					}
					if(bytesRead > 800){
						Tools.debug(out.toString().substring(0, 800));
					}
					
					Message received = receiver.OpenMessageBytes(buffer);
					if(received != null){
						this.handleMessage(received);
					}
						
					while(receiver.getOffset() != 0){
						buffer = Arrays.copyOfRange(buffer, receiver.getOffset(), buffer.length);
						
						received = receiver.OpenMessageBytes(buffer);
						if(received != null){
							this.handleMessage(received);
						}
					}
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
	
	
	private void runTasks() throws IOException, Exception{
		synchronized(this.edgeState){
			int state = this.edgeState.get();
			if((state & this.EDGE_GREETING_COMPLETE) != this.EDGE_GREETING_COMPLETE){
				if((state & EDGE_RECV_HANDSHAKE) != 0){ //If we've received the handshake
					if((state & EDGE_SENT_HANDSHAKE) == 0){ //If we have not yet sent our handshake
						Tools.debug("[Edge.runTasks] Sending handshake...");
						if(this.destination == null){
							Tools.debug("[Edge.runTasks] destination is null");
						}
						
						this.sendHandshake(); //Send our handshake
					}
					else{ //If we have sent our handshake
						if((state & EDGE_SENT_BITFIELD) == 0){ //If we havent sent this host's bitfield
							if(this.destination != null){
								Tools.debug("[Edge.runTasks] Sending bitfield...");
								this.sendBitfield();
							}
						}
					}
				}
				Tools.debug("[Edge.runTasks] edgeState = %s",Tools.intToBinString(this.edgeState.get()));
			}
			else{ // OKAY: We've exchanged handshakes and bitfields, let's continue... 
				if((state & this.EDGE_SENT_INTERESTED) == 0 && (state & this.EDGE_SENT_NOTINTERESTED) == 0){
					//Tools.debug("[Edge.runTasks] Sending interested status...");
					this.sendInterestedStatus();
				}
				
				if((state & this.EDGE_RECV_UNCHOKE) ==  this.EDGE_RECV_UNCHOKE){
					//if an unchoke message has been received, send interested
					Tools.debug("[Edge.runTasks] Sending interested status in response to unchoke...");
					this.sendInterestedStatus();
					//clear the unchoke flag
					this.edgeState.set(state & this.EDGE_CLEAR_RECV_UNCHOKE);
				}
				
				//if((state & this.EDGE_RECV_UNCHOKE) != 0){ //Same condition as above, but I want to keep this logic separate for now. 
					if(!NeighborController.host.hasRequestedPieceFrom(this.destination) && NeighborController.host.hasInterestIn(this.destination)){
						Tools.debug("[Edge.runTasks] Getting ready to request a piece...");
						int requestPiece = NeighborController.host.getPieceIdToRequestFrom(this.destination);
						if(requestPiece != -1){ //If there are pieces we can actually request
							Request requestMessage = new Request(requestPiece);
							
							NeighborController.host.addRequest(this.destination.getPeerID(), requestPiece);
							Tools.debug("[Edge.runTasks] Requesting piece %d from peer %d...",requestPiece,this.destination.getPeerID());
							this.sendRequest(requestMessage);
						}
						else{
							Tools.debug("[Edge.runTasks] No pieces to request from peer %d.",this.destination.getPeerID());
						}
					}
					if((state & this.EDGE_RECV_REQUEST) != 0){
						int index = this.destination.getLastRequestedPiece();
						byte[] piece = NeighborController.host.getPiece(index);
						Piece pieceMessage = new Piece(index,piece);
						
						this.edgeState.set(this.edgeState.get() & this.EDGE_CLEAR_RECV_REQUEST);
						
						Tools.debug("[Edge.runTasks] Sending piece %d to peer %d...",index,this.destination.getPeerID());
						Tools.debug("[Edge.runTasks] Piece MD5: %s [l: %d, s: %2x e: %2x]",Tools.getMD5(piece),piece.length,piece[0],piece[piece.length - 1]);
						//FileManager.writeBytesToFile("bytes-sent.txt", pieceMessage.toBytes());
						this.sendPiece(pieceMessage);
					}
					if((state & this.EDGE_RECV_REQUESTED_PIECE) != 0){ //Received our piece but haven't sent a have
						Tools.debug("[Edge.runTasks] Received piece index " + lastPieceIndex);
						//if a piece has been received, send have messages to all peers
						sendHaves(lastPieceIndex);
						this.edgeState.set((this.edgeState.get() & this.EDGE_CLEAR_RECV_REQUESTED_PIECE) & this.EDGE_CLEAR_SENT_REQUEST);
					}
					
					if(NeighborController.host.downloadIsComplete() && !NeighborController.host.hasFile()){
						Tools.debug("[Edge.runTasks] Oh snap we're done!");
						Tools.debug("[Edge.runTasks] Merging pieces!");
						
						FileManager.combinePiecesToFile();
						Tools.debug("[Edge.runTasks] A'ight.");
						NeighborController.host.setHasFile(true);
					}
				//}

				
			}
		}
	}
	
	/**
	 * Process and perform events relevant to the message that has been received.
	 * @param received
	 * @throws IllegalArgumentException 
	 * @throws IOException 
	 */
	private void handleMessage(Message received) throws IllegalArgumentException, IOException{
		this.lastMessage = received;
			
		synchronized(this.edgeState){
			if(received instanceof Handshake){
				Tools.debug("[Edge.handleMessage] Received Handshake!");
				this.edgeState.set(this.edgeState.get() | EDGE_RECV_HANDSHAKE);
			}
			
			else if(received instanceof BitfieldMessage){
				Tools.debug("[Edge.handleMessage] Received Bitfield!");
				this.edgeState.set(this.edgeState.get() | EDGE_RECV_BITFIELD);
				this.edgeState.set(this.edgeState.get() & EDGE_CLEAR_SENT_INTEREST);
				
				BitfieldMessage bfMessage = (BitfieldMessage)received;
				if(this.destination == null){
					throw new IOException("Can't handle bitfield message as this.destination is null.");
				}
				else{
					this.destination.setBitfield(bfMessage.getBitfield());
				}
				
				Tools.debug("[Edge.handleMessage] Bitfield assigned to peer object.");
			}
			else if(received instanceof Choke){
				Tools.debug("[Edge.handleMessage] Received Choke!");
				this.edgeState.set(this.edgeState.get() | EDGE_RECV_CHOKE);
				Logger.logChoking(destination.getPeerID());
			}
			else if(received instanceof Unchoke){
				Tools.debug("[Edge.handleMessage] Received Unchoke");
				this.edgeState.set(this.edgeState.get() | EDGE_RECV_UNCHOKE);
				Logger.logUnchoking(destination.getPeerID());
			}
			else if(received instanceof Interested){
				Tools.debug("[Edge.handleMessage] Received interested from %s!",this.destination.getPeerID());
				this.edgeState.set(this.edgeState.get() | EDGE_RECV_INTERESTED);
				
				this.destination.setInterest(true);
				Logger.logInterested(destination.getPeerID());
			}
			else if(received instanceof NotInterested){
				Tools.debug("[Edge.handleMessage] Received not interested from %s!",this.destination.getPeerID());
				this.edgeState.set(this.edgeState.get() | EDGE_RECV_NOTINTERESTED);
				
				this.destination.setInterest(false);
				Logger.logNotInterested(destination.getPeerID());
			}
			else if(received instanceof Have){
				Have have = (Have)received;
				int index = have.GetPayloadValue();
				
				this.destination.setHasPiece(index,true);
				this.edgeState.set(this.edgeState.get() & this.EDGE_CLEAR_SENT_INTEREST); //Because we've just received a HAVE, we may change our interest in this peer.
				Logger.logHave(destination.getPeerID(), index);
				
				if(this.destination.getBitfield().isAll(true)){
					this.destination.setHasFile(true);
				}
			}
			else if(received instanceof Request){
				Request req = (Request)received;
				Tools.debug("[Edge.handleMessage] Received request of piece %d!",req.GetPayloadValue());
				
				this.destination.setLastRequestedPiece(req.GetPayloadValue());
				this.edgeState.set(this.edgeState.get() | this.EDGE_RECV_REQUEST);
			}
			else if(received instanceof Piece){
				Piece newPiece = (Piece)received;
				lastPieceIndex = newPiece.getIndex();
				Tools.debug("[Edge.handleMessage] Received piece %d!",newPiece.getIndex());
				Tools.debug("[Edge.handleMessage] Piece MD5: %s [l: %d, s: %2x e: %2x]",Tools.getMD5(newPiece.getData()),newPiece.getData().length,newPiece.getData()[0],newPiece.getData()[newPiece.getData().length - 1]);

				//FileManager.writeBytesToFile("message-received.txt", newPiece.toBytes());
				NeighborController.host.savePiece(newPiece);
				
				this.edgeState.set(this.edgeState.get() | EDGE_RECV_REQUESTED_PIECE);
				FileManager.writeBytesToFile("message-received.txt", newPiece.toBytes());
				Logger.logPiece(destination.getPeerID(), lastPieceIndex);
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
		
		Tools.debug("[Edge.blockForHandshake] edgeState = %s.",Tools.byteToBinString((byte)this.edgeState.get()));
		if((this.edgeState.get() & EDGE_RECV_HANDSHAKE) == 0){ //Timeout
			return -1; 
		}
		
		if(this.lastMessage != null){
			Tools.debug("[Edge.blockForHandshake] last message is NOT null");
			if(this.lastMessage instanceof Handshake){
				return ((Handshake)this.lastMessage).getPeerId();
			}
		}
		Tools.debug("[Edge.blockForHandshake] reached final return.");
		return -1;
	}
	
	/**
	 * Wait until this.destination is no longer null. 
	 * This is mostly to solve bugs with NullPointerExceptions on dependencies of this.destination. 
	 * @return
	 */
	public boolean blockForPeer(){
		Tools.debug("[Edge.blockForPeer] destination %s null.",(this.destination == null)?"is":"is not");
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

	/**
	 * Set this to true to force this edge to stop performing actions after a handshake has been received.
	 * This was created so an Edge can be cloned from this one, which will then perform everything else.
	 * @param value True or False
	 */
	public void breakAfterHandshakeReceived(boolean value){
		this.breakAtHandshakeRecv.set(value);
	}
	
	
	private void sendInterestedStatus(){
		if((this.edgeState.get() & this.EDGE_RECV_BITFIELD) != 0){
			if(NeighborController.host.hasInterestIn(this.destination)){
				Tools.debug("[Edge.sendInterestedStatus] Sending interest to peer %d!",this.destination.getPeerID());
				this.sendInterested();
			}
			else{
				Tools.debug("[Edge.sendInterestedStatus] Sending lack of interest to peer %d.",this.destination.getPeerID());
				this.sendNotInterested();
			}
		}
		else{
			Tools.debug("[Edge.sendInterestedStatus] Have not received bitfield, skipping sending of interest...");
		}
	}

	
}
