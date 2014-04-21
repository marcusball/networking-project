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
	private Message lastMessage;
	
	public Edge() throws IOException{
		this(null);
	}
	public Edge(Peer destinationPeer) throws IOException{
		this.destination = destinationPeer;
		this.lastMessage = null;
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
		Socket s = new Socket(this.destination.getHostName(), this.destination.getListeningPort());
		this.setClientSocket(s);
	}
	
	public boolean sendMessage(Message message){
		try {
			this.out.write(message.toBytes());
			return true;
		} 
		catch (IOException e) {
			Tools.debug("Unable to send message to %s! IOException occurred: \"%s\".",this.destination.getHostName(),e.getMessage());
		}
		return false;
	}
	
	public void sendHandshake(){
		Handshake handshake = new Handshake(NeighborController.host.getPeerID());
		this.sendMessage(handshake);
	}
	
	public void run(){
		try{
			byte[] buffer;
			int bytesRead;
			Tools.debug("%s Edge: Now listening for responses...",this.destination.getHostName());
			while(true){
				if(this.in.available() > 0){
					buffer = new byte[5 + NeighborController.host.pieceSize()]; //This is the maximum length any message will ever take.
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
			Tools.debug("Error while checking for incoming messages! IOException: \"%s\".",ioe.getMessage());
		}
		catch(Exception e){
			Tools.debug("Error while checking for incoming messages! Exception: \"%s\".",e.getMessage());
		}
	}
	
	private void handleMessage(Message received){
		if(received instanceof Handshake){
			Tools.debug("RECEIVED HANDSHAKE!");
			synchronized(this.hasReceivedHandshake){
				synchronized(this.lastMessage){
					this.hasReceivedHandshake.set(true);
					this.lastMessage = received;
				}
			}
		}
	}
	
	public int blockForHandshake(){
		synchronized(this.hasReceivedHandshake){
			long startTime = System.currentTimeMillis();
			long currentTime = System.currentTimeMillis();
			while(this.hasReceivedHandshake.get() == false && (startTime - currentTime) <= 10000){
				currentTime = System.currentTimeMillis();
			}
		}
		synchronized(this.lastMessage){
			if(this.lastMessage instanceof Handshake){
				return ((Handshake)this.lastMessage).getPeerId();
			}
		}
		return -1;
	}
	
	public Socket getSocket(){
		return this.client;
	}
	/*private Peer origin;
	private Peer destination;
	
	Socket socket;
	ServerSocket listener;
	
	DataInputStream in;
	PrintWriter out;
	
	public Edge(Peer origin) throws IOException, SocketTimeoutException{
		this.origin = origin;
		//if an edge is created without a destination, listen for one
		this.origin = origin;
		listener = new ServerSocket(origin.getListeningPort());
		Scanner input = new Scanner(System.in);
		System.out.println("Press any key to terminate");
		while(true){
			Tools.debug("Waiting for connection...");
			socket = listener.accept(); 
			Tools.debug("Connection accepted from %s.",socket.getInetAddress().getHostAddress());
			//in = new DataInputStream(socket.getInputStream());
			out = new PrintWriter(socket.getOutputStream(),true);
			InputStream inAlt = socket.getInputStream();

			while(true){
				if(inAlt.available() > 0){
					int length = inAlt.available();
					byte[] buffer = new byte[5 + this.origin.pieceSize()]; //This is the maximum length any message will ever take.
					int read = inAlt.read(buffer);
					
					buffer = Arrays.copyOfRange(buffer, 0, read); //Trim off the excess buffer space.
					
					for(byte b : buffer){
						System.out.printf("%2x ",b);
					}
					System.out.println();
					
					try {
						IMessage received = MessageReceiver.OpenMessageBytes(buffer);
						if(received instanceof Handshake){
							Tools.debug("RECEIVED HANDSHAKE!");
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
//			while((in.available() > 0)){
//				System.out.printf("%2x ",in.readByte());
//			}
		}
//		byte[] handshake = getHandshake();
//		byte[] compare = "HELLO".getBytes();
//		boolean same = true;
//		for(int i = 0; i < 5; i++){
//			same = (handshake[i] == compare[i]);
//		}
//		
		
	}
	
	public Edge(Peer origin, Peer destination) throws IOException{
		this.origin = origin;
		this.destination = destination;
		socket = new Socket(this.destination.getHostName(), this.destination.getListeningPort());	

		in = new DataInputStream(socket.getInputStream());
		out = new PrintWriter(socket.getOutputStream(),true);
		Tools.debug("Sending handshake to %s.",destination.getHostName());
		//sendHandshake(true);
		socket.getOutputStream().write((new Handshake(this.origin.getPeerID())).toBytes());
		while(true){
			Tools.debug("Received: %s",in.readUTF());
		}
	}
	
	public Peer  getOrigin(){
		return origin;
	}
	
	public Peer getDestination(){
		return destination;
	}
	
	public byte[] getHandshake() throws IOException{
		byte[] handshake = new byte[32];
		int count = 0;
		while((in.available() > 0) && (count < 32)){
			handshake[count] = in.readByte();
		}
		return handshake;
	}
	
	public byte[] getMessage() throws IOException{
		//first get message length (field is 4 bytes = 1 int)
		int length = in.readInt();
		int count = 0;
		byte[] message = new byte[4+length];
		//convert that length into bytes and put it in the bytes message array
		for (count = 0; count < 4; count++) {
		    message[count] = (byte)(length >>> (count * 8));
		}
		
		//then continue reading the buffer while count < (length+4)
		while((in.available() > 0) && (count < (length+4))){
			message[count] = in.readByte();
		}
		
		return message;
	}
	
	public void sendHandshake() throws IOException{
		Handshake handshake = new Handshake(this.origin.getPeerID());
		out.print(handshake.toBytes());
		for(byte outByte : handshake.toBytes()){
			System.out.printf("%2x ",outByte);
		}
		out.flush();	
	}
	
	public void sendChoke() throws IOException{
		Choke choke = new Choke();
		out.print(choke.toBytes());
	}
	
	public void sendUnchoke() throws IOException{
		Unchoke unchoke = new Unchoke();
		out.print(unchoke.toBytes());
	}
	
	public void sendInterested() throws IOException{
		Interested interested = new Interested();
		out.print(interested.toBytes());
	}
	
	public void sendNotInterested() throws IOException{
		NotInterested notinterested = new NotInterested();
		out.print(notinterested.toBytes());
	}
	
	public void sendHave(int index) throws IOException{
		Have have = new Have(index);
		out.print(have.toBytes());
	}
	
	public void sendBitfield(byte bitfield[]) throws IOException{
		//TODO: Fix this 
	}
	
	public void sendRequest(int index) throws IOException{
		Request request = new Request(index);
		out.print(request.toBytes());
	}*/
	
	
}
