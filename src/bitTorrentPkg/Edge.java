package bitTorrentPkg;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import bitTorrentPkg.Messages.Bitfield;
import bitTorrentPkg.Messages.Choke;
import bitTorrentPkg.Messages.Have;
import bitTorrentPkg.Messages.Interested;
import bitTorrentPkg.Messages.NormalMessage;
import bitTorrentPkg.Messages.NotInterested;
import bitTorrentPkg.Messages.Request;
import bitTorrentPkg.Messages.Unchoke;

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
	
	public NormalMessage listen() throws IOException{
		sender = receiver.accept();
		receiver.bind(sender.getLocalSocketAddress());
		
		return null;
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
		
		PrintWriter out = new PrintWriter(sender.getOutputStream());
		out.print(handshake);
		out.close();
	}
	
	public void sendChoke() throws IOException{
		Choke choke = new Choke();
		PrintWriter out = new PrintWriter(sender.getOutputStream());
		out.print(choke.toBytes());
		out.close();
	}
	
	public void sendUnchoke() throws IOException{
		Unchoke unchoke = new Unchoke();
		PrintWriter out = new PrintWriter(sender.getOutputStream());
		out.print(unchoke.toBytes());
		out.close();
	}
	
	public void sendInterested() throws IOException{
		Interested interested = new Interested();
		PrintWriter out = new PrintWriter(sender.getOutputStream());
		out.print(interested.toBytes());
		out.close();
	}
	
	public void sendNotInterested() throws IOException{
		NotInterested notinterested = new NotInterested();
		PrintWriter out = new PrintWriter(sender.getOutputStream());
		out.print(notinterested.toBytes());
		out.close();
	}
	
	public void sendHave(int index) throws IOException{
		Have have = new Have(index);
		PrintWriter out = new PrintWriter(sender.getOutputStream());
		out.print(have.toBytes());
		out.close();
	}
	
	public void sendBitfield(byte bitfield[]) throws IOException{
		Bitfield thebitfield = new Bitfield(bitfield);
		PrintWriter out = new PrintWriter(sender.getOutputStream());
		out.print(thebitfield.toBytes());
		out.close();
	}
	
	public void sendRequest(int index) throws IOException{
		Request request = new Request(index);
		PrintWriter out = new PrintWriter(sender.getOutputStream());
		out.print(request.toBytes());
		out.close();
	}
	
	
}
