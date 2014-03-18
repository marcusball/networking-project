package bitTorrentPkg.Messages;

public class Have extends NormalMessage{
	public Have(int index){
		this.messageType = 4;
		this.payload = new byte[4];

		for(int i=0;i<4;i+=1){
			this.payload[3 - i] = (byte)((index >> 8*i) & 255); 
		}
	}
}
