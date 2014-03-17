package bitTorrentPkg;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.util.*;

public class Logger {
	private int peerID;
	private String logFileName;
	private int numOfLogs;
	PrintWriter writer;
	
	//LOGGER CONSTRUCTORS------------------------------------------------------------
	public Logger() throws FileNotFoundException, UnsupportedEncodingException{
		//a peer ID is necessary to create the log file
		//if one is not given, assume peerID = -1
		peerID = -1;
		logFileName = "log_peer_-1.log";
		writer = new PrintWriter(logFileName, "UTF-8");
		writer.println(new Date().toString() + ": " + peerID + " created");
	}
	
	public Logger(int peerID) throws FileNotFoundException, UnsupportedEncodingException{
		this.peerID = peerID;
		logFileName = "log_peer_" + peerID + ".log";
		writer = new PrintWriter(logFileName, "UTF-8");
	}
	
	//LOGGER METHODS------------------------------------------------------------
	//typical get methods
	public int getPeerID(){
		return peerID;
	}
	
	public String getLogFileName(){
		return logFileName;
	}
	
	public PrintWriter getWriter(){
		return writer;
	}
	
	//methods for writing to log file
	public void testLog(){
		//this log has been tested and works
		writer.println(new Date().toString() + ": Test log. Peer ID = " + peerID);
	}
	
	//TODO: All logs beneath here have not been tested, with the exception of closeLog()
	
	public void logTCPConnection(int targetPeerID){
		writer.println(new Date().toString() + ": Peer " + peerID + " makes a connection to Peer " 
						+ targetPeerID);
	}
	
	public void logChangeOfPrefNeighbors(int neighborIDs[]){
		String IDs = null;
		for(int i = 0; i < neighborIDs.length; i++){
			if(i != neighborIDs.length -1) //if it's not the last ID
				IDs = IDs + neighborIDs[i] + ","; //add a comma
			else
				IDs = IDs + neighborIDs[i]; //else no comma
		
		}
		writer.println(new Date().toString() + "Peer " + peerID + " has the preferred neighbors " + IDs);
		
	}
	
	public void logChangeOfOptUnchokedNeighbor(int neighborID){
		writer.println(new Date().toString() + "Peer " + peerID + " has optimistically unchoked neighbor "
						+ neighborID);
	}
	
	public void logUnchoking(int unchokedID){
		writer.println(new Date().toString() + "Peer " + peerID + " is unchoked by " + unchokedID);
	}
	
	public void logChoking(int chokedID){
		writer.println(new Date().toString() + "Peer " + peerID + " is choked by " + chokedID);
	}
	
	//TODO: Write methods for logging have messages, interested messages, 
	//		not interested messages, downloading a piece, and completion of download
	//		Also consider creating non-required logs for debugging in the future
	
	public void closeLog(){
		writer.println(new Date().toString() + ": Log file closed.");
		writer.close();
	}
	
}
