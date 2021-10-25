package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Abstract representation for any kind of Discord interaction.
 * <br>This includes things such as {@link net.dv8tion.jda.api.interactions.commands.CommandInteraction Slash-Commands} or {@link ComponentInteraction Buttons}.
 *
 * <p>To properly handle an interaction you must acknowledge it.
 *
 * <p><b>You can only acknowledge an interaction once!</b> Any additional acknowledgements will result in exceptions.
 * You can use {@link #isAcknowledged()} to check whether the interaction has been acknowledged already.
 */
public interface Interaction extends ISnowflake
{
    /**
     * The raw interaction type.
     * <br>It is recommended to use {@link #getType()} instead.
     *
     * @return The raw interaction type
     */
    int getTypeRaw();

    /**
     * The {@link InteractionType} for this interaction.
     *
     * @return The {@link InteractionType} or {@link InteractionType#UNKNOWN}
     */
    @Nonnull
    default InteractionType getType()
    {
        return InteractionType.fromKey(getTypeRaw());
    }

    /**
     * The interaction token used for responding to an interaction.
     *
     * @return The interaction token
     */
    @Nonnull
    String getToken();

    /**
     * The {@link Guild} this interaction happened in.
     * <br>This is null in direct messages.
     *
     * @return The {@link Guild} or null
     */
    @Nullable
    Guild getGuild();

    /**
     * Whether this interaction came from a {@link Guild}.
     * <br>This is identical to {@code getGuild() != null}
     *
     * @return True, if this interaction happened in a guild
     */
    default boolean isFromGuild()
    {
        return getGuild() != null;
    }

    /**
     * The {@link User} who caused this interaction.
     *
     * @return The {@link User}
     */
    @Nonnull
    User getUser();

    /**
     * The {@link Member} who caused this interaction.
     * <br>This is null if the interaction is not from a guild.
     *
     * @return The {@link Member}
     */
    @Nullable
    Member getMember();

    /**
     * The {@link InteractionHook}.
     *
     * @return The interaction hook
     */
    @Nonnull
    InteractionHook getHook();

    /**
     * Whether this interaction has already been acknowledged.
     * <br> Each interaction can only be acknowledged once.
     *
     * @return True, if this interaction has already been acknowledged
     */
    boolean isAcknowledged();

    /**
     * Returns the {@link net.dv8tion.jda.api.JDA JDA} instance of this interaction
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();
}
