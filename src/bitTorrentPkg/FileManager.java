package bitTorrentPkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.IOException;

public class FileManager {
	private final static String directoryPath = "peer_%d";
	private final static String partFileName = "piece_%d.rofl";
	
	private static String shareFilePath;
	private static File shareFile;
	private static RandomAccessFile shareFileRAF;
	static{
		
	}
	
	public static void openSharedFile(String fileName){
		try {
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
	
	private static String getFilePath(String fileName){
		return String.format(directoryPath + "/" + fileName,NeighborController.host.getPeerID());
	}
	
	public static String getShareFilePath(){
		return shareFilePath;
	}
	
	public static FileInfo getFileInfo() throws IOException{
		return new FileManager().new FileInfo(shareFilePath,shareFileRAF.length());
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
}
