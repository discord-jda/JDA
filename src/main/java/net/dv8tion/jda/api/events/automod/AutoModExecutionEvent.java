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

package net.dv8tion.jda.api.events.automod;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.automod.AutoModExecution;
import net.dv8tion.jda.api.entities.automod.AutoModResponse;
import net.dv8tion.jda.api.entities.automod.AutoModRule;
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that an automated {@link AutoModResponse} has been triggered through an {@link AutoModRule}.
 *
 * <p><b>Requirements</b><br>
 * This event requires the {@link GatewayIntent#AUTO_MODERATION_EXECUTION AUTO_MODERATION_EXECUTION} intent to be enabled.
 * Additionally, access to {@link #getContent()} and {@link #getMatchedContent()} requires the {@link GatewayIntent#MESSAGE_CONTENT MESSAGE_CONTENT} intent to be enabled.
 */
public class AutoModExecutionEvent extends Event implements AutoModExecution
{
    private final AutoModExecution execution;

    public AutoModExecutionEvent(@Nonnull JDA api, long responseNumber, @Nonnull AutoModExecution execution)
    {
        super(api, responseNumber);
        this.execution = execution;
    }
    
    @Nonnull
    @Override
    public Guild getGuild()
    {
        return execution.getGuild();
    }
    
    @Nullable
    @Override
    public GuildMessageChannelUnion getChannel()
    {
        return execution.getChannel();
    }

    @Nonnull
    @Override
    public AutoModResponse getResponse()
    {
        return execution.getResponse();
    }

    @Nonnull
    @Override
    public AutoModTriggerType getTriggerType()
    {
        return execution.getTriggerType();
    }

    @Override
    public long getUserIdLong()
    {
        return execution.getUserIdLong();
    }

    @Override
    public long getRuleIdLong()
    {
        return execution.getRuleIdLong();
    }

    @Override
    public long getMessageIdLong()
    {
        return execution.getMessageIdLong();
    }

    @Override
    public long getAlertMessageIdLong()
    {
        return execution.getAlertMessageIdLong();
    }

    @Nonnull
    @Override
    public String getContent()
    {
        return execution.getContent();
    }

    @Nullable
    @Override
    public String getMatchedContent()
    {
        return execution.getMatchedContent();
    }

    @Nullable
    @Override
    public String getMatchedKeyword()
    {
        return execution.getMatchedKeyword();
    }
}
