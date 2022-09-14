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

package net.dv8tion.jda.api.entities.channel.forums;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.requests.restaction.ForumPostAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.entities.ForumTagSnowflakeImpl;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Minimal representation for a forum tag.
 * <br>This is primarily useful for creating posts with {@link ForumPostAction#setTags(Collection)}.
 */
public interface ForumTagSnowflake extends ISnowflake
{
    /**
     * Wraps the provided id into a ForumTagSnowflake instance.
     *
     * @param  id
     *         The id of an existing forum tag
     *
     * @return ForumTagSnowflake instance for the provided id
     */
    @Nonnull
    static ForumTagSnowflake fromId(long id)
    {
        return new ForumTagSnowflakeImpl(id);
    }

    /**
     * Wraps the provided id into a ForumTagSnowflake instance.
     *
     * @param  id
     *         The id of an existing forum tag
     *
     * @throws IllegalArgumentException
     *         If the provided id is not a valid snowflake
     *
     * @return ForumTagSnowflake instance for the provided id
     */
    @Nonnull
    static ForumTagSnowflake fromId(@Nonnull String id)
    {
        return new ForumTagSnowflakeImpl(MiscUtil.parseSnowflake(id));
    }
}
