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

package net.dv8tion.jda.internal.requests.restaction.interactions;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.api.requests.restaction.interactions.UpdateAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.InteractionHookImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UpdateActionImpl extends CallbackActionImpl implements UpdateAction
{
    private List<MessageEmbed> embeds = null;
    private List<ActionRow> components = null;
    private String content = null;

    public UpdateActionImpl(InteractionHookImpl hook)
    {
        super(hook);
    }

    private boolean isEmpty()
    {
        return content == null && embeds == null && components == null && files.isEmpty();
    }

    @Override
    protected DataObject getJSON()
    {
        DataObject json = DataObject.empty();
        if (isEmpty())
            return json.put("type", ResponseType.DEFERRED_MESSAGE_UPDATE.getRaw());
        json.put("type", ResponseType.MESSAGE_UPDATE.getRaw());
        DataObject data = DataObject.empty(); // TODO: Apparently content is required, follow up on this with the api team
        if (content != null)
            data.put("content", content);
        if (embeds != null)
            data.put("embeds", DataArray.fromCollection(embeds));
        if (components != null)
            data.put("components", DataArray.fromCollection(components));
        json.put("data", data);
        return json;
    }

    public UpdateAction applyMessage(Message message)
    {
        this.content = message.getContentRaw();
        this.embeds.addAll(message.getEmbeds());
        // TODO: Components
        return this;
    }

    // TODO: Can this support files?

    @Nonnull
    @Override
    public UpdateAction setEmbeds(@Nonnull Collection<MessageEmbed> embeds)
    {
        Checks.noneNull(embeds, "MessageEmbed");
        for (MessageEmbed embed : embeds)
        {
            Checks.check(embed.isSendable(),
                    "Provided Message contains an empty embed or an embed with a length greater than %d characters, which is the max for bot accounts!",
                    MessageEmbed.EMBED_MAX_LENGTH_BOT);
        }
        if (this.embeds == null)
            this.embeds = new ArrayList<>();

        if (embeds.size() + this.embeds.size() > 10)
            throw new IllegalStateException("Cannot have more than 10 embeds per message!");
        this.embeds.addAll(embeds);
        return this;
    }

    @Nonnull
    @Override
    public UpdateAction setActionRows(@Nonnull ActionRow... rows)
    {
        Checks.noneNull(rows, "ActionRows");
        if (components == null)
            components = new ArrayList<>();
        Checks.check(components.size() + rows.length <= 5, "Can only have 5 action rows per message!");
        Collections.addAll(components, rows);
        return this;
    }

    @Nonnull
    @Override
    public UpdateAction setContent(@Nullable String content)
    {
        this.content = content == null ? "" : content;
        return this;
    }
}
