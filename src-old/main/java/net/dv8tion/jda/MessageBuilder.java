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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda;

import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.MessageImpl;

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
    protected final StringBuilder builder = new StringBuilder();
    protected final List<User> mentioned = new LinkedList<>();
    protected final List<TextChannel> mentionedTextChannels = new LinkedList<>();
    protected final List<Role> mentionedRoles = new LinkedList<>();
    protected boolean mentionEveryone = false;
    protected boolean isTTS = false;
    protected Pattern formatPattern;

    public MessageBuilder()
    {
        formatPattern = Pattern.compile(String.format("%s|%s|%s|%s|%s",
                USER_KEY, ROLE_KEY, TEXTCHANNEL_KEY, EVERYONE_KEY, HERE_KEY));
    }

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
     * Appends a string to the Message
     *
     * @param text the text to append
     * @return this instance
     */
    public MessageBuilder appendString(String text)
    {
        builder.append(text);
        return this;
    }

    /**
     * Appends a formatted string to the Message
     *
     * @param text   the text to append
     * @param format the format(s) to apply to the text
     * @return this instance
     */
    public MessageBuilder appendString(String text, Formatting... format)
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
     * the token replacement functionality that String.format(String, Object...) supports, but it also supports
     * specialized token replacement specific to JDA objects.
     * <p>
     * Current tokens:
     * <ul>
     *     <li><b>%U%</b> - Used to mention a {@link net.dv8tion.jda.entities.User User}.
     *          Same as {@link #appendMention(net.dv8tion.jda.entities.User)}</li>
     *     <li><b>%R%</b> - Used to mention a {@link net.dv8tion.jda.entities.Role Role}.
     *          Same as {@link #appendMention(net.dv8tion.jda.entities.Role)}</li>
     *     <li><b>%TC%</b> - Used to mention a {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     *          Same as {@link #appendMention(net.dv8tion.jda.entities.TextChannel)}</li>
     *     <li><b>%E%</b> - Used to mention @everyone. Same as {@link #appendEveryoneMention()}</li>
     *     <li><b>%H%</b> - Used to mention @here. Same as {@link #appendHereMention()}</li>
     * </ul>
     * <p>
     * Example: <br>
     * If you placed the following code in an method handling a
     * {@link net.dv8tion.jda.events.message.MessageReceivedEvent MessageReceivedEvent}<br>
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
        Matcher m = formatPattern.matcher(format);
        List<Class> classes = Arrays.asList(User.class, TextChannel.class, Role.class);
        while (m.find() && stringIndex < format.length())
        {
            Class target = null;
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
                            mentioned.add(u);
                        }
                        else if (arg instanceof TextChannel)
                        {
                            TextChannel tc = (TextChannel) arg;
                            args[i] = tc.getAsMention();
                            mentionedTextChannels.add(tc);
                        }
                        else if (arg instanceof Role)
                        {
                            Role r = (Role) arg;
                            args[i] = r.getAsMention();
                            mentionedRoles.add(r);
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
                    mentionEveryone = true;
                }
                else
                {
                    sb.append("@here");
                    mentionEveryone = true;
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
    public MessageBuilder appendCodeBlock(String text, String language)
    {
        builder.append("```").append(language).append('\n').append(text).append("\n```");
        return this;
    }

    /**
     * Appends a mention to the Message
     *
     * @param user the user to mention
     * @return this instance
     */
    public MessageBuilder appendMention(User user)
    {
        builder.append("<@").append(user.getId()).append('>');
        mentioned.add(user);
        return this;
    }

    /**
     * Appends a @everyone mention to the Message
     *
     * @return this instance
     */
    public MessageBuilder appendEveryoneMention()
    {
        builder.append("@everyone");
        mentionEveryone = true;
        return this;
    }
    
    /**
     * Appends a @here mention to the Message
     *
     * @return this instance
     */
    public MessageBuilder appendHereMention()
    {
        builder.append("@here");
        mentionEveryone = true;
        return this;
    }

    /**
     * Appends a channel mention to the Message.
     * For this to work, the given TextChannel has to be from the Guild the mention is posted to.
     *
     * @param channel the TextChannel to mention
     * @return this instance
     */
    public MessageBuilder appendMention(TextChannel channel)
    {
        builder.append("<#").append(channel.getId()).append('>');
        mentionedTextChannels.add(channel);
        return this;
    }

    /**
     * Appends a Role mention to the Message.
     * For this to work, the given Role has to be from the Guild the mention is posted to.
     *
     * @param role the Role to mention
     * @return this instance
     */
    public MessageBuilder appendMention(Role role)
    {
        builder.append("<@&").append(role.getId()).append('>');
        mentionedRoles.add(role);
        return this;
    }

    /**
     * Returns the current length of the content that will be built into a {@link net.dv8tion.jda.entities.Message Message}
     * when {@link #build()} is called.<br>
     * If this value is <code>0</code> or greater than <code>2000</code> when {@link #build()} is called, an exception
     * will be raised.
     *
     * @return
     *      The currently length of the content that will be built into a Message.
     */
    public int getLength()
    {
        return builder.length();
    }

    /**
     * Creates a {@link net.dv8tion.jda.entities.Message Message} object from this Builder
     *
     * @return the created {@link net.dv8tion.jda.entities.Message Message}
     *
     * @throws java.lang.UnsupportedOperationException
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

        return new MessageImpl("", null).setContent(message).setTTS(isTTS).setMentionedUsers(mentioned)
                .setMentionedChannels(mentionedTextChannels).setMentionedRoles(mentionedRoles).setMentionsEveryone(mentionEveryone);
    }

    /**
     * Holds the Available formatting used in {@link #appendString(String, Formatting...)}
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
