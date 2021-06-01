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
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * A {@link InteractionCallbackAction} which can be used to edit the message for an interaction.
 */
public interface UpdateInteractionAction extends InteractionCallbackAction
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
    UpdateInteractionAction setContent(@Nullable final String content);

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
    default UpdateInteractionAction setEmbeds(@Nonnull MessageEmbed... embeds)
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
     *         If null is provided, one of the embeds is too big, or more than 10 embeds are provided
     *
     * @return The same update action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    UpdateInteractionAction setEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds);

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
    default UpdateInteractionAction setActionRows(@Nonnull Collection<? extends ActionRow> rows)
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
    UpdateInteractionAction setActionRows(@Nonnull ActionRow... rows);

    /**
     * Set only one action row for convenience.
     *
     * @param  components
     *         The action row components, such as {@link net.dv8tion.jda.api.interactions.components.Button Buttons}
     *
     * @throws IllegalArgumentException
     *         If null or more than 5 components are provided
     *
     * @return The same update action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default UpdateInteractionAction setActionRow(@Nonnull Component... components)
    {
        return setActionRows(ActionRow.of(components));
    }

    /**
     * Set only one action row for convenience.
     *
     * @param  components
     *         The action row components, such as {@link net.dv8tion.jda.api.interactions.components.Button Buttons}
     *
     * @throws IllegalArgumentException
     *         If null or more than 5 components are provided
     *
     * @return The same update action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    default UpdateInteractionAction setActionRow(@Nonnull Collection<? extends Component> components)
    {
        return setActionRows(ActionRow.of(components));
    }

/////// This is waiting for https://github.com/discord/discord-api-docs/issues/3048
//
//    /**
//     * Removes all attachments that are currently attached to the existing message except for the ones provided.
//     * <br>For example {@code retainFilesById(Arrays.asList("123"))} would remove all attachments except for the one with the id 123.
//     *
//     * <p>To remove all attachments from the message you can pass an empty list.
//     *
//     * @param  ids
//     *         The ids for the attachments which should be retained on the message
//     *
//     * @throws IllegalArgumentException
//     *         If any of the ids is null or not a valid snowflake
//     *
//     * @return The same update action, for chaining convenience
//     */
//    @Nonnull
//    @CheckReturnValue
//    UpdateInteractionAction retainFilesById(@Nonnull Collection<String> ids);
//
//    /**
//     * Removes all attachments that are currently attached to the existing message except for the ones provided.
//     * <br>For example {@code retainFilesById(Arrays.asList("123"))} would remove all attachments except for the one with the id 123.
//     *
//     * <p>To remove all attachments from the message you can pass an empty list.
//     *
//     * @param  ids
//     *         The ids for the attachments which should be retained on the message
//     *
//     * @throws IllegalArgumentException
//     *         If any of the ids is null or not a valid snowflake
//     *
//     * @return The same update action, for chaining convenience
//     */
//    @Nonnull
//    @CheckReturnValue
//    default UpdateInteractionAction retainFilesById(@Nonnull String... ids)
//    {
//        Checks.notNull(ids, "IDs");
//        return retainFilesById(Arrays.asList(ids));
//    }
//
//    /**
//     * Removes all attachments that are currently attached to the existing message except for the ones provided.
//     * <br>For example {@code retainFilesById(Arrays.asList("123"))} would remove all attachments except for the one with the id 123.
//     *
//     * <p>To remove all attachments from the message you can pass an empty list.
//     *
//     * @param  ids
//     *         The ids for the attachments which should be retained on the message
//     *
//     * @throws IllegalArgumentException
//     *         If any of the ids is null or not a valid snowflake
//     *
//     * @return The same update action, for chaining convenience
//     */
//    @Nonnull
//    @CheckReturnValue
//    default UpdateInteractionAction retainFilesById(long... ids)
//    {
//        Checks.notNull(ids, "IDs");
//        return retainFilesById(Arrays
//                .stream(ids)
//                .mapToObj(Long::toUnsignedString)
//                .collect(Collectors.toList())
//        );
//    }
//
//    /**
//     * Removes all attachments that are currently attached to the existing message except for the ones provided.
//     * <br>For example {@code retainFiles(message.getAttachments().subList(1, message.getAttachments().size()))} would only remove the first attachment from the message.
//     *
//     * <p>To remove all attachments from the message you can pass an empty list.
//     *
//     * @param  attachments
//     *         The attachments which should be retained on the message
//     *
//     * @throws IllegalArgumentException
//     *         If any of the ids is null or not a valid snowflake
//     *
//     * @return The same update action, for chaining convenience
//     */
//    @Nonnull
//    @CheckReturnValue
//    default UpdateInteractionAction retainFiles(@Nonnull Collection<? extends Message.Attachment> attachments)
//    {
//        Checks.noneNull(attachments, "Attachments");
//        return retainFilesById(attachments
//                .stream()
//                .map(Message.Attachment::getId)
//                .collect(Collectors.toList())
//        );
//    }
}
