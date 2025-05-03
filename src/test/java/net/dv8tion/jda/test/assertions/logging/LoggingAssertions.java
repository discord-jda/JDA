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

package net.dv8tion.jda.test.assertions.logging;

import net.dv8tion.jda.test.util.SnapshotHandler;
import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggingAssertions
{
    private final SnapshotHandler snapshotHandler;
    private final List<String> logs;

    public LoggingAssertions(SnapshotHandler snapshotHandler, List<String> logs)
    {
        this.snapshotHandler = snapshotHandler;
        this.logs = logs;
    }

    @Contract("->this")
    public LoggingAssertions isEmpty()
    {
        assertThat(logs).isEmpty();
        return this;
    }

    @Contract("->this")
    public LoggingAssertions matchesSnapshot()
    {
        return matchesSnapshot(null);
    }

    @Contract("->this")
    public LoggingAssertions matchesSnapshot(String suffix)
    {
        this.snapshotHandler.compareWithSnapshot(String.join("\n", logs), suffix);
        return this;
    }

    @Contract("_->this")
    public LoggingAssertions containsLine(String line)
    {
        assertThat(logs).contains(line);
        return this;
    }

    @Contract("_->this")
    public LoggingAssertions doesNotContainLineMatching(Predicate<? super String> predicate)
    {
        assertThat(logs).noneMatch(predicate);
        return this;
    }
}
