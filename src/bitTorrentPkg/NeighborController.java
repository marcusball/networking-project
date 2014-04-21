package bitTorrentPkg;

import java.io.IOException;
import java.util.*;

public class NeighborController {
	public static Host host; 
	
	//ArrayList of peers and upload status
	public static HashMap<Integer,Peer> peers;
	
	private static int optimisticPeerIndex;
	
	//intervals and timers for unchoking
	private static long unchokingInterval;
	private static long optUnchokingInterval;
	private static Timer changeNeighbors;
	private static Timer changeOptUnchoked;
	
	//private long startTime;

	
	public static void setHost(Host h){
		host = h;
	}
	public static void init(){
		if(host == null){
			throw new NullPointerException("Host is null! You must call setHost(Host h) before you can call init()!");
		}
		peers = new HashMap<Integer, Peer>();
		
		unchokingInterval = (long) host.getUnchokingInterval();
		optUnchokingInterval = (long) host.getOptUnchokingInterval();
		
		changeNeighbors = new Timer();
		changeOptUnchoked = new Timer();
		changeNeighbors.schedule(new NeighborController().new Unchoke(), unchokingInterval*1000);
		changeOptUnchoked.schedule(new NeighborController().new OptimisticUnchoke(), optUnchokingInterval*1000);	
		//create the timers and add tasks to them at their respective intervals
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
	
	public static Collection<Peer> getPeers(){
		return peers.values();
	}
	
	class Unchoke extends TimerTask{
		public void run(){
			//TODO: Calculate download rates of all peers 
			//		Then unchoke the peers with the highest rates 
			//		Only unchoke if the peer is interested
			
		}
	}
	
	class OptimisticUnchoke extends TimerTask{
		public void run(){
			if(peers.size() >= 1){ //Make sure we actually have peers. 
				optimisticPeerIndex = new Random(System.currentTimeMillis()).nextInt(peers.size());
			}
			//TODO: Send a unchoke message to peer "random" using a thread
			//		Expect to receive an request message
			
		}
	}
}
