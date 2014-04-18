package bitTorrentPkg.Messages;

import java.util.Arrays;

import bitTorrentPkg.Tools;
public class Handshake implements Message {
	private int peerId;
	public Handshake(int peerID){
		this.peerId = peerID;
	}
	public Handshake(byte[] message) throws Exception{
		if(message.length != 32){
			throw new Exception("Handshake message class expects byte array of length 32!");
		}
		
		byte[] peerIdBytes = Arrays.copyOfRange(message, 27, 31);
		this.peerId = Tools.bytesToInt(peerIdBytes);
	}
	
	public byte[] toBytes() {
		byte[] output = new byte[32];
		byte[] header = "HELLO".getBytes();
		for(int i=0;i<header.length;i+=1){
			output[i] = header[i];
		}
		
		for(int i=0;i<4;i+=1){
			output[output.length - i - 1] = (byte)((this.peerId >> 8*i) & 255); 
		}
		return output;
	}
	public int getPeerId(){
		return this.peerId;
	}
}
