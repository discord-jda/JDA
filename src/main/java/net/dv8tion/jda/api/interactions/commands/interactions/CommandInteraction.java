package net.dv8tion.jda.api.interactions.commands.interactions;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.Interaction;

import javax.annotation.Nonnull;

public interface CommandInteraction extends Interaction
{
    /**
     * The command name.
     * <br>This can be useful for abstractions.
     *
     * <p>Note that commands can have these following structures:
     * <ul>
     *     <li>{@code /name subcommandGroup subcommandName}</li>
     *     <li>{@code /name subcommandName}</li>
     *     <li>{@code /name}</li>
     * </ul>
     *
     * You can use {@link #getCommandPath()} to simplify your checks.
     *
     * @return The command name
     */
    @Nonnull
    String getName();

    /**
     * Returns the {@link #getName()} of this command
     *
     * <p>Example: {@code /ban -> "ban"}
     *
     * @return The command path
     */
    @Nonnull
    default String getCommandPath()
    {
        return getName();
    }

    @Nonnull
    @Override
    MessageChannel getChannel();

    /**
     * The command id
     *
     * @return The command id
     */
    long getCommandIdLong();

    /**
     * The command id
     *
     * @return The command id
     */
    @Nonnull
    default String getCommandId()
    {
        return Long.toUnsignedString(getCommandIdLong());
    }
}
