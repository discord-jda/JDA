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
