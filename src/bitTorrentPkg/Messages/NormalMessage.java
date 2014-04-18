package bitTorrentPkg.Messages;

public class NormalMessage implements Message {
	public byte messageType;
	public byte[] payload;
	
	public byte[] toBytes(){
		byte[] output = new byte[5]; //Instantiate the minimum length of the array
		if(this.payload != null && this.payload.length > 0){
			int payloadLength = this.payload.length;
			output[0] = (byte)((payloadLength >> 24) & 255); // most significant 8-bits
			output[1] = (byte)((payloadLength >> 16) & 255); // second most significant 8-bits
			output[2] = (byte)((payloadLength >> 8) & 255); // next 8-bits
			output[3] = (byte)((payloadLength) & 255); // least significant 8-bits
		}
		else{
			output[0] = output[1] = output[2] = output[3] = (byte)0;
		}
		output[4] = this.messageType;
		if(this.payload != null && this.payload.length > 0){
			output = concat(output,this.payload);
		}
		
		return output;
	}
	
	protected byte[] concat(byte[] A, byte[] B) {
	   byte[] C= new byte[A.length+B.length];
	   System.arraycopy(A, 0, C, 0, A.length);
	   System.arraycopy(B, 0, C, A.length, B.length);
	   return C;
	}
}
