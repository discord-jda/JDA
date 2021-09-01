package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;

public interface UserCommandInteraction extends CommandInteraction
{
    /**
     * The id of the user who undergoes this interaction.
     *
     * @return The id
     */
    long getInteractedIdLong();

    /**
     * The id of the user who undergoes this interaction.
     *
     * @return The id
     */
    @Nonnull
    default String getInteractedId() {
        return Long.toUnsignedString(getInteractedIdLong());
    }

    /**
     * The {@link User} who undergoes this interaction.
     *
     * @return The {@link User}
     */
    @Nonnull
    User getInteractedUser();

    /**
     * The {@link Member} who undergoes this interaction.
     * <br>This is null if the interaction is not from a guild.
     *
     * @return The {@link Member}
     */
    Member getInteractedMember();
}
