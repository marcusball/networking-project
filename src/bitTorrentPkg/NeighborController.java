package bitTorrentPkg;

import java.io.IOException;
import java.util.*;




public class NeighborController {
	private Peer client; //the peer representing this client
	
	//ArrayList of peers and upload status
	private ArrayList<Peer> peers;
	private ArrayList<Boolean> isUnchocked = new ArrayList<Boolean>();
	private int optimisticPeerIndex;
	
	//intervals and timers for unchoking
	private long unchokingInterval;
	private long optUnchokingInterval;
	private Timer changeNeighbors;
	private Timer changeOptUnchoked;
	
	
	//NeighborController constructor needs home peer to determine intervals
	public NeighborController(Peer client){
		this.client = client;
		//get the intervals from the peer
		unchokingInterval = (long) this.client.getUnchokingInterval();
		optUnchokingInterval = (long) this.client.getOptUnchokingInterval();
		//create the timers and add tasks to them at their respective intervals
		changeNeighbors = new Timer();
		changeNeighbors.schedule(new Unchoke(), unchokingInterval*1000);
		changeOptUnchoked = new Timer();
		changeOptUnchoked.schedule(new OptimisticUnchoke(), optUnchokingInterval*1000);
		
	}
	
	public void addPeer(Peer peer){
		peers.add(peer);
		//assume it's chocked to start
		isUnchocked.add(false);
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
			optimisticPeerIndex = new Random(System.currentTimeMillis()).nextInt(peers.size());
			//TODO: Send a unchoke message to peer "random" using a thread
			//		Expect to receive an request message
			try {
				client.others.get(optimisticPeerIndex).sendUnchoke();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Could not send unchoke to optimisticly unchoked peer" + client.others.get(optimisticPeerIndex).getDestination().getPeerID());
				e.printStackTrace();
			}
		}
	}
	
	
}
