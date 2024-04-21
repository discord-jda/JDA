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
package net.dv8tion.jda.api.utils.messages

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ActionRow.Companion.of
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Abstraction of the common setters used for messages in the API.
 * <br></br>These setters can both be applied to [edit requests][MessageEditRequest] and [create requests][MessageCreateRequest] for messages in various parts of the API.
 *
 * @param <R>
 * Return type used for chaining method calls
 *
 * @see MessageCreateRequest
 *
 * @see MessageEditRequest
 *
 * @see AbstractMessageBuilder
 *
 * @see MessageCreateBuilder
 *
 * @see MessageEditBuilder
</R> */
open interface MessageRequest<R : MessageRequest<R>?> : MessageData {
    /**
     * The message content, which shows above embeds and attachments.
     *
     * @param  content
     * The content (up to {@value Message#MAX_CONTENT_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     * If the content is longer than {@value Message#MAX_CONTENT_LENGTH} characters
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun setContent(content: String?): R?

    /**
     * The [MessageEmbeds][MessageEmbed] that should be attached to the message.
     * <br></br>You can use [Collections.emptyList] to remove all embeds from the message.
     *
     *
     * This requires [Permission.MESSAGE_EMBED_LINKS][net.dv8tion.jda.api.Permission.MESSAGE_EMBED_LINKS] in the channel.
     *
     * @param  embeds
     * The embeds to attach to the message (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws IllegalArgumentException
     * If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return The same instance for chaining
     *
     * @see Collections.emptyList
     */
    @Nonnull
    fun setEmbeds(@Nonnull embeds: Collection<MessageEmbed?>?): R?

    /**
     * The [MessageEmbeds][MessageEmbed] that should be attached to the message.
     * <br></br>You can use `new MessageEmbed[0]` to remove all embeds from the message.
     *
     *
     * This requires [Permission.MESSAGE_EMBED_LINKS][net.dv8tion.jda.api.Permission.MESSAGE_EMBED_LINKS] in the channel.
     *
     * @param  embeds
     * The embeds to attach to the message (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws IllegalArgumentException
     * If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun setEmbeds(@Nonnull vararg embeds: MessageEmbed?): R? {
        return setEmbeds(Arrays.asList(*embeds))
    }

    /**
     * The [LayoutComponents][LayoutComponent] that should be attached to the message.
     * <br></br>You can use [Collections.emptyList] to remove all components from the message.
     *
     *
     * The most commonly used layout is [ActionRow].
     *
     *
     * **Example: Set action rows**<br></br>
     * <pre>`final List<LayoutComponent> list = new ArrayList<>();
     * list.add(ActionRow.of(selectMenu); // first row
     * list.add(ActionRow.of(button1, button2)); // second row (shows below the first)
     *
     * channel.sendMessage("Content is still required")
     * .setComponents(list)
     * .queue();
    `</pre> *
     *
     *
     * **Example: Remove action rows**<br></br>
     * <pre>`channel.sendMessage("Content is still required")
     * .setComponents(Collections.emptyList())
     * .queue();
    `</pre> *
     *
     * @param  components
     * The components for the message (up to {@value Message#MAX_COMPONENT_COUNT})
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If any component is not [message compatible][LayoutComponent.isMessageCompatible]
     *  * If more than {@value Message#MAX_COMPONENT_COUNT} components are provided
     *
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun setComponents(@Nonnull components: Collection<LayoutComponent?>): R?

    /**
     * The [LayoutComponents][LayoutComponent] that should be attached to the message.
     * <br></br>You can call this method without anything to remove all components from the message.
     *
     *
     * The most commonly used layout is [ActionRow].
     *
     *
     * **Example: Set action rows**<br></br>
     * <pre>`channel.sendMessage("Content is still required")
     * .setComponents(
     * ActionRow.of(selectMenu) // first row
     * ActionRow.of(button1, button2)) // second row (shows below the first)
     * .queue();
    `</pre> *
     *
     *
     * **Example: Remove action rows**<br></br>
     * <pre>`channel.sendMessage("Content is still required")
     * .setComponents()
     * .queue();
    `</pre> *
     *
     * @param  components
     * The components for the message (up to {@value Message#MAX_COMPONENT_COUNT})
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If any component is not [message compatible][LayoutComponent.isMessageCompatible]
     *  * If more than {@value Message#MAX_COMPONENT_COUNT} components are provided
     *
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun setComponents(@Nonnull vararg components: LayoutComponent?): R? {
        return setComponents(Arrays.asList(*components))
    }

    /**
     * Convenience method to set the components of a message to a single [ActionRow] of components.
     * <br></br>To remove components, you should use [.setComponents] instead.
     *
     *
     * **Example**<br></br>
     *
     * <pre>`final List<ItemComponent> list = new ArrayList<>();
     * list.add(button1);
     * list.add(button2);
     *
     * channel.sendMessage("Content is still required")
     * .setActionRow(list)
     * .queue();
    `</pre> *
     *
     * is equivalent to:
     *
     * <pre>`final List<LayoutComponent> list = new ArrayList<>();
     * list.add(ActionRow.of(button1, button2));
     *
     * channel.sendMessage("Content is still required")
     * .setComponents(list)
     * .queue();
    `</pre> * <br></br>
     *
     * @param  components
     * The [ItemComponents][ItemComponent] for the message (up to {@value Message#MAX_COMPONENT_COUNT})
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If any component is not [message compatible][ItemComponent.isMessageCompatible]
     *  * In all the same cases as [ActionRow.of] throws an exception
     *
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun setActionRow(@Nonnull components: Collection<ItemComponent?>?): R {
        return setComponents(of(components))
    }

    /**
     * Convenience method to set the components of a message to a single [ActionRow] of components.
     * <br></br>To remove components, you should use [.setComponents] instead.
     *
     *
     * **Example**<br></br>
     *
     * <pre>`channel.sendMessage("Content is still required")
     * .setActionRow(button1, button2)
     * .queue();
    `</pre> *
     *
     * is equivalent to:
     *
     * <pre>`channel.sendMessage("Content is still required")
     * .setComponents(ActionRow.of(button1, button2))
     * .queue();
    `</pre> * <br></br>
     *
     * @param  components
     * The [ItemComponents][ItemComponent] for the message (up to {@value Message#MAX_COMPONENT_COUNT})
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If any component is not [message compatible][ItemComponent.isMessageCompatible]
     *  * In all the same cases as [ActionRow.of] throws an exception
     *
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun setActionRow(@Nonnull vararg components: ItemComponent?): R {
        return setComponents(of(*components))
    }

    /**
     * Set whether embeds should be suppressed on this message.
     * <br></br>This also includes rich embeds added via [.setEmbeds].
     *
     *
     * Default: false
     *
     * @param  suppress
     * True, if all embeds should be suppressed
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun setSuppressEmbeds(suppress: Boolean): R?

    /**
     * The [FileUploads][FileUpload] that should be attached to the message.
     * <br></br>This will replace all the existing attachments on the message, if this is an edit request.
     * You can use [MessageEditRequest.setAttachments] to keep existing attachments, instead of this method.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     *
     * **Example**<br></br>
     * Create an embed with a custom image, uploaded alongside the message:
     * <pre>`MessageEmbed embed = new EmbedBuilder()
     * .setDescription("Image of a cute cat")
     * .setImage("attachment://cat.png") // here "cat.png" is the name used in the FileUpload.fromData factory method
     * .build();
     *
     * // The name here will be "cat.png" to discord, what the file is called on your computer is irrelevant and only used to read the data of the image.
     * FileUpload file = FileUpload.fromData(new File("mycat-final-copy.png"), "cat.png"); // Opens the file called "cat.png" and provides the data used for sending
     *
     * channel.sendMessageEmbeds(embed)
     * .setFiles(file)
     * .queue();
    `</pre> *
     *
     * @param  files
     * The [FileUploads][FileUpload] to attach to the message,
     * null or an empty list will set the attachments to an empty list and remove them from the message
     *
     * @throws IllegalArgumentException
     * If null is provided inside the collection
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun setFiles(files: Collection<FileUpload?>?): R

    /**
     * The [FileUploads][FileUpload] that should be attached to the message.
     * <br></br>This will replace all the existing attachments on the message, if this is an edit request.
     * You can use [MessageEditRequest.setAttachments] to keep existing attachments, instead of this method.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     *
     * **Example**<br></br>
     * Create an embed with a custom image, uploaded alongside the message:
     * <pre>`MessageEmbed embed = new EmbedBuilder()
     * .setDescription("Image of a cute cat")
     * .setImage("attachment://cat.png") // here "cat.png" is the name used in the FileUpload.fromData factory method
     * .build();
     *
     * // The name here will be "cat.png" to discord, what the file is called on your computer is irrelevant and only used to read the data of the image.
     * FileUpload file = FileUpload.fromData(new File("mycat-final-copy.png"), "cat.png"); // Opens the file called "cat.png" and provides the data used for sending
     *
     * channel.sendMessageEmbeds(embed)
     * .setFiles(file)
     * .queue();
    `</pre> *
     *
     * @param  files
     * The [FileUploads][FileUpload] to attach to the message,
     * null or an empty list will set the attachments to an empty list and remove them from the message
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun setFiles(@Nonnull vararg files: FileUpload?): R {
        Checks.noneNull(files, "Files")
        return setFiles(Arrays.asList(*files))
    }
    // Allowed Mentions Methods
    /**
     * Whether to mention the used, when replying to a message.
     * <br></br>This only matters in combination with [MessageCreateAction.setMessageReference(...)][net.dv8tion.jda.api.requests.restaction.MessageCreateAction.setMessageReference]!
     *
     *
     * This is true by default but can be configured using [.setDefaultMentionRepliedUser]!
     *
     * @param  mention
     * True, to mention the author if the referenced message
     *
     * @return The same instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    fun mentionRepliedUser(mention: Boolean): R?

    /**
     * Sets the [MentionTypes][net.dv8tion.jda.api.entities.Message.MentionType] that should be parsed.
     * <br></br>If a message is sent with an empty Set of MentionTypes, then it will not ping any User, Role or `@everyone`/`@here`,
     * while still showing up as mention tag.
     *
     *
     * If `null` is provided to this method, then all Types will be mentionable
     * (unless whitelisting via one of the `mention*` methods is used).
     *
     *
     * Note: A default for this can be set using [AllowedMentions.setDefaultMentions(Collection)][.setDefaultMentions].
     *
     * @param  allowedMentions
     * MentionTypes that are allowed to being parsed and mentioned.
     * All other mention types will not be mentioned by this message.
     * You can pass `null` or `EnumSet.allOf(MentionType.class)` to allow all mentions.
     *
     * @return The same instance for chaining
     */
    @Nonnull
    @CheckReturnValue
    fun setAllowedMentions(allowedMentions: Collection<MentionType?>?): R?

    /**
     * Used to provide a whitelist for [Users][net.dv8tion.jda.api.entities.User], [Members][net.dv8tion.jda.api.entities.Member]
     * and [Roles][net.dv8tion.jda.api.entities.Role] that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     * <br></br>On other types of [IMentionable][net.dv8tion.jda.api.entities.IMentionable], this does nothing.
     *
     *
     * **Note:** When a User/Member is whitelisted this way, then parsing of User mentions is automatically disabled (same applies to Roles).
     * <br></br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * [.setDefaultMentions] or [.setAllowedMentions].
     *
     * @param  mentions
     * Users, Members and Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The same instance for chaining
     *
     * @see .setAllowedMentions
     * @see .setDefaultMentions
     */
    @Nonnull
    @CheckReturnValue
    fun mention(@Nonnull mentions: Collection<IMentionable?>): R?

    /**
     * Used to provide a whitelist for [Users][net.dv8tion.jda.api.entities.User], [Members][net.dv8tion.jda.api.entities.Member]
     * and [Roles][net.dv8tion.jda.api.entities.Role] that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     * <br></br>On other types of [IMentionable][net.dv8tion.jda.api.entities.IMentionable], this does nothing.
     *
     *
     * **Note:** When a User/Member is whitelisted this way, then parsing of User mentions is automatically disabled (same applies to Roles).
     * <br></br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * [.setDefaultMentions] or [.setAllowedMentions].
     *
     * @param  mentions
     * Users, Members and Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The same instance for chaining
     *
     * @see .setAllowedMentions
     * @see .setDefaultMentions
     */
    @Nonnull
    @CheckReturnValue
    fun mention(@Nonnull vararg mentions: IMentionable?): R? {
        Checks.notNull(mentions, "Mentions")
        return mention(Arrays.asList(*mentions))
    }

    /**
     * Used to provide a whitelist of [Users][net.dv8tion.jda.api.entities.User] that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     *
     * **Note:** When a User is whitelisted this way, then parsing of User mentions is automatically disabled.
     * <br></br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * [.setDefaultMentions] or [.setAllowedMentions].
     *
     * @param  userIds
     * Ids of Users that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The same instance for chaining
     *
     * @see .setAllowedMentions
     * @see .setDefaultMentions
     */
    @Nonnull
    @CheckReturnValue
    fun mentionUsers(@Nonnull userIds: Collection<String?>?): R?

    /**
     * Used to provide a whitelist of [Users][net.dv8tion.jda.api.entities.User] that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     *
     * **Note:** When a User is whitelisted this way, then parsing of User mentions is automatically disabled.
     * <br></br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * [.setDefaultMentions] or [.setAllowedMentions].
     *
     * @param  userIds
     * Ids of Users that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The same instance for chaining
     *
     * @see .setAllowedMentions
     * @see .setDefaultMentions
     */
    @Nonnull
    @CheckReturnValue
    fun mentionUsers(@Nonnull vararg userIds: String?): R? {
        Checks.notNull(userIds, "User IDs")
        return mentionUsers(Arrays.asList(*userIds))
    }

    /**
     * Used to provide a whitelist of [Users][net.dv8tion.jda.api.entities.User] that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     *
     * **Note:** When a User is whitelisted this way, then parsing of User mentions is automatically disabled.
     * <br></br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * [.setDefaultMentions] or [.setAllowedMentions].
     *
     * @param  userIds
     * Ids of Users that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The same instance for chaining
     *
     * @see .setAllowedMentions
     * @see .setDefaultMentions
     */
    @Nonnull
    @CheckReturnValue
    fun mentionUsers(@Nonnull vararg userIds: Long): R? {
        Checks.notNull(userIds, "UserId array")
        val stringIds: Array<String?> = arrayOfNulls(userIds.size)
        for (i in userIds.indices) stringIds.get(i) = java.lang.Long.toUnsignedString(userIds.get(i))
        return mentionUsers(*stringIds)
    }

    /**
     * Used to provide a whitelist of [Roles][net.dv8tion.jda.api.entities.Role] that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     *
     * **Note:** When a Role is whitelisted this way, then parsing of Role mentions is automatically disabled.
     * <br></br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * [.setDefaultMentions] or [.setAllowedMentions].
     *
     * @param  roleIds
     * Ids of Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The same instance for chaining
     *
     * @see .setAllowedMentions
     * @see .setDefaultMentions
     */
    @Nonnull
    @CheckReturnValue
    fun mentionRoles(@Nonnull roleIds: Collection<String?>?): R?

    /**
     * Used to provide a whitelist of [Roles][net.dv8tion.jda.api.entities.Role] that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     *
     * **Note:** When a Role is whitelisted this way, then parsing of Role mentions is automatically disabled.
     * <br></br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * [.setDefaultMentions] or [.setAllowedMentions].
     *
     * @param  roleIds
     * Ids of Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The same instance for chaining
     *
     * @see .setAllowedMentions
     * @see .setDefaultMentions
     */
    @Nonnull
    @CheckReturnValue
    fun mentionRoles(@Nonnull vararg roleIds: String?): R? {
        Checks.notNull(roleIds, "Role IDs")
        return mentionRoles(Arrays.asList(*roleIds))
    }

    /**
     * Used to provide a whitelist of [Roles][net.dv8tion.jda.api.entities.Role] that should be pinged,
     * even when they would not be pinged otherwise according to the Set of allowed mention types.
     *
     *
     * **Note:** When a Role is whitelisted this way, then parsing of Role mentions is automatically disabled.
     * <br></br>Also note that whitelisting users or roles implicitly disables parsing of other mentions, if not otherwise set via
     * [.setDefaultMentions] or [.setAllowedMentions].
     *
     * @param  roleIds
     * Ids of Roles that should be explicitly whitelisted to be pingable.
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The same instance for chaining
     *
     * @see .setAllowedMentions
     * @see .setDefaultMentions
     */
    @Nonnull
    @CheckReturnValue
    fun mentionRoles(@Nonnull vararg roleIds: Long): R? {
        Checks.notNull(roleIds, "RoleId array")
        val stringIds: Array<String?> = arrayOfNulls(roleIds.size)
        for (i in roleIds.indices) stringIds.get(i) = java.lang.Long.toUnsignedString(roleIds.get(i))
        return mentionRoles(*stringIds)
    }

    /**
     * Applies all the data of the provided [Message] and attempts to copy it.
     * <br></br>This cannot copy the file attachments of the message, they must be manually downloaded and provided to [.setFiles].
     * <br></br>The [allowed mentions][.setAllowedMentions] are not updated to reflect the provided message, and might mention users that the message did not.
     *
     *
     * For edit requests, this will set [MessageEditRequest.setReplace] to `true`, and replace the existing message completely.
     *
     * @param  message
     * The message to copy the data from
     *
     * @throws IllegalArgumentException
     * If null is provided or the message is a system message
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun applyMessage(@Nonnull message: Message): R

    companion object {
        @get:Nonnull
        var defaultMentions: EnumSet<MentionType>?
            /**
             * Returns the default [MentionTypes][net.dv8tion.jda.api.entities.Message.MentionType] previously set by
             * [AllowedMentions.setDefaultMentions(Collection)][.setDefaultMentions].
             *
             * @return Default mentions set by AllowedMentions.setDefaultMentions(Collection)
             */
            get() {
                return AllowedMentionsData.Companion.getDefaultMentions()
            }
            /**
             * Sets the [MentionTypes][net.dv8tion.jda.api.entities.Message.MentionType] that should be parsed by default.
             * This just sets the default for all RestActions and can be overridden on a per-action basis using [.setAllowedMentions].
             * <br></br>If a message is sent with an empty Set of MentionTypes, then it will not ping any User, Role or `@everyone`/`@here`,
             * while still showing up as mention tag.
             *
             *
             * If `null` is provided to this method, then all Types will be pingable
             * (unless whitelisting via one of the `mention*` methods is used).
             *
             *
             * **Example**<br></br>
             * <pre>`// Disable EVERYONE and HERE mentions by default (to avoid mass ping)
             * EnumSet<Message.MentionType> deny = EnumSet.of(Message.MentionType.EVERYONE, Message.MentionType.HERE);
             * MessageRequest.setDefaultMentions(EnumSet.complementOf(deny));
            `</pre> *
             *
             * @param  allowedMentions
             * MentionTypes that are allowed to being parsed and pinged. `null` to disable and allow all mentions.
             */
            set(allowedMentions) {
                AllowedMentionsData.Companion.setDefaultMentions(allowedMentions)
            }
        var isDefaultMentionRepliedUser: Boolean
            /**
             * Returns the default mention behavior for replies.
             * <br></br>If this is `true` then all replies will mention the author of the target message by default.
             * You can specify this individually with [.mentionRepliedUser] for each message.
             *
             *
             * Default: **true**
             *
             * @return True, if replies mention by default
             */
            get() {
                return AllowedMentionsData.Companion.isDefaultMentionRepliedUser()
            }
            /**
             * Sets the default value for [.mentionRepliedUser]
             *
             *
             * Default: **true**
             *
             * @param mention
             * True, if replies should mention by default
             */
            set(mention) {
                AllowedMentionsData.Companion.setDefaultMentionRepliedUser(mention)
            }
    }
}
