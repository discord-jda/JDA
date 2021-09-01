package net.dv8tion.jda.api.events.interaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a slash/user/message command was used in a {@link MessageChannel}.
 * <br>Every CommandEvent is derived from this event and can be cast.
 *
 * <p>Can be used to detect any CommandEvent.
 *
 * <h2>Requirements</h2>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 */
public abstract class GenericCommandEvent extends GenericInteractionCreateEvent implements CommandInteraction
{
    private final CommandInteraction commandInteraction;

    public GenericCommandEvent(@NotNull JDA api, long responseNumber, @NotNull CommandInteraction interaction)
    {
        super(api, responseNumber, interaction);
        this.commandInteraction = interaction;
    }

    @NotNull
    @Override
    public String getName()
    {
        return commandInteraction.getName();
    }
}
