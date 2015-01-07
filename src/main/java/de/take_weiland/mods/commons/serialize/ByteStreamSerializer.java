package de.take_weiland.mods.commons.serialize;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * @author diesieben07
 */
public interface ByteStreamSerializer<T> {

	void write(T obj, MCDataOutput out);

	T read(MCDataInput in);

	interface Contents<T> {

		void write(T obj, MCDataOutput out);

		void read(T obj, MCDataInput in);

	}

	interface SPI {

		<T> ByteStreamSerializer<T> getStreamSerializer(TypeSpecification<T> type);

		<T> ByteStreamSerializer.Contents<T> getContentStreamSerializer(TypeSpecification<T> type);

	}

	abstract class SPIAdapter implements SPI {

		@Override
		public <T> ByteStreamSerializer<T> getStreamSerializer(TypeSpecification<T> type) {
			return null;
		}

		@Override
		public <T> Contents<T> getContentStreamSerializer(TypeSpecification<T> type) {
			return null;
		}
	}

}
