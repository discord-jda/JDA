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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;
import net.dv8tion.jda.api.entities.sticker.Sticker;
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateRequest;
import net.dv8tion.jda.internal.requests.restaction.MessageCreateActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public interface MessageCreateAction extends MessageCreateRequest<MessageCreateAction>, RestAction<Message>
{
    /**
     * Sets the default value for {@link #failOnInvalidReply(boolean)}
     *
     * <p>Default: <b>false</b>
     *
     * @param fail
     *        True, to throw a exception if the referenced message does not exist
     */
    static void setDefaultFailOnInvalidReply(boolean fail)
    {
        MessageCreateActionImpl.setDefaultFailOnInvalidReply(fail);
    }

    @Nonnull
    @Override
    MessageCreateAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    default MessageCreateAction addCheck(@Nonnull BooleanSupplier checks)
    {
        return (MessageCreateAction) RestAction.super.addCheck(checks);
    }

    @Nonnull
    @Override
    default MessageCreateAction timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (MessageCreateAction) RestAction.super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    default MessageCreateAction deadline(long timestamp)
    {
        return (MessageCreateAction) RestAction.super.deadline(timestamp);
    }

    @Nonnull
    MessageCreateAction setNonce(@Nullable String nonce);

    @Nonnull
    MessageCreateAction setMessageReference(@Nullable String messageId);

    @Nonnull
    default MessageCreateAction setMessageReference(long messageId)
    {
        return setMessageReference(Long.toUnsignedString(messageId));
    }

    @Nonnull
    default MessageCreateAction setMessageReference(@Nullable Message message)
    {
        return setMessageReference(message == null ? null : message.getId());
    }

    /**
     * Whether to throw a exception if the referenced message does not exist, when replying to a message.
     * <br>This only matters in combination with {@link #setMessageReference(Message)} and {@link #setMessageReference(long)}!
     *
     * <p>This is false by default but can be configured using {@link #setDefaultFailOnInvalidReply(boolean)}!
     *
     * @param  fail
     *         True, to throw a exception if the referenced message does not exist
     *
     * @return Updated MessageCreateAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    MessageCreateAction failOnInvalidReply(boolean fail);

    /**
     * Set the stickers to send alongside this message.
     * <br>This is not supported for message edits.
     *
     * @param  stickers
     *         The stickers to send, or null to not send any stickers
     *
     * @throws IllegalStateException
     *         If this request is a message edit request
     * @throws IllegalArgumentException
     *         <ul>
     *           <li>If any of the provided stickers is a {@link GuildSticker},
     *               which is either {@link GuildSticker#isAvailable() unavailable} or from a different guild.</li>
     *           <li>If the collection has more than {@value Message#MAX_STICKER_COUNT} stickers</li>
     *           <li>If a collection with null entries is provided</li>
     *         </ul>
     *
     * @return Updated MessageCreateAction for chaining convenience
     *
     * @see    Sticker#fromId(long)
     */
    @Nonnull
    @CheckReturnValue
    MessageCreateAction setStickers(@Nullable Collection<? extends StickerSnowflake> stickers);

    /**
     * Set the stickers to send alongside this message.
     * <br>This is not supported for message edits.
     *
     * @param  stickers
     *         The stickers to send, or null to not send any stickers
     *
     * @throws IllegalStateException
     *         If this request is a message edit request
     * @throws IllegalArgumentException
     *         <ul>
     *           <li>If any of the provided stickers is a {@link GuildSticker},
     *               which is either {@link GuildSticker#isAvailable() unavailable} or from a different guild.</li>
     *           <li>If the collection has more than {@value Message#MAX_STICKER_COUNT} stickers</li>
     *           <li>If a collection with null entries is provided</li>
     *         </ul>
     *
     * @return Updated MessageCreateAction for chaining convenience
     *
     * @see    Sticker#fromId(long)
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction setStickers(@Nullable StickerSnowflake... stickers)
    {
        if (stickers != null)
            Checks.noneNull(stickers, "Sticker");
        return setStickers(stickers == null ? null : Arrays.asList(stickers));
    }
}
