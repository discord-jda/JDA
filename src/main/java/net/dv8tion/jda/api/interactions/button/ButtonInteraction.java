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

package net.dv8tion.jda.api.interactions.button;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.api.interactions.Component;
import net.dv8tion.jda.api.interactions.ComponentInteraction;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageActionImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public interface ButtonInteraction extends ComponentInteraction
{
    @Nullable
    @Override
    default Component getComponent()
    {
        return getButton();
    }

    @Nullable // null for ephemeral messages
    Button getButton();

    @Nonnull
    @CheckReturnValue
    default RestAction<Void> updateButton(@Nullable Button newButton)
    {
        Message message = getMessage();
        if (message == null)
            throw new IllegalStateException("Cannot update button for ephemeral messages! Discord does not provide enough information to perform the update.");
        List<ActionRow> components = new ArrayList<>(message.getActionRows());
        String id = getComponentId();
        find: for (int i = 0; i < components.size(); i++)
        {
            List<Component> row = components.get(i).getComponents();
            for (int j = 0; j < row.size(); j++)
            {
                if (id.equals(row.get(j).getId()))
                {
                    row = new ArrayList<>(row);
                    if (newButton == null)
                        row.remove(j);
                    else
                        row.set(j, newButton);
                    if (row.isEmpty())
                        components.remove(i);
                    else
                        components.set(i, ActionRow.of(row));
                    break find;
                }
            }

        }
        if (isAcknowledged())
        {
            // this doesn't work for ephemeral messages :(
            WebhookMessageActionImpl action = (WebhookMessageActionImpl) getHook().editMessageById(message.getId(), "content");
            return action.applyMessage(message)
                    .addActionRows(components)
                    .map(it -> null);
        }
        else
        {
            return editMessage(message.getContentRaw()) // content is required
                    .setActionRows(components)
                    .map(it -> null);
        }
    }
}
