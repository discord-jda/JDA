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


import net.dv8tion.jda.api.utils.data.DataObject;
import okio.BufferedSink;
import okio.Okio;
import okio.Path;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class SnapshotHandler
{
    private final TestInfo testInfo;
    private final Logger logger;

    public SnapshotHandler(TestInfo testInfo)
    {
        this.testInfo = testInfo;
        this.logger = LoggerFactory.getLogger(SnapshotHandler.class);
    }

    public void compareWithSnapshot(DataObject actual, String suffix)
    {
        Class<?> currentClass = testInfo.getTestClass().orElseThrow(AssertionError::new);
        String filePath = getFilePath(suffix);

        try (InputStream stream = currentClass.getResourceAsStream(filePath))
        {
            assertThat(stream).as("Loading sample from resource file '%s'", filePath).isNotNull();
            assertThat(DataObject.fromJson(stream).toPrettyString())
                .isEqualToNormalizingWhitespace(actual.toPrettyString());
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
        catch (AssertionError e)
        {
            try
            {
                updateOrCreateIfNecessary(actual, suffix);
            }
            catch (Exception exception)
            {
                e.addSuppressed(exception);
            }
            throw e;
        }
    }

    private void updateOrCreateIfNecessary(DataObject actual, String suffix) throws IOException
    {
        if (System.getProperty("updateSnapshots") == null)
            return;

        Class<?> currentClass = testInfo.getTestClass().orElseThrow(AssertionError::new);
        String filePath = getFilePath(suffix);

        String workingDirectory = System.getProperty("user.dir");
        String path = currentClass.getPackage().getName().replace(".", "/") + "/" + filePath;

        Path fileLocation = Path.get(workingDirectory, true)
            .resolve("src/test/resources", true)
            .resolve(path, true);

        File file = fileLocation.toFile();
        if (!file.exists())
        {
            logger.info("Creating snapshot {}", file);
            file.getParentFile().mkdirs();
            assertThat(file.createNewFile()).isTrue();
        }

        try (BufferedSink sink = Okio.buffer(Okio.sink(file)))
        {
            logger.info("Updating snapshot {}", file);
            sink.writeString(actual.toPrettyString(), StandardCharsets.UTF_8);
        }
    }

    private String getFilePath(String suffix)
    {
        Class<?> currentClass = testInfo.getTestClass().orElseThrow(AssertionError::new);
        Method testMethod = testInfo.getTestMethod().orElseThrow(AssertionError::new);
        String fileName = currentClass.getSimpleName() + "/" + testMethod.getName();
        if (suffix != null && !suffix.isEmpty())
            fileName += "_" + suffix;
        fileName += ".json";
        return fileName;
    }
}
