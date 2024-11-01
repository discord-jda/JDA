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

package net.dv8tion.jda.internal.interactions.modal;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.LaunchActivityCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.interactions.DeferrableInteractionImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.LaunchActivityCallbackActionImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.MessageEditCallbackActionImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.ReplyCallbackActionImpl;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModalInteractionImpl extends DeferrableInteractionImpl implements ModalInteraction
{
    private final String modalId;
    private final List<ModalMapping> mappings;
    private final Message message;

    public ModalInteractionImpl(JDAImpl api, DataObject object)
    {
        super(api, object);

        DataObject data = object.getObject("data");
        this.modalId = data.getString("custom_id");
        this.mappings = data.optArray("components").orElseGet(DataArray::empty)
                .stream(DataArray::getObject)
                .map(dataObject -> dataObject.getArray("components"))
                .flatMap(dataArray -> dataArray.stream(DataArray::getObject))
                .map(ModalMapping::new)
                .collect(Collectors.toList());
        this.message = object.optObject("message")
                .map(o -> api.getEntityBuilder().createMessageWithChannel(o, getMessageChannel(), false))
                .orElse(null);
    }

    @Nonnull
    @Override
    public String getModalId()
    {
        return modalId;
    }

    @Nonnull
    @Override
    public List<ModalMapping> getValues()
    {
        return Collections.unmodifiableList(mappings);
    }

    @Override
    public Message getMessage()
    {
        return message;
    }

    @Nonnull
    @Override
    public ReplyCallbackAction deferReply()
    {
        return new ReplyCallbackActionImpl(hook);
    }

    @Nonnull
    @Override
    public MessageEditCallbackAction deferEdit()
    {
        return new MessageEditCallbackActionImpl(hook);
    }

    @Nonnull
    @Override
    public LaunchActivityCallbackAction replyWithLaunchedActivity()
    {
        return new LaunchActivityCallbackActionImpl(this);
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public MessageChannelUnion getChannel()
    {
        return (MessageChannelUnion) super.getChannel();
    }
}
