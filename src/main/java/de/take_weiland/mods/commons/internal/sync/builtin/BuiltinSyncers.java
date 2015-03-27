package de.take_weiland.mods.commons.internal.sync.builtin;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.sync.SimpleSyncer;
import de.take_weiland.mods.commons.sync.SyncerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author diesieben07
 */
public final class BuiltinSyncers implements SyncerFactory {

    private final Map<Class<?>, SimpleSyncer<?, ?>> cache = new HashMap<>();

    @Override
    public <V, C> SimpleSyncer<V, C> getSyncer(TypeSpecification<V> type) {
        Class<? super V> raw = type.getRawType();

        SimpleSyncer<?, ?> syncer = cache.get(raw);
        if (syncer == null && !cache.containsKey(raw)) {
            syncer = newSyncer(raw);
            cache.put(raw, syncer);
        }
        //noinspection unchecked
        return (SimpleSyncer<V, C>) syncer;
    }

    private static SimpleSyncer<?, ?> newSyncer(Class<?> type) {
        if (type == String.class) {
            return new ForImmutable<String>(String.class) {
                @Override
                public String writeAndUpdate(String value, String companion, MCDataOutput out) {
                    out.writeString(value);
                    return value;
                }

                @Override
                public String read(String oldValue, String companion, MCDataInput in) {
                    return in.readString();
                }
            };
        } else if (type == UUID.class) {
            return new ForImmutable<UUID>(UUID.class) {
                @Override
                public UUID writeAndUpdate(UUID value, UUID companion, MCDataOutput out) {
                    out.writeUUID(value);
                    return value;
                }

                @Override
                public UUID read(UUID oldValue, UUID companion, MCDataInput in) {
                    return in.readUUID();
                }
            };
        } else if (type == boolean.class) {
            return new ForImmutable<Boolean>(boolean.class) {
                @Override
                public Boolean writeAndUpdate(Boolean value, Boolean companion, MCDataOutput out) {
                    out.writeBoolean(value);
                    return value;
                }

                @Override
                public Boolean read(Boolean oldValue, Boolean companion, MCDataInput in) {
                    return in.readBoolean();
                }
            };
        } else if (type == byte.class) {
            return new ForImmutable<Byte>(byte.class) {
                @Override
                public Byte writeAndUpdate(Byte value, Byte companion, MCDataOutput out) {
                    out.writeByte(value);
                    return value;
                }

                @Override
                public Byte read(Byte oldValue, Byte companion, MCDataInput in) {
                    return in.readByte();
                }
            };
        } else if (type == short.class) {
            return new ForImmutable<Short>(short.class) {
                @Override
                public Short writeAndUpdate(Short value, Short companion, MCDataOutput out) {
                    out.writeShort(value);
                    return value;
                }

                @Override
                public Short read(Short oldValue, Short companion, MCDataInput in) {
                    return in.readShort();
                }
            };
        } else if (type == int.class) {
            return new ForImmutable<Integer>(int.class) {

                @Override
                public Integer writeAndUpdate(Integer value, Integer companion, MCDataOutput out) {
                    out.writeInt(value);
                    return value;
                }

                @Override
                public Integer read(Integer oldValue, Integer companion, MCDataInput in) {
                    return in.readInt();
                }
            };
        } else if (type == char.class) {
            return new ForImmutable<Character>(char.class) {

                @Override
                public Character writeAndUpdate(Character value, Character companion, MCDataOutput out) {
                    out.writeChar(value);
                    return value;
                }

                @Override
                public Character read(Character oldValue, Character companion, MCDataInput in) {
                    return in.readChar();
                }
            };
        } else if (type == long.class) {
            return new ForImmutable<Long>(long.class) {

                @Override
                public Long writeAndUpdate(Long value, Long companion, MCDataOutput out) {
                    out.writeLong(value);
                    return value;
                }

                @Override
                public Long read(Long oldValue, Long companion, MCDataInput in) {
                    return in.readLong();
                }
            };
        } else if (type == float.class) {
            return new ForImmutable<Float>(float.class) {
                @Override
                public Float writeAndUpdate(Float value, Float companion, MCDataOutput out) {
                    out.writeFloat(value);
                    return value;
                }

                @Override
                public Float read(Float oldValue, Float companion, MCDataInput in) {
                    return in.readFloat();
                }
            };
        } else if (type == double.class) {
            return new ForImmutable<Double>(double.class) {
                @Override
                public Double writeAndUpdate(Double value, Double companion, MCDataOutput out) {
                    out.writeDouble(value);
                    return value;
                }

                @Override
                public Double read(Double oldValue, Double companion, MCDataInput in) {
                    return in.readDouble();
                }
            };
        }
        return null;
    }

    private static abstract class ForImmutable<V> implements SimpleSyncer<V, V> {

        private final Class<V> type;

        ForImmutable(Class<V> type) {
            this.type = type;
        }

        @Override
        public final Class<V> getValueType() {
            return type;
        }

        @Override
        public final Class<V> getCompanionType() {
            return type;
        }

        @Override
        public boolean equal(V value, V companion) {
            return Objects.equal(value, companion);
        }
    }

}