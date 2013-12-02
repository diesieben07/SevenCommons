package de.take_weiland.mods.commons.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.OutputStream;

import com.google.common.io.OutputSupplier;

public class SplittingOutputStream<T extends OutputStream> extends OutputStream {

	private final int chunkSize;
	private final OutputSupplier<T> supplier;
	private final Consumer<T> streamEndCallback;
	
	private int currentChunk = 0;
	private T currentStream;
	
	public SplittingOutputStream(int chunkSize, OutputSupplier<T> supplier, Consumer<T> streamEndCallback) throws IOException {
		checkArgument(chunkSize > 0, "Chunk size must be > 0");
		checkNotNull(supplier, "Supplier must not be null");
		
		this.chunkSize = chunkSize;
		this.supplier = supplier;
		this.streamEndCallback = streamEndCallback;
		
		nextStream();
	}
	
	private void nextStream() throws IOException {
		if (currentStream != null) {
			currentStream.flush();
			currentStream.close();
			if (streamEndCallback != null) {
				streamEndCallback.apply(currentStream);
			}
		}
		currentChunk = 0;
		currentStream = supplier.getOutput();
	}
	
	private void checkOpen() throws IOException {
		if (currentStream == null) {
			throw new IOException("Stream closed.");
		}
	}
	
	@Override
	public void write(int b) throws IOException {
		checkOpen();
		if (currentChunk == chunkSize) {
			nextStream();
		}
		currentStream.write(b);
		currentChunk++;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		checkOpen();
		if (len + currentChunk < chunkSize) {
			currentStream.write(b, off, len);
			currentChunk += len;
		} else {
			int written = 0;
			while (written < len) {
				if (currentChunk == chunkSize) {
					nextStream();
				}
				int toWrite = Math.min(len - written, chunkSize - currentChunk);
				currentStream.write(b, off + written, toWrite);
				written += toWrite;
				currentChunk += toWrite;
			}
		}
	}

	@Override
	public void flush() throws IOException {
		checkOpen();
		currentStream.flush();
	}

	@Override
	public void close() throws IOException {
		if (currentStream != null) {
			currentStream.close();
			if (streamEndCallback != null) {
				streamEndCallback.apply(currentStream);
			}
		}
	}
	
}
