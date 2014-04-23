package bitTorrentPkg;

import java.util.ArrayList;

import org.fusesource.jansi.*;

/*
 * http://stackoverflow.com/questions/15051688/is-it-possible-to-rewrite-previous-line-in-console
 */

public final class ConsoleManager {
	private final static int consoleLineHeight = 24; //Number of lines we will use in the console
	private final static int maxConsoleMessageCount = 5; //Maximum number of messages we will show in the console.
	private final static int maxConsoleWidth = 79; //Maximum number of characters to place on a line.
	
	private final static String messageFormatConf = "| %s |"; //Edit this
	private final static String messageFormat; //Don't edit this
	private final static int paddingCharCount;
	
	private final static LimitedQueue<String> messages;
	private final static LimitedQueue<String> outputBuffer;
	static{
		AnsiConsole.systemInstall();
		messages = new LimitedQueue<String>(consoleLineHeight);
		
		paddingCharCount = String.format(messageFormatConf,"").length();
		messageFormat = messageFormatConf.replaceFirst("%s", "%-" + (maxConsoleWidth - paddingCharCount) + "s"); 
		outputBuffer = new LimitedQueue<String>(consoleLineHeight);
		//This creates a format string width the length specified. 
	}
	
	public final synchronized static void println(String message){
		message = message.replaceAll("\\s+", " ");
		
		int messageWidth = maxConsoleWidth - paddingCharCount;
		if(message.length() > messageWidth){
			String sub;
			int end;
			for(int i=0;i<=Math.ceil(message.length() / messageWidth);i+=1){
				end = (i + 1) * (messageWidth) - 1;
				if(end > message.length()){
					end = message.length() - 1;
				}
				sub = message.substring(i * messageWidth, end);
				messages.add(sub);
			}
		}
		else{
			messages.add(message);
		}
		
		write();
	}
	private final synchronized static void writeLine(String safeMessage){
		//System.out.print(ansi.render().newline());
		outputBuffer.add(String.format(messageFormat,safeMessage));
	}
	
	public final synchronized static void update(){
		write();
	}
	
	private final synchronized static void write(){
		synchronized(outputBuffer){
			//erasePreviousLines();
			writePeerInfo();
			writeMessages();
			writeBufferedOutput();
			
			outputBuffer.clear();
		}
		//writeMessages();
	}
	
	private final synchronized static void writePeerInfo(){
		hr();
		//for(int i=0;i<5;i++){
			//System.out.printf("| Peer %-4d [..................||||||] |\n",i);
		//}
		ArrayList<Peer> peers = NeighborController.getPeers();
		if(peers == null || peers.size() == 0){
			writeLine("No peers connected...");
		}
		else{
			for(int i=0;i<peers.size();i+=1){
				writeLine(String.format("Peer %-5d [.....................................]",peers.get(i).getPeerID()));
			}
		}
		hr();
	}
	private final synchronized static void writeMessages(){
		int peerInfoSize = 3 + ((NeighborController.peers == null || NeighborController.peers.size() == 0)?1:NeighborController.peers.size());
		int index;
		for(int i=0;i<(consoleLineHeight - peerInfoSize);i++){
			//index = (consoleLineHeight - peerInfoSize) - i - 1;
			index = messages.size() - (consoleLineHeight - peerInfoSize) + i;
			if(index>=0){
				writeLine(messages.get(index));
			}
			//else{
			//	System.out.print(ansi.newline());
			//}
		}
		hr();
	}
	
	private final synchronized static void hr(){
		StringBuilder hr = new StringBuilder();
		hr.append('+');
		for(int i=0;i<(maxConsoleWidth - 2);i++){
			hr.append('-');
		}
		hr.append('+');
		//System.out.print(ansi.render(hr.toString()).newline());
		outputBuffer.add(hr.toString());
	}
	
	private final synchronized static void writeBufferedOutput(){
		System.out.print(Ansi.ansi().eraseScreen());
		for(String line : outputBuffer){
			System.out.print(Ansi.ansi().render(line).newline());
		}
	}
}
