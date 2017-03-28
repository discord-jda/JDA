/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.core;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.MessageImpl;
import org.apache.http.util.Args;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder system used to build {@link net.dv8tion.jda.core.entities.Message Messages}.
 * <br>Internally the builder uses a {@link java.lang.StringBuilder} to take advantage of the efficiencies offered by the
 * StringBuilder, and the methods provided by this class are a combination of those offered by the StringBuilder and
 * {@link String#format(String, Object...)}.
 *
 * @since  1.0
 * @author Michael Ritter and Aljoscha Grebe
 */
public class MessageBuilder implements Appendable
{
    protected static final Pattern USER_MENTION_PATTERN = Pattern.compile("<@!?([0-9]+)>");
    protected static final Pattern CHANNEL_MENTION_PATTERN = Pattern.compile("<#!?([0-9]+)>");
    protected static final Pattern ROLE_MENTION_PATTERN = Pattern.compile("<@&!?([0-9]+)>");

    protected final StringBuilder builder = new StringBuilder();

    protected boolean isTTS = false;
    protected MessageEmbed embed;

    public MessageBuilder() {}

    /**
     * Makes the created Message a TTS message.
     * <br>TTS stands for Text-To-Speech. When a TTS method is received by the Discord client,
     * it is vocalized so long as the user has not disabled TTS.
     *
     * @param  tts
     *         whether the created Message should be a tts message
     *
     * @return Returns the {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
     */
    public MessageBuilder setTTS(boolean tts)
    {
        this.isTTS = tts;
        return this;
    }
    
    /**
     * Adds a {@link net.dv8tion.jda.core.entities.MessageEmbed} to the Message. Embeds can be built using
     * the {@link net.dv8tion.jda.core.EmbedBuilder} and offer specialized formatting.
     *
     * @param  embed
     *         the embed to add, or null to remove
     *
     * @return Returns the {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
     */
    public MessageBuilder setEmbed(MessageEmbed embed)
    {
        this.embed = embed;
        return this;
    }

    /**
     * Appends a String to the Message.
     *
     * @param  text
     *         the text to append
     *
     * @return Returns the {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
     */
    @Override
    public MessageBuilder append(CharSequence text)
    {
        builder.append(text);
        return this;
    }

    @Override
    public MessageBuilder append(CharSequence text, int start, int end)
    {
        builder.append(text, start, end);
        return this;
    }

    @Override
    public MessageBuilder append(char c)
    {
        builder.append(c);
        return this;
    }

    /**
     * Appends the string representation of an object to the Message.
     * <br>This is the same as {@link #append(CharSequence) append(String.valueOf(object))}
     * 
     * @param  object
     *         the object to append
     *
     * @return Returns the {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
     */
    public MessageBuilder append(Object object)
    {
        return append(String.valueOf(object));
    }

    /**
     * Appends a mention to the Message.
     * <br>Typical usage would be providing an {@link net.dv8tion.jda.core.entities.IMentionable IMentionable} like
     * {@link net.dv8tion.jda.core.entities.User User} or {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *
     * @param  mention
     *         the mention to append
     *
     * @return The {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
     */
    public MessageBuilder append(IMentionable mention)
    {
        builder.append(mention.getAsMention());
        return this;
    }

    /**
     * Appends a String using the specified chat {@link net.dv8tion.jda.core.MessageBuilder.Formatting Formatting(s)}.
     *
     * @param  text
     *         the text to append.
     * @param  format
     *         the format(s) to apply to the text.
     *
     * @return Returns the {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
     */
    public MessageBuilder append(CharSequence text, Formatting... format)
    {
        boolean blockPresent = false;
        for (Formatting formatting : format)
        {
            if (formatting == Formatting.BLOCK)
            {
                blockPresent = true;
                continue;
            }
            builder.append(formatting.getTag());
        }
        if (blockPresent)
            builder.append(Formatting.BLOCK.getTag());

        builder.append(text);

        if (blockPresent)
            builder.append(Formatting.BLOCK.getTag());
        for (int i = format.length - 1; i >= 0; i--)
        {
            if (format[i] == Formatting.BLOCK) continue;
            builder.append(format[i].getTag());
        }
        return this;
    }

    /**
     * This method is an extended form of {@link String#format(String, Object...)}. It allows for all of
     * the token replacement functionality that String.format(String, Object...) supports.
     * <br>A lot of JDA entities implement {@link java.util.Formattable Formattable} and will provide
     * specific format outputs for their specific type.
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.entities.IMentionable IMentionable}
     *     <br>These will output their {@link net.dv8tion.jda.core.entities.IMentionable#getAsMention() getAsMention} by default,
     *         some implementations have alternatives such as {@link net.dv8tion.jda.core.entities.User User} and {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.</li>
     *     <li>{@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}
     *     <br>All message channels format to {@code "#" + getName()} by default, TextChannel has special handling
     *         and uses the getAsMention output by default and the MessageChannel output as alternative ({@code #} flag).</li>
     *     <li>{@link net.dv8tion.jda.core.entities.Message Message}
     *     <br>Messages by default output their {@link net.dv8tion.jda.core.entities.Message#getContent() getContent()} value and
     *         as alternative use the {@link net.dv8tion.jda.core.entities.Message#getRawContent() getRawContent()} value</li>
     * </ul>
     *
     * <p>Example:
     * <br>If you placed the following code in an method handling a
     * {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     * <br><pre>{@code
     * User user = event.getAuthor();
     * MessageBuilder builder = new MessageBuilder();
     * builder.appendFormat("%#s is really cool!", user);
     * builder.build();
     * }</pre>
     *
     * It would build a message that mentions the author and says that he is really cool!. If the user's
     * name was "Minn" and his discriminator "6688", it would say:
     * <br><pre>  "Minn#6688 is really cool!"</pre>
     * <br>Note that this uses the {@code #} flag to utilize the alternative format for {@link net.dv8tion.jda.core.entities.User User}.
     * <br>By default it would fallback to {@link net.dv8tion.jda.core.entities.IMentionable#getAsMention()}
     *
     * @param  format
     *         a format string.
     * @param  args
     *         an array objects that will be used to replace the tokens, they must be
     *         provided in the order that the tokens appear in the provided format string.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided format string is {@code null} or empty
     *
     * @return The {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
     */
    public MessageBuilder appendFormat(String format, Object... args)
    {
        Args.notEmpty(format, "Format String");
        this.append(String.format(format, args));
        return this;
    }

    /**
     * Appends a code-block to the Message.
     * <br>Discord uses <a href="https://highlightjs.org/">Highlight.js</a> for its language highlighting support. You can find out what
     * specific languages are supported <a href="https://github.com/isagalaev/highlight.js/tree/master/src/languages">here</a>.
     *
     * @param  text
     *         the code to append
     * @param  language
     *         the language of the code. If unknown use an empty string
     *
     * @return Returns the {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
     */
    public MessageBuilder appendCodeBlock(CharSequence text, CharSequence language)
    {
        builder.append("```").append(language).append('\n').append(text).append("\n```");
        return this;
    }

    /**
     * Returns the current length of the content that will be built into a {@link net.dv8tion.jda.core.entities.Message Message}
     * when {@link #build()} is called.
     * <br>If this value is {@code 0} (and there is no embed) or greater than {@code 2000} when {@link #build()} is called, an exception
     * will be raised as you cannot send an empty message to Discord and Discord has a hard limit of 2000 characters per message.
     *
     * <p><b>Hint:</b> You can use {@link #build(int, int)} or
     * {@link #buildAll(net.dv8tion.jda.core.MessageBuilder.SplitPolicy...) buildAll(SplitPolicy...)} as possible ways to
     * deal with the 2000 character cap.
     *
     * @return the current length of the content that will be built into a Message.
     */
    public int length()
    {
        return builder.length();
    }

    /**
     * Checks if the message contains any contend. This includes text as well as embeds.
     * 
     * @return weather the message contains content
     */
    public boolean isEmpty() {
        return builder.length() == 0 && embed == null;
    }

    /**
     * Creates a {@link net.dv8tion.jda.core.entities.Message Message} object from this MessageBuilder
     *
     * <p><b>Hint:</b> You can use {@link #build(int, int)} or
     * {@link #buildAll(net.dv8tion.jda.core.MessageBuilder.SplitPolicy...) buildAll(SplitPolicy...)} as possible ways to
     * deal with the 2000 character cap.
     *
     * @throws java.lang.IllegalStateException
     *         <ul>
     *             <li>If you attempt to build() an empty Message ({@link #length()} is {@code 0} and no
     *             {@link net.dv8tion.jda.core.entities.MessageEmbed} was provided to {@link #setEmbed(net.dv8tion.jda.core.entities.MessageEmbed)})</li>
     *             <li>If you attempt to build() a Message with more than 2000 characters of content.</li>
     *         </ul>
     *
     * @return the created {@link net.dv8tion.jda.core.entities.Message Message}
     */
    public Message build()
    {
        String message = builder.toString();
        if (this.isEmpty())
            throw new IllegalStateException("Cannot build a Message with no content. (You never added any content to the message)");
        if (message.length() > 2000)
            throw new IllegalStateException("Cannot build a Message with more than 2000 characters. Please limit your input.");

        return new MessageImpl("", null, false).setContent(message).setTTS(isTTS)
                .setEmbeds(embed == null ? new LinkedList<>() : Collections.singletonList(embed));
    }

    /**
     * Replaces each substring that matches the target string with the specified replacement string.
     * The replacement proceeds from the beginning of the string to the end, for example, replacing
     * "aa" with "b" in the message "aaa" will result in "ba" rather than "ab".
     *
     * @param  target
     *         the sequence of char values to be replaced
     * @param  replacement
     *         the replacement sequence of char values
     *
     * @return Returns the {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
     */
    public MessageBuilder replaceAll(String target, String replacement)
    {
        int index;
        while ((index = builder.indexOf(target)) != -1)
        {
            builder.replace(index, index + target.length(), replacement);
        }
        return this;
    }

    /**
     * Replaces the first substring that matches the target string with the specified replacement string.
     *
     * @param  target
     *         the sequence of char values to be replaced
     * @param  replacement
     *         the replacement sequence of char values
     *
     * @return Returns the {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
     */
    public MessageBuilder replaceFirst(String target, String replacement)
    {
        int index = builder.indexOf(target);
        if (index != -1)
        {
            builder.replace(index, index + target.length(), replacement);
        }
        return this;
    }

    /**
     * Replaces the last substring that matches the target string with the specified replacement string.
     *
     * @param  target
     *         the sequence of char values to be replaced
     * @param  replacement
     *         the replacement sequence of char values
     *
     * @return Returns the {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
     */
    public MessageBuilder replaceLast(String target, String replacement)
    {
        int index = builder.lastIndexOf(target);
        if (index != -1)
        {
            builder.replace(index, index + target.length(), replacement);
        }
        return this;
    }

    /**
     * Removes all mentions and replaces them with the closest looking textual representation.
     *
     * <p>Use this over {@link #stripMentions(Guild)} if {@link net.dv8tion.jda.core.entities.User User} mentions
     * should be replaced with their {@link net.dv8tion.jda.core.entities.User#getName()} instead of their Nicknames.
     *
     * @param jda
     *        The JDA instance used to resolve the mentions.
     *
     * @return Returns the {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
     */
    public MessageBuilder stripMentions(JDA jda)
    {
        return this.stripMentions(jda, (Guild) null, MentionType.EVERYONE, MentionType.HERE, MentionType.CHANNEL, MentionType.ROLE, MentionType.USER);
    }

    /**
     * Removes all mentions and replaces them with the closest looking textual representation.
     *
     * <p>Use this over {@link #stripMentions(JDA)} if {@link net.dv8tion.jda.core.entities.User User} mentions should
     * be replaced with their nicknames in a specific guild based.
     * <br>Uses {@link net.dv8tion.jda.core.entities.Member#getEffectiveName()}
     *
     * @param  guild
     *         the guild for {@link net.dv8tion.jda.core.entities.User User} mentions
     *
     * @return Returns the {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
     */
    public MessageBuilder stripMentions(Guild guild)
    {
        return this.stripMentions(guild.getJDA(), guild, MentionType.EVERYONE, MentionType.HERE, MentionType.CHANNEL, MentionType.ROLE, MentionType.USER);
    }

    /**
     * Removes all mentions of the specified types and replaces them with the closest looking textual representation.
     *
     * <p>Use this over {@link #stripMentions(JDA, MentionType...)} if {@link net.dv8tion.jda.core.entities.User User} mentions should
     * be replaced with their nicknames in a specific guild based.
     * <br>Uses {@link net.dv8tion.jda.core.entities.Member#getEffectiveName()}
     *
     * @param  guild
     *         the guild for {@link net.dv8tion.jda.core.entities.User User} mentions
     * @param  types
     *         the {@link MentionType MentionTypes} that should be stripped
     *
     * @return Returns the {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
     */
    public MessageBuilder stripMentions(Guild guild, MentionType... types)
    {
        return this.stripMentions(guild.getJDA(), guild, types);
    }

    /**
     * Removes all mentions of the specified types and replaces them with the closest looking textual representation.
     *
     * <p>Use this over {@link #stripMentions(Guild, MentionType...)} if {@link net.dv8tion.jda.core.entities.User User}
     * mentions should be replaced with their {@link net.dv8tion.jda.core.entities.User#getName()}.
     * 
     * @param  jda
     *         The JDA instance used to resolve the mentions.
     * @param  types
     *         the {@link MentionType MentionTypes} that should be stripped
     *
     * @return Returns the {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder} instance. Useful for chaining.
     */
    public MessageBuilder stripMentions(JDA jda, MentionType... types)
    {
        return this.stripMentions(jda, (Guild) null, types);
    }

    private MessageBuilder stripMentions(JDA jda, Guild guild, MentionType... types)
    {
        if (types == null)
            return this;

        String string = null;

        for (MentionType mention : types)
        {
            if (mention != null)
            {
                switch (mention)
                {
                    case EVERYONE:
                        replaceAll("@everyone", "@\u0435veryone");
                        break;
                    case HERE:
                        replaceAll("@here", "@h\u0435re");
                        break;
                    case CHANNEL:
                    {
                        if (string == null)
                        {
                            string = builder.toString();
                        }
                        
                        Matcher matcher = CHANNEL_MENTION_PATTERN.matcher(string);
                        while (matcher.find())
                        {
                            TextChannel channel = jda.getTextChannelById(matcher.group(1));
                            if (channel != null)
                            {
                                replaceAll(matcher.group(), "#" + channel.getName());
                            }
                        }
                        break;
                    }
                    case ROLE:
                    {    
                        if (string == null)
                        {
                            string = builder.toString();
                        }

                        Matcher matcher = ROLE_MENTION_PATTERN.matcher(string);
                        while (matcher.find())
                        {
                            for (Guild g : jda.getGuilds())
                            {
                                Role role = g.getRoleById(matcher.group(1));
                                if (role != null)
                                {
                                    replaceAll(matcher.group(), "@"+role.getName());
                                    break;
                                }
                            }
                        }
                        break;
                    }
                    case USER:
                    {
                        if (string == null)
                        {
                            string = builder.toString();
                        }

                        Matcher matcher = USER_MENTION_PATTERN.matcher(string);
                        while (matcher.find())
                        {
                            User user = jda.getUserById(matcher.group(1));
                            String replacement = null;

                            if (user == null)
                                continue;

                            Member member;

                            if (guild != null && (member = guild.getMember(user)) != null)
                                replacement = member.getEffectiveName();
                            else
                                replacement = user.getName();

                            replaceAll(matcher.group(), "@" + replacement);
                        }
                        break;
                    }
                }
            }
        }
        
        return this;
    }

    /**
     * Returns the underlying {@link StringBuilder}.
     * 
     * @return the {@link StringBuilder} used by this {@link MessageBuilder}
     */
    public StringBuilder getStringBuilder()
    {
        return this.builder;
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring between the specified indices.
     *
     * <p>If no such value of {@code target} exists, then {@code -1} is returned.
     *
     * @param  target
     *         the substring to search for.
     * @param  fromIndex
     *         the index from which to start the search.
     * @param  endIndex
     *         the index at which to end the search.
     *
     * @throws java.lang.IndexOutOfBoundsException
     *         <ul>
     *             <li>If the {@code fromIndex} is outside of the range of {@code 0} to {@link #length()}</li>
     *             <li>If the {@code endIndex} is outside of the range of {@code 0} to {@link #length()}</li>
     *             <li>If the {@code fromIndex} is greater than {@code endIndex}</li>
     *         </ul>
     *
     * @return the index of the first occurrence of the specified substring between
     *         the specified indices or {@code -1} if there is no such occurrence.
     */
    public int indexOf(CharSequence target, int fromIndex, int endIndex)
    {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("index out of range: " + fromIndex);
        if (endIndex < 0)
            throw new IndexOutOfBoundsException("index out of range: " + endIndex);
        if (fromIndex > length())
            throw new IndexOutOfBoundsException("fromIndex > length()");
        if (fromIndex > endIndex)
            throw new IndexOutOfBoundsException("fromIndex > endIndex");
        
        if (endIndex >= builder.length())
        {
            endIndex = builder.length() - 1;
        }

        int targetCount = target.length();
        if (targetCount == 0)
        {
            return fromIndex;
        }

        char strFirstChar = target.charAt(0);
        int max = endIndex + targetCount - 1;

        lastCharSearch:
        for (int i = fromIndex; i <= max; i++)
        {
            if (builder.charAt(i) == strFirstChar)
            {
                for (int j = 1; j < targetCount; j++)
                {
                    if (builder.charAt(i + j) != target.charAt(j))
                    {
                        continue lastCharSearch;
                    }
                }
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index within this string of the last occurrence of the
     * specified substring between the specified indices.
     *
     * If no such value of {@code target} exists, then {@code -1} is returned.
     *
     * @param  target
     *         the substring to search for.
     * @param  fromIndex
     *         the index from which to start the search.
     * @param  endIndex
     *         the index at which to end the search.
     *
     * @throws java.lang.IndexOutOfBoundsException
     *         <ul>
     *             <li>If the {@code fromIndex} is outside of the range of {@code 0} to {@link #length()}</li>
     *             <li>If the {@code endIndex} is outside of the range of {@code 0} to {@link #length()}</li>
     *             <li>If the {@code fromIndex} is greater than {@code endIndex}</li>
     *         </ul>
     *
     * @return the index of the last occurrence of the specified substring between
     *         the specified indices or {@code -1} if there is no such occurrence.
     */
    public int lastIndexOf(CharSequence target, int fromIndex, int endIndex)
    {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("index out of range: " + fromIndex);
        if (endIndex < 0)
            throw new IndexOutOfBoundsException("index out of range: " + endIndex);
        if (fromIndex > length())
            throw new IndexOutOfBoundsException("fromIndex > length()");
        if (fromIndex > endIndex)
            throw new IndexOutOfBoundsException("fromIndex > endIndex");
        
        if (endIndex >= builder.length())
        {
            endIndex = builder.length() - 1;
        }

        int targetCount = target.length();
        if (targetCount == 0)
        {
            return endIndex;
        }

        int rightIndex = endIndex - targetCount;

        if (fromIndex > rightIndex)
        {
            fromIndex = rightIndex;
        }

        int strLastIndex = targetCount - 1;
        char strLastChar = target.charAt(strLastIndex);

        int min = fromIndex + targetCount - 1;

        lastCharSearch:
        for (int i = endIndex; i >= min; i--)
        {
            if (builder.charAt(i) == strLastChar)
            {
                for (int j = strLastIndex - 1, k = 1; j >= 0; j--, k++)
                {
                    if (builder.charAt(i - k) != target.charAt(j))
                    {
                        continue lastCharSearch;
                    }
                }
                return i - target.length() + 1;
            }
        }
        return -1;
    }

    /**
     * Creates a {@link java.util.Queue Queue} of {@link net.dv8tion.jda.core.entities.Message Message} objects from this MessageBuilder.
     *
     * <p>This method splits the content if it exceeds 2000 chars. The splitting behaviour can be customized using {@link SplitPolicy SplitPolicies}.
     * The method will try the policies in the order they are passed to it.
     * <br>If no SplitPolicy is provided each message will be split after exactly 2000 chars.
     *
     * <p><b>This is not Markdown safe.</b> An easy workaround is to include <a href="https://en.wikipedia.org/wiki/Zero-width_space">Zero Width Spaces</a>
     * as predetermined breaking points to the message and only split on them.
     *
     * @param  policy
     *         The {@link net.dv8tion.jda.core.MessageBuilder.SplitPolicy} defining how to split the text in the
     *         MessageBuilder into different, individual messages.
     * 
     * @return the created {@link net.dv8tion.jda.core.entities.Message Messages}
     */
    public Queue<Message> buildAll(SplitPolicy... policy)
    {
        if (this.isEmpty())
            throw new UnsupportedOperationException("Cannot build a Message with no content. (You never added any content to the message)");

        LinkedList<Message> messages = new LinkedList<Message>();

        if (builder.length() <= 2000) {
            messages.add(this.build());
            return messages;
        }

        if (policy == null || policy.length == 0)
        {
            policy = new SplitPolicy[]{ SplitPolicy.ANYWHERE };
        }

        int currentBeginIndex = 0;

        messageLoop:
        while (currentBeginIndex < builder.length() - 2001)
        {
            for (int i = 0; i < policy.length; i++)
            {
                int currentEndIndex = policy[i].nextMessage(currentBeginIndex, this);
                if (currentEndIndex != -1)
                {
                    messages.add(build(currentBeginIndex, currentEndIndex));
                    currentBeginIndex = currentEndIndex;
                    continue messageLoop;
                }
            }
            throw new RuntimeException("failed to split the messages");
        }

        if (currentBeginIndex < builder.length() - 1)
        {
            messages.add(build(currentBeginIndex, builder.length() - 1));
        }

        if (this.embed != null)
        {
            ((MessageImpl) messages.get(messages.size() - 1)).setEmbeds(Collections.singletonList(embed));
        }

        return messages;
    }

    protected Message build(int beginIndex, int endIndex)
    {
        return new MessageImpl("", null, false).setContent(builder.substring(beginIndex, endIndex)).setTTS(isTTS);
    }

    /**
     * Interface to allow custom implementation of Splitting rules for
     * {@link #buildAll(net.dv8tion.jda.core.MessageBuilder.SplitPolicy...) MessageBuilder.buildAll(SplitPolicy...)}.
     */
    public interface SplitPolicy
    {
        /**
         * Splits on newline chars {@code `\n`}.
         */
        SplitPolicy NEWLINE = new CharSequenceSplitPolicy("\n", true);

        /**
         * Splits on space chars {@code `\u0020`}.
         */
        SplitPolicy SPACE = new CharSequenceSplitPolicy(" ", true);

        /**
         * Splits exactly after 2000 chars.
         */
        SplitPolicy ANYWHERE = (i, b) -> Math.min(i + 2000, b.length());

        /**
         * Creates a new {@link SplitPolicy} splitting on the specified chars.
         *
         * @param  chars
         *         the chars to split on
         * @param  remove
         *         weather to remove the chars when splitting on them
         *
         * @return a new {@link SplitPolicy}
         */
        static SplitPolicy onChars(CharSequence chars, boolean remove)
        {
            return new CharSequenceSplitPolicy(chars, remove);
        }

        /**
         * Default {@link SplitPolicy} implementation. Splits on a specified {@link CharSequence}.
         */
        class CharSequenceSplitPolicy implements SplitPolicy
        {
            private final boolean remove;
            private final CharSequence chars;

            private CharSequenceSplitPolicy(final CharSequence chars, final boolean remove)
            {
                this.chars = chars;
                this.remove = remove;
            }

            @Override
            public int nextMessage(final int currentBeginIndex, final MessageBuilder builder)
            {
                int currentEndIndex = builder.lastIndexOf(this.chars, currentBeginIndex, currentBeginIndex + 2000 - (this.remove ? this.chars.length() : 0));
                if (currentEndIndex < 0)
                {
                    return -1;
                }
                else
                {
                    return currentEndIndex + this.chars.length();
                }
            }
        }

        /**
         * Calculates the endIndex for the next {@link net.dv8tion.jda.core.entities.Message Message}.
         * 
         * @param  currentBeginIndex
         *         the index the next {@link net.dv8tion.jda.core.entities.Message Message} should start from
         * @param  builder
         *         the {@link net.dv8tion.jda.core.MessageBuilder MessageBuilder}
         *
         * @return the end Index of the next {@link net.dv8tion.jda.core.entities.Message Message}
         * 
         * @throws java.lang.RuntimeException when splitting fails
         * 
         */
        int nextMessage(int currentBeginIndex, MessageBuilder builder);
    }

    /**
     * Holds the strippable mention types used in {@link MessageBuilder#stripMentions(JDA, MentionType...)}
     * and {@link MessageBuilder#stripMentions(Guild, MentionType...)}
     */
    public enum MentionType {
        /**
         * <b>@everyone</b> mentions 
         */
        EVERYONE,
        /**
         * <b>@here</b> mentions
         */
        HERE,
        /**
         * <b>@User</b> mentions
         */
        USER,
        /**
         * <b>#channel</b> mentions
         */
        CHANNEL,
        /**
         * <b>@Role</b> mentions
         */
        ROLE;
    }

    /**
     * Holds the available formatting used in {@link MessageBuilder#append(java.lang.CharSequence, net.dv8tion.jda.core.MessageBuilder.Formatting...)}
     */
    public enum Formatting
    {
        ITALICS("*"),
        BOLD("**"),
        STRIKETHROUGH("~~"),
        UNDERLINE("__"),
        BLOCK("`");

        private final String tag;

        Formatting(String tag)
        {
            this.tag = tag;
        }

        private String getTag()
        {
            return tag;
        }
    }
}
