package bitTorrentPkg.Messages;

public class Piece extends NormalMessage{
	public Piece(int index, byte[] data){
		this.messageType = 7;
		this.payload = new byte[4];
	
		for(int i=0;i<4;i+=1){
			this.payload[3 - i] = (byte)((index >> 8*i) & 255); 
		}
		
		this.payload = this.concat(this.payload,data);
	}
}
