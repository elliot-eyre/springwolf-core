// SPDX-License-Identifier: Apache-2.0
package io.github.springwolf.core.asyncapi.components.examples.formatter;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ExampleFormatter {
    private static final SimpleDateFormat ISO_DATE_FORMAT;

    static {
        ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
        // Force the formatter to use UTC instead of the local system timezone
        ISO_DATE_FORMAT.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
    }

    public static Object processExampleObject(Object example) {
        if (example instanceof Date exampleDate) {
            return ISO_DATE_FORMAT.format(exampleDate);
        } else if (example instanceof OffsetDateTime exampleOffsetDateTime) {
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(exampleOffsetDateTime);
        } else {
            return example;
        }
    }
}
