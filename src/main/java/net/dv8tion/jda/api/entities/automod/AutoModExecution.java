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
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Event triggered by an {@link AutoModRule} activation.
 */
public interface AutoModExecution
{
    /**
     * The {@link Guild} that this execution occurred in.
     *
     * @return The {@link Guild}
     */
    @Nonnull
    Guild getGuild();

    /**
     * The {@link GuildMessageChannelUnion} that this execution occurred in.
     *
     * <p>This might be {@code null} if the execution occurred by future event types.
     *
     * @return The {@link GuildMessageChannelUnion}
     */
    @Nullable
    GuildMessageChannelUnion getChannel();

    /**
     * The {@link AutoModResponse} that has been triggered by this execution.
     *
     * @return The {@link AutoModResponse}
     */
    @Nonnull
    AutoModResponse getResponse();

    /**
     * The {@link AutoModTriggerType} for the execution.
     *
     * @return The {@link AutoModTriggerType}
     */
    @Nonnull
    AutoModTriggerType getTriggerType();

    /**
     * The id of the user that triggered this execution.
     *
     * @return The id of the user
     */
    long getUserIdLong();

    /**
     * The id of the user that triggered this execution.
     *
     * @return The id of the user
     */
    @Nonnull
    default String getUserId()
    {
        return Long.toUnsignedString(getUserIdLong());
    }

    /**
     * The id of the {@link AutoModRule} which has been triggered.
     *
     * @return The id of the rule
     */
    long getRuleIdLong();

    /**
     * The id of the {@link AutoModRule} which has been triggered.
     *
     * @return The id of the rule
     */
    @Nonnull
    default String getRuleId()
    {
        return Long.toUnsignedString(getRuleIdLong());
    }

    /**
     * The id of the {@link net.dv8tion.jda.api.entities.Message Message} which triggered the rule.
     *
     * @return The id of the message, or 0 if the message has been blocked
     */
    long getMessageIdLong();

    /**
     * The id of the {@link net.dv8tion.jda.api.entities.Message Message} which triggered the rule.
     *
     * @return The id of the message, or {@code null} if the message has been blocked
     */
    @Nullable
    default String getMessageId()
    {
        long id = getMessageIdLong();
        return id == 0L ? null : Long.toUnsignedString(getMessageIdLong());
    }

    /**
     * The id of the alert {@link net.dv8tion.jda.api.entities.Message Message} sent to the alert channel.
     *
     * @return The id of the alert message, or 0 if {@link AutoModResponse#getType()} is not {@link AutoModResponse.Type#SEND_ALERT_MESSAGE}
     */
    long getAlertMessageIdLong();

    /**
     * The id of the alert {@link net.dv8tion.jda.api.entities.Message Message} sent to the alert channel.
     *
     * @return The id of the alert message, or {@code null} if {@link AutoModResponse#getType()} is not {@link AutoModResponse.Type#SEND_ALERT_MESSAGE}
     */
    @Nullable
    default String getAlertMessageId()
    {
        long id = getAlertMessageIdLong();
        return id == 0L ? null : Long.toUnsignedString(getAlertMessageIdLong());
    }

    /**
     * The user content that triggered this rule.
     *
     * <p>This is empty if {@link GatewayIntent#MESSAGE_CONTENT} is not enabled.
     * However, you can still use {@link #getMatchedKeyword()} regardless.
     *
     * @return The user content
     */
    @Nonnull
    String getContent();

    /**
     * The substring match of the user content that triggered this rule.
     *
     * <p>This is empty if {@link GatewayIntent#MESSAGE_CONTENT} is not enabled.
     * However, you can still use {@link #getMatchedKeyword()} regardless.
     *
     * @return The user content substring
     */
    @Nullable
    String getMatchedContent();

    /**
     * The keyword that was found in the {@link #getContent()}.
     *
     * @return The keyword that was found in the content
     */
    @Nullable
    String getMatchedKeyword();
}
