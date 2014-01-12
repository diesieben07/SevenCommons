package de.take_weiland.mods.commons.internal.sync;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import de.take_weiland.mods.commons.network.Packets;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;

public interface SyncedField {

	boolean needsSyncing();
	
	void write(DataOutput out) throws IOException;
	
	void read(DataInput in) throws IOException;
	
	void refreshed();
	
	public static class Int implements SyncedField {

		private int current;
		private int last;
		
		@Override
		public boolean needsSyncing() {
			return current != last;
		}

		@Override
		public void write(DataOutput out) throws IOException {
			out.writeInt(current);
		}

		@Override
		public void read(DataInput in) throws IOException {
			current = in.readInt();
		}

		@Override
		public void refreshed() {
			last = current;
		}
		
	}
	
	public static class Byte implements SyncedField {

		private byte current;
		private byte last;
		
		@Override
		public boolean needsSyncing() {
			return current != last;
		}

		@Override
		public void write(DataOutput out) throws IOException {
			out.writeByte(current);
		}

		@Override
		public void read(DataInput in) throws IOException {
			current = in.readByte();
		}

		@Override
		public void refreshed() {
			last = current;
		}
		
	}
	
	public static class Short implements SyncedField {

		private short current;
		private short last;
		
		@Override
		public boolean needsSyncing() {
			return current != last;
		}

		@Override
		public void write(DataOutput out) throws IOException {
			out.writeShort(current);
		}

		@Override
		public void read(DataInput in) throws IOException {
			current = in.readShort();
		}

		@Override
		public void refreshed() {
			last = current;
		}
		
	}
	
	public static class Long implements SyncedField {

		private long current;
		private long last;
		
		@Override
		public boolean needsSyncing() {
			return current != last;
		}

		@Override
		public void write(DataOutput out) throws IOException {
			out.writeLong(current);
		}

		@Override
		public void read(DataInput in) throws IOException {
			current = in.readLong();
		}

		@Override
		public void refreshed() {
			last = current;
		}
		
	}
	
	public static class String extends AbstractSyncedObject<java.lang.String> {

		@Override
		public void write(DataOutput out) throws IOException {
			out.writeUTF(current);
		}

		@Override
		public void read(DataInput in) throws IOException {
			current = in.readUTF();
		}
		
	}
	
	public static class Fluid extends AbstractSyncedObject<FluidStack> {

		@Override
		public void write(DataOutput out) throws IOException {
			Packets.writeFluidStack(out, current);
		}

		@Override
		public void read(DataInput in) throws IOException {
			current = Packets.readFluidStack(in);
		}

		@Override
		public boolean needsSyncing() {
			return Fluids.identical(last, current);
		}
		
	}
	
}
