package bitTorrentPkg;

public class Tools {
	public static int bytesToInt(byte[] fourBytes) throws Exception{
		if(fourBytes.length != 4){
			throw new Exception(String.format("Expected four byte input! Received %d bytes!",fourBytes.length));
		}
		int val = 0;
		for(int i=0;i<4;i+=1){
			val |= fourBytes[i] << (8 * (3-i));
		}
		return val;
	}
	
	public static void debug(String message){
		System.out.println("DEBUG: " + message);
	}
	public static void debug(String message,Object... args){
		System.out.printf("DEBUG: " + message + '\n',args);
	}
	public static String byteToBinString(byte val){
		String s = Integer.toBinaryString(val);
		if(s.length() > 8) s = s.substring(s.length() - 8);
		return String.format("%08d", Integer.parseInt(s));
	}
}
