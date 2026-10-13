// SPDX-License-Identifier: Apache-2.0
package io.github.springwolf.asyncapi.v3.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.core.util.Yaml31;
import lombok.Getter;
import tools.jackson.core.JacksonException;
import tools.jackson.core.PrettyPrinter;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;

public class DefaultAsyncApiSerializerService implements AsyncApiSerializerService {

    private final PrettyPrinter printer = new CustomPrettyPrinter();

    /**
     *  Get the current JSON object mapper configuration.
     */
    @Getter
    private ObjectMapper jsonMapper = Json31.mapper();

    @Getter
    private ObjectMapper yamlMapper = Yaml31.mapper();

    public DefaultAsyncApiSerializerService() {
        jsonMapper = jsonMapper
                .rebuild()
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_ABSENT)
                        .withContentInclusion(JsonInclude.Include.NON_ABSENT))
                .defaultPrettyPrinter(printer)
                .build();

        yamlMapper = yamlMapper
                .rebuild()
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_ABSENT)
                        .withContentInclusion(JsonInclude.Include.NON_ABSENT))
                .defaultPrettyPrinter(printer)
                .build();
    }

    @Override
    public String toJsonString(Object object) throws JacksonException {
        return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    @Override
    public String toYaml(Object object) throws JacksonException {
        return yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    private static class CustomPrettyPrinter extends DefaultPrettyPrinter {
        public CustomPrettyPrinter() {
            super(Separators.createDefaultInstance().withObjectNameValueSpacing(Separators.Spacing.AFTER));
            this._arrayIndenter = new DefaultIndenter();
        }

        @Override
        public CustomPrettyPrinter createInstance() {
            return new CustomPrettyPrinter();
        }
    }
}
