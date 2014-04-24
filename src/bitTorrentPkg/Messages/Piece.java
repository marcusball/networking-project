package bitTorrentPkg.Messages;

import java.util.Arrays;
public class Piece extends NormalMessage{
	private int pieceIndex;
	
	public Piece(int index, byte[] data){
		this.messageType = 7;
		this.payload = new byte[4];
		
		this.pieceIndex = index;
	
		for(int i=0;i<4;i+=1){
			this.payload[3 - i] = (byte)((index >> 8*i) & 255); 
		}
		
		this.payload = this.concat(this.payload,data);
	}
	
	public int getIndex(){
		return this.pieceIndex;
	}
	public byte[] getData(){
		return Arrays.copyOfRange(this.payload,3,this.payload.length - 1);
	}
}
