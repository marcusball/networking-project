package bitTorrentPkg;

import java.io.IOException;
import java.util.*;

public class NeighborController {
	public static Host host; 
	
	//ArrayList of peers and upload status
	public static ArrayList<Peer> peers;
	public static ArrayList<Peer> unchokedPeers;
	
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
		peers = new ArrayList<Peer>();
		
		unchokingInterval = (long) host.getUnchokingInterval();
		optUnchokingInterval = (long) host.getOptUnchokingInterval();
		
		changeNeighbors = new Timer();
		changeOptUnchoked = new Timer();
		changeNeighbors.schedule(new NeighborController().new Unchoke(), unchokingInterval*1000);
		changeOptUnchoked.schedule(new NeighborController().new OptimisticUnchoke(), optUnchokingInterval*1000);	
		//create the timers and add tasks to them at their respective intervals
	}
	
	public static void addPeer(Peer other){
		peers.add(other);
	}
	
	public static boolean hasPeer(int id){
		for(int i = 0; i < peers.size(); i++){
			if(peers.get(i).getPeerID() == id){
				return true;
			}
		}
		return false;
	}
	
	public static Peer getPeer(int id){
		for(int i = 0; i < peers.size(); i++){
			if(peers.get(i).getPeerID() == id){
				return peers.get(i);
			}
		}
		return null;
	}
	
	public static ArrayList<Peer> getPeers(){
		return peers;
	}
	
	class Unchoke extends TimerTask{
		public void run(){
			if(peers.size() >= 1){
				LinkedList<Peer> peersToUnchoke = new LinkedList<Peer>();
				if(!host.hasFile()){
					//first fill peersToUnchoke with peers until it has numOfPrefNeighbors
					int count = 0;
					while(peersToUnchoke.size() < host.getNumOfPrefNeighbors()){
						peersToUnchoke.add(peers.get(count));
						count++;
					}
					
					//for each peer, find the min dl speed in peersToUnchoke and compare
						//if peer has greater download speed then the min, replace the peer in peersToUnchoke with the curr peer
					//TODO: This is a loop in a loop (O(n^2)).  We shouldn't have a large enough n for it to cause problems, but
						//if we ever get performance issues, check this loop out, maybe optimize
					for(int i = 0; i < peers.size(); i++){
						int j = 0;
						float min = peersToUnchoke.get(0).getDLRate();
						int minIndex = 0;
						while(j < host.getNumOfPrefNeighbors()){
							//test this peerToUnchoke against the min
							if(peersToUnchoke.get(j).getDLRate() < min){
								//if it's less than the min, save the position and overwrite min
								min = peersToUnchoke.get(j).getDLRate();
								minIndex = j;
							}
							j++;
						}
						//now that min has been calculated, compare this peer against it
						if(peers.get(i).getDLRate() > min){
							//if the curr peer is faster, replace the min peer with the curr peer
							peersToUnchoke.remove(minIndex);
							peersToUnchoke.add(peers.get(i));
						}
					}
				}else{
					//if the host has the file already, randomly select peers to unchoke
					for(int i = 0; i < peers.size(); i++){
						int randIndex = new Random(System.currentTimeMillis()).nextInt(peers.size());
						peersToUnchoke.add(peers.get(randIndex));
					}
					
				}
				//now that peers to unchoke have been found, reiterate through peers and peersToUnchoke, sending choke and unchoke messages when necessary
					//also change the state of "boolean unchoked" in the Peer object
				
				for(int i = 0; i < peers.size(); i++){
					//for each peer...
					if(peers.get(i).isUnchoked()){
						//if this peer is already unchoked, see if you need to send a choke message
						boolean sendChoked = true;
						for(int j = 0; j < peersToUnchoke.size(); j++){
							if(peers.get(i).getPeerID() == peersToUnchoke.get(j).getPeerID()){
								//if we find that our peer is in peersToUnchoke, then we keep it unchoked
								//so we don't send a choked message
								sendChoked = false;
							}
						}
						if(sendChoked){
							//the peer has been switched from unchoked to choked\
							peers.get(i).choke();
						}
					}else{
						//if this peer is choked, see if we need to unchoke it
						boolean sendUnchoked = false;
						for(int j = 0; j < peersToUnchoke.size(); j++){
							if(peers.get(i).getPeerID() == peersToUnchoke.get(j).getPeerID()){
								//if we find that our peer is in peersToUnchoke, then we keep it unchoked
								//so we don't send a choked message
								sendUnchoked = true;
							}
						}
						if(sendUnchoked){
							//the peer has been switched from choked to unchoked
							peers.get(i).unchoke();
						}
					}
					//if the peer was unchoked and remains unchoked
						//or if the peer was choked and remains choked
						//then don't do anything
					
				}
			}
			
		}
	}
	
	class OptimisticUnchoke extends TimerTask{
		public void run(){
			int randIndex;
			if(peers.size() >= 1){ //Make sure we actually have peers. 
				//get a random index
				randIndex = new Random(System.currentTimeMillis()).nextInt(peers.size());
				//choke the current opt neighbor and unchoke the next opt neighbor
				peers.get(optimisticPeerIndex).optChoke();
				optimisticPeerIndex = randIndex;
				peers.get(optimisticPeerIndex).optUnchoke();
			}
			
		}
	}
}
