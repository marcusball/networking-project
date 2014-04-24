package bitTorrentPkg;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class Tools {
	private final static boolean useFancyOutput = false;
	public static int bytesToInt(byte[] fourBytes) throws IllegalArgumentException{
		if(fourBytes.length != 4){
			throw new IllegalArgumentException(String.format("Expected four byte input! Received %d bytes!",fourBytes.length));
		}
		int val = 0;
		//Tools.debug("[bytesToInt] val = %s",val);
		for(int i=0;i<4;i+=1){
			val |= toUnsigned(fourBytes[i]) << (8 * (3-i));
			//Tools.debug("[bytesToInt] val = %s (%2x %s)",val,fourBytes[i],Tools.byteToBinString(fourBytes[i]));
		}
		return val;
	}
	
	public static void debug(String message){
		if(!useFancyOutput){
			System.out.println("DEBUG: " + message);
		}
		else{
			ConsoleManager.println(message);
		}
	}
	public static void debug(String message,Object... args){
		if(!useFancyOutput){
			System.out.printf("DEBUG: " + message + '\n',args);
		}
		else{
			ConsoleManager.println(String.format(message,args));
		}
	}
	public static String byteToBinString(byte val){
		String s = Integer.toBinaryString(val);
		if(s.length() > 8) s = s.substring(s.length() - 8);
		return String.format("%08d", Integer.parseInt(s));
	}
	
	public static String intToBinString(int val){
		String s = Integer.toBinaryString(val);
		return String.format("%s", s);
	}
	
	public static String byteArrayToString(byte[] bytes){
		StringBuilder s = new StringBuilder();
		for(byte b : bytes){
			s.append(String.format("%2x ",b));
		}
		return s.toString();
	}
	
	public static int toUnsigned(byte b){
		return b & 0xFF;
	}
	
	public static String getMD5(byte[] bytes){
		try {
			MessageDigest md;
			md = MessageDigest.getInstance("MD5");
			md.update(bytes);
			byte[] output = md.digest();
			
			return toHex(output);
		} 
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String toHex(byte[] bytes) {
	    BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "X", bi);
	}
}
