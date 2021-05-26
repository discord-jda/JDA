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

package net.dv8tion.jda.api.requests.restaction.interactions;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * A {@link CallbackAction} which can be used to edit the message for an interaction.
 */
public interface UpdateAction extends CallbackAction
{
    /**
     * Set the new content for this message.
     *
     * @param  content
     *         The new message content
     *
     * @throws IllegalArgumentException
     *         If the provided content is longer than {@link net.dv8tion.jda.api.entities.Message#MAX_CONTENT_LENGTH MAX_CONTENT_LENGTH} characters
     *
     * @return The same update action, for chaining convenience
     */
    @Nonnull
    UpdateAction setContent(@Nullable final String content);

    /**
     * Set the {@link MessageEmbed MessageEmbeds} for the message
     *
     * @param  embeds
     *         The message embeds
     *
     * @throws IllegalArgumentException
     *         If null is provided, or one of the embeds is too big
     *
     * @return The same update action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default UpdateAction setEmbeds(@Nonnull MessageEmbed... embeds)
    {
        Checks.noneNull(embeds, "MessageEmbed");
        return setEmbeds(Arrays.asList(embeds));
    }

    /**
     * Set the {@link MessageEmbed MessageEmbeds} for the message
     *
     * @param  embeds
     *         The message embeds
     *
     * @throws IllegalArgumentException
     *         If null is provided, or one of the embeds is too big
     *
     * @return The same update action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    UpdateAction setEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds);

    /**
     * Set the action rows for the message.
     *
     * @param  rows
     *         The new action rows
     *
     * @throws IllegalArgumentException
     *         If null is provided or more than 5 actions rows are provided
     *
     * @return The same update action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default UpdateAction setActionRows(@Nonnull Collection<? extends ActionRow> rows)
    {
        Checks.noneNull(rows, "ActionRows");
        return setActionRows(rows.toArray(new ActionRow[0]));
    }

    /**
     * Set the action rows for the message.
     *
     * @param  rows
     *         The new action rows
     *
     * @throws IllegalArgumentException
     *         If null is provided or more than 5 actions rows are provided
     *
     * @return The same update action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    UpdateAction setActionRows(@Nonnull ActionRow... rows);
}
