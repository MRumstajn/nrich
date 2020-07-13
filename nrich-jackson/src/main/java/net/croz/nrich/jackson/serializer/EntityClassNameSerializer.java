package net.croz.nrich.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class EntityClassNameSerializer extends JsonSerializer<Class<?>> {

    @Override
    public void serialize(final Class<?> type, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(type.getName());
    }
}