package bitTorrentPkg;

import bitTorrentPkg.Messages.*;
public class MessageReceiver {
	public static IMessage OpenMessageBytes(byte[] message) throws Exception{
		byte[] lengthBytes = new byte[4];
		System.arraycopy(message, 0, lengthBytes,0,4);
		try{
			int messageLength = GetMessageLength(lengthBytes);
			if(messageLength > message.length - 5){ //HEARTBLEED
				throw new Exception("Message supplied length that exceeds received message length! Stated length: " + messageLength + ", received: " + (message.length - 5));
			}
			
			byte messageType = message[4];
			byte[] messagePayload = new byte[messageLength];
			if(messageLength > 0){
				System.arraycopy(message,5,messagePayload,0,messageLength);
			}
			
			IMessage received = null;
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
						throw new Exception("Received peice message with payload length less than 5!");
					}
					byte[] indexBytes = new byte[4];
					byte[] piece = new byte[messageLength - 4];
					System.arraycopy(messagePayload,0,indexBytes,0,4);
					System.arraycopy(messagePayload,5,piece,5,messageLength - 4);
					
					received = new Piece(BytesToInt(indexBytes),piece);
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
		return BytesToInt(lengthBytes);
	}
	private static int BytesToInt(byte[] fourBytes) throws Exception{
		if(fourBytes.length != 4){
			throw new Exception(String.format("Expected four byte input! Received %d bytes!",fourBytes.length));
		}
		int val = 0;
		for(int i=0;i<4;i+=1){
			val |= fourBytes[i] << (8 * (3-i));
		}
		return val;
	}
}
