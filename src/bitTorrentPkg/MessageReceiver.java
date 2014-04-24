package bitTorrentPkg;

import bitTorrentPkg.Messages.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
public class MessageReceiver {
	private AtomicBoolean isAwaitingPiece;
	private byte[] pieceBuffer;
	private int bytesReceived;
	
	private int offset = 0;
	public MessageReceiver(){
		this.isAwaitingPiece = new AtomicBoolean(false);
	}
	public Message OpenMessageBytes(byte[] message) throws IOException,Exception{
		synchronized(this.isAwaitingPiece){
			if(this.isAwaitingPiece.get()){ //If we've received some of a messages bytes, then this is the continuation of those bytes.
				Tools.debug("[MessageReceiver] pieceBuffer length: %d; message length: %d; bytes received: %d",pieceBuffer.length, message.length, this.bytesReceived);
				int end = message.length;
				if(this.bytesReceived + message.length > this.pieceBuffer.length){
					end = this.pieceBuffer.length - this.bytesReceived;
					this.offset = end; 
				}
				else{
					this.offset = 0;
				}
				Tools.debug("[MessageReceiver] message, 0, buffer, %d, %d",this.bytesReceived, end);
				System.arraycopy(message, 0, pieceBuffer, this.bytesReceived, end);
				this.bytesReceived += end;
				
				if(this.bytesReceived == this.pieceBuffer.length){
					Tools.debug("[MessageReceiver] Finished receiving pieces [%d/%d bytes]!",this.bytesReceived,this.pieceBuffer.length);
					
					this.isAwaitingPiece.set(false);
					Message m = this.OpenMessageBytes(pieceBuffer);
					this.pieceBuffer = null;
					this.bytesReceived =0;
					return m;
				}
				else{
					Tools.debug("[MessageReceiver] Received partial message %d [Total: %d/%d bytes].",message.length,this.bytesReceived,this.pieceBuffer.length);
					return null;
				}
			}
			else{
				if(MessageIsHandshake(message)){
					return new Handshake(message);
				}
				
				byte[] lengthBytes = Arrays.copyOfRange(message, 0, 4);
				try{
					Tools.debug("[MessageReceiver] Length: [%s], value: %d",Tools.byteArrayToString(lengthBytes),Tools.bytesToInt(lengthBytes));
					

					int messageLength = GetMessageLength(lengthBytes);
					if(messageLength + 5 < message.length){
						this.offset = 5 + messageLength;
					}
					if(messageLength + 5 == message.length){
						this.offset = 0;
					}
					
					if(messageLength > message.length - 5){
						//throw new IOException("Message supplied length that exceeds received message length! Stated length: " + messageLength + ", received: " + (message.length - 5));
						this.isAwaitingPiece.set(true);
						this.pieceBuffer = new byte[messageLength + 5]; //We're going to include message header
						this.bytesReceived = 0;
						System.arraycopy(message, 0, pieceBuffer, this.bytesReceived, message.length);
						this.bytesReceived += message.length;
						
						Tools.debug("[MessageReceiver] Received partial message %d [Total: %d/%d bytes].",message.length,this.bytesReceived,this.pieceBuffer.length);
						
						return null;
					}
					else{
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
								received = new BitfieldMessage(messagePayload,NeighborController.host.getNumOfPieces());
								break;
							case 6:
								received = new Request(messagePayload);
								break;
							case 7:
								if(messageLength < 5){
									throw new IOException("Received peice message with payload length less than 5!");
								}
								byte[] indexBytes = Arrays.copyOfRange(messagePayload, 0, 4);
								byte[] piece = Arrays.copyOfRange(messagePayload, 4, messageLength);
								
								received = new Piece(Tools.bytesToInt(indexBytes),piece);
								break;
							default:
								break;
							
						}
						return received;
					}
				}
				catch(Exception e){
					throw e;
				}
			}
			
		}
	}
	public int getOffset(){
		return this.offset; 
	}
	private int GetMessageLength(byte[] lengthBytes) throws Exception{
		return Tools.bytesToInt(lengthBytes);
	}
	private byte[] GetHandshakeMagicBytes(){
		byte[] output = new byte[28];
		byte[] header = "HELLO".getBytes();
		for(int i=0;i<header.length;i+=1){
			output[i] = header[i];
		}
		return output;
	}
	
	private boolean MessageIsHandshake(byte[] message){
		if(message.length != 32){
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
