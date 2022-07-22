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
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.AllowedMentionsImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
public abstract class AbstractMessageBuilder<T, R extends AbstractMessageBuilder<T, R>> implements MessageRequest<R>
{
    protected final List<MessageEmbed> embeds = new ArrayList<>(10);
    protected final List<LayoutComponent> components = new ArrayList<>(5);
    protected final StringBuilder content = new StringBuilder(Message.MAX_CONTENT_LENGTH);
    protected AllowedMentionsImpl allowedMentions = new AllowedMentionsImpl();

    @Nonnull
    @Override
    public R setContent(@Nullable String content)
    {
        this.content.setLength(0);
        if (content != null)
            this.content.append(content.trim());
        return (R) this;
    }

    @Nonnull
    @Override
    public R mentionRepliedUser(boolean mention)
    {
        allowedMentions.mentionRepliedUser(mention);
        return (R) this;
    }

    @Nonnull
    @Override
    public R allowedMentions(@Nullable Collection<Message.MentionType> allowedMentions)
    {
        this.allowedMentions.allowedMentions(allowedMentions);
        return (R) this;
    }

    @Nonnull
    @Override
    public R mention(@Nonnull IMentionable... mentions)
    {
        allowedMentions.mention(mentions);
        return (R) this;
    }

    @Nonnull
    @Override
    public R mentionUsers(@Nonnull String... userIds)
    {
        allowedMentions.mentionUsers(userIds);
        return (R) this;
    }

    @Nonnull
    @Override
    public R mentionRoles(@Nonnull String... roleIds)
    {
        allowedMentions.mentionRoles(roleIds);
        return (R) this;
    }

    @Nonnull
    @Override
    public R setEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        Checks.noneNull(embeds, "Embeds");
        this.embeds.clear();
        this.embeds.addAll(embeds);
        return (R) this;
    }

    @Nonnull
    @Override
    public R setComponents(@Nonnull Collection<? extends LayoutComponent> layouts)
    {
        Checks.noneNull(layouts, "ComponentLayouts");
        for (LayoutComponent layout : layouts)
            Checks.check(layout.isMessageCompatible(), "Provided component layout is invalid for messages!");
        this.components.clear();
        this.components.addAll(layouts);
        return (R) this;
    }

    @Nonnull
    @Override
    public abstract R setFiles(@Nullable Collection<? extends FileUpload> files);

    public abstract boolean isEmpty();
    public abstract boolean isValid();

    @Nonnull
    public abstract T build();

    @Nonnull
    public R clear()
    {
        this.embeds.clear();
        this.components.clear();
        this.content.setLength(0);
        this.allowedMentions = new AllowedMentionsImpl();
        return (R) this;
    }
}
