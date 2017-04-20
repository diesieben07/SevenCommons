package de.take_weiland.mods.commons.serialize;

/**
 * @author diesieben07
 */
public interface BaseSerializer<T> {

    Characteristics characteristics();

    abstract class Characteristics {

        private final SerializationMethod serializationMethod;
        private final boolean handlesNull;

        protected Characteristics(SerializationMethod serializationMethod, boolean handlesNull) {
            this.serializationMethod = serializationMethod;
            this.handlesNull = handlesNull;
        }

        public SerializationMethod getSerializationMethod() {
            return serializationMethod;
        }

        public boolean handlesNull() {
            return handlesNull;
        }

    }
}
