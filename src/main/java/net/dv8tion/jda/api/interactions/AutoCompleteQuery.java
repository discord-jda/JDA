/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * The query input for an {@link net.dv8tion.jda.api.interactions.callbacks.IAutoCompleteCallback auto-complete interaction}.
 *
 * <p>The {@link #getValue() value} of such a query may not be a valid instance of the expected {@link #getType() type}.
 * Discord does not do any validation for auto-complete queries. However, you are required to reply with the correct type.
 */
public class AutoCompleteQuery
{
    private final String name;
    private final String value;
    private final OptionType type;

    public AutoCompleteQuery(@Nonnull OptionMapping option)
    {
        this.name = option.getName();
        this.value = option.getAsString();
        this.type = option.getType();
    }

    /**
     * The name of the input field, usually an option name in {@link CommandAutoCompleteInteraction}.
     *
     * @return The option name
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * The query value that the user is currently typing.
     *
     * <p>This is not validated and may not be a valid value for an actual command.
     * For instance, a user may input invalid numbers for {@link OptionType#NUMBER}.
     *
     * @return The current auto-completable query value
     */
    @Nonnull
    public String getValue()
    {
        return value;
    }

    /**
     * The expected option type for this query.
     *
     * @return The option type expected from this auto-complete response
     */
    @Nonnull
    public OptionType getType()
    {
        return type;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, value, type);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof AutoCompleteQuery))
            return false;
        AutoCompleteQuery query = (AutoCompleteQuery) obj;
        return type == query.type && name.equals(query.name) && value.equals(query.value);
    }

    @Override
    public String toString()
    {
        return "AutoCompleteQuery[" + type + "](" + name + "=" + value + ")";
    }
}
