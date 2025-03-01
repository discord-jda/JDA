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

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.action_row.ActionRow;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateRequest;
import net.dv8tion.jda.api.utils.messages.MessagePollData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unchecked")
public interface MessageCreateBuilderMixin<R extends MessageCreateRequest<R>> extends AbstractMessageBuilderMixin<R, MessageCreateBuilder>, MessageCreateRequest<R>
{
    @Nonnull
    @Override
    default R addContent(@Nonnull String content)
    {
        getBuilder().addContent(content);
        return (R) this;
    }

    @Nonnull
    @Override
    default R addEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        getBuilder().addEmbeds(embeds);
        return (R) this;
    }

    @Nonnull
    @Override
    default R addComponentTree(@Nonnull Collection<? extends MessageTopLevelComponent> components)
    {
        getBuilder().addComponentTree(components);
        return (R) this;
    }

    @Nonnull
    @Override
    default R addActionRows(@Nonnull Collection<? extends ActionRow> components)
    {
        getBuilder().addActionRows(components);
        return (R) this;
    }

    @Nonnull
    @Override
    default R addFiles(@Nonnull Collection<? extends FileUpload> files)
    {
        getBuilder().addFiles(files);
        return (R) this;
    }

    @Nullable
    @Override
    default MessagePollData getPoll()
    {
        return getBuilder().getPoll();
    }

    @Nonnull
    @Override
    default R setPoll(@Nullable MessagePollData poll)
    {
        getBuilder().setPoll(poll);
        return (R) this;
    }

    @Nonnull
    @Override
    default R setTTS(boolean tts)
    {
        getBuilder().setTTS(tts);
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
    default List<FileUpload> getAttachments()
    {
        return getBuilder().getAttachments();
    }

    @Nonnull
    @Override
    default R setSuppressedNotifications(boolean suppressed)
    {
        getBuilder().setSuppressedNotifications(suppressed);
        return (R) this;
    }

    @Nonnull
    @Override
    default R setVoiceMessage(boolean voiceMessage)
    {
        getBuilder().setVoiceMessage(voiceMessage);
        return (R) this;
    }
}
