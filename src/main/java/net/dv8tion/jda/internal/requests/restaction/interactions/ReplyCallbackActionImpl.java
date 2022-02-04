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

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.InteractionHookImpl;
import net.dv8tion.jda.internal.utils.AllowedMentionsImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

public class ReplyCallbackActionImpl extends DeferrableCallbackActionImpl implements ReplyCallbackAction
{
    private final List<MessageEmbed> embeds = new ArrayList<>();
    private final AllowedMentionsImpl allowedMentions = new AllowedMentionsImpl();
    private final List<ActionRow> components = new ArrayList<>();
    private String content = "";

    private int flags;
    private boolean tts;

    public ReplyCallbackActionImpl(InteractionHookImpl hook)
    {
        super(hook);
    }

    public ReplyCallbackActionImpl applyMessage(Message message)
    {
        this.content = message.getContentRaw();
        this.tts = message.isTTS();
        this.embeds.addAll(message.getEmbeds());
        this.components.addAll(message.getActionRows());
        this.allowedMentions.applyMessage(message);
        return this;
    }

    protected DataObject toData()
    {
        DataObject json = DataObject.empty();
        if (isEmpty())
        {
            json.put("type", ResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE.getRaw());
            if (flags != 0)
                json.put("data", DataObject.empty().put("flags", flags));
        }
        else
        {
            DataObject payload = DataObject.empty();
            payload.put("allowed_mentions", allowedMentions);
            payload.put("content", content);
            payload.put("tts", tts);
            payload.put("flags", flags);
            if (!embeds.isEmpty())
                payload.put("embeds", DataArray.fromCollection(embeds));
            if (!components.isEmpty())
                payload.put("components", DataArray.fromCollection(components));
            json.put("data", payload);

            json.put("type", ResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getRaw()); // This type seemingly makes no difference right now, idk why it exists
        }
        return json;
    }

    private boolean isEmpty()
    {
        //Intentionally does not check components.isEmpty() here
        // You cannot send a message with only components at this time.
        return Helpers.isEmpty(content) && embeds.isEmpty() && files.isEmpty();
    }

    @Nonnull
    @Override
    public ReplyCallbackActionImpl setEphemeral(boolean ephemeral)
    {
        if (ephemeral)
            this.flags |= 64;
        else
            this.flags &= ~64;
        return this;
    }

    @Nonnull
    @Override
    public ReplyCallbackAction addFile(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options)
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
    public ReplyCallbackAction addEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        Checks.noneNull(embeds, "MessageEmbed");
        for (MessageEmbed embed : embeds)
        {
            Checks.check(embed.isSendable(),
                "Provided Message contains an empty embed or an embed with a length greater than %d characters, which is the max for bot accounts!",
                MessageEmbed.EMBED_MAX_LENGTH_BOT);
        }

        if (embeds.size() + this.embeds.size() > 10)
            throw new IllegalStateException("Cannot have more than 10 embeds per message!");
        this.embeds.addAll(embeds);
        return this;
    }

    @Nonnull
    @Override
    public ReplyCallbackAction addActionRows(@Nonnull ActionRow... rows)
    {
        Checks.noneNull(rows, "ActionRows");
        Checks.check(components.size() + rows.length <= 5, "Can only have 5 action rows per message!");
        Checks.checkDuplicateIds(Stream.concat(this.components.stream(), Arrays.stream(rows)));

        Collections.addAll(components, rows);
        return this;
    }

    @Nonnull
    @Override
    public ReplyCallbackAction setCheck(BooleanSupplier checks)
    {
        return (ReplyCallbackAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public ReplyCallbackAction timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (ReplyCallbackAction) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public ReplyCallbackAction deadline(long timestamp)
    {
        return (ReplyCallbackAction) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public ReplyCallbackActionImpl setTTS(boolean isTTS)
    {
        this.tts = isTTS;
        return this;
    }

    @Nonnull
    @Override
    public ReplyCallbackActionImpl setContent(String content)
    {
        if (content != null)
            Checks.notLonger(content, Message.MAX_CONTENT_LENGTH, "Content");
        this.content = content == null ? "" : content;
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ReplyCallbackAction mentionRepliedUser(boolean mention)
    {
        allowedMentions.mentionRepliedUser(mention);
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ReplyCallbackAction allowedMentions(@Nullable Collection<Message.MentionType> allowedMentions)
    {
        this.allowedMentions.allowedMentions(allowedMentions);
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ReplyCallbackAction mention(@Nonnull IMentionable... mentions)
    {
        allowedMentions.mention(mentions);
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ReplyCallbackAction mentionUsers(@Nonnull String... userIds)
    {
        allowedMentions.mentionUsers(userIds);
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ReplyCallbackAction mentionRoles(@Nonnull String... roleIds)
    {
        allowedMentions.mentionRoles(roleIds);
        return this;
    }
}
