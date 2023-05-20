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

package net.dv8tion.jda.internal.entities.automod;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.automod.AutoModExecution;
import net.dv8tion.jda.api.entities.automod.AutoModResponse;
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AutoModExecutionImpl implements AutoModExecution
{
    private final Guild guild;
    private final GuildMessageChannel channel;
    private final AutoModResponse response;
    private final AutoModTriggerType type;
    private final long userId, ruleId, messageId, alertMessageId;
    private final String content, matchedContent, matchedKeyword;

    public AutoModExecutionImpl(Guild guild, DataObject json)
    {
        this.guild = guild;
        this.channel = guild.getChannelById(GuildMessageChannel.class, json.getUnsignedLong("channel_id"));
        this.response = new AutoModResponseImpl(guild, json.getObject("action"));
        this.type = AutoModTriggerType.fromKey(json.getInt("rule_trigger_type", -1));
        this.userId = json.getUnsignedLong("user_id");
        this.ruleId = json.getUnsignedLong("rule_id");
        this.messageId = json.getUnsignedLong("message_id", 0L);
        this.alertMessageId = json.getUnsignedLong("alert_system_message_id", 0L);
        this.content = json.getString("content", "");
        this.matchedContent = json.getString("matched_content", null);
        this.matchedKeyword = json.getString("matched_keyword", null);
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Nullable
    @Override
    public GuildMessageChannelUnion getChannel()
    {
        return (GuildMessageChannelUnion) channel;
    }

    @Nonnull
    @Override
    public AutoModResponse getResponse()
    {
        return response;
    }

    @Nonnull
    @Override
    public AutoModTriggerType getTriggerType()
    {
        return type;
    }

    @Override
    public long getUserIdLong()
    {
        return userId;
    }

    @Override
    public long getRuleIdLong()
    {
        return ruleId;
    }

    @Override
    public long getMessageIdLong()
    {
        return messageId;
    }

    @Override
    public long getAlertMessageIdLong()
    {
        return alertMessageId;
    }

    @Nonnull
    @Override
    public String getContent()
    {
        return content;
    }

    @Nullable
    @Override
    public String getMatchedContent()
    {
        return matchedContent;
    }

    @Nullable
    @Override
    public String getMatchedKeyword()
    {
        return matchedKeyword;
    }
}
