package de.take_weiland.mods.commons.serialize;

/**
 * @author diesieben07
 */
public interface BaseSPI {

    SerializationMethod getDefaultMethod(TypeSpecification<?> type);

    abstract class Adapter implements BaseSPI {

        @Override
        public SerializationMethod getDefaultMethod(TypeSpecification<?> type) {
            return SerializationMethod.DEFAULT;
        }
    }

}
