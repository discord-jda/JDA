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
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Abstract builder implementation of {@link MessageRequest}.
 *
 * <p>This builder cannot be instantiated directly. You should use {@link MessageCreateBuilder} or {@link MessageEditBuilder} instead.
 *
 * @param <T>
 *        The result type used for {@link #build()}
 * @param <R>
 *        The return type used for method chaining
 *
 * @see   MessageCreateBuilder
 * @see   MessageEditBuilder
 */
@SuppressWarnings("unchecked")
public abstract class AbstractMessageBuilder<T, R extends AbstractMessageBuilder<T, R>> implements MessageRequest<R>
{
    protected final List<MessageEmbed> embeds = new ArrayList<>(Message.MAX_EMBED_COUNT);
    protected final List<LayoutComponent> components = new ArrayList<>(Message.MAX_COMPONENT_COUNT);
    protected final StringBuilder content = new StringBuilder(Message.MAX_CONTENT_LENGTH);
    protected AllowedMentionsData mentions = new AllowedMentionsData();
    protected int messageFlags;

    protected AbstractMessageBuilder() {}

    @Nonnull
    @Override
    public R setContent(@Nullable String content)
    {
        if (content != null)
        {
            content = content.trim();
            Checks.notLonger(content, Message.MAX_CONTENT_LENGTH, "Content");
            this.content.setLength(0);
            this.content.append(content);
        }
        else
        {
            this.content.setLength(0);
        }
        return (R) this;
    }

    @Nonnull
    @Override
    public String getContent()
    {
        return content.toString();
    }

    @Nonnull
    @Override
    public R mentionRepliedUser(boolean mention)
    {
        mentions.mentionRepliedUser(mention);
        return (R) this;
    }

    @Nonnull
    @Override
    public R setAllowedMentions(@Nullable Collection<Message.MentionType> allowedMentions)
    {
        mentions.setAllowedMentions(allowedMentions);
        return (R) this;
    }

    @Nonnull
    @Override
    public R mention(@Nonnull Collection<? extends IMentionable> mentions)
    {
        this.mentions.mention(mentions);
        return (R) this;
    }

    @Nonnull
    @Override
    public R mentionUsers(@Nonnull Collection<String> userIds)
    {
        this.mentions.mentionUsers(userIds);
        return (R) this;
    }

    @Nonnull
    @Override
    public R mentionRoles(@Nonnull Collection<String> roleIds)
    {
        this.mentions.mentionRoles(roleIds);
        return (R) this;
    }

    @Nonnull
    @Override
    public Set<String> getMentionedUsers()
    {
        return mentions.getMentionedUsers();
    }

    @Nonnull
    @Override
    public Set<String> getMentionedRoles()
    {
        return mentions.getMentionedRoles();
    }

    @Nonnull
    @Override
    public EnumSet<Message.MentionType> getAllowedMentions()
    {
        return mentions.getAllowedMentions();
    }

    @Override
    public boolean isMentionRepliedUser()
    {
        return mentions.isMentionRepliedUser();
    }

    @Nonnull
    @Override
    public R setEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        Checks.noneNull(embeds, "Embeds");
        Checks.check(embeds.size() <= Message.MAX_EMBED_COUNT, "Cannot send more than %d embeds in a message!", Message.MAX_EMBED_COUNT);
        this.embeds.clear();
        this.embeds.addAll(embeds);
        return (R) this;
    }

    @Nonnull
    @Override
    public List<MessageEmbed> getEmbeds()
    {
        return Collections.unmodifiableList(embeds);
    }

    @Nonnull
    @Override
    public R setComponents(@Nonnull Collection<? extends LayoutComponent> components)
    {
        Checks.noneNull(components, "ComponentLayouts");
        for (LayoutComponent layout : components)
            Checks.check(layout.isMessageCompatible(), "Provided component layout is invalid for messages!");
        Checks.check(components.size() <= Message.MAX_COMPONENT_COUNT, "Cannot send more than %d component layouts in a message!", Message.MAX_COMPONENT_COUNT);
        this.components.clear();
        this.components.addAll(components);
        return (R) this;
    }

    @Nonnull
    @Override
    public List<LayoutComponent> getComponents()
    {
        return Collections.unmodifiableList(components);
    }

    @Nonnull
    @Override
    public R setSuppressEmbeds(boolean suppress)
    {
        int flag = Message.MessageFlag.EMBEDS_SUPPRESSED.getValue();
        if (suppress)
            this.messageFlags |= flag;
        else
            this.messageFlags &= ~flag;
        return (R) this;
    }

    @Override
    public boolean isSuppressEmbeds()
    {
        return (this.messageFlags & Message.MessageFlag.EMBEDS_SUPPRESSED.getValue()) != 0;
    }

    /**
     * The flags set on this message.
     *
     * @return The currently set message flags
     */
    public long getMessageFlagsRaw()
    {
        return messageFlags;
    }

    /**
     * Whether this builder is considered empty, this checks for all <em>required</em> fields of the request type.
     * <br>On a create request, this checks for {@link #setContent(String) content}, {@link #setEmbeds(Collection) embeds}, {@link #setComponents(Collection) components}, and {@link #setFiles(Collection) files}.
     * <br>An edit request is only considered empty if no setters were called. And never empty, if the builder is a {@link MessageEditRequest#setReplace(boolean) replace request}.
     *
     * @return True, if the builder state is empty
     */
    public abstract boolean isEmpty();

    /**
     * Whether this builder has a valid state to build.
     * <br>If this is {@code false}, then {@link #build()} throws an {@link IllegalStateException}.
     * You can check the exception docs on {@link #build()} for specifics.
     *
     * @return True, if the builder is in a valid state
     */
    public abstract boolean isValid();

    /**
     * Builds a validated instance of this builder's state, which can then be used for requests.
     *
     * @throws IllegalStateException
     *         For {@link MessageCreateBuilder}
     *         <ul>
     *             <li>If the builder is {@link #isEmpty() empty}</li>
     *             <li>If the content set is longer than {@value Message#MAX_CONTENT_LENGTH}</li>
     *             <li>If more than {@value Message#MAX_EMBED_COUNT} embeds are set</li>
     *             <li>If more than {@value Message#MAX_COMPONENT_COUNT} component layouts are set</li>
     *         </ul>
     *         For {@link MessageEditBuilder}
     *         <ul>
     *             <li>If the content set is longer than {@value Message#MAX_CONTENT_LENGTH}</li>
     *             <li>If more than {@value Message#MAX_EMBED_COUNT} embeds are set</li>
     *             <li>If more than {@value Message#MAX_COMPONENT_COUNT} component layouts are set</li>
     *         </ul>
     *
     * @return The validated data instance
     */
    @Nonnull
    public abstract T build();

    /**
     * Clears this builder's state, resetting it to the initial state identical to creating a new instance.
     *
     * <p><b>WARNING:</b> This will remove all the files added to the builder, but will not close them.
     * You can use {@link #closeFiles()} <em>before</em> calling {@code clear()} to close the files explicitly.
     *
     * @return The same builder instance for chaining
     */
    @Nonnull
    public R clear()
    {
        this.embeds.clear();
        this.components.clear();
        this.content.setLength(0);
        this.mentions.clear();
        this.messageFlags = 0;
        return (R) this;
    }

    /**
     * Closes and removes all {@link net.dv8tion.jda.api.utils.FileUpload FileUploads} added to this builder.
     *
     * <p>This will keep any {@link net.dv8tion.jda.api.utils.AttachmentUpdate AttachmentUpdates} added to this builder, as those do not require closing.
     * You can use {@link MessageEditRequest#setAttachments(AttachedFile...)} to remove them as well.
     *
     * @return The same builder instance for chaining
     */
    @Nonnull
    public abstract R closeFiles();
}
