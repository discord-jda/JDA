package net.dv8tion.jda.api.interactions.commands.build;

import javax.annotation.Nonnull;
import net.dv8tion.jda.api.interactions.commands.build.slash.SlashCommandData;

/**
 * The base of a command.
 *
 * @see SlashCommandData
 * @see UserCommandData
 * @see MessageCommandData
 */
public interface CommandDataBase<T extends CommandData>
{

    /**
     * Whether this command is available to everyone by default.
     * <br>If this is disabled, you need to explicitly whitelist users and roles per guild.
     *
     * @param  enabled
     *         True, if this command is enabled by default for everyone. (Default: true)
     *
     * @return The CommandData instance, for chaining
     */
    @Nonnull
    T setDefaultEnabled(boolean enabled);


    /**
     * Configure the name
     *
     * @param  name
     *         The lowercase alphanumeric (with dash) name, 1-32 characters
     *
     * @throws IllegalArgumentException
     *         If the name is null, not alphanumeric, or not between 1-32 characters
     *
     * @return The builder, for chaining
     */
    @Nonnull
    T setName(@Nonnull String name);

}
