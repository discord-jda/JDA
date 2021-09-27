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

package net.dv8tion.jda.api.interactions.components;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.requests.restaction.interactions.UpdateInteractionAction;
import net.dv8tion.jda.internal.requests.restaction.interactions.UpdateInteractionActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interaction on a message {@link Component}.
 *
 * <p>Instead of {@link #deferReply()} and {@link #reply(String)} you can use {@link #deferEdit()} and {@link #editMessage(String)} with these interactions!
 * <b>You can only acknowledge an interaction once!</b>
 */
public interface ComponentInteraction extends Interaction
{
    /**
     * The custom component Id provided to the component when it was originally created.
     * <br>This value should be used to determine what action to take in regards to this interaction.
     *
     * <br>This id does not have to be numerical.
     *
     * @return The component ID
     */
    @Nonnull
    String getComponentId();

    /**
     * The {@link Component} instance.
     * <br>This is null on interactions for ephemeral messages.
     *
     * @return The {@link Component}, or null if this message is ephemeral
     */
    @Nullable
    Component getComponent();

    /**
     * The {@link Message} instance.
     *
     * @return The {@link Message}
     */
    @Nonnull
    Message getMessage();

    /**
     * The id of the message.
     *
     * @return The message id
     */
    long getMessageIdLong();

    /**
     * The id of the message.
     *
     * @return The message id
     */
    @Nonnull
    default String getMessageId()
    {
        return Long.toUnsignedString(getMessageIdLong());
    }

    /**
     * The {@link Component.Type}
     *
     * @return The {@link Component.Type}
     */
    @Nonnull
    Component.Type getComponentType();

    /**
     * The respective {@link MessageChannel} for this interaction.
     *
     * @return The {@link MessageChannel}
     */
    @Nonnull
    @Override
    MessageChannel getChannel();

    /**
     * No-op acknowledgement of this interaction.
     * <br>This tells discord you intend to update the message that the triggering component is a part of using the {@link #getHook() InteractionHook} instead of sending a reply message.
     * You are not required to actually update the message, this will simply acknowledge that you accepted the interaction.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>Use {@link #editMessage(String)} to edit it directly.
     *
     * @return {@link UpdateInteractionAction} that can be used to update the message
     *
     * @see    #editMessage(String)
     */
    @Nonnull
    @CheckReturnValue
    UpdateInteractionAction deferEdit();

    /**
     * Acknowledgement of this interaction with a message update.
     * <br>You can use {@link #getHook()} to edit the message further.
     *
     * <p><b>You can only use deferEdit() or editMessage() once per interaction!</b> Use {@link #getHook()} for any additional updates.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     *
     * @param  message
     *         The new message content to use
     *
     * @throws IllegalArgumentException
     *         If the provided message is null
     *
     * @return {@link UpdateInteractionAction} that can be used to further update the message
     */
    @Nonnull
    @CheckReturnValue
    default UpdateInteractionAction editMessage(@Nonnull Message message)
    {
        Checks.notNull(message, "Message");
        UpdateInteractionActionImpl action = (UpdateInteractionActionImpl) deferEdit();
        return action.applyMessage(message);
    }

    /**
     * Acknowledgement of this interaction with a message update.
     * <br>You can use {@link #getHook()} to edit the message further.
     *
     * <p><b>You can only use deferEdit() or editMessage() once per interaction!</b> Use {@link #getHook()} for any additional updates.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     *
     * @param  content
     *         The new message content to use
     *
     * @throws IllegalArgumentException
     *         If the provided content is null
     *
     * @return {@link UpdateInteractionAction} that can be used to further update the message
     */
    @Nonnull
    @CheckReturnValue
    default UpdateInteractionAction editMessage(@Nonnull String content)
    {
        Checks.notNull(content, "Content");
        return deferEdit().setContent(content);
    }

    /**
     * Acknowledgement of this interaction with a message update.
     * <br>You can use {@link #getHook()} to edit the message further.
     *
     * <p><b>You can only use deferEdit() or editMessage() once per interaction!</b> Use {@link #getHook()} for any additional updates.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     *
     * @param  components
     *         The new message components, such as {@link ActionRow}
     *
     * @throws IllegalArgumentException
     *         If the provided components are null
     *
     * @return {@link UpdateInteractionAction} that can be used to further update the message
     */
    @Nonnull
    @CheckReturnValue
    default UpdateInteractionAction editComponents(@Nonnull Collection<? extends ComponentLayout> components)
    {
        Checks.noneNull(components, "Components");
        if (components.stream().anyMatch(it -> !(it instanceof ActionRow)))
            throw new UnsupportedOperationException("The provided component layout is not supported");
        List<ActionRow> actionRows = components.stream().map(ActionRow.class::cast).collect(Collectors.toList());
        return deferEdit().setActionRows(actionRows);
    }

    /**
     * Acknowledgement of this interaction with a message update.
     * <br>You can use {@link #getHook()} to edit the message further.
     *
     * <p><b>You can only use deferEdit() or editMessage() once per interaction!</b> Use {@link #getHook()} for any additional updates.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     *
     * @param  components
     *         The new message components, such as {@link ActionRow}
     *
     * @throws IllegalArgumentException
     *         If the provided components are null
     *
     * @return {@link UpdateInteractionAction} that can be used to further update the message
     */
    @Nonnull
    @CheckReturnValue
    default UpdateInteractionAction editComponents(@Nonnull ComponentLayout... components)
    {
        Checks.noneNull(components, "ComponentLayouts");
        return editComponents(Arrays.asList(components));
    }

    /**
     * Acknowledgement of this interaction with a message update.
     * <br>You can use {@link #getHook()} to edit the message further.
     *
     * <p><b>You can only use deferEdit() or editMessage() once per interaction!</b> Use {@link #getHook()} for any additional updates.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     *
     * @param  embeds
     *         The new {@link MessageEmbed MessageEmbeds}
     *
     * @throws IllegalArgumentException
     *         If any of the provided embeds is null
     *
     * @return {@link UpdateInteractionAction} that can be used to further update the message
     */
    @Nonnull
    @CheckReturnValue
    default UpdateInteractionAction editMessageEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        Checks.noneNull(embeds, "MessageEmbed");
        return deferEdit().setEmbeds(embeds);
    }

    /**
     * Acknowledgement of this interaction with a message update.
     * <br>You can use {@link #getHook()} to edit the message further.
     *
     * <p><b>You can only use deferEdit() or editMessage() once per interaction!</b> Use {@link #getHook()} for any additional updates.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     *
     * @param  embeds
     *         The new message embeds to include in the message
     *
     * @throws IllegalArgumentException
     *         If any of the provided embeds is null
     *
     * @return {@link UpdateInteractionAction} that can be used to further update the message
     */
    @Nonnull
    @CheckReturnValue
    default UpdateInteractionAction editMessageEmbeds(@Nonnull MessageEmbed... embeds)
    {
        Checks.noneNull(embeds, "MessageEmbed");
        return deferEdit().setEmbeds(embeds);
    }

    /**
     * Acknowledgement of this interaction with a message update.
     * <br>You can use {@link #getHook()} to edit the message further.
     *
     * <p><b>You can only use deferEdit() or editMessage() once per interaction!</b> Use {@link #getHook()} for any additional updates.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     *
     * @param  format
     *         The format string for the new message content
     * @param  args
     *         The format arguments
     *
     * @throws IllegalArgumentException
     *         If the provided format is null
     *
     * @return {@link UpdateInteractionAction} that can be used to further update the message
     */
    @Nonnull
    @CheckReturnValue
    default UpdateInteractionAction editMessageFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return editMessage(String.format(format, args));
    }
}
