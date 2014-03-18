package bitTorrentPkg.Messages;

public class Bitfield extends NormalMessage{
	public Bitfield(byte[] bitfield){
		this.messageType = 5;
		this.payload = bitfield;
	}
}
