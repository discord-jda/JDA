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

import static net.dv8tion.jda.test.TestHelper.captureLogging;

import net.dv8tion.jda.api.utils.data.SerializableArray;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.test.assertions.logging.LoggingAssertions;
import net.dv8tion.jda.test.util.SnapshotHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class AbstractSnapshotTest {
    protected SnapshotHandler snapshotHandler;

    @BeforeEach
    void initializeSnapshotHandler(TestInfo testInfo) {
        this.snapshotHandler = new SnapshotHandler(testInfo);
    }

    @Nonnull
    @CheckReturnValue
    protected LoggingAssertions assertThatLoggingFrom(Runnable runnable) {
        List<String> logs = captureLogging(runnable);
        return new LoggingAssertions(snapshotHandler, logs);
    }

    protected void assertWithSnapshot(String actual, String suffix) {
        snapshotHandler.compareWithSnapshot(actual, suffix);
    }

    protected void assertWithSnapshot(SerializableData data) {
        snapshotHandler.compareWithSnapshot(data.toData(), null);
    }

    protected void assertWithSnapshot(SerializableArray data) {
        assertWithSnapshot(data, null);
    }

    protected void assertWithSnapshot(SerializableArray data, String suffix) {
        snapshotHandler.compareWithSnapshot(data.toDataArray(), suffix);
    }
}
