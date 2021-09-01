package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.Message;

import javax.annotation.Nonnull;

public interface MessageCommandInteraction extends CommandInteraction
{

    /**
     * The id of the message clicked on.
     *
     * @return The id
     */
    long getInteractedIdLong();

    /**
     * The id of the message clicked on.
     *
     * @return The id
     */
    @Nonnull
    default String getInteractedId() {
        return Long.toUnsignedString(getInteractedIdLong());
    }

    /**
     * The {@link Message} clicked on.
     *
     * @return The {@link Message} clicked on
     */
    @Nonnull
    Message getInteractedMessage();

    @Nonnull
    @Override
    AbstractChannel getChannel();
}
