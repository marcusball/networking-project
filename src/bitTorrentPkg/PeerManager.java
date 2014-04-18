package bitTorrentPkg;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class PeerManager {
	public static Peer self;
	public static HashMap<Integer,Peer> peers;
	
	public PeerManager(){
		peers = new HashMap<Integer,Peer>();
	}
	
	public static void addPeer(Peer other){
		peers.put(other.getPeerID(), other);
	}
	
	public static boolean hasPeer(int id){
		return peers.containsKey(id);
	}
	
	public static Peer getPeer(int id){
		if(hasPeer(id)){
			return peers.get(id);
		}
		return null;
	}
}
