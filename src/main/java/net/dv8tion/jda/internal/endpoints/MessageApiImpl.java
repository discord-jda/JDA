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

package net.dv8tion.jda.internal.endpoints;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.endpoints.MessageApi;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessagePollData;
import net.dv8tion.jda.internal.requests.restaction.RestMessageCreateActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.Collection;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class MessageApiImpl implements MessageApi {
    private final JDA jda;
    private final String channelId;

    public MessageApiImpl(JDA jda, String channelId) {
        this.jda = jda;
        this.channelId = channelId;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageCreateAction sendMessage(@Nonnull MessageCreateData data) {
        Checks.notNull(data, "Message");
        return new RestMessageCreateActionImpl(jda, channelId).applyData(data);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageCreateAction sendMessage(@Nonnull CharSequence content) {
        Checks.notNull(content, "Content");
        return new RestMessageCreateActionImpl(jda, channelId).setContent(content.toString());
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageCreateAction sendMessageEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds) {
        return new RestMessageCreateActionImpl(jda, channelId).setEmbeds(embeds);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageCreateAction sendMessageComponents(
            @Nonnull Collection<? extends MessageTopLevelComponent> components) {
        Checks.noneNull(components, "MessageTopLevelComponents");
        return new RestMessageCreateActionImpl(jda, channelId).setComponents(components);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageCreateAction sendMessagePoll(@Nonnull MessagePollData poll) {
        Checks.notNull(poll, "Poll");
        return new RestMessageCreateActionImpl(jda, channelId).setPoll(poll);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public MessageCreateAction sendFiles(@Nonnull Collection<? extends FileUpload> files) {
        Checks.notEmpty(files, "File Collection");
        Checks.noneNull(files, "Files");
        return new RestMessageCreateActionImpl(jda, channelId).addFiles(files);
    }
}
