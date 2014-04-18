package bitTorrentPkg;

import bitTorrentPkg.Messages.*;

import java.io.IOException;
import java.util.Arrays;
public class MessageReceiver {
	public static Message OpenMessageBytes(byte[] message) throws IOException,Exception{
		if(MessageIsHandshake(message)){
			return new Handshake(message);
		}
		
		byte[] lengthBytes = Arrays.copyOfRange(message, 0, 4);
		try{
			int messageLength = GetMessageLength(lengthBytes);
			if(messageLength > message.length - 5){ //HEARTBLEED
				throw new IOException("Message supplied length that exceeds received message length! Stated length: " + messageLength + ", received: " + (message.length - 5));
			}
			
			byte messageType = message[4];
			byte[] messagePayload = new byte[messageLength];
			if(messageLength > 0){
				System.arraycopy(message,5,messagePayload,0,messageLength);
			}
			
			Message received = null;
			switch(messageType){
				case 0:
					received = new Choke();
					break;
				case 1:
					received = new Unchoke();
					break;
				case 2:
					received = new Interested();
					break;
				case 3:
					received = new NotInterested();
					break;
				case 4:
					received = new Have(messagePayload);
					break;
				case 5:
					received = new BitfieldMessage(messagePayload);
					break;
				case 6:
					received = new Request(messagePayload);
					break;
				case 7:
					if(messageLength < 5){
						throw new IOException("Received peice message with payload length less than 5!");
					}
					byte[] indexBytes = Arrays.copyOfRange(messagePayload, 0, 3);
					byte[] piece = Arrays.copyOfRange(messagePayload, 4, messageLength - 1);
					
					received = new Piece(Tools.bytesToInt(indexBytes),piece);
					break;
				default:
					break;
				
			}
			return received;
		}
		catch(Exception e){
			throw e;
		}
	}
	private static int GetMessageLength(byte[] lengthBytes) throws Exception{
		return Tools.bytesToInt(lengthBytes);
	}
	private static byte[] GetHandshakeMagicBytes(){
		byte[] output = new byte[28];
		byte[] header = "HELLO".getBytes();
		for(int i=0;i<header.length;i+=1){
			output[i] = header[i];
		}
		return output;
	}
	private static boolean MessageIsHandshake(byte[] message){
		if(message.length != 32){
			Tools.debug("Message isn't handshake, length is %d.",message.length);
			return false;
		}
		
		byte[] temp = Arrays.copyOfRange(message, 0, 28);
		byte[] handshake = GetHandshakeMagicBytes();
		for(int x=0;x<handshake.length;x+=1){
			if(handshake[x] != temp[x]){
				return false;
			}
		}
		return true;
	}
}
