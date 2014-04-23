package bitTorrentPkg;

public class Bitfield {
	private long length;
	protected byte[] container;
	public Bitfield(long numOfPieces){
		this(numOfPieces,false);
	}
	public Bitfield(long length,boolean intialVal){
		this.length = length;
		this.container = new byte[(int)Math.ceil(length / 8.0)]; //Make sure we have enough bytes for all of the bits
		Tools.debug("[Bitfield] Creating container of length %d.",(int)Math.ceil(length / 8.0));
		if(intialVal == true){
			for(int i=0;i<this.container.length;i+=1){
				this.container[i] = (byte)255;
			}
		}
	}
	public Bitfield(byte[] bitfield){
		this.length = bitfield.length * 8;
		this.container = bitfield;
	}
	public void setValueAll(boolean value){
		byte newValue = (byte) ((value)?255:0);
		for(int i=0;i<this.container.length;i+=1){
			this.container[i] = newValue;
		}
	}
	public boolean getValue(int index){
		if(index > this.length - 1){
			throw new java.lang.ArrayIndexOutOfBoundsException(String.format("Index %d is out of bounds. Bitfield size is %d!", index,this.length));
		}
		
		int containerIndex = (int)Math.floor(index / 8);
		int byteIndex = 7 - (index % 8);
		
		Tools.debug("getValue: byte: %s, getting bit at %d.",Tools.byteToBinString(this.container[containerIndex]),(index % 8));
		return ((this.container[containerIndex] >> byteIndex) & 0x01) == 1;
	}
	public void setValue(int index, boolean value){
		if(index > this.length - 1){
			throw new java.lang.ArrayIndexOutOfBoundsException(String.format("Index %d is out of bounds. Bitfield size is %d!", index,this.length));
		}
		
		int containerIndex = (int)Math.floor(index / 8);
		int byteIndex = 7 - (index % 8);
		
		byte modifier = (byte)(1 << byteIndex);
		byte bitClear = (byte)(255 ^ modifier);
		//Tools.debug("setValue: modifier byte: %s; bitclear byte: %s",Tools.byteToBinString(modifier),Tools.byteToBinString(bitClear));
		Tools.debug("setValue: Original byte: %s",Tools.byteToBinString(this.container[containerIndex]));
		byte newByte = (byte)((this.container[containerIndex] & bitClear)); //Zeros out the bit at the specified index
		if(value == true){
			newByte |= modifier; //Set the bit at the specified index equal to 1.
		}
		this.container[containerIndex] = newByte;
		Tools.debug("setValue: New byte:      %s",Tools.byteToBinString(this.container[containerIndex]));
	}
	
	public boolean checkForInterest(Bitfield other){
		if(other.container.length != this.container.length){
			throw new IllegalArgumentException(String.format("Other bitfield length (%d) does not equal this bitfield length (%d)!",other.length,this.length));
		}
		
		if(other.isAll(false)){
			Tools.debug("[Bitfield.checkForInterest] Other possesses no pieces.");
			return false;
		}
		
		byte xor, xcheck;
		for(int x=0;x<this.container.length;x+=1){
			xor = (byte)(this.container[x] ^ other.container[x]);
			if(xor != 0){ //There were dissimilarities
				xcheck = (byte)(other.container[x] & xor);
				
				Tools.debug("%s ^ %s = %s. check = %s.",Tools.byteToBinString(this.container[x]),Tools.byteToBinString(other.container[x]),Tools.byteToBinString(xor),Tools.byteToBinString(xcheck));
				if(xcheck != 0 && xcheck < xor){ //There were differences, and at least one difference corresponded to a 1 bit in other
					//Therefore, other contains a piece we do not have
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean isAll(boolean check){
		byte comp = (byte)((check)?255:0);
		for(byte b : this.container){
			if((b ^ comp) != 0){ //If there were any dissimilarities between the bytes
				return false;
			}
		}
		return true;
	}
	
	public byte[] toBytes(){
		return this.container;
	}
}
