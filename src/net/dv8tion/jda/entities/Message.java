/**
 * Created by Michael Ritter on 15.12.2015.
 */
package net.dv8tion.jda.entities;

import java.time.OffsetDateTime;
import java.util.List;

public interface Message
{
    /**
     * The Id of this Message
     *
     * @return String Id
     */
    String getId();

    /**
     * A immutable list of all mentioned users. if noone was mentioned, this list is empty
     *
     * @return list of mentioned users
     */
    List<User> getMentionedUsers();

    /**
     * Is this Message mentioning everyone using @everyone?
     *
     * @return if mentioning everyone
     */
    boolean mentionsEveryone();

    /**
     * Gives a copy of the Object holding the time this message was originally sent
     *
     * @return time of Message
     */
    OffsetDateTime getTime();

    /**
     * Returns true, if this Message was edited as least once
     *
     * @return if edited
     */
    boolean isEdited();

    /**
     * Gives a copy of the Object holding the time this message was last recently updated
     * If this message was never updated ({@link #isEdited()} returns false), this will be NULL
     *
     * @return time of most recent update
     */
    OffsetDateTime getEditedTimestamp();

    /**
     * The author of this Message
     *
     * @return Message author
     */
    User getAuthor();

    /**
     * The purely textual content of this message.
     *
     * @return message-text
     */
    String getContent();

    /**
     * The channel this message was sent in
     *
     * @return the messages channel
     */
    TextChannel getChannel();

    /**
     * Is this Message supposed to be TTS (Text-to-speach)
     *
     * @return if message is tts
     */
    boolean isTTS();

    /**
     * Replies to this message with a blank String.
     * This will not mention anyone
     *
     * @param text the content of the new message
     */
    void reply(String text);
}
