package bitTorrentPkg.Messages;

import bitTorrentPkg.Bitfield;

public class BitfieldMessage extends NormalMessage{
	
	public BitfieldMessage(byte[] bitfield){
		this.messageType = 5;
		this.payload = bitfield;
	}
	public BitfieldMessage(Bitfield bitfield){
		this.messageType = 5;
		this.payload = bitfield.toBytes();
	}
	
	public Bitfield getBitfield(){
		return new Bitfield(this.payload);
	}
}
