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

package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.callbacks.IAutoCompleteCallback;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.annotation.Nonnull;

/**
 * Interaction for auto-complete options in slash-commands.
 * <br>These interactions may provide incomplete lists of options with invalid values as they represent partial command
 * executions. Some required options may be missing.
 * All the provided options can be used as "context" for the focused option, but they might not be valid.
 *
 * <p>This is used to suggest up to 25 choices for the focused option.
 *
 * @see #getFocusedOption()
 * @see OptionData#setAutoComplete(boolean)
 */
public interface CommandAutoCompleteInteraction extends IAutoCompleteCallback, CommandInteractionPayload
{
    /**
     * The focused option which the user is typing.
     *
     * <p>This is not validated by the Discord API and may contain invalid/incomplete inputs.
     *
     * @return The focused {@link AutoCompleteQuery}
     */
    @Nonnull
    AutoCompleteQuery getFocusedOption();
}
