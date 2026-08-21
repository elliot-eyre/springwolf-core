// SPDX-License-Identifier: Apache-2.0
package io.github.springwolf.examples.kafka.configuration;

import com.hubspot.jackson3.datatype.protobuf.ProtobufJacksonConfig;
import com.hubspot.jackson3.datatype.protobuf.ProtobufModule;
import io.github.springwolf.core.asyncapi.schemas.ModelConvertersProvider;
import io.github.springwolf.core.standalone.StandaloneConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
@StandaloneConfiguration
public class ProtobufConfiguration {

    // Forced zero-parameter constructor implicitly handled by the compiler

    @Autowired
    public void configureProtobuf(ModelConvertersProvider modelConvertersProvider) {
        ObjectMapper baseObjectMapper = modelConvertersProvider.getObjectMapper();

        // 1. Instantiating your Jackson 3 HubSpot Protobuf modules
        JacksonModule protobufModule = new ProtobufModule(
                ProtobufJacksonConfig.builder().acceptLiteralFieldnames(true).build());

        JacksonModule protobufPropertiesModule = new ProtobufPropertiesModule();

        // 2. Rebuilding the immutable mapper to attach the modules in Jackson 3
        ObjectMapper configuredObjectMapper = baseObjectMapper
                .rebuild()
                .addModule(protobufModule)
                .addModule(protobufPropertiesModule)
                .build();

        // 3. Critically re-assigning the new immutable instance back to the provider
        modelConvertersProvider.setObjectMapper(configuredObjectMapper);
    }
}
