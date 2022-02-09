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

import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.util.List;

public interface ModalInteraction extends IReplyCallback
{
    /**
     * Returns the custom id of the Modal in question
     * @return Custom id
     */
    @NotNull
    String getModalId();

    /**
     * Returns a List of {@link ActionRow ActionRows} the modal in question contains
     *
     * Contains information like the text the user entered on {@link TextInput TextInputs}
     *
     * @return List of {@link ActionRow ActionRows}
     */
    @NotNull
    List<ActionRow> getComponents();

    /**
     * Convenience method to get a {@link TextInput TextInput} by its id from the List of components.
     *
     * Returns null if no TextInput with that id has been found
     *
     * @param id The custom id
     *
     * @return TextInput with this id, or null
     */
    @Nullable
    default TextInput getInputField(String id)
    {
        return getComponents().stream()
                .map(ActionRow::getComponents)
                .filter(TextInput.class::isInstance)
                .map(TextInput.class::cast)
                .filter(textInput -> textInput.getId().equals(id))
                .findFirst().orElse(null);
    }

    @NotNull
    InteractionHook getHook();

    @NotNull
    @CheckReturnValue
    default ReplyCallbackAction deferReply()
    {
        return deferReply(false);
    }

    @NotNull
    @CheckReturnValue
    ReplyCallbackAction deferReply(boolean ephemeral);

    @NotNull
    @CheckReturnValue
    ReplyCallbackAction reply(String content);
}
