// SPDX-License-Identifier: Apache-2.0
package io.github.springwolf.core.asyncapi.schemas;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JavaType;

import java.util.Iterator;

@Slf4j
public class ProtobufModelConverter implements ModelConverter {
    private static final String PROTOBUF_BYTE_STRING = "com.google.protobuf.ByteString";

    @Override
    public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        Class<?> rawClass = null;

        if (type.getType() instanceof Class<?> clazz) {
            rawClass = clazz;
        } else if (type.getType() instanceof JavaType javaType) {
            rawClass = javaType.getRawClass();
        }

        if (rawClass == null) {
            return chain.hasNext() ? chain.next().resolve(type, context, chain) : null;
        }

        // Special case: ByteString should be represented as a string,
        // not inspected as a protobuf Java class.
        if (PROTOBUF_BYTE_STRING.equals(rawClass.getName())) {
            return new StringSchema().format("byte").title("ByteString");
        }

        // Other protobuf implementation/internal classes should NOT be
        // passed down to ModelResolver.
        if (isProtobufInternalType(rawClass)) {
            return new Schema<>().type("object");
        }

        if (chain.hasNext()) {
            return chain.next().resolve(type, context, chain);
        }

        return null;
    }

    private boolean isProtobufInternalType(Class<?> clazz) {
        String name = clazz.getName();

        return name.startsWith("com.google.protobuf.DescriptorProtos$")
                || name.startsWith("com.google.protobuf.Descriptors$")
                || name.startsWith("com.google.protobuf.WireFormat$")
                || name.equals("com.google.protobuf.Parser")
                || name.equals("com.google.protobuf.Message")
                || name.equals("com.google.protobuf.MessageLite")
                || name.equals("com.google.protobuf.UnknownFieldSet");
    }
}
