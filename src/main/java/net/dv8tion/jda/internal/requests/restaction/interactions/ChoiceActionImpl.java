package net.dv8tion.jda.internal.requests.restaction.interactions;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.interactions.ChoiceAction;
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;
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
    private final OptionType type;

    public ChoiceActionImpl(InteractionHookImpl hook, OptionType optionType)
    {
        super(hook);
        this.choices = new LinkedHashMap<>();
        this.type = optionType;
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

        json.put("type", InteractionCallbackAction.ResponseType.COMMAND_AUTOCOMPLETE_RESULT.getRaw());
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
        if (type != OptionType.STRING)
            throw new IllegalArgumentException("Cannot add string choice for OptionType." + type);
        choices.put(name, value);
        return this;
    }

    @Nonnull
    public ChoiceAction respondChoice(@Nonnull String name, double value)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, OptionData.MAX_CHOICE_NAME_LENGTH, "Name");
        Checks.check(value >= OptionData.MIN_NEGATIVE_NUMBER, "Double value may not be lower than %f", OptionData.MIN_NEGATIVE_NUMBER);
        Checks.check(value <= OptionData.MAX_POSITIVE_NUMBER, "Double value may not be larger than %f", OptionData.MAX_POSITIVE_NUMBER);
        Checks.check(choices.size() < OptionData.MAX_CHOICES, "Cannot have more than 25 choices for an option!");
        if (type != OptionType.NUMBER)
            throw new IllegalArgumentException("Cannot add double choice for OptionType." + type);
        choices.put(name, value);
        return this;
    }

    @Nonnull
    public ChoiceAction respondChoice(@Nonnull String name, long value)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, OptionData.MAX_CHOICE_NAME_LENGTH, "Name");
        Checks.check(value >= OptionData.MIN_NEGATIVE_NUMBER, "Long value may not be lower than %f", OptionData.MIN_NEGATIVE_NUMBER);
        Checks.check(value <= OptionData.MAX_POSITIVE_NUMBER, "Long value may not be larger than %f", OptionData.MAX_POSITIVE_NUMBER);
        Checks.check(choices.size() < OptionData.MAX_CHOICES, "Cannot have more than 25 choices for an option!");
        if (type != OptionType.INTEGER)
            throw new IllegalArgumentException("Cannot add long choice for OptionType." + type);
        choices.put(name, value);
        return this;
    }

    @NotNull
    @Override
    public ChoiceAction respondChoices(@Nonnull Command.Choice... choices)
    {
        Checks.noneNull(choices, "Choices");
        Checks.check(choices.length + this.choices.size() <= OptionData.MAX_CHOICES, "Cannot have more than 25 choices for an option!");

        for (Command.Choice choice : choices) {
            if (type == OptionType.INTEGER)
                respondChoice(choice.getName(), choice.getAsLong());
            else if (type == OptionType.STRING)
                respondChoice(choice.getName(), choice.getAsString());
            else if (type == OptionType.NUMBER)
                respondChoice(choice.getName(), choice.getAsDouble());
            else
                throw new IllegalArgumentException("Cannot add choice for type " + type);
        }
        return this;
    }
}
