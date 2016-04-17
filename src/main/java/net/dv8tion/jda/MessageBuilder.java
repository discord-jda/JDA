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
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.MessageImpl;

import java.util.LinkedList;
import java.util.List;

public class MessageBuilder
{
    private final StringBuilder builder = new StringBuilder();
    private final List<User> mentioned = new LinkedList<>();
    private final List<TextChannel> mentionedTextChannels = new LinkedList<>();
    private boolean mentionEveryone = false;
    private boolean isTTS = false;

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
                .setMentionedChannels(mentionedTextChannels).setMentionsEveryone(mentionEveryone);
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
