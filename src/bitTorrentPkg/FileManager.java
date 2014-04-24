package bitTorrentPkg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {
	private final static String directoryPath = "peer_%d";
	private final static String partFileName = "piece_%d.rofl";
	
	private static String shareFilePath;
	private static File shareFile;
	private static RandomAccessFile shareFileRAF;
	
	private static boolean hasDirectory = false;
	
	public static void openSharedFile(String fileName){
		try {
			checkDirectory();
			shareFilePath = getFilePath(fileName);
			shareFile = new File(shareFilePath);
			shareFileRAF = new RandomAccessFile(shareFile,"r");
			
			Tools.debug("[FileManager.openSharedFile] Opened file: %s.",shareFilePath);
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void checkDirectory(){
		if(!hasDirectory){
			File dir = new File(String.format(directoryPath,NeighborController.host.getPeerID()));
			if(!dir.exists() || !dir.isDirectory()){
				dir.mkdir();
			}
			hasDirectory = true;
		}
	}
	
	private static String getFilePath(String fileName){
		return String.format(directoryPath + "/" + fileName,NeighborController.host.getPeerID());
	}
	private static String getPiecePath(int pieceId){
		return String.format(directoryPath + "/" + partFileName,NeighborController.host.getPeerID(),pieceId);
	}
	
	public static String getShareFilePath(){
		return shareFilePath;
	}
	
	public static FileInfo getFileInfo() throws IOException{
		return new FileManager().new FileInfo(shareFilePath,shareFileRAF.length());
	}
	
	public static byte[] getFilePiece(int pieceIndex, int pieceSize) throws IOException{
		if(NeighborController.host.hasFile()){
			FileChannel f = shareFileRAF.getChannel();
			f.position(pieceSize * pieceIndex);
		
			ByteBuffer buffer = ByteBuffer.allocate(pieceSize);
			int readBytes = f.read(buffer);
			if(readBytes < pieceSize){
				Tools.debug("[FileManager.getFile] Short piece! Read %d bytes, for pieces of %d bytes.",readBytes,pieceSize);
			}
			buffer.limit(readBytes);
			return buffer.array();
		}else{
			return Files.readAllBytes(Paths.get(getPiecePath(pieceIndex)));
		}
	}
	
	public static void writeFilePiece(int pieceId,byte[] data) throws IOException{
		writeBytesToFile(getPiecePath(pieceId),data);
	}
	
	public static void writeBytesToFile(String file,byte[] toWrite) throws IOException{
		checkDirectory();
		
		File piece = new File(file);
		if(!piece.exists()){
			piece.createNewFile();
		}
		else{
			Tools.debug("[FileManager.writeBytesToFile] File already exists?");
		}

		FileOutputStream out = new FileOutputStream(piece);
		out.write(toWrite);
		out.close();
	}
	
	public class FileInfo{
		private String fileName;
		private long byteLength;
		public FileInfo(){
		}
		public FileInfo(String fn, long len){
			this.fileName = fn;
			this.byteLength = len;
		}
		protected void setFileName(String fn){
			this.fileName = fn;
		}
		protected void setByteLength(int bl){
			this.byteLength = bl;
		}
		
		public String getFileName(){
			return this.fileName;
		}
		public long getByteLength(){
			return this.byteLength;
		}
	}
	
	public static void combinePiecesToFile() throws IOException{
		//create new file in peer_%d/Filename
		File file = new File(getFilePath(NeighborController.host.getFileName()));
		FileOutputStream out = new FileOutputStream(file);
		File piece;
		FileInputStream in;
		
		for(int i = 0; i < NeighborController.host.getNumOfPieces(); i++){
			//for every piece, read and write to the file
			piece = new File(getPiecePath(NeighborController.host.getPeerID()));
			in = new FileInputStream(piece);
			while(in.available() > 0){
				out.write(in.read());
			}
		}
		
	}
}
