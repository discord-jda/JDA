package net.dv8tion.jda.internal.requests.restaction.interactions;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.interactions.ChoiceAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.InteractionHookImpl;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class ChoiceActionImpl extends InteractionCallbackActionImpl implements ChoiceAction
{
    private final Map<String, Object> choices;

    public ChoiceActionImpl(InteractionHookImpl hook)
    {
        super(hook);
        this.choices = new LinkedHashMap<>();
    }

    protected DataObject toData()
    {
        DataObject json = DataObject.empty();
        DataObject payload = DataObject.empty();
        if (!choices.isEmpty())
            payload.put("choices", DataArray.fromCollection(choices.entrySet()
                    .stream()
                    .map(entry -> DataObject.empty().put("name", entry.getKey()).put("value", entry.getValue()))
                    .collect(Collectors.toList())));
        json.put("data", payload);

        json.put("type", ResponseType.COMMAND_AUTOCOMPLETE_RESULT.getRaw());
        System.out.println(json.toPrettyString());
        return json;
    }

    private boolean isEmpty()
    {
        return choices.isEmpty();
    }

    @Nonnull
    @Override
    public ChoiceAction setCheck(BooleanSupplier checks)
    {
        return (ChoiceAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public ChoiceAction timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (ChoiceAction) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public ChoiceAction deadline(long timestamp)
    {
        return (ChoiceAction) super.deadline(timestamp);
    }

    @Nonnull
    public ChoiceAction respondChoice(@Nonnull String name, @Nonnull String value)
    {
        Checks.notEmpty(name, "Name");
        Checks.notEmpty(value, "Value");
        Checks.notLonger(name, OptionData.MAX_CHOICE_NAME_LENGTH, "Name");
        Checks.notLonger(value, OptionData.MAX_CHOICE_VALUE_LENGTH, "Value");
        Checks.check(choices.size() < OptionData.MAX_CHOICES, "Cannot have more than 25 choices for an option!");
        choices.put(name, value);
        return this;
    }

    @NotNull
    @Override
    public ChoiceAction respondChoices(@Nonnull Command.Choice... choices)
    {
        Checks.noneNull(choices, "Choices");
        Checks.check(choices.length + this.choices.size() <= OptionData.MAX_CHOICES, "Cannot have more than 25 choices for an option!");

        for (Command.Choice choice : choices)
            respondChoice(choice.getName(), choice.getAsString());

        return this;
    }
}
