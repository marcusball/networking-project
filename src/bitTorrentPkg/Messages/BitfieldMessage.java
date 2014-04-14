package bitTorrentPkg.Messages;

public class BitfieldMessage extends NormalMessage{
	public BitfieldMessage(byte[] bitfield){
		this.messageType = 5;
		this.payload = bitfield;
	}
}
