// SPDX-License-Identifier: Apache-2.0
package io.github.springwolf.examples.kafka.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;

import tools.jackson.core.Version;
import tools.jackson.core.util.VersionUtil;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.introspect.AnnotatedField;
import tools.jackson.databind.introspect.NopAnnotationIntrospector;
import tools.jackson.databind.introspect.VisibilityChecker;

/**
 * Jackson 3 module that works together with Protobuf configuration.
 * When added to the springdoc/springwolf ObjectMapper, it allows protobuf messages to show up in swagger ui.
 * <p>
 * Copyright (c) 2018 InnoGames GmbH  MIT license
 */
public class ProtobufPropertiesModule extends JacksonModule {

    private static final Logger log =
            LoggerFactory.getLogger(ProtobufPropertiesModule.class);

    private final Map<Class<?>, Map<String, FieldDescriptor>> cache =
            new ConcurrentHashMap<>();

    private final NopAnnotationIntrospector annotationIntrospector =
            new NopAnnotationIntrospector() {

        @Override
        public VisibilityChecker findAutoDetectVisibility(
                MapperConfig<?> cfg,
                AnnotatedClass ac,
                VisibilityChecker checker) {

            if (Message.class.isAssignableFrom(ac.getRawType())) {
                return checker
                        .withGetterVisibility(Visibility.PUBLIC_ONLY)
                        .withFieldVisibility(Visibility.ANY);
            }

            return super.findAutoDetectVisibility(cfg, ac, checker);
        }

        @Override
        public Object findNamingStrategy(
                MapperConfig<?> cfg,
                AnnotatedClass ac) {

            if (!Message.class.isAssignableFrom(ac.getRawType())) {
                return super.findNamingStrategy(cfg, ac);
            }

            return new PropertyNamingStrategies.NamingBase() {

                @Override
                public String translate(String propertyName) {
                    if (propertyName != null
                            && propertyName.endsWith("_")) {
                        return propertyName.substring(
                                0,
                                propertyName.length() - 1);
                    }

                    return propertyName;
                }
            };
        }

        @Override
        public JsonFormat.Value findFormat(
                MapperConfig<?> cfg,
                Annotated annotated) {

            JsonFormat.Value format =
                    super.findFormat(cfg, annotated);

            if (annotated instanceof AnnotatedField field) {

                Class<?> declaringClass =
                        field.getDeclaringClass();

                if (Timestamp.class.equals(declaringClass)) {
                    return JsonFormat.Value.forShape(Shape.STRING);
                }

                if (Message.class.isAssignableFrom(declaringClass)) {

                    Map<String, FieldDescriptor> descriptors =
                            cache.computeIfAbsent(
                                    declaringClass,
                                    ProtobufPropertiesModule.this
                                            ::getDescriptorForType);

                    FieldDescriptor descriptor =
                            descriptors.get(field.getName());

                    if (descriptor != null
                            && descriptor.getType()
                                    == FieldDescriptor.Type.STRING) {

                        return JsonFormat.Value.forShape(
                                Shape.STRING);
                    }
                }
            }

            return format;
        }
    };

    @Override
    public String getModuleName() {
        return "ProtobufPropertyModule";
    }

    @Override
    public Version version() {
        return VersionUtil.versionFor(getClass());
    }

    @Override
    public void setupModule(SetupContext context) {
        context.insertAnnotationIntrospector(annotationIntrospector);
    }

    private Map<String, FieldDescriptor> getDescriptorForType(
            Class<?> type) {

        try {
            Descriptor descriptor =
                    (Descriptor) type
                            .getMethod("getDescriptor")
                            .invoke(null);

            Map<String, FieldDescriptor> result =
                    new HashMap<>();

            descriptor.getFields().forEach(field -> {
                result.put(field.getName(), field);
                result.put(field.getJsonName(), field);
            });

            return result;

        } catch (Exception e) {
            log.error(
                    "Error getting protobuf descriptor for swagger.",
                    e);

            return new HashMap<>();
        }
    }
}