package net.dv8tion.jda.api.requests.restaction.interactions;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * A {@link InteractionCallbackAction} which can be used to update the choices for an autocomplete interaction
 */
public interface ChoiceAction extends InteractionCallbackAction
{
    @Nonnull
    @Override
    ChoiceAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    ChoiceAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    ChoiceAction deadline(long timestamp);

    /**
     * Add a predefined choice for this option.
     * <br>The user can only provide one of the choices and cannot specify any other value.
     *
     * @param  name
     *         The name used in the client
     * @param  value
     *         The value received in {@link net.dv8tion.jda.api.interactions.commands.OptionMapping OptionMapping}
     *
     * @throws IllegalArgumentException
     *         If any of the following checks fail
     *         <ul>
     *             <li>{@code name} is not null, empty and less or equal to {@value OptionData#MAX_CHOICE_NAME_LENGTH} characters long</li>
     *             <li>{@code value} is not null, empty and less or equal to {@value OptionData#MAX_CHOICE_VALUE_LENGTH} characters long</li>
     *             <li>The amount of already set choices is less than {@link OptionData#MAX_CHOICES}</li>
     *             <li>The {@link OptionType} is {@link OptionType#STRING}</li>
     *             <li>Autocomplete is not enabled</li>
     *         </ul>
     *
     * @return The OptionData instance, for chaining
     */
    @Nonnull
    ChoiceAction respondChoice(@Nonnull String name, @Nonnull String value);

    @Nonnull
    @CheckReturnValue
    ChoiceAction respondChoices(@Nonnull Command.Choice... choices);

    @Nonnull
    @CheckReturnValue
    default ChoiceAction respondChoices(@Nonnull Collection<? extends Command.Choice> choices) {
        Checks.noneNull(choices, "Choices");
        return respondChoices(choices.toArray(new Command.Choice[0]));
    }
}
