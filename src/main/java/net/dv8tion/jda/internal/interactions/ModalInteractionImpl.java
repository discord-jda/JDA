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

package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.interactions.ModalInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.ReplyCallbackActionImpl;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModalInteractionImpl extends DeferrableInteractionImpl implements ModalInteraction
{
    private final String modalId;
    private final List<ActionRow> components;

    public ModalInteractionImpl(JDAImpl api, DataObject object)
    {
        super(api, object);

        DataObject data = object.getObject("data");

        this.modalId = data.getString("custom_id");

        this.components = data.getArray("components")
                .stream(DataArray::getObject)
                .map(ActionRow::fromData)
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public String getModalId()
    {
        return modalId;
    }

    @Nonnull
    @Override
    public List<ActionRow> getComponents()
    {
        return Collections.unmodifiableList(components);
    }

    @Override
    @Nonnull
    public ReplyCallbackAction deferReply(boolean ephemeral)
    {
        return new ReplyCallbackActionImpl(hook).setEphemeral(ephemeral);
    }

    @Override
    @Nonnull
    public ReplyCallbackAction reply(String content)
    {
        return new ReplyCallbackActionImpl(hook).setContent(content);
    }
}
