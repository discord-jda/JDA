/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.dv8tion.jda.core;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.MessageImpl;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageBuilder
{
    public static final String USER_KEY = "%U%";
    public static final String ROLE_KEY = "%R%";
    public static final String TEXTCHANNEL_KEY = "%TC%";
    public static final String EVERYONE_KEY = "%E%";
    public static final String HERE_KEY = "%H%";

    /**
     * A constant for <b>@everyone</b> mentions.
     * Used in {@link #append(IMentionable)}
     */
    public static final IMentionable EVERYONE_MENTION = () -> "@everyone";
    /**
     * A constant for <b>@here</b> mentions.
     * Used in {@link #append(IMentionable)}
     */
    public static final IMentionable HERE_MENTION = () -> "@here";

    protected static final Pattern FORMAT_PATTERN = Pattern.compile(String.format("%s|%s|%s|%s|%s", USER_KEY, ROLE_KEY, TEXTCHANNEL_KEY, EVERYONE_KEY, HERE_KEY));
    protected static final Pattern USER_MENTION_PATTERN = Pattern.compile("<@!{0,1}([0-9]+)>");
    protected static final Pattern CHANNEL_MENTION_PATTERN = Pattern.compile("<#!{0,1}([0-9]+)>");
    protected static final Pattern ROLE_MENTION_PATTERN = Pattern.compile("<@&!{0,1}([0-9]+)>");

    protected final StringBuilder builder = new StringBuilder();

    protected boolean isTTS = false;
    protected MessageEmbed embed;

    public MessageBuilder() {}

    /**
     * Makes the created Message a TTS message
     *
     * @param tts whether the created Message should be a tts message
     * @return this instance
     */
    public MessageBuilder setTTS(boolean tts)
    {
        this.isTTS = tts;
        return this;
    }
    
    /**
     * Adds an embed to the Message
     *
     * @param embed the embed to add, or null to remove
     * @return this instance
     */
    public MessageBuilder setEmbed(MessageEmbed embed)
    {
        this.embed = embed;
        return this;
    }

    /**
     * Appends a string to the Message
     * 
     * @deprecated use {@link #append(CharSequence)} instead
     *
     * @param text the text to append
     * @return this instance
     */
    @Deprecated
    public MessageBuilder appendString(CharSequence text)
    {
        return this.append(text);
    }


    /**
     * Appends a string to the Message
     *
     * @param text the text to append
     * @return this instance
     */
    public MessageBuilder append(CharSequence text)
    {
        builder.append(text);
        return this;
    }

    /**
     * Appends a mention to the Message
     *
     * @param mention the mention to append
     * @return this instance
     */
    public MessageBuilder append(IMentionable mention)
    {
        builder.append(mention.getAsMention());
        return this;
    }

    /**
     * Appends a formatted string to the Message
     *
     * @param text   the text to append
     * @param format the format(s) to apply to the text
     * @return this instance
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
     * Appends a formatted string to the Message
     * 
     * @deprecated use {@link #append(CharSequence, Formatting...)} instead
     *
     * @param text   the text to append
     * @param format the format(s) to apply to the text
     * @return this instance
     */
    @Deprecated
    public MessageBuilder appendString(CharSequence text, Formatting... format)
    {
        return this.append(text, format);
    }

    /**
     * This method is an extended form of {@link String#format(String, Object...)}. It allows for all of
     * the token replacement functionality that String.format(String, Object...) supports, but it also supports
     * specialized token replacement specific to JDA objects.
     * <p>
     * Current tokens:
     * <ul>
     *     <li><b>%U%</b> - Used to mention a {@link net.dv8tion.jda.core.entities.User User}.
     *          Same as {@link #appendMention(net.dv8tion.jda.core.entities.User)}</li>
     *     <li><b>%R%</b> - Used to mention a {@link net.dv8tion.jda.core.entities.Role Role}.
     *          Same as {@link #appendMention(net.dv8tion.jda.core.entities.Role)}</li>
     *     <li><b>%TC%</b> - Used to mention a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *          Same as {@link #appendMention(net.dv8tion.jda.core.entities.TextChannel)}</li>
     *     <li><b>%E%</b> - Used to mention @everyone. Same as {@link #appendEveryoneMention()}</li>
     *     <li><b>%H%</b> - Used to mention @here. Same as {@link #appendHereMention()}</li>
     * </ul>
     * <p>
     * Example: <br>
     * If you placed the following code in an method handling a
     * {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}<br>
     *
     * <pre>{@code
     * User user = event.getAuthor();
     * MessageBuilder builder = new MessageBuilder();
     * builder.appendFormat("%U% is really cool!", user);
     * builder.build();
     * }</pre>
     *
     * It would build a message that mentions the author and says that he is really cool!. If the user's
     * name was "Bob", it would say:<br>
     * <pre>  "Bob is really cool!"</pre>
     *
     * @param format
     *          A format string.
     * @param args
     *          An array objects that will be used to replace the tokens.
     *          They must be provided in the order that the tokens appear in the provided format string.
     * @return
     *      this instance of the MessageBuilder. Useful for chaining.
     */
    public MessageBuilder appendFormat(String format, Object... args)
    {
        if (format == null || format.isEmpty())
            return this;

        int index = 0;
        int stringIndex = 0;
        StringBuilder sb = new StringBuilder();
        Matcher m = FORMAT_PATTERN.matcher(format);
        List<Class< ? extends IMentionable>> classes = Arrays.asList(User.class, TextChannel.class, Role.class);
        while (m.find() && stringIndex < format.length())
        {
            Class<? extends IMentionable> target = null;
            boolean everyone = false;
            switch (m.group())
            {
                case USER_KEY:
                    target = User.class;
                    break;
                case TEXTCHANNEL_KEY:
                    target = TextChannel.class;
                    break;
                case ROLE_KEY:
                    target = Role.class;
                    break;
                case EVERYONE_KEY:
                    everyone = true;
                    break;
                case HERE_KEY:
                    everyone = false;
                    break;
                default:
                    throw new IllegalArgumentException("MessageBuilder's format regex triggered on an unknown key. How?!");
            }

            sb.append(format.substring(stringIndex, m.start())).append("%s");
            stringIndex = m.end();
            if (target != null)
            {
                boolean found = false;
                for (int i = index; i < args.length; i++)
                {
                    Object arg = args[i];

                    //This is a JDA object. If it isn't, skip it.
                    if (classes.stream().anyMatch(c -> c.isInstance(arg)))
                    {
                        //This isn't the object type we were expecting.
                        if (!target.isInstance(arg))
                            throw new IllegalArgumentException(String.format("Expected: %s at args index: %d but received: %s instead",
                                    target.getSimpleName(), index, arg.getClass().getSimpleName()));
                        if (arg instanceof User)
                        {
                            User u = (User) arg;
                            args[i] = u.getAsMention();
                        }
                        else if (arg instanceof TextChannel)
                        {
                            TextChannel tc = (TextChannel) arg;
                            args[i] = tc.getAsMention();
                        }
                        else if (arg instanceof Role)
                        {
                            Role r = (Role) arg;
                            args[i] = r.getAsMention();
                        }
                        else
                            throw new IllegalArgumentException("When checking instances of arguments, something failed. Contact dev.");

                        index++;
                        found = true;
                        break;
                    }
                    index++;
                }
                if (!found)
                    throw new MissingFormatArgumentException(m.group());
            }
            else
            {
                if (everyone)
                {
                    sb.append("@everyone");
                }
                else
                {
                    sb.append("@here");
                }
            }
        }
        if (stringIndex < format.length())
            sb.append(format.substring(stringIndex, format.length()));
        String finalFormat = String.format(sb.toString(), args);
        builder.append(finalFormat);
        return this;
    }

    /**
     * Appends a code-block to the Message
     *
     * @param text     the code to append
     * @param language the language of the code. If unknown use an empty string
     * @return this instance
     */
    public MessageBuilder appendCodeBlock(CharSequence text, CharSequence language)
    {
        builder.append("```").append(language).append('\n').append(text).append("\n```");
        return this;
    }

    /**
     * Appends a mention to the Message
     *
     * @deprecated use {@link #append(IMentionable)} instead
     *
     * @param user the user to mention
     * @return this instance
     */
    @Deprecated
    public MessageBuilder appendMention(User user)
    {
        return this.append(user);
    }

    /**
     * Appends a @everyone mention to the Message
     * 
     * @deprecated use {@linkplain MessageBuilder#append(IMentionable)} with {@link MessageBuilder#EVERYONE_MENTION} instead
     *
     * @return this instance
     */
    @Deprecated
    public MessageBuilder appendEveryoneMention()
    {
        return this.append(EVERYONE_MENTION);
    }
    
    /**
     * Appends a @here mention to the Message
     * 
     * @deprecated use {@linkplain MessageBuilder#append(IMentionable)} with {@link MessageBuilder#HERE_MENTION} instead
     *
     * @return this instance
     */
    @Deprecated
    public MessageBuilder appendHereMention()
    {
        return this.append(HERE_MENTION);
    }

    /**
     * Appends a channel mention to the Message.
     * For this to work, the given TextChannel has to be from the Guild the mention is posted to.
     *
     * @deprecated use {@link #append(IMentionable)} instead
     *
     * @param channel the TextChannel to mention
     * @return this instance
     */
    @Deprecated
    public MessageBuilder appendMention(TextChannel channel)
    {
        return this.append(channel);
    }

    /**
     * Appends a Role mention to the Message.
     * For this to work, the given Role has to be from the Guild the mention is posted to.
     *
     * @deprecated use {@link #append(IMentionable)} instead
     *
     * @param role the Role to mention
     * @return this instance
     */
    @Deprecated
    public MessageBuilder appendMention(Role role)
    {
        return this.append(role);
    }

    /**
     * Returns the current length of the content that will be built into a {@link net.dv8tion.jda.core.entities.Message Message}
     * when {@link #build()} is called.<br>
     * If this value is <code>0</code> or greater than <code>2000</code> when {@link #build()} is called, an exception
     * will be raised.
     *
     * @return
     *      The currently length of the content that will be built into a Message.
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
     * Creates a {@link net.dv8tion.jda.core.entities.Message Message} object from this Builder
     *
     * @return the created {@link net.dv8tion.jda.core.entities.Message Message}
     *
     * @throws UnsupportedOperationException
     *      <ul>
     *          <li>If you attempt to build() an empty Message (no content added to the Message)</li>
     *          <li>If you attempt to build() a Message with more than 2000 characters of content.</li>
     *      </ul>
     */
    public Message build()
    {
        String message = builder.toString();
        if (message.isEmpty())
            throw new UnsupportedOperationException("Cannot build a Message with no content. (You never added any content to the message)");
        if (message.length() > 2000)
            throw new UnsupportedOperationException("Cannot build a Message with more than 2000 characters. Please limit your input.");

        return new MessageImpl("", null, false).setContent(message).setTTS(isTTS)
                .setEmbeds(embed == null ? new LinkedList<>() : Collections.singletonList(embed));
    }

    /**
     * Replaces each substring that matches the target string with the specified replacement string.
     * The replacement proceeds from the beginning of the string to the end, for example, replacing
     * "aa" with "b" in the message "aaa" will result in "ba" rather than "ab".
     *
     * @param  target The sequence of char values to be replaced
     * @param  replacement The replacement sequence of char values
     * @return this instance
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
     * @param  target The sequence of char values to be replaced
     * @param  replacement The replacement sequence of char values
     * @return this instance
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
     * @param  target The sequence of char values to be replaced
     * @param  replacement The replacement sequence of char values
     * @return this instance
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
     * Removes all mentions of the specified types and replaces them with the closest looking textual representation.
     *
     * @param  jda The JDA instance, only needed for {@link MentionType.USER User}, {@link MentionType.CHANNEL Channel} and {@link MentionType.GUILD Guild} mentions
     * @param types The mention types that should be stripped
     * @return this instance
     */
    public MessageBuilder stripMentions(JDA jda, MentionType... types) {
        String string = null;
        if (types != null)
        {
            for (MentionType mention : types)
            {
                switch (mention)
                {
                    case EVERYONE:
                        replaceAll("@everyone", "@\u200Beveryone");
                        break;
                    case HERE:
                        replaceAll("@here", "@\u200Bhere");
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
                        }
                        break;
                    case ROLE:
                        {
                            if (string == null)
                            {
                                string = builder.toString();
                            }
                            
                            Matcher matcher = ROLE_MENTION_PATTERN.matcher(string);
                            while (matcher.find())
                            {
                                for (Guild guild : jda.getGuilds())
                                {
                                    Role role = guild.getRoleById(matcher.group(1));
                                    if (role != null)
                                    {
                                        replaceAll(matcher.group(), "@"+role.getName());
                                        break;
                                    }
                                }
                            }
                        }
                        break;
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
                                if (user != null)
                                {
                                    replaceAll(matcher.group(), "@"+user.getName());
                                }
                            }
                        }
                        break;
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
    public StringBuilder getStringBuilder() {
        return this.builder;
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring between the specified indices.
     *
     * If no such value of <i>k</i> exists, then {@code -1} is returned.
     *
     * @param   target the substring to search for.
     * @param   fromIndex the index from which to start the search.
     * @param   endIndex the index at which to end the search.
     * @return  the index of the first occurrence of the specified substring between the specified indices
     *          or {@code -1} if there is no such occurrence.
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

        lastCharSearch: for (int i = fromIndex; i <= max; i++)
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
     * If no such value of <i>k</i> exists, then {@code -1} is returned.
     *
     * @param   target the substring to search for.
     * @param   fromIndex the index from which to start the search.
     * @param   endIndex the index at which to end the search.
     * @return  the index of the last occurrence of the specified substring between the specified indices
     *          or {@code -1} if there is no such occurrence.
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

        lastCharSearch: for (int i = endIndex; i >= min; i--)
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
     * This is not finished yet and just 
     * Docs are also to be made.
     */
    public Queue<Message> buildAll(SplitPolicy... policy)
    {
        if (builder.length() == 0)
            throw new UnsupportedOperationException("Cannot build a Message with no content. (You never added any content to the message)");

        LinkedList<Message> messages = new LinkedList<Message>();

        if (builder.length() <= 2000) {
            messages.add(this.build());
            return messages;
        } 

        int currentBeginIndex = 0;

        messageLoop: while (currentBeginIndex < builder.length() - 2001)
        {
            for (int i = 0; i < policy.length; i++)
            {
                try
                {
                    int currentEndIndex = policy[i].nextMessage(currentBeginIndex, this);
                    messages.add(build(currentBeginIndex, currentEndIndex));
                    currentBeginIndex = currentEndIndex;
                    continue messageLoop;
                }
                catch (Exception e) {}
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

    public static abstract class SplitPolicy
    {

        public static SplitPolicy onChars(CharSequence chars, boolean remove)
        {
            return new CharSequenceSplitPolicy(chars, remove);
        }

        public static class CharSequenceSplitPolicy extends SplitPolicy
        {
            private final boolean remove;
            private final CharSequence chars;

            public CharSequenceSplitPolicy(final CharSequence chars, final boolean remove)
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
                    throw new IllegalArgumentException("could not split the message");
                }
                else
                {
                    currentEndIndex += this.chars.length();
                    return currentEndIndex;
                }
            }

        }

        public static final SplitPolicy NEWLINE = new CharSequenceSplitPolicy("\n", true);
        public static final SplitPolicy SPACE = new CharSequenceSplitPolicy(" ", true);

        public static final SplitPolicy ANYWHERE = new SplitPolicy()
        {
            @Override
            public int nextMessage(final int currentBeginIndex, final MessageBuilder builder)
            {
                final int currentEndIndex = Math.min(currentBeginIndex + 2000, builder.getStringBuilder().length());
                if (currentEndIndex < 0)
                {
                    throw new IllegalArgumentException("could not split the message");
                }
                return currentEndIndex;
            }
        };

        public SplitPolicy() {}

        public abstract int nextMessage(int currentBeginIndex, MessageBuilder builder);
    }

    /**
     * Holds the available mention types used in {@link MessageBuilder#stripMentions(JDA, MentionType...)}
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
     * Holds the available formatting used in {@link MessageBuilder#appendString(String, net.dv8tion.jda.core.MessageBuilder.Formatting...)}
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
