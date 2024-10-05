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

package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.message.MessageCreateBuilderMixin;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class MessageCreateActionImpl extends RestActionImpl<Message> implements MessageCreateAction, MessageCreateBuilderMixin<MessageCreateAction>
{
    protected static final SecureRandom nonceGenerator = new SecureRandom();
    protected static boolean defaultFailOnInvalidReply = false;

    private final MessageChannel channel;
    private final MessageCreateBuilder builder = new MessageCreateBuilder();
    private final List<String> stickers = new ArrayList<>();
    private String nonce;
    private MessageReferenceData messageReference;
    private boolean failOnInvalidReply = defaultFailOnInvalidReply;

    public static void setDefaultFailOnInvalidReply(boolean fail)
    {
        defaultFailOnInvalidReply = fail;
    }

    public MessageCreateActionImpl(MessageChannel channel)
    {
        super(channel.getJDA(), Route.Messages.SEND_MESSAGE.compile(channel.getId()));
        this.channel = channel;
    }


    @Override
    public MessageCreateBuilder getBuilder()
    {
        return builder;
    }

    @Override
    protected RequestBody finalizeData()
    {
        if (builder.isEmpty())
        {
            // Special cases where builder is empty but can still send message on this endpoint
            DataObject body = DataObject.empty().put("flags", builder.getMessageFlagsRaw());
            populateBody(body);

            if (!stickers.isEmpty() || messageReference != null && messageReference.type == MessageReference.MessageReferenceType.FORWARD)
                return getRequestBody(body);

            throw new IllegalStateException("Cannot build empty messages! Must provide at least one of: content, embed, file, poll, or stickers");
        }

        try (MessageCreateData data = builder.build())
        {
            DataObject json = data.toData();
            populateBody(json);

            return getMultipartBody(data.getFiles(), json);
        }
    }

    private void populateBody(DataObject json)
    {
        json.put("enforce_nonce", true);
        if (nonce != null && !nonce.isEmpty())
            json.put("nonce", nonce);
        else
            json.put("nonce", Long.toUnsignedString(nonceGenerator.nextLong()));
        if (stickers != null && !stickers.isEmpty())
            json.put("sticker_ids", stickers);
        if (messageReference != null)
        {
            json.put("message_reference", messageReference.toData()
                .put("fail_if_not_exists", failOnInvalidReply)
            );
        }
    }

    @Override
    protected void handleSuccess(Response response, Request<Message> request)
    {
        request.onSuccess(api.getEntityBuilder().createMessageWithChannel(response.getObject(), channel, false));
    }

    @Nonnull
    @Override
    public MessageCreateAction setNonce(@Nullable String nonce)
    {
        if (nonce != null)
            Checks.notLonger(nonce, Message.MAX_NONCE_LENGTH, "Nonce");
        this.nonce = nonce;
        return this;
    }

    @Nonnull
    @Override
    public MessageCreateAction setMessageReference(@Nonnull MessageReference.MessageReferenceType type, @Nullable String guildId, @Nonnull String channelId, @Nonnull String messageId)
    {
        Checks.notNull(type, "Type");
        if (guildId != null)
            Checks.isSnowflake(guildId, "Guild ID");
        Checks.isSnowflake(channelId, "Channel ID");
        Checks.isSnowflake(messageId, "Message ID");
        Checks.check(type != MessageReference.MessageReferenceType.UNKNOWN, "Cannot create a message reference of UNKNOWN type");
        this.messageReference = new MessageReferenceData(type, guildId, channelId, messageId);
        return this;
    }

    @Nonnull
    @Override
    public MessageCreateAction setMessageReference(@Nullable String messageId)
    {
        if (messageId != null)
            Checks.isSnowflake(messageId);
        String guildId = null;
        if (channel instanceof GuildChannel)
            guildId = ((GuildChannel) channel).getGuild().getId();
        this.messageReference = new MessageReferenceData(MessageReference.MessageReferenceType.DEFAULT, guildId, channel.getId(), messageId);
        return this;
    }

    @Nonnull
    @Override
    public MessageCreateAction failOnInvalidReply(boolean fail)
    {
        failOnInvalidReply = fail;
        return this;
    }

    @Nonnull
    @Override
    public MessageCreateAction setStickers(@Nullable Collection<? extends StickerSnowflake> stickers)
    {
        this.stickers.clear();
        if (stickers == null || stickers.isEmpty())
            return this;

        if (!channel.getType().isGuild())
            throw new IllegalStateException("Cannot send stickers in direct messages!");

        GuildChannel guildChannel = (GuildChannel) channel;

        Checks.noneNull(stickers, "Stickers");
        Checks.check(stickers.size() <= Message.MAX_STICKER_COUNT,
                "Cannot send more than %d stickers in a message!", Message.MAX_STICKER_COUNT);
        for (StickerSnowflake sticker : stickers)
        {
            if (sticker instanceof GuildSticker)
            {
                GuildSticker guildSticker = (GuildSticker) sticker;
                Checks.check(guildSticker.isAvailable(),
                        "Cannot use unavailable sticker. The guild may have lost the boost level required to use this sticker!");
                Checks.check(guildSticker.getGuildIdLong() == guildChannel.getGuild().getIdLong(),
                        "Sticker must be from the same guild. Cross-guild sticker posting is not supported!");
            }
        }

        this.stickers.addAll(stickers.stream().map(StickerSnowflake::getId).collect(Collectors.toList()));
        return this;
    }

    @Nonnull
    @Override
    public MessageCreateAction setCheck(BooleanSupplier checks)
    {
        return (MessageCreateAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public MessageCreateAction deadline(long timestamp)
    {
        return (MessageCreateAction) super.deadline(timestamp);
    }

    private class MessageReferenceData implements SerializableData
    {
        private final MessageReference.MessageReferenceType type;
        private final String messageId;
        private final String channelId;
        private final String guildId;

        private MessageReferenceData(MessageReference.MessageReferenceType type, String guildId, String channelId, String messageId)
        {
            this.type = type;
            this.messageId = messageId;
            this.guildId = guildId;
            this.channelId = channelId;
        }

        @Nonnull
        @Override
        public DataObject toData()
        {
            DataObject data = DataObject.empty()
                    .put("type", type.getId())
                    .put("message_id", messageId)
                    .put("channel_id", channelId);
            if (guildId != null)
                data.put("guild_id", guildId);
            return data;
        }
    }
}
