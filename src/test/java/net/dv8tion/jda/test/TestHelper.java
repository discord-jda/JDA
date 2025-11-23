/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.test;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHelper {
    public static List<String> captureLogging(Runnable task) {
        return captureLogging(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME), task);
    }

    public static List<String> captureLogging(Logger logger, Runnable task) {
        assertThat(logger).isInstanceOf(ch.qos.logback.classic.Logger.class);
        ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;

        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logbackLogger.addAppender(listAppender);
        try {
            task.run();
            return listAppender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .collect(Collectors.toList());
        } finally {
            logbackLogger.detachAppender(listAppender);
            listAppender.stop();
        }
    }
}
