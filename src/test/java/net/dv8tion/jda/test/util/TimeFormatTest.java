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

package net.dv8tion.jda.test.util;

import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.Timestamp;
import net.dv8tion.jda.internal.utils.ClockProvider;
import net.dv8tion.jda.test.AbstractSnapshotTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TimeFormatTest extends AbstractSnapshotTest {
    private static final Instant INSTANT = Instant.parse("2026-02-08T12:34:56Z");

    @EnumSource
    @ParameterizedTest
    void testFromStyle(TimeFormat format) {
        assertThat(TimeFormat.fromStyle(format.getStyle())).isSameAs(format);
    }

    @EnumSource
    @ParameterizedTest
    void testTimeFormatCreateAndParse(TimeFormat format) {
        Timestamp timestampFromInstant = format.atInstant(INSTANT);
        assertThat(TimeFormat.parse(timestampFromInstant.toString())).isEqualTo(timestampFromInstant);

        Timestamp timestampFromEpoch = format.atTimestamp(INSTANT.toEpochMilli());
        assertThat(TimeFormat.parse(timestampFromEpoch.toString())).isEqualTo(timestampFromEpoch);
    }

    @EnumSource
    @ParameterizedTest
    void testTimeFormatBefore(TimeFormat format) {
        ClockProvider.withFixedTime(INSTANT, () -> {
            long expectedTimestamp = INSTANT.minus(2, ChronoUnit.MINUTES).toEpochMilli();

            assertThat(format.before(Duration.ofMinutes(2)).getTimestamp()).isEqualTo(expectedTimestamp);
            assertThat(format.before(TimeUnit.MINUTES.toMillis(2)).getTimestamp())
                    .isEqualTo(expectedTimestamp);
        });
    }

    @EnumSource
    @ParameterizedTest
    void testTimeFormatAfter(TimeFormat format) {
        ClockProvider.withFixedTime(INSTANT, () -> {
            long expectedTimestamp = INSTANT.plus(2, ChronoUnit.MINUTES).toEpochMilli();

            assertThat(format.after(Duration.ofMinutes(2)).getTimestamp()).isEqualTo(expectedTimestamp);
            assertThat(format.after(TimeUnit.MINUTES.toMillis(2)).getTimestamp())
                    .isEqualTo(expectedTimestamp);
        });
    }
}
