package bitTorrentPkg;

//io related
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.io.IOException;

//Socket related
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.InetSocketAddress;

//Messages
import bitTorrentPkg.Messages.BitfieldMessage;
import bitTorrentPkg.Messages.Choke;
import bitTorrentPkg.Messages.Handshake;
import bitTorrentPkg.Messages.Have;
import bitTorrentPkg.Messages.Interested;
import bitTorrentPkg.Messages.NormalMessage;
import bitTorrentPkg.Messages.NotInterested;
import bitTorrentPkg.Messages.Request;
import bitTorrentPkg.Messages.Unchoke;

public class Edge {
	private Peer origin;
	private Peer destination;
	
	Socket socket;
	ServerSocket listener;
	
	DataInputStream in;
	PrintWriter out;
	
	public Edge(Peer origin) throws IOException, SocketTimeoutException{
		//if an edge is created without a destination, listen for one
		listener = new ServerSocket(origin.getListeningPort());
		socket = listener.accept(); 
		in = new DataInputStream(socket.getInputStream());
		out = new PrintWriter(socket.getOutputStream());
		byte[] handshake = getHandshake();
		byte[] compare = "HELLO".getBytes();
		boolean same = true;
		for(int i = 0; i < 5; i++){
			same = (handshake[i] == compare[i]);
		}
		
		
	}
	
	public Edge(Peer origin, Peer destination) throws IOException{
		this.origin = origin;
		this.destination = destination;
		socket = new Socket(this.destination.getHostName(), this.destination.getListeningPort());	
		in = new DataInputStream(socket.getInputStream());
		out = new PrintWriter(socket.getOutputStream());
		sendHandshake(true);
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
	
	public void sendHandshake() throws IOException {
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
		
		out.print(handshake);
	}
	
	public void sendHandshake(boolean newHandShake) throws IOException{
		if(newHandShake){
			Handshake handshake = new Handshake(this.origin.getPeerID());
			out.print(handshake.toBytes());
		}
		else{
			this.sendHandshake();
		}	
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
	}
	
	
}
