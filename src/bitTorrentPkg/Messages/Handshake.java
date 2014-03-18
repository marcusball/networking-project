package bitTorrentPkg.Messages;

public class Handshake implements IMessage {
	private int peerId;
	public Handshake(int peerID){
		this.peerId = peerID;
	}
	
	@Override
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

}
