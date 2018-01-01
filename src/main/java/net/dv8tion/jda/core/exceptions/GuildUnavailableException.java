/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.core.exceptions;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.Guild Guild} is not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
 * <br>Thrown when an operation requires a Guild to be available and {@link net.dv8tion.jda.core.entities.Guild#isAvailable() Guild#isAvailable()} is {@code false}
 */
public class GuildUnavailableException extends RuntimeException
{
    /**
     * Creates a new GuildUnavailableException instance
     */
    public GuildUnavailableException()
    {
        this("This operation is not possible due to the Guild being temporarily unavailable");
    }

    /**
     * Creates a new GuildUnavailableException instance
     *
     * @param reason
     *        The reason for this Exception
     */
    public GuildUnavailableException(String reason)
    {
        super(reason);
    }
}
