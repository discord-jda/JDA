/**
 * Created by Michael Ritter on 17.12.2015.
 */
package net.dv8tion.jda;

import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.MessageImpl;

import java.util.LinkedList;
import java.util.List;

public class MessageBuilder
{
    private final StringBuilder builder = new StringBuilder();
    private final List<User> mentioned = new LinkedList<>();

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
     * @param text       the text to append
     * @param formatting the format to apply to the text
     * @return this instance
     */
    public MessageBuilder appendString(String text, Formatting formatting)
    {
        builder.append(formatting.getOpenTag()).append(text).append(formatting.getCloseTag());
        return this;
    }

    /**
     * Appends a code-block to the Message
     *
     * @param text     the code to append
     * @param language the language of the code. If unknown use an empty string or use the {@link Formatting#BLOCK BLOCK Formatting}
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
        builder.append('@').append(user.getUsername());
        mentioned.add(user);
        return this;
    }

    /**
     * Creates a {@link net.dv8tion.jda.entities.Message Message} object from this Builder
     *
     * @return the created {@link net.dv8tion.jda.entities.Message Message}
     */
    public Message build()
    {
        return new MessageImpl("", null).setContent(builder.toString()).setMentionedUsers(mentioned);
    }

    /**
     * Holds the Available formatting used in {@link #appendString(String, Formatting)}
     */
    public enum Formatting
    {
        ITALICS("*"),
        BOLD("**"),
        BOLD_ITALICS("***"),
        STRIKETHROUGH("~~"),
        UNDERLINE("__"),
        UNDERLINE_ITALICS("__*", "*__"),
        UNDERLINE_BOLD("__**", "**__"),
        UNDERLINE_BOLD_ITALICS("__***", "***__"),
        BLOCK("```\n", "```");

        private final String open, close;

        Formatting(String open)
        {
            this(open, open);
        }

        Formatting(String open, String close)
        {
            this.open = open;
            this.close = close;
        }

        private String getOpenTag()
        {
            return open;
        }

        private String getCloseTag()
        {
            return close;
        }

    }
}
