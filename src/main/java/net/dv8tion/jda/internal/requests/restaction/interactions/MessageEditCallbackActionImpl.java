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
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.InteractionHookImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class MessageEditCallbackActionImpl extends DeferrableCallbackActionImpl implements MessageEditCallbackAction
{
    private List<String> retainedFiles = null;
    private List<MessageEmbed> embeds = null;
    private List<ActionRow> components = null;
    private String content = null;

    public MessageEditCallbackActionImpl(InteractionHookImpl hook)
    {
        super(hook);
    }

    private boolean isEmpty()
    {
        return content == null && embeds == null && components == null && files.isEmpty() && retainedFiles == null;
    }

    @Override
    protected DataObject toData()
    {
        DataObject json = DataObject.empty();
        if (isEmpty())
            return json.put("type", ResponseType.DEFERRED_MESSAGE_UPDATE.getRaw());
        json.put("type", ResponseType.MESSAGE_UPDATE.getRaw());
        DataObject data = DataObject.empty();
        if (content != null)
            data.put("content", content);
        if (embeds != null)
            data.put("embeds", DataArray.fromCollection(embeds));
        if (components != null)
            data.put("components", DataArray.fromCollection(components));
        if (retainedFiles != null)
        {
            data.put("attachments", DataArray.fromCollection(
                retainedFiles.stream()
                    .map(id -> DataObject.empty().put("id", id))
                    .collect(Collectors.toList()))
            );
        }
        json.put("data", data);
        return json;
    }

    public MessageEditCallbackAction applyMessage(Message message)
    {
        this.content = message.getContentRaw();
        this.embeds = new ArrayList<>(message.getEmbeds());
        this.components = new ArrayList<>(message.getActionRows());
        return this;
    }

    @Nonnull
    @Override
    public MessageEditCallbackAction setEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        Checks.noneNull(embeds, "MessageEmbed");
        Checks.check(embeds.size() <= 10, "Cannot have more than 10 embeds per message!");
        for (MessageEmbed embed : embeds)
        {
            Checks.check(embed.isSendable(),
                    "Provided Message contains an empty embed or an embed with a length greater than %d characters, which is the max for bot accounts!",
                    MessageEmbed.EMBED_MAX_LENGTH_BOT);
        }
        if (this.embeds == null)
            this.embeds = new ArrayList<>();
        this.embeds.clear();
        this.embeds.addAll(embeds);
        return this;
    }

    @Nonnull
    @Override
    public MessageEditCallbackAction setActionRows(@Nonnull ActionRow... rows)
    {
        Checks.noneNull(rows, "ActionRows");
        Checks.check(rows.length <= 5, "Can only have 5 action rows per message!");
        Checks.checkDuplicateIds(Arrays.stream(rows));
        this.components = new ArrayList<>();
        Collections.addAll(components, rows);
        return this;
    }

    @Nonnull
    @Override
    public MessageEditCallbackAction addFile(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(data, "Data");
        Checks.notEmpty(name, "Name");
        Checks.noneNull(options, "Options");
        if (options.length > 0)
            name = "SPOILER_" + name;

        files.put(name, data);
        return this;
    }

    @Nonnull
    @Override
    public MessageEditCallbackAction retainFilesById(@Nonnull Collection<String> ids)
    {
        Checks.noneNull(ids, "IDs");
        ids.forEach(Checks::isSnowflake);
        this.retainedFiles = new ArrayList<>();
        this.retainedFiles.addAll(ids);
        return this;
    }

    @Nonnull
    @Override
    public MessageEditCallbackAction setContent(@Nullable String content)
    {
        if (content != null)
            Checks.notLonger(content, Message.MAX_CONTENT_LENGTH, "Content");
        this.content = content == null ? "" : content;
        return this;
    }
}
