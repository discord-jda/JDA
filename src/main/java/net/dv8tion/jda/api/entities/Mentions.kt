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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.interactions.commands.SlashCommandReference
import org.apache.commons.collections4.Bag
import javax.annotation.Nonnull

/**
 * Interface to access the mentions of various entities.
 */
interface Mentions {
    @get:Nonnull
    val jDA: JDA?

    /**
     * Indicates if everyone is mentioned, by either using `@everyone` or `@here`.
     *
     *
     * This is different from checking if `@everyone` is in the string, since mentions require additional flags to trigger.
     *
     * @return True, if everyone is mentioned
     */
    fun mentionsEveryone(): Boolean

    @JvmField
    @get:Nonnull
    val users: List<User?>?

    @get:Nonnull
    val usersBag: Bag<User?>?

    @JvmField
    @get:Nonnull
    val channels: List<GuildChannel?>?

    @get:Nonnull
    val channelsBag: Bag<GuildChannel?>?

    /**
     * An immutable list of all mentioned [GuildChannels][net.dv8tion.jda.api.entities.channel.middleman.GuildChannel] of type `clazz`.
     * <br></br>If none were mentioned, this list is empty. Elements are sorted in order of appearance.
     *
     *
     * **This may include GuildChannels from other [Guilds][net.dv8tion.jda.api.entities.Guild]**
     *
     *
     * **Example**<br></br>
     * <pre>`List<GuildMessageChannel> getCoolMessageChannels(Message msg)
     * {
     * List<GuildMessageChannel> channels = msg.getMentions().getChannels(GuildMessageChannel.class);
     * return channels.stream()
     * .filter(channel -> channel.getName().contains("cool"))
     * .collect(Collectors.toList());
     * }
    `</pre> *
     *
     * @param  clazz
     * The [GuildChannel] sub-class [class object][Class] of the type of channel desired
     *
     * @throws java.lang.IllegalArgumentException
     * If `clazz` is `null`
     *
     * @return Immutable list of mentioned GuildChannels that are of type `clazz`.
     */
    @Nonnull
    fun <T : GuildChannel?> getChannels(@Nonnull clazz: Class<T>?): List<T>?

    /**
     * A [Bag][org.apache.commons.collections4.Bag] of mentioned channels of type `clazz`.
     * <br></br>This can be used to retrieve the amount of times a channel was mentioned.
     *
     *
     * **This may include GuildChannels from other [Guilds][net.dv8tion.jda.api.entities.Guild]**
     *
     *
     * **Example**<br></br>
     * <pre>`void sendCount(Message msg)
     * {
     * Bag<GuildMessageChannel> mentions = msg.getMentions().getChannelsBag(GuildMessageChannel.class);
     * StringBuilder content = new StringBuilder();
     * for (GuildMessageChannel channel : mentions.uniqueSet())
     * {
     * content.append("#")
     * .append(channel.getName())
     * .append(": ")
     * .append(mentions.getCount(channel))
     * .append("\n");
     * }
     * msg.getChannel().sendMessage(content.toString()).queue();
     * }
    `</pre> *
     *
     * @param  clazz
     * The [GuildChannel] sub-class [class object][Class] of the type of channel desired
     *
     * @throws java.lang.IllegalArgumentException
     * If `clazz` is `null`
     *
     * @return [Bag][org.apache.commons.collections4.Bag] of mentioned channels of type `clazz`
     *
     * @see .getChannels
     */
    @Nonnull
    fun <T : GuildChannel?> getChannelsBag(@Nonnull clazz: Class<T>?): Bag<T>?

    @JvmField
    @get:Nonnull
    val roles: List<Role?>?

    @get:Nonnull
    val rolesBag: Bag<Role?>?

    @JvmField
    @get:Nonnull
    val customEmojis: List<CustomEmoji?>?

    @get:Nonnull
    val customEmojisBag: Bag<CustomEmoji?>?

    @get:Nonnull
    val members: List<Member?>?

    @get:Nonnull
    val membersBag: Bag<Member?>?

    @get:Nonnull
    val slashCommands: List<SlashCommandReference?>?

    @get:Nonnull
    val slashCommandsBag: Bag<SlashCommandReference?>?

    /**
     * Combines all instances of [IMentionable][net.dv8tion.jda.api.entities.IMentionable]
     * filtered by the specified [MentionType][net.dv8tion.jda.api.entities.Message.MentionType] values.
     * <br></br>If a [Member] is available, it will be taken in favor of a [User].
     * This only provides either the Member or the User instance, rather than both.
     *
     *
     * If no MentionType values are given, all types are used.
     *
     * @param  types
     * [MentionTypes][net.dv8tion.jda.api.entities.Message.MentionType] to include
     *
     * @throws java.lang.IllegalArgumentException
     * If provided with `null`
     *
     * @return Immutable list of filtered [IMentionable][net.dv8tion.jda.api.entities.IMentionable] instances
     */
    @Nonnull
    fun getMentions(@Nonnull vararg types: MentionType?): List<IMentionable?>?

    /**
     * Checks if given [IMentionable][net.dv8tion.jda.api.entities.IMentionable]
     * was mentioned in any way (@User, @everyone, @here, @Role).
     * <br></br>If no filtering [MentionTypes][net.dv8tion.jda.api.entities.Message.MentionType] are
     * specified, all types are used.
     *
     *
     * [MentionType.HERE][Message.MentionType.HERE] and [MentionType.EVERYONE][Message.MentionType.EVERYONE]
     * will only be checked, if the given [IMentionable][net.dv8tion.jda.api.entities.IMentionable] is of type
     * [User][net.dv8tion.jda.api.entities.User] or [Member][net.dv8tion.jda.api.entities.Member].
     * <br></br>Online status of Users/Members is **NOT** considered when checking [MentionType.HERE][Message.MentionType.HERE].
     *
     * @param  mentionable
     * The mentionable entity to check on.
     * @param  types
     * The types to include when checking whether this type was mentioned.
     * This will be used with [getMentions(MentionType...)][.getMentions]
     *
     * @return True, if the given mentionable was mentioned in this message
     */
    fun isMentioned(@Nonnull mentionable: IMentionable?, @Nonnull vararg types: MentionType?): Boolean
}
