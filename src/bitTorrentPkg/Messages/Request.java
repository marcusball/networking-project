package bitTorrentPkg.Messages;

public class Request extends NormalMessage {
	public Request(int requestIndex){
		this.messageType = 6;
		
		this.payload = new byte[4];

		for(int i=0;i<4;i+=1){
			this.payload[3 - i] = (byte)((requestIndex >> 8*i) & 255); 
		}
	}
}
