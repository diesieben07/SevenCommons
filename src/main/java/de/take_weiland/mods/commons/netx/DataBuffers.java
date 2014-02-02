package de.take_weiland.mods.commons.netx;


public final class DataBuffers {

	private static final int DEFAULT_CAPACTIY = 128;

	private DataBuffers() { }
	
	public static DataBuf newBuffer(byte[] data) {
		return newBuffer(data, 0, data.length);
	}
	
	public static DataBuf newBuffer(byte[] data, int off, int len) {
        validateArray(data, off, len);
		return new DataBufImpl(data, off, len);
	}
	
	static DataBufImpl newBuffer0(byte[] data) {
		return new DataBufImpl(data, 0, data.length);
	}
	
	public static WritableDataBuf newWritableBuffer() {
		return newWritable0(DEFAULT_CAPACTIY);
	}
	
	public static WritableDataBuf newWritableBuffer(int capacity) {
		return newWritable0(capacity);
	}
	
	static <TYPE extends Enum<TYPE>> WritableDataBufImpl<TYPE> newWritable0(int capacity) {
		if (capacity <= 0) {
			capacity = DEFAULT_CAPACTIY;
		}
		return new WritableDataBufImpl<TYPE>(new byte[capacity]);
	}
	
	static void validateArray(byte[] arr, int off, int len) {
		if (arr == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > arr.length - off) {
            throw new IndexOutOfBoundsException();
        }
	}

}
