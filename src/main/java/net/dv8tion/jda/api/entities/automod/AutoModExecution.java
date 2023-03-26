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

package net.dv8tion.jda.api.entities.automod;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface AutoModExecution
{
    @Nonnull
    Guild getGuild();

    @Nullable
    GuildMessageChannelUnion getChannel();

    @Nonnull
    AutoModResponse getResponse();

    @Nonnull
    AutoModTriggerType getTriggerType();

    long getUserIdLong();

    @Nonnull
    default String getUserId()
    {
        return Long.toUnsignedString(getUserIdLong());
    }

    long getRuleIdLong();

    @Nonnull
    default String getRuleId()
    {
        return Long.toUnsignedString(getRuleIdLong());
    }

    long getMessageIdLong();

    @Nullable
    default String getMessageId()
    {
        long id = getMessageIdLong();
        return id == 0L ? null : Long.toUnsignedString(getMessageIdLong());
    }

    long getAlertMessageIdLong();

    @Nullable
    default String getAlertMessageId()
    {
        long id = getAlertMessageIdLong();
        return id == 0L ? null : Long.toUnsignedString(getAlertMessageIdLong());
    }

    @Nonnull
    String getContent();

    @Nullable
    String getMatchedContent();

    @Nullable
    String getMatchedKeyword();
}
