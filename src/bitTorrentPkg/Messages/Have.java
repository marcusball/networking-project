package bitTorrentPkg.Messages;

import bitTorrentPkg.Tools;

public class Have extends NormalMessage{
	public Have(int index){
		this.messageType = 4;
		this.payload = new byte[4];

		for(int i=0;i<4;i+=1){
			this.payload[3 - i] = (byte)((index >> 8*i) & 255); 
		}
	}
	
	public Have(byte[] index) throws Exception{
		if(index.length != 4){ //We're expecting an int
			String f = String.format("Did not receive expected 4-byte input! Received input of length %d.",index.length);
			throw new Exception(f);
		}
		this.payload = index;
	}
	public int GetPayloadValue() throws Exception{
		if(this.payload.length != 4){
			throw new Exception(String.format("Expected payload to be 4 bytes! Payload was %d bytes.",this.payload.length));
		}
		int pieceId = Tools.bytesToInt(this.payload);
		return pieceId;
	}
}
