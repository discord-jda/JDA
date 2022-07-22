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

package net.dv8tion.jda.internal.utils.message;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.AbstractMessageBuilder;
import net.dv8tion.jda.api.utils.messages.MessageRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

@SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
public interface AbstractMessageBuilderMixin<R extends MessageRequest<R>, B extends AbstractMessageBuilder<?, B>> extends MessageRequest<R>
{
    B getBuilder();

    @Nonnull
    @Override
    default R setContent(@Nullable String content)
    {
        getBuilder().setContent(content);
        return (R) this;
    }

    @Nonnull
    @Override
    default R setEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        getBuilder().setEmbeds(embeds);
        return (R) this;
    }

    @Nonnull
    @Override
    default R setComponents(@Nonnull Collection<? extends LayoutComponent> layouts)
    {
        getBuilder().setComponents(layouts);
        return (R) this;
    }

    @Nonnull
    @Override
    default R setFiles(@Nullable Collection<? extends FileUpload> files)
    {
        getBuilder().setFiles(files);
        return (R) this;
    }

    @Nonnull
    @Override
    default R mentionRepliedUser(boolean mention)
    {
        getBuilder().mentionRepliedUser(mention);
        return (R) this;
    }

    @Nonnull
    @Override
    default R allowedMentions(@Nullable Collection<Message.MentionType> allowedMentions)
    {
        getBuilder().allowedMentions(allowedMentions);
        return (R) this;
    }

    @Nonnull
    @Override
    default R mention(@Nonnull IMentionable... mentions)
    {
        getBuilder().mention(mentions);
        return (R) this;
    }

    @Nonnull
    @Override
    default R mentionUsers(@Nonnull String... userIds)
    {
        getBuilder().mentionUsers(userIds);
        return (R) this;
    }

    @Nonnull
    @Override
    default R mentionRoles(@Nonnull String... roleIds)
    {
        getBuilder().mentionRoles(roleIds);
        return (R) this;
    }
}
