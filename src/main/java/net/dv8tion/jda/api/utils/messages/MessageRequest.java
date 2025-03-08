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

import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

/**
 * Abstraction of the common setters used for messages in the API.
 * <br>These setters can both be applied to {@link MessageEditRequest edit requests} and {@link MessageCreateRequest create requests} for messages in various parts of the API.
 *
 * @param <R>
 *        Return type used for chaining method calls
 *
 * @see   MessageCreateRequest
 * @see   MessageEditRequest
 * @see   AbstractMessageBuilder
 * @see   MessageCreateBuilder
 * @see   MessageEditBuilder
 */
public interface MessageRequest<R extends MessageRequest<R>> extends MessageData
{
    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Message.MentionType MentionTypes} that should be parsed by default.
     * This just sets the default for all RestActions and can be overridden on a per-action basis using {@link #setAllowedMentions(Collection)}.
     * <br>If a message is sent with an empty Set of MentionTypes, then it will not ping any User, Role or {@code @everyone}/{@code @here},
     * while still showing up as mention tag.
     *
     * <p>If {@code null} is provided to this method, then all Types will be pingable
     * (unless whitelisting via one of the {@code mention*} methods is used).
     *
     * <p><b>Example</b><br>
     * <pre>{@code
     * // Disable EVERYONE and HERE mentions by default (to avoid mass ping)
     * EnumSet<Message.MentionType> deny = EnumSet.of(Message.MentionType.EVERYONE, Message.MentionType.HERE);
     * MessageRequest.setDefaultMentions(EnumSet.complementOf(deny));
     * }</pre>
     *
     * @param  allowedMentions
     *         MentionTypes that are allowed to being parsed and pinged. {@code null} to disable and allow all mentions.
     */
    static void setDefaultMentions(@Nullable Collection<Message.MentionType> allowedMentions)
    {
        AllowedMentionsData.setDefaultMentions(allowedMentions);
    }

    /**
     * Returns the default {@link net.dv8tion.jda.api.entities.Message.MentionType MentionTypes} previously set by
     * {@link #setDefaultMentions(Collection) AllowedMentions.setDefaultMentions(Collection)}.
     *
     * @return Default mentions set by AllowedMentions.setDefaultMentions(Collection)
     */
    @Nonnull
    static EnumSet<Message.MentionType> getDefaultMentions()
    {
        return AllowedMentionsData.getDefaultMentions();
    }

    /**
     * Sets the default value for {@link #mentionRepliedUser(boolean)}
     *
     * <p>Default: <b>true</b>
     *
     * @param mention
     *        True, if replies should mention by default
     */
    static void setDefaultMentionRepliedUser(boolean mention)
    {
        AllowedMentionsData.setDefaultMentionRepliedUser(mention);
    }

    /**
     * Sets whether V2 components will be used by default.
     * <br>When enabled, {@link #useComponentsV2()} gets called for every message builder.
     *
     * <p>This can still be overridden by calling {@link #useComponentsV2(boolean)} manually.
     *
     * @param  use
     *         {@code true} to enable V2 components by default, {@code false} to disabled them by default.
     */
    static void setDefaultUseComponentsV2(boolean use)
    {
        AbstractMessageBuilder.isDefaultUseComponentsV2 = use;
    }

    /**
     * Whether V2 components are used by default.
     * <br>When enabled, {@link #useComponentsV2()} gets called for every message builder.
     *
     * <p>This can still be overridden by calling {@link #useComponentsV2(boolean)} manually.
     *
     * @return {@code true} if every message will use Components V2 by default, {@code false} if not
     */
    static boolean isDefaultUseComponentsV2()
    {
        return AbstractMessageBuilder.isDefaultUseComponentsV2;
    }

    /**
     * Returns the default mention behavior for replies.
     * <br>If this is {@code true} then all replies will mention the author of the target message by default.
     * You can specify this individually with {@link #mentionRepliedUser(boolean)} for each message.
     *
     * <p>Default: <b>true</b>
     *
     * @return True, if replies mention by default
     */
    static boolean isDefaultMentionRepliedUser()
    {
        return AllowedMentionsData.isDefaultMentionRepliedUser();
    }

    /**
     * The message content, which shows above embeds and attachments.
     *
     * @param  content
     *         The content (up to {@value Message#MAX_CONTENT_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If the content is longer than {@value Message#MAX_CONTENT_LENGTH} characters
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R setContent(@Nullable String content);

    /**
     * The {@link MessageEmbed MessageEmbeds} that should be attached to the message.
     * <br>You can use {@link Collections#emptyList()} to remove all embeds from the message.
     *
     * <p>This requires {@link net.dv8tion.jda.api.Permission#MESSAGE_EMBED_LINKS Permission.MESSAGE_EMBED_LINKS} in the channel.
     *
     * @param  embeds
     *         The embeds to attach to the message (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws IllegalArgumentException
     *         If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return The same instance for chaining
     *
     * @see    Collections#emptyList()
     */
    @Nonnull
    R setEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds);

    /**
     * The {@link MessageEmbed MessageEmbeds} that should be attached to the message.
     * <br>You can use {@code new MessageEmbed[0]} to remove all embeds from the message.
     *
     * <p>This requires {@link net.dv8tion.jda.api.Permission#MESSAGE_EMBED_LINKS Permission.MESSAGE_EMBED_LINKS} in the channel.
     *
     * @param  embeds
     *         The embeds to attach to the message (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws IllegalArgumentException
     *         If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default R setEmbeds(@Nonnull MessageEmbed... embeds)
    {
        return setEmbeds(Arrays.asList(embeds));
    }

    /**
     * The {@link MessageTopLevelComponent MessageTopLevelComponents} that should be attached to the message.
     * <br>You can use {@link Collections#emptyList()} to remove all components from the message.
     *
     * <p>The most commonly used layout is {@link ActionRow}.
     *
     * <p><b>Example: Set action rows</b><br>
     * <pre>{@code
     * final List<MessageTopLevelComponent> list = new ArrayList<>();
     * list.add(ActionRow.of(selectMenu); // first row
     * list.add(ActionRow.of(button1, button2)); // second row (shows below the first)
     *
     * channel.sendMessage("Content is still required")
     *   .setComponents(list)
     *   .queue();
     * }</pre>
     *
     * <p><b>Example: Remove action rows</b><br>
     * <pre>{@code
     * channel.sendMessage("Content is still required")
     *    .setComponents(Collections.emptyList())
     *    .queue();
     * }</pre>
     *
     * @param  components
     *         The {@link MessageTopLevelComponent MessageTopLevelComponents} to set, can be empty to remove components,
     *         can contain up to {@value Message#MAX_COMPONENT_COUNT} V1 components in total,
     *         or {@value Message#MAX_COMPONENT_COUNT_COMPONENTS_V2} in total for {@linkplain MessageRequest#isUsingComponentsV2() V2 components}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the provided components are not {@linkplain Component.Type#isMessageCompatible() compatible with messages}</li>
     *         </ul>
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R setComponents(@Nonnull Collection<? extends MessageTopLevelComponent> components);

    /**
     * The {@link MessageTopLevelComponent MessageTopLevelComponents} that should be attached to the message.
     * <br>You can call this method without anything to remove all components from the message.
     *
     * <p>The most commonly used layout is {@link ActionRow}.
     *
     * <p><b>Example: Set action rows</b><br>
     * <pre>{@code
     * channel.sendMessage("Content is still required")
     *   .setComponents(
     *     ActionRow.of(selectMenu) // first row
     *     ActionRow.of(button1, button2)) // second row (shows below the first)
     *   .queue();
     * }</pre>
     *
     * <p><b>Example: Remove action rows</b><br>
     * <pre>{@code
     * channel.sendMessage("Content is still required")
     *   .setComponents()
     *   .queue();
     * }</pre>
     *
     * @param  components
     *         The {@link MessageTopLevelComponent MessageTopLevelComponents} to set, can be empty to remove components,
     *         can contain up to {@value Message#MAX_COMPONENT_COUNT} V1 components in total,
     *         or {@value Message#MAX_COMPONENT_COUNT_COMPONENTS_V2} in total for {@linkplain MessageRequest#isUsingComponentsV2() V2 components}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the provided components are not {@linkplain Component.Type#isMessageCompatible() compatible with messages}</li>
     *         </ul>
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default R setComponents(@Nonnull MessageTopLevelComponent... components)
    {
        return setComponents(Arrays.asList(components));
    }

    /**
     * The {@link MessageTopLevelComponent MessageTopLevelComponents} that should be attached to the message.
     * <br>You can call this method without anything to remove all components from the message.
     *
     * <p>The most commonly used layout is {@link ActionRow}.
     *
     * <p><b>Example: Set action rows</b><br>
     * <pre>{@code
     * channel.sendMessage("Content is still required")
     *   .setComponents(
     *     ActionRow.of(selectMenu) // first row
     *     ActionRow.of(button1, button2)) // second row (shows below the first)
     *   .queue();
     * }</pre>
     *
     * <p><b>Example: Remove action rows</b><br>
     * <pre>{@code
     * channel.sendMessage("Content is still required")
     *   .setComponents()
     *   .queue();
     * }</pre>
     *
     * @param  tree
     *         The new {@link ComponentTree} to set, can be empty to remove components,
     *         can contain up to {@value Message#MAX_COMPONENT_COUNT} V1 components,
     *         or {@value Message#MAX_COMPONENT_COUNT_COMPONENTS_V2} for {@linkplain MessageRequest#isUsingComponentsV2() V2 components}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the provided components are not {@linkplain Component.Type#isMessageCompatible() compatible with messages}</li>
     *         </ul>
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default R setComponents(@Nonnull ComponentTree<MessageTopLevelComponentUnion> tree)
    {
        Checks.notNull(tree, "MessageComponentTree");
        return setComponents(tree.getComponents());
    }

    /**
     * Sets whether this message is allowed to use V2 components.
     *
     * <p>Using V2 components allows for more top-level components ({@value Message#MAX_COMPONENT_COUNT_COMPONENTS_V2}),
     * and more components in total ({@value Message#MAX_COMPONENT_COUNT_IN_COMPONENT_TREE}).
     * <br>They also allow you to use a larger choice of components,
     * such as any component extending {@link MessageTopLevelComponent},
     * as long as they are {@linkplain Component.Type#isMessageCompatible() compatible}.
     * <br>The character limit for the messages also gets changed to {@value Message#MAX_CONTENT_LENGTH_COMPONENT_V2}.
     *
     * <p>This however comes with a few drawbacks:
     * <ul>
     *     <li>You cannot send content, embeds, polls or stickers</li>
     *     <li>It does not support audio files</li>
     *     <li>It does not support text preview for files</li>
     *     <li>URLs don't create embeds</li>
     * </ul>
     *
     * <p>A default value can be set in {@link #setDefaultUseComponentsV2(boolean)}.
     *
     * @param  use
     *         {@code true} to enable V2 components, {@code false} to disabled them.
     *
     * @return The same instance for chaining
     *
     * @see    MessageTopLevelComponent
     * @see    #setDefaultUseComponentsV2(boolean)
     */
    @Nonnull
    R useComponentsV2(boolean use);

    /**
     * Enables using V2 components.
     * <br>This is a shortcut for {@code useComponentV2(true)}
     *
     * <p>Using V2 components allows for more top-level components ({@value Message#MAX_COMPONENT_COUNT_COMPONENTS_V2}),
     * and more components in total ({@value Message#MAX_COMPONENT_COUNT_IN_COMPONENT_TREE}).
     * <br>They also allow you to use a larger choice of components,
     * such as any component extending {@link MessageTopLevelComponent},
     * as long as they are {@linkplain Component.Type#isMessageCompatible() compatible}.
     * <br>The character limit for the messages also gets changed to {@value Message#MAX_CONTENT_LENGTH_COMPONENT_V2}.
     *
     * <p>This however comes with a few drawbacks:
     * <ul>
     *     <li>You cannot send content, embeds, polls or stickers</li>
     *     <li>It does not support audio files</li>
     *     <li>It does not support text preview for files</li>
     *     <li>URLs don't create embeds</li>
     * </ul>
     *
     * @return The same instance for chaining
     *
     * @see    MessageTopLevelComponent
     */
    @Nonnull
    default R useComponentsV2()
    {
        return useComponentsV2(true);
    }

    /**
     * Convenience method to set the components of a message to a single {@link ActionRow} of components.
     * <br>To remove components, you should use {@link #setComponents(Collection)} instead.
     *
     * <p><b>Example</b><br>
     *
     * <pre>{@code
     * channel.sendMessage("Content is still required")
     *   .setActionRow(button1, button2)
     *   .queue();
     * }</pre>
     *
     * is equivalent to:
     *
     * <pre>{@code
     * channel.sendMessage("Content is still required")
     *   .setComponents(ActionRow.of(button1, button2))
     *   .queue();
     * }</pre><br>
     *
     * @param  components
     *         The {@link ActionRowChildComponent ActionRowChildComponents} for the message (up to 5)
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any component is not {@link ActionRowChildComponent#isMessageCompatible() message compatible}</li>
     *             <li>In all the same cases as {@link ActionRow#of(ActionRowChildComponent...)} throws an exception</li>
     *         </ul>
     *
     * @return The same instance for chaining
     *
     * @deprecated
     *         Replace with {@link #setComponents(MessageTopLevelComponent...) setComponents(ActionRow.of(components))}
     */
    @Nonnull
    @Deprecated
    @ReplaceWith("setComponents(ActionRow.of(components))")
    default R setActionRow(@Nonnull Collection<? extends ItemComponent> components)
    {
        return setComponents(ActionRow.of(components));
    }

    /**
     * Convenience method to set the components of a message to a single {@link ActionRow} of components.
     * <br>To remove components, you should use {@link #setComponents(MessageTopLevelComponent...)} instead.
     *
     * <p><b>Example</b><br>
     *
     * <pre>{@code
     * channel.sendMessage("Content is still required")
     *   .setActionRow(button1, button2)
     *   .queue();
     * }</pre>
     *
     * is equivalent to:
     *
     * <pre>{@code
     * channel.sendMessage("Content is still required")
     *   .setComponents(ActionRow.of(button1, button2))
     *   .queue();
     * }</pre><br>
     *
     * @param  components
     *         The {@link ActionRowChildComponent ActionRowChildComponents} for the message (up to 5)
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any component is not {@link ActionRowChildComponent#isMessageCompatible() message compatible}</li>
     *             <li>In all the same cases as {@link ActionRow#of(ActionRowChildComponent...)} throws an exception</li>
     *         </ul>
     *
     * @return The same instance for chaining
     *
     * @deprecated
     *         Replace with {@link #setComponents(MessageTopLevelComponent...) setActionRows(ActionRow.of(components))}
     */
    @Nonnull
    @Deprecated
    @ReplaceWith("setComponents(ActionRow.of(components))")
    default R setActionRow(@Nonnull ItemComponent... components)
    {
        return setComponents(ActionRow.of(components));
    }

    /**
     * Set whether embeds should be suppressed on this message.
     * <br>This also includes rich embeds added via {@link #setEmbeds(MessageEmbed...)}.
     *
     * <p>Default: false
     *
     * @param  suppress
     *         True, if all embeds should be suppressed
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R setSuppressEmbeds(boolean suppress);

    /**
     * The {@link FileUpload FileUploads} that should be attached to the message.
     * <br>This will replace all the existing attachments on the message, if this is an edit request.
     * You can use {@link MessageEditRequest#setAttachments(Collection)} to keep existing attachments, instead of this method.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * <p><b>Example</b><br>
     * Create an embed with a custom image, uploaded alongside the message:
     * <pre>{@code
     * MessageEmbed embed = new EmbedBuilder()
     *         .setDescription("Image of a cute cat")
     *         .setImage("attachment://cat.png") // here "cat.png" is the name used in the FileUpload.fromData factory method
     *         .build();
     *
     * // The name here will be "cat.png" to discord, what the file is called on your computer is irrelevant and only used to read the data of the image.
     * FileUpload file = FileUpload.fromData(new File("mycat-final-copy.png"), "cat.png"); // Opens the file called "cat.png" and provides the data used for sending
     *
     * channel.sendMessageEmbeds(embed)
     *        .setFiles(file)
     *        .queue();
     * }</pre>
     *
     * @param  files
     *         The {@link FileUpload FileUploads} to attach to the message,
     *         null or an empty list will set the attachments to an empty list and remove them from the message
     *
     * @throws IllegalArgumentException
     *         If null is provided inside the collection
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R setFiles(@Nullable Collection<? extends FileUpload> files);

    /**
     * The {@link FileUpload FileUploads} that should be attached to the message.
     * <br>This will replace all the existing attachments on the message, if this is an edit request.
     * You can use {@link MessageEditRequest#setAttachments(AttachedFile...)} to keep existing attachments, instead of this method.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * <p><b>Example</b><br>
     * Create an embed with a custom image, uploaded alongside the message:
     * <pre>{@code
     * MessageEmbed embed = new EmbedBuilder()
     *         .setDescription("Image of a cute cat")
     *         .setImage("attachment://cat.png") // here "cat.png" is the name used in the FileUpload.fromData factory method
     *         .build();
     *
     * // The name here will be "cat.png" to discord, what the file is called on your computer is irrelevant and only used to read the data of the image.
     * FileUpload file = FileUpload.fromData(new File("mycat-final-copy.png"), "cat.png"); // Opens the file called "cat.png" and provides the data used for sending
     *
     * channel.sendMessageEmbeds(embed)
     *        .setFiles(file)
     *        .queue();
     * }</pre>
     *
     * @param  files
     *         The {@link FileUpload FileUploads} to attach to the message,
     *         null or an empty list will set the attachments to an empty list and remove them from the message
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default R setFiles(@Nonnull FileUpload... files)
    {
        Checks.noneNull(files, "Files");
        return setFiles(Arrays.asList(files));
    }

    // Allowed Mentions Methods

    /**
     * Whether to mention the user, when replying to a message.
     * <br>This only matters in combination with {@link net.dv8tion.jda.api.requests.restaction.MessageCreateAction#setMessageReference(Message) MessageCreateAction.setMessageReference(...)}!
     *
     * <p>This is true by default but can be configured using {@link #setDefaultMentionRepliedUser(boolean)}!
     *
     * @param  mention
     *         True, to mention the author in the referenced message
     *
     * @return The same instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    R mentionRepliedUser(boolean mention);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Message.MentionType MentionTypes} that should be parsed.
     * <br>If a message is sent with an empty Set of MentionTypes, then it will not ping any User, Role or {@code @everyone}/{@code @here},
     * while still showing up as mention tag.
     *
     * <p>If {@code null} is provided to this method, then all Types will be mentionable
     * (unless whitelisting via one of the {@code mention*} methods is used).
     *
     * <p>Note: A default for this can be set using {@link #setDefaultMentions(Collection) AllowedMentions.setDefaultMentions(Collection)}.
     *
     * @param  allowedMentions
     *         MentionTypes that are allowed to being parsed and mentioned.
     *         All other mention types will not be mentioned by this message.
     *         You can pass {@code null} or {@code EnumSet.allOf(MentionType.class)} to allow all mentions.
     *
     * @return The same instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    R setAllowedMentions(@Nullable Collection<Message.MentionType> allowedMentions);

    /**
     * Used to provide a whitelist for {@link net.dv8tion.jda.api.entities.User Users}, {@link net.dv8tion.jda.api.entities.Member Members}
     * and {@link net.dv8tion.jda.api.entities.Role Roles} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     * <br>On other types of {@link net.dv8tion.jda.api.entities.IMentionable IMentionable}, this does nothing.
     *
     * <p><b>Note:</b> When a User/Member is whitelisted this way, then parsing of User mentions is automatically disabled (same applies to Roles).
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link #setDefaultMentions(Collection)} or {@link #setAllowedMentions(Collection)}.
     *
     * @param  mentions
     *         Users, Members and Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The same instance for chaining
     *
     * @see    #setAllowedMentions(Collection)
     * @see    #setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    R mention(@Nonnull Collection<? extends IMentionable> mentions);

    /**
     * Used to provide a whitelist for {@link net.dv8tion.jda.api.entities.User Users}, {@link net.dv8tion.jda.api.entities.Member Members}
     * and {@link net.dv8tion.jda.api.entities.Role Roles} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     * <br>On other types of {@link net.dv8tion.jda.api.entities.IMentionable IMentionable}, this does nothing.
     *
     * <p><b>Note:</b> When a User/Member is whitelisted this way, then parsing of User mentions is automatically disabled (same applies to Roles).
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link #setDefaultMentions(Collection)} or {@link #setAllowedMentions(Collection)}.
     *
     * @param  mentions
     *         Users, Members and Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The same instance for chaining
     *
     * @see    #setAllowedMentions(Collection)
     * @see    #setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    default R mention(@Nonnull IMentionable... mentions)
    {
        Checks.notNull(mentions, "Mentions");
        return mention(Arrays.asList(mentions));
    }

    /**
     * Used to provide a whitelist of {@link net.dv8tion.jda.api.entities.User Users} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     * <p><b>Note:</b> When a User is whitelisted this way, then parsing of User mentions is automatically disabled.
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link #setDefaultMentions(Collection)} or {@link #setAllowedMentions(Collection)}.
     *
     * @param  userIds
     *         Ids of Users that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The same instance for chaining
     *
     * @see    #setAllowedMentions(Collection)
     * @see    #setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    R mentionUsers(@Nonnull Collection<String> userIds);

    /**
     * Used to provide a whitelist of {@link net.dv8tion.jda.api.entities.User Users} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     * <p><b>Note:</b> When a User is whitelisted this way, then parsing of User mentions is automatically disabled.
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link #setDefaultMentions(Collection)} or {@link #setAllowedMentions(Collection)}.
     *
     * @param  userIds
     *         Ids of Users that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The same instance for chaining
     *
     * @see    #setAllowedMentions(Collection)
     * @see    #setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    default R mentionUsers(@Nonnull String... userIds)
    {
        Checks.notNull(userIds, "User IDs");
        return mentionUsers(Arrays.asList(userIds));
    }

    /**
     * Used to provide a whitelist of {@link net.dv8tion.jda.api.entities.User Users} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     * <p><b>Note:</b> When a User is whitelisted this way, then parsing of User mentions is automatically disabled.
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link #setDefaultMentions(Collection)} or {@link #setAllowedMentions(Collection)}.
     *
     * @param  userIds
     *         Ids of Users that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The same instance for chaining
     *
     * @see    #setAllowedMentions(Collection)
     * @see    #setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    default R mentionUsers(@Nonnull long... userIds)
    {
        Checks.notNull(userIds, "UserId array");
        String[] stringIds = new String[userIds.length];
        for (int i = 0; i < userIds.length; i++)
            stringIds[i] = Long.toUnsignedString(userIds[i]);
        return mentionUsers(stringIds);
    }

    /**
     * Used to provide a whitelist of {@link net.dv8tion.jda.api.entities.Role Roles} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     * <p><b>Note:</b> When a Role is whitelisted this way, then parsing of Role mentions is automatically disabled.
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link #setDefaultMentions(Collection)} or {@link #setAllowedMentions(Collection)}.
     *
     * @param  roleIds
     *         Ids of Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The same instance for chaining
     *
     * @see    #setAllowedMentions(Collection)
     * @see    #setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    R mentionRoles(@Nonnull Collection<String> roleIds);

    /**
     * Used to provide a whitelist of {@link net.dv8tion.jda.api.entities.Role Roles} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     * <p><b>Note:</b> When a Role is whitelisted this way, then parsing of Role mentions is automatically disabled.
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link #setDefaultMentions(Collection)} or {@link #setAllowedMentions(Collection)}.
     *
     * @param  roleIds
     *         Ids of Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The same instance for chaining
     *
     * @see    #setAllowedMentions(Collection)
     * @see    #setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    default R mentionRoles(@Nonnull String... roleIds)
    {
        Checks.notNull(roleIds, "Role IDs");
        return mentionRoles(Arrays.asList(roleIds));
    }

    /**
     * Used to provide a whitelist of {@link net.dv8tion.jda.api.entities.Role Roles} that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     * <p><b>Note:</b> When a Role is whitelisted this way, then parsing of Role mentions is automatically disabled.
     * <br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * {@link #setDefaultMentions(Collection)} or {@link #setAllowedMentions(Collection)}.
     *
     * @param  roleIds
     *         Ids of Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The same instance for chaining
     *
     * @see    #setAllowedMentions(Collection)
     * @see    #setDefaultMentions(Collection)
     */
    @Nonnull
    @CheckReturnValue
    default R mentionRoles(@Nonnull long... roleIds)
    {
        Checks.notNull(roleIds, "RoleId array");
        String[] stringIds = new String[roleIds.length];
        for (int i = 0; i < roleIds.length; i++)
            stringIds[i] = Long.toUnsignedString(roleIds[i]);
        return mentionRoles(stringIds);
    }

    /**
     * Applies all the data of the provided {@link Message} and attempts to copy it.
     * <br>This cannot copy the file attachments of the message, they must be manually downloaded and provided to {@link #setFiles(FileUpload...)}.
     * <br>The {@link #setAllowedMentions(Collection) allowed mentions} are not updated to reflect the provided message, and might mention users that the message did not.
     *
     * <p>For edit requests, this will set {@link MessageEditRequest#setReplace(boolean)} to {@code true}, and replace the existing message completely.
     *
     * @param  message
     *         The message to copy the data from
     *
     * @throws IllegalArgumentException
     *         If null is provided or the message is a system message
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R applyMessage(@Nonnull Message message);
}
