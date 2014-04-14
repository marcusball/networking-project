package bitTorrentPkg.Messages;

public class Request extends NormalMessage {
	public Request(int requestIndex){
		this.messageType = 6;
		
		this.payload = new byte[4];

		for(int i=0;i<4;i+=1){
			this.payload[3 - i] = (byte)((requestIndex >> 8*i) & 255); 
		}
	}
	public Request(byte[] requestIndexBytes) throws Exception{
		if(requestIndexBytes.length != 4){ //We're expecting an int
			String f = String.format("Did not receive expected 4-byte input! Received input of length %d.",requestIndexBytes.length);
			throw new Exception(f);
		}
		this.payload = requestIndexBytes;
	}
	public int GetPayloadValue() throws Exception{
		if(this.payload.length != 4){
			throw new Exception(String.format("Expected payload to be 4 bytes! Payload was %d bytes.",this.payload.length));
		}
		int length = 0;
		for(int i=0;i<4;i+=1){
			length |= this.payload[i] << (8 * (3-i));
		}
		return length;
	}
}
