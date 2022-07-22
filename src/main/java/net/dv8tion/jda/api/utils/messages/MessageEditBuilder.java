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

package net.dv8tion.jda.api.utils.messages;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.AllowedMentionsImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.IOUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MessageEditBuilder extends AbstractMessageBuilder<MessageEditData, MessageEditBuilder> implements MessageEditRequest<MessageEditBuilder>
{
    protected static final int CONTENT       = 1;
    protected static final int EMBEDS        = 1 << 1;
    protected static final int COMPONENTS    = 1 << 2;
    protected static final int ATTACHMENTS   = 1 << 3;
    protected static final int MENTIONS      = 1 << 4;

    protected static final int ALL = CONTENT | EMBEDS | COMPONENTS | ATTACHMENTS | MENTIONS;

    private boolean replace = false;
    private int set = 0;

    private final List<AttachedFile> attachments = new ArrayList<>(10);

    public MessageEditBuilder() {}

    @Nonnull
    public static MessageEditBuilder from(@Nonnull MessageEditData data)
    {
        return new MessageEditBuilder().applyData(data);
    }

    @Nonnull
    @Override
    public MessageEditBuilder mentionRepliedUser(boolean mention)
    {
        super.mentionRepliedUser(mention);
        set |= MENTIONS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder allowedMentions(@Nullable Collection<Message.MentionType> allowedMentions)
    {
        super.allowedMentions(allowedMentions);
        set |= MENTIONS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder mention(@Nonnull IMentionable... mentions)
    {
        super.mention(mentions);
        set |= MENTIONS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder mentionUsers(@Nonnull String... userIds)
    {
        super.mentionUsers(userIds);
        set |= MENTIONS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder mentionRoles(@Nonnull String... roleIds)
    {
        super.mentionRoles(roleIds);
        set |= MENTIONS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder setAttachments(@Nullable Collection<? extends AttachedFile> attachments)
    {
        this.attachments.clear();
        set |= ATTACHMENTS;
        if (attachments != null)
            this.attachments.addAll(attachments);
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder setFiles(@Nullable Collection<? extends FileUpload> files)
    {
        return setAttachments(files);
    }

    @Nonnull
    @Override
    public MessageEditBuilder replace(boolean isReplace)
    {
        this.replace = isReplace;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder setContent(@Nullable String content)
    {
        super.setContent(content);
        set |= CONTENT;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder setEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        super.setEmbeds(embeds);
        set |= EMBEDS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder setComponents(@Nonnull Collection<? extends LayoutComponent> layouts)
    {
        super.setComponents(layouts);
        set |= COMPONENTS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder applyData(@Nonnull MessageEditData data)
    {
        Checks.notNull(data, "Data");
        this.set = data.getFlags();
        if ((this.set & ALL) == ALL)
            this.replace = true;

        if (this.isSet(CONTENT))
            this.setContent(data.getContent());
        if (this.isSet(EMBEDS))
            this.setEmbeds(data.getEmbeds());
        if (this.isSet(COMPONENTS))
            this.setComponents(data.getComponents());
        if (this.isSet(ATTACHMENTS))
            this.setAttachments(data.getAttachments());

        if (this.isSet(MENTIONS))
        {
            String[] empty = new String[0];
            this.allowedMentions.mentionUsers(data.getMentionedUsers().toArray(empty));
            this.allowedMentions.mentionRoles(data.getMentionedRoles().toArray(empty));
            this.allowedMentions.allowedMentions(data.getAllowedMentions());
            this.allowedMentions.mentionRepliedUser(data.isMentionRepliedUser());
        }

        return this;
    }

    @Override
    public boolean isEmpty()
    {
        return !replace && set == 0;
    }

    @Override
    public boolean isValid()
    {
        if (isSet(EMBEDS) && embeds.size() > Message.MAX_EMBED_COUNT)
            return false;
        if (isSet(COMPONENTS) && components.size() > Message.MAX_COMPONENT_COUNT)
            return false;
        return !isSet(CONTENT) || Helpers.codePointLength(content.toString()) <= Message.MAX_CONTENT_LENGTH;
    }

    private boolean isSet(int flag)
    {
        return replace || (set & flag) != 0;
    }

    @Nonnull
    @Override
    public MessageEditData build()
    {
        // Copy to prevent modifying data after building
        String content = this.content.toString();
        List<MessageEmbed> embeds = new ArrayList<>(this.embeds);
        List<AttachedFile> attachments = new ArrayList<>(this.attachments);
        List<LayoutComponent> components = new ArrayList<>(this.components);
        AllowedMentionsImpl allowedMentions = this.allowedMentions.copy();

        int length = isSet(CONTENT) ? Helpers.codePointLength(content) : 0;
        if (length > Message.MAX_CONTENT_LENGTH)
            throw new IllegalStateException("Message content is too long! Max length is " + Message.MAX_CONTENT_LENGTH + " characters, provided " + length);

        if (isSet(EMBEDS) && embeds.size() > Message.MAX_EMBED_COUNT)
            throw new IllegalStateException("Cannot build message with over " + Message.MAX_EMBED_COUNT + " embeds, provided " + embeds.size());

        if (isSet(COMPONENTS) && components.size() > Message.MAX_COMPONENT_COUNT)
            throw new IllegalStateException("Cannot build message with over " + Message.MAX_COMPONENT_COUNT + " component layouts, provided " + components.size());

        return new MessageEditData(replace ? ALL : set, content, embeds, attachments, components, allowedMentions);
    }

    @Nonnull
    @Override
    public MessageEditBuilder clear()
    {
        this.set = 0;
        this.attachments.clear();
        return super.clear();
    }

    @Nonnull
    @Override
    public MessageEditBuilder closeFiles()
    {
        attachments.forEach(IOUtil::silentClose);
        attachments.removeIf(FileUpload.class::isInstance);
        return this;
    }
}
