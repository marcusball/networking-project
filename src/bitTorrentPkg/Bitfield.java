package bitTorrentPkg;

public class Bitfield {
	private int length;
	private byte[] container;
	public Bitfield(int length){
		this(length,false);
	}
	public Bitfield(int length,boolean intialVal){
		this.length = length;
		container = new byte[(int)Math.ceil(length / 8.0)]; //Make sure we have enough bytes for all of the bits
		Tools.debug("Bitfield: creating container of length %d.");
		if(intialVal == true){
			for(int i=0;i<container.length;i+=1){
				container[i] = (byte)255;
			}
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
}
