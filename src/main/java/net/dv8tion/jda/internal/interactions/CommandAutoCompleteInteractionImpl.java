package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.interactions.ChoiceAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.ChoiceActionImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class CommandAutoCompleteInteractionImpl extends CommandInteractionImpl implements CommandAutoCompleteInteraction
{
    public CommandAutoCompleteInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
    }

    @Nonnull
    public ChoiceAction respondChoice(@Nonnull String name, @Nonnull String value)
    {
        return new ChoiceActionImpl(this.hook).respondChoice(name, value);
    }

    @NotNull
    @Override
    public ChoiceAction respondChoices(@Nonnull Command.Choice... choices)
    {
        return new ChoiceActionImpl(this.hook).respondChoices(choices);
    }

    @Override
    public OptionMapping getFocusedOption()
    {
        return getOptions().stream()
                .filter(OptionMapping::isFocused)
                .findFirst()
                .orElseGet(null);
    }
}
