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

package net.dv8tion.jda.internal.interactions.component;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.PremiumRequiredCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import net.dv8tion.jda.internal.interactions.DeferrableInteractionImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.MessageEditCallbackActionImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.ModalCallbackActionImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.PremiumRequiredCallbackActionImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.ReplyCallbackActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

public abstract class ComponentInteractionImpl extends DeferrableInteractionImpl implements ComponentInteraction
{
    protected final String customId;
    protected final Message message;
    protected final long messageId;

    public ComponentInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
        this.customId = data.getObject("data").getString("custom_id");
        // message might be just id and flags for ephemeral messages in which case our "message" is null
        DataObject messageJson = data.getObject("message");
        messageId = messageJson.getUnsignedLong("id");

        if (messageJson.isNull("type"))
        {
            message = null;
        }
        else
        {
            Guild guild = getGuild();
            MessageChannel channel = getChannel();
            message = jda.getEntityBuilder().createMessageBestEffort(messageJson, channel, guild);
            // We assume that component interactions come from messages the bot sent
            ((ReceivedMessage) message).withHook(getHook());
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public MessageChannelUnion getChannel()
    {
        return (MessageChannelUnion) super.getChannel();
    }

    @Nonnull
    @Override
    public String getComponentId()
    {
        return customId;
    }

    @Nonnull
    @Override
    public Message getMessage()
    {
        return message;
    }

    @Override
    public long getMessageIdLong()
    {
        return messageId;
    }

    @Nonnull
    @Override
    public MessageEditCallbackActionImpl deferEdit()
    {
        return new MessageEditCallbackActionImpl(this.hook);
    }

    @Nonnull
    @Override
    public ReplyCallbackAction deferReply()
    {
        return new ReplyCallbackActionImpl(this.hook);
    }

    @Nonnull
    @Override
    public ModalCallbackAction replyModal(@Nonnull Modal modal)
    {
        Checks.notNull(modal, "Modal");

        return new ModalCallbackActionImpl(this, modal);
    }

    @Nonnull
    @Override
    public PremiumRequiredCallbackAction replyWithPremiumRequired()
    {
        return new PremiumRequiredCallbackActionImpl(this);
    }
}
