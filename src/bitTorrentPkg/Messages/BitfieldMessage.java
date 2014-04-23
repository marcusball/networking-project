package bitTorrentPkg.Messages;

import bitTorrentPkg.Bitfield;

public class BitfieldMessage extends NormalMessage{
	private long bitfieldLength;
	public BitfieldMessage(byte[] bitfield){
		this.messageType = 5;
		this.payload = bitfield;
		this.bitfieldLength = -1; //Yeah, I'm sorry. I hate magic numbers too. 
	}
	public BitfieldMessage(byte[] bitfield,long length){
		this.messageType = 5;
		this.payload = bitfield;
		this.bitfieldLength = length;
	}
	public BitfieldMessage(Bitfield bitfield){
		this.messageType = 5;
		this.bitfieldLength = bitfield.getLength();
		this.payload = bitfield.toBytes();
	}
	
	public Bitfield getBitfield(){
		if(this.bitfieldLength < 0){
			return new Bitfield(this.payload,this.bitfieldLength);
		}
		else{
			return new Bitfield(this.payload);
		}
	}
}
