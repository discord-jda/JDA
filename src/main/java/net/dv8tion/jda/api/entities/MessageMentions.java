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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import org.apache.commons.collections4.Bag;

import javax.annotation.Nonnull;
import java.util.List;

//TODO-v5 | Docs
public interface MessageMentions
{
    //TODO-v5 | Docs
    @Nonnull
    JDA getJDA();

    /**
     * Indicates if this Message mentions everyone, using @everyone or @here.
     *
     * @return True, if message is mentioning everyone
     */
    boolean mentionsEveryone();

    /**
     * An immutable list of all mentioned {@link net.dv8tion.jda.api.entities.User Users}.
     * <br>If no user was mentioned, this list is empty. Elements are sorted in order of appearance. This only
     * counts direct mentions of the user and not mentions through roles or the everyone tag.
     *
     * @return immutable list of mentioned users
     */
    @Nonnull
    List<User> getUsers();

    /**
     * A {@link org.apache.commons.collections4.Bag Bag} of mentioned users.
     * <br>This can be used to retrieve the amount of times a user was mentioned in this message. This only
     * counts direct mentions of the user and not mentions through roles or the everyone tag.
     *
     * <h2>Example</h2>
     * <pre>{@code
     * void sendCount(Message msg)
     * {
     *     List<User> mentions = msg.getMentions().getUsers(); // distinct list, in order of appearance
     *     Bag<User> count = msg.getMentions().getUsersBag();
     *     StringBuilder content = new StringBuilder();
     *     for (User user : mentions)
     *     {
     *         content.append(user.getAsTag())
     *                .append(": ")
     *                .append(count.getCount(user))
     *                .append("\n");
     *     }
     *     msg.getChannel().sendMessage(content.toString()).queue();
     * }
     * }</pre>
     *
     * @return {@link org.apache.commons.collections4.Bag Bag} of mentioned users
     *
     * @see    #getUsers()
     */
    @Nonnull
    Bag<User> getUsersBag();

    /**
     * A immutable list of all mentioned {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannels}.
     * <br>If none were mentioned, this list is empty. Elements are sorted in order of appearance.
     *
     * <p><b>This may include GuildChannels from other {@link net.dv8tion.jda.api.entities.Guild Guilds}</b>
     *
     * @return immutable list of mentioned TextChannels
     */
    @Nonnull
    List<GuildChannel> getChannels();

    /**
     * A {@link org.apache.commons.collections4.Bag Bag} of mentioned channels.
     * <br>This can be used to retrieve the amount of times a channel was mentioned in this message.
     *
     * <h2>Example</h2>
     * <pre>{@code
     * void sendCount(Message msg)
     * {
     *     List<GuildChannel> mentions = msg.getMentions().getChannels(); // distinct list, in order of appearance
     *     Bag<GuildChannel> count = msg.getMentions().getChannelsBag();
     *     StringBuilder content = new StringBuilder();
     *     for (GuildChannel channel : mentions)
     *     {
     *         content.append("#")
     *                .append(channel.getName())
     *                .append(": ")
     *                .append(count.getCount(channel))
     *                .append("\n");
     *     }
     *     msg.getChannel().sendMessage(content.toString()).queue();
     * }
     * }</pre>
     *
     * @return {@link org.apache.commons.collections4.Bag Bag} of mentioned channels
     *
     * @see    #getChannels()
     */
    @Nonnull
    Bag<GuildChannel> getChannelsBag();

    /**
     * A immutable list of all mentioned {@link net.dv8tion.jda.api.entities.Role Roles}.
     * <br>If none were mentioned, this list is empty. Elements are sorted in order of appearance. This only
     * counts direct mentions of the role and not mentions through the everyone tag.
     *
     * <p><b>This may include Roles from other {@link net.dv8tion.jda.api.entities.Guild Guilds}</b>
     *
     * @return immutable list of mentioned Roles
     */
    @Nonnull
    List<Role> getRoles();

    /**
     * A {@link org.apache.commons.collections4.Bag Bag} of mentioned roles.
     * <br>This can be used to retrieve the amount of times a role was mentioned in this message. This only
     * counts direct mentions of the role and not mentions through the everyone tag.
     *
     * <h2>Example</h2>
     * <pre>{@code
     * void sendCount(Message msg)
     * {
     *     List<Role> mentions = msg.getMentions().getRoles(); // distinct list, in order of appearance
     *     Bag<Role> count = msg.getMentions().getRolesBag();
     *     StringBuilder content = new StringBuilder();
     *     for (Role role : mentions)
     *     {
     *         content.append(role.getName())
     *                .append(": ")
     *                .append(count.getCount(role))
     *                .append("\n");
     *     }
     *     msg.getChannel().sendMessage(content.toString()).queue();
     * }
     * }</pre>
     *
     * @return {@link org.apache.commons.collections4.Bag Bag} of mentioned roles
     *
     * @see    #getRoles()
     */
    @Nonnull
    Bag<Role> getRolesBag();

    /**
     * All {@link net.dv8tion.jda.api.entities.Emote Emotes} used in this Message.
     * <br><b>This only includes Custom Emotes, not unicode Emojis.</b> JDA classifies Emotes as the Custom Emojis uploaded
     * to a Guild and retrievable with {@link net.dv8tion.jda.api.entities.Guild#getEmotes()}. These are not the same
     * as the unicode emojis that Discord also supports. Elements are sorted in order of appearance.
     *
     * <p><b><u>Unicode emojis are not included as {@link net.dv8tion.jda.api.entities.Emote Emote}!</u></b>
     *
     * @return An immutable list of the Emotes used in this message (example match {@literal <:jda:230988580904763393>})
     */
    @Nonnull
    List<Emote> getEmotes();

    /**
     * A {@link org.apache.commons.collections4.Bag Bag} of emotes used in this message.
     * <br>This can be used to retrieve the amount of times an emote was used in this message.
     *
     * <h2>Example</h2>
     * <pre>{@code
     * void sendCount(Message msg)
     * {
     *     List<Emote> emotes = msg.getMentions().getEmotes(); // distinct list, in order of appearance
     *     Bag<Emote> count = msg.getMentions().getEmotesBag();
     *     StringBuilder content = new StringBuilder();
     *     for (Emote emote : emotes)
     *     {
     *         content.append(emote.getName())
     *                .append(": ")
     *                .append(count.getCount(role))
     *                .append("\n");
     *     }
     *     msg.getChannel().sendMessage(content.toString()).queue();
     * }
     * }</pre>
     *
     * @return {@link org.apache.commons.collections4.Bag Bag} of used emotes
     *
     * @see    #getEmotes()
     */
    @Nonnull
    Bag<Emote> getEmotesBag();

    /**
     * Creates an immutable list of {@link net.dv8tion.jda.api.entities.Member Members}
     * representing the users of {@link #getUsers()} in the
     * {@link net.dv8tion.jda.api.entities.Guild Guild} this Message was sent in.
     *
     * @return Immutable list of mentioned Members, or an empty list if this message was not sent in a guild
     */
    @Nonnull
    List<Member> getMembers();

    @Nonnull
    Bag<Member> getMembersBag();

    /**
     * Combines all instances of {@link net.dv8tion.jda.api.entities.IMentionable IMentionable}
     * filtered by the specified {@link net.dv8tion.jda.api.entities.Message.MentionType MentionType} values.
     * <br>This does not include {@link #getUsers()} to avoid duplicates.
     *
     * <p>If no MentionType values are given this will fallback to all types.
     *
     * @param  types
     *         {@link net.dv8tion.jda.api.entities.Message.MentionType MentionTypes} to include
     *
     * @throws java.lang.IllegalArgumentException
     *         If provided with {@code null}
     *
     * @return Immutable list of filtered {@link net.dv8tion.jda.api.entities.IMentionable IMentionable} instances
     */
    @Nonnull
    List<IMentionable> getMentions(@Nonnull Message.MentionType... types);

    /**
     * Checks if given {@link net.dv8tion.jda.api.entities.IMentionable IMentionable}
     * was mentioned in this message in any way (@User, @everyone, @here, @Role).
     * <br>If no filtering {@link net.dv8tion.jda.api.entities.Message.MentionType MentionTypes} are
     * specified this will fallback to all mention types.
     *
     * <p>{@link Message.MentionType#HERE MentionType.HERE} and {@link Message.MentionType#EVERYONE MentionType.EVERYONE}
     * will only be checked, if the given {@link net.dv8tion.jda.api.entities.IMentionable IMentionable} is of type
     * {@link net.dv8tion.jda.api.entities.User User} or {@link net.dv8tion.jda.api.entities.Member Member}.
     * <br>Online status of Users/Members is <b>NOT</b> considered when checking {@link Message.MentionType#HERE MentionType.HERE}.
     *
     * @param  mentionable
     *         The mentionable entity to check on.
     * @param  types
     *         The types to include when checking whether this type was mentioned.
     *         This will be used with {@link #getMentions(Message.MentionType...) getMentions(MentionType...)}
     *
     * @return True, if the given mentionable was mentioned in this message
     */
    boolean isMentioned(@Nonnull IMentionable mentionable, @Nonnull Message.MentionType... types);
}
