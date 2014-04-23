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
	/**
	 * Creates a bitfield given the byte format of a bitfield. 
	 * NOTE: This constructor is imprecise as the private length field might not be correct
	 * if not all of the last byte is actually used. Use Bitfield(byte[],long length) to specify correct length
	 * @param bitfield
	 */
	public Bitfield(byte[] bitfield){
		this.length = bitfield.length * 8;
		this.container = bitfield;
	}
	public Bitfield(byte[] bitfield, long l){
		if((int)Math.ceil(l / 8.0) != bitfield.length){
			throw new IllegalArgumentException(String.format("The given length of %d, corresponding to %d container bytes, does not match the given byte array length %d!",l,(int)Math.ceil(l / 8.0),bitfield.length));
		}
		
		this.length = l;
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
		
		//Tools.debug("getValue: byte: %s, getting bit at %d.",Tools.byteToBinString(this.container[containerIndex]),(index % 8));
		return ((this.container[containerIndex] >> byteIndex) & 0x01) == 1;
	}
	public void setValue(long index, boolean value){
		if(index > this.length - 1){
			throw new java.lang.ArrayIndexOutOfBoundsException(String.format("Index %d is out of bounds. Bitfield size is %d!", index,this.length));
		}
		
		int containerIndex = (int)Math.floor(index / 8);
		int byteIndex = (int)(7 - (index % 8));
		
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
			return false;
		}
		
		byte xor, xcheck;
		for(int x=0;x<this.container.length;x+=1){
			xor = (byte)(this.container[x] ^ other.container[x]);
			if(xor != 0){ //There were dissimilarities
				xcheck = (byte)(other.container[x] & xor);
				if(xcheck != 0 && xcheck <= xor){ //There were differences, and at least one difference corresponded to a 1 bit in other
					//Therefore, other contains a piece we do not have
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean isAll(boolean check){
		byte comp = (byte)((check)?255:0);
		byte b;
		for(int x=0;x<this.container.length;x+=1){
			b = this.container[x];
			if((b ^ comp) != 0){ //If there were any dissimilarities between the bytes
				if(x == this.container.length - 1){
					int shift = (int)(this.length % 8);
					if(shift == 0){
						return false; //the last byte is all used, and we already know the initial check is false.
					}
					
					byte clear = (byte)(0x80 >> (shift - 1)); // 1000 0000 >> (remainder - 1)
					Tools.debug("[Bitfield.isAll] clear byte is %s.",Tools.byteToBinString(clear));
					byte test = (byte)(b & clear); //Zero out any of the unused bits
					
					if(((test ^ comp) & clear) != 0){ //Do the test again (and zero out the unused bits of the result).
						return false;
					}
				}
				else{
					return false;
				}
			}
		}
		return true;
	}
	
	public byte[] toBytes(){
		return this.container;
	}
	
	/**
	 * Gets a random index containing any value.
	 * @return
	 */
	public int getRandomIndex(){
		int randomPiece = (int)Math.floor(Math.random() * (this.length + 1));
		return randomPiece;
	}
	
	/**
	 * Gets a random index which contains the given value
	 * @param withValue The value which must be at the randomly chosen index.
	 * @return
	 */
	public int getRandomIndex(boolean withValue){
		int randomChunk = (int)Math.floor(Math.random() * (this.container.length));
		byte testByte = (byte)((withValue)?0:255); //If we're looking for a zero, we want to XOR with all 1s; if we want a 1, we want to XOR all 0s. 
		
		if(this.isAll(!withValue)){ //There 
			return -1; 
		}
		
		Tools.debug("[Bitfield.getRandomIndex] Let's do that random chunk thang");
		while((this.container[randomChunk] ^ testByte) == 0){
			randomChunk = (int)Math.floor(Math.random() * (this.container.length));
		}
		
		int maxByteIndex = 8;
		if(randomChunk == this.container.length - 1){ //If this is the last byte (of which, not all bits may be used).
			maxByteIndex = (int)(this.length % 8);
		}
		int randomPiece = (int)Math.floor(Math.random() * (maxByteIndex + 1));
		Tools.debug("[Bitfield.getRandomIndex] git dat piece from %s.",Tools.byteToBinString(this.container[randomChunk]));
		while(this.getValue(randomChunk * 8 + randomPiece) != withValue){
			randomPiece = (int)Math.floor(Math.random() * (maxByteIndex + 1));
		}
		return randomChunk * 8 + randomPiece;
	}
	
	public Bitfield xor(Bitfield other){
		byte[] xorField = new byte[this.container.length];
		for(int x=0;x<this.container.length;x+=1){
			xorField[x] = (byte)(this.container[x] ^ other.container[x]);
		}
		return new Bitfield(xorField,this.length);
	}
	public Bitfield not(){
		byte[] notField = new byte[this.container.length];
		for(int x=0;x<this.container.length;x+=1){
			notField[x] = (byte)(~this.container[x]);
		}
		return new Bitfield(notField,this.length);
	}
	public Bitfield and(Bitfield other){
		byte[] andField = new byte[this.container.length];
		for(int x=0;x<this.container.length;x+=1){
			andField[x] = (byte)(this.container[x] & other.container[x]);
		}
		return new Bitfield(andField,this.length);
	}
	
	/**
	 * Gets the number of pieces this bitfield represents.
	 * Warning: this is imprecise if the Bitfield(byte[]) constructor is used without the length argument. 
	 * @return
	 */
	public long getLength(){
		return this.length;
	}
}
