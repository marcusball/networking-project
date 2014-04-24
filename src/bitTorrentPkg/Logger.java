package bitTorrentPkg;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.util.*;

public class Logger {
	protected static int peerID = -1;
	protected static String logFileName;
	protected static int numOfLogs;
	protected static PrintWriter writer;
	
	public static void setPeerID(int pID) throws FileNotFoundException, UnsupportedEncodingException{
		peerID = pID;
		logFileName = "log_peer_" + peerID + ".log";
		writer = new PrintWriter(logFileName, "UTF-8");
	}
	
	//LOGGER METHODS------------------------------------------------------------
	//typical get methods
	public static int getPeerID(){
		return peerID;
	}
	
	public static String getLogFileName(){
		return logFileName;
	}
	
	public static PrintWriter getWriter(){
		return writer;
	}
	
	//methods for writing to log file
	public static void testLog(){
		if(peerID != -1){	
			//this log has been tested and works
			writer.println(new Date().toString() + ": Test log. Peer ID = " + peerID);
			writer.flush();
		}else{
			Tools.debug("[Logger] Log not set!");
		}
	
	}
	
	//TODO: All logs beneath here have not been tested, with the exception of closeLog()
	
	public static void logTCPConnectionTo(int targetPeerID){
		Tools.debug("[Logger] TCP Connection logged.");
		if(peerID != -1){	
			writer.println(new Date().toString() + ": Peer " + peerID + " makes a connection to Peer " 
						+ targetPeerID);
			writer.flush();
		}else{
			Tools.debug("[Logger] Log not set!");
		}	
	}
	
	public static void logTCPConnectionFrom(int targetPeerID){
		if(peerID != -1){	
			writer.println(new Date().toString() + ": Peer " + peerID + " makes a connection from Peer " 
						+ targetPeerID);
			writer.flush();
		}else{
			Tools.debug("[Logger] Log not set!");
		}	
	}
	
	public static void logChangeOfPrefNeighbors(int neighborIDs[]){
		if(peerID != -1){	
			String IDs = null;
			for(int i = 0; i < neighborIDs.length; i++){
				if(i != neighborIDs.length -1) //if it's not the last ID
					IDs = IDs + neighborIDs[i] + ","; //add a comma
				else
					IDs = IDs + neighborIDs[i]; //else no comma
			
			}
			writer.println(new Date().toString() + "Peer " + peerID + " has the preferred neighbors " + IDs);
			writer.flush();
		}else{
			Tools.debug("[Logger] Log not set!");
		}	
			
	}
	
	public static void logChangeOfOptUnchokedNeighbor(int neighborID){
		if(peerID != -1){	
			writer.println(new Date().toString() + ": Peer " + peerID + " has optimistically unchoked neighbor "
						+ neighborID);
			writer.flush();
		}else{
			Tools.debug("[Logger] Log not set!");
		}	
	}
	
	public static void logUnchoking(int source){
		if(peerID != -1){	
			writer.println(new Date().toString() + ": Peer " + peerID + " is unchoked by " + source);
			writer.flush();
		}else{
			Tools.debug("[Logger] Log not set!");
		}	
	}
	
	public static void logChoking(int source){
		if(peerID != -1){	
			writer.println(new Date().toString() + ": Peer " + peerID + " is choked by " + source);
			writer.flush();
		}else{
			Tools.debug("[Logger] Log not set!");
		}	
	}
	
	//TODO: Write methods for logging have messages, interested messages, 
	//		not interested messages, downloading a piece, and completion of download
	//		Also consider creating non-required logs for debugging in the future
	
	// pID is a quick way of denoting peer 2's ID
	public static void logHave(int pID, int pieceIndex) {
		if(peerID != -1){	
			writer.println(new Date().toString() + ": Peer " + peerID + " received a have message from Peer "
					+ pID + " for the piece " + pieceIndex);
			writer.flush();
		}else{
			Tools.debug("[Logger] Log not set!");
		}	
	}
	
	// Same definition of pID as above method
	public static void logInterested(int pID) {
		if(peerID != -1){	
			writer.println(new Date().toString() + ": Peer " + peerID + " received an interested message from Peer "
					+ pID);
			writer.flush();
		}else{
			Tools.debug("[Logger] Log not set!");
		}	
	}
	
	public static void logNotInterested(int pID) {
		if(peerID != -1){	
			writer.println(new Date().toString() + ": Peer " + peerID + " received a not interested message from Peer "
					+ pID);
			writer.flush();
		}else{
			Tools.debug("[Logger] Log not set!");
		}	
	}
	
	// Same pieceIndex used a few methods above; numPieces introduced for the first time
	public static void logPiece(int pID, int pieceIndex, int numPieces) {
		if(peerID != -1){	
			writer.println(new Date().toString() + ": Peer " + peerID + " has downloaded the piece " + pieceIndex + " from " + pID);
			writer.flush();
			writer.println("Now the number of pieces it has is: " + numPieces);
			writer.flush();
		}else{
			Tools.debug("[Logger] Log not set!");
		}	
	}
	
	public static void logDownloadComplete() {
		if(peerID != -1){	
			writer.println(new Date().toString() + ": Peer " + peerID + " has downloaded the complete file.");
			writer.flush();
		}else{
			Tools.debug("[Logger] Log not set!");
		}	
	}
	
	public static void closeLog(){
		if(peerID != -1){	
			writer.println(new Date().toString() + ": Log file closed.");
			writer.flush();
			writer.close();
			
		}else{
			Tools.debug("[Logger] Log not set!");
		}
	}
	
}
