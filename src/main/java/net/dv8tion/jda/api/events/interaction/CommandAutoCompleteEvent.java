package net.dv8tion.jda.api.events.interaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ChoiceAction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CommandAutoCompleteEvent extends GenericInteractionCreateEvent implements CommandAutoCompleteInteraction
{
    private final CommandAutoCompleteInteraction commandAutoCompleteInteraction;

    public CommandAutoCompleteEvent(@Nonnull JDA api, long responseNumber, @Nonnull CommandAutoCompleteInteraction interaction)
    {
        super(api, responseNumber, interaction);
        this.commandAutoCompleteInteraction = interaction;
    }

    @Nonnull
    @Override
    public MessageChannel getChannel()
    {
        return commandAutoCompleteInteraction.getChannel();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return commandAutoCompleteInteraction.getName();
    }

    @Nullable
    @Override
    public String getSubcommandName()
    {
        return commandAutoCompleteInteraction.getSubcommandName();
    }

    @Nullable
    @Override
    public String getSubcommandGroup()
    {
        return commandAutoCompleteInteraction.getSubcommandGroup();
    }

    @Override
    public long getCommandIdLong()
    {
        return commandAutoCompleteInteraction.getCommandIdLong();
    }

    @Nonnull
    @Override
    public List<OptionMapping> getOptions()
    {
        return commandAutoCompleteInteraction.getOptions();
    }

    @NotNull
    @Override
    public ChoiceAction respondChoice(@NotNull String name, @NotNull String value)
    {
        return commandAutoCompleteInteraction.respondChoice(name, value);
    }

    @NotNull
    @Override
    public ChoiceAction respondChoices(@Nonnull Command.Choice... choices)
    {
        return commandAutoCompleteInteraction.respondChoices(choices);
    }

    @Override
    public OptionMapping getFocusedOption()
    {
        return commandAutoCompleteInteraction.getFocusedOption();
    }

    @Nonnull
    @Override
    public String getCommandString()
    {
        return commandAutoCompleteInteraction.getCommandString();
    }
}
