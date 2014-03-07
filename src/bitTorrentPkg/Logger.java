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
		writer.println(new Time(System.currentTimeMillis()).toString() + ": Test log. Peer ID = " + peerID);
	}
	
	public void logTCPConnection(int targetPeerID){
		
	}
	
	public void closeLog(){
		writer.println(new Time(System.currentTimeMillis()).toString() + ": Log file closed.");
		writer.close();
	}
	
}
