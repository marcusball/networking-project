package bitTorrentPkg;

public class RequestedPiece {
	protected long timestamp; //time piece was requested
	protected int pieceIndex; //piece index that was requested
	protected int peerID; //peerID this piece was requested from
	
	public RequestedPiece(int pieceIndex, int peerID){
		this.pieceIndex = pieceIndex;
		this.peerID = peerID;
		timestamp = System.currentTimeMillis();
	}
	
	public long getTimestamp(){
		return timestamp;
	}
	
	public int getPieceIndex(){
		return pieceIndex;
	}
	
	public int getPeerID(){
		return peerID;
	}
}
