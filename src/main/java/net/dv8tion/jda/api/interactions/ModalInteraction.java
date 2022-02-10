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
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface ModalInteraction extends IReplyCallback
{
    /**
     * Returns the custom id of the Modal in question
     *
     * @return Custom id
     */
    @Nonnull
    String getModalId();

    /**
     * Returns a List of {@link ActionRow ActionRows} the modal in question contains
     *
     * <p>Contains information like the text the user entered on {@link TextInput TextInputs}
     *
     * @return List of {@link ActionRow ActionRows}
     */
    @Nonnull
    List<ActionRow> getComponents();

    /**
     * Convenience method to get a {@link TextInput TextInput} by its id from the List of components.
     *
     * <p>Returns null if no TextInput with that id has been found
     *
     * @param  id
     *         The custom id
     *
     * @return TextInput with this id, or null if not found
     */
    @Nullable
    default TextInput getTextInputField(String id)
    {
        return getComponents().stream()
                .map(ActionRow::getComponents)
                .flatMap(List::stream)
                .filter(TextInput.class::isInstance)
                .map(TextInput.class::cast)
                .filter(textInput -> textInput.getId().equals(id))
                .findFirst().orElse(null);
    }

    @Nonnull
    InteractionHook getHook();

    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction deferReply()
    {
        return deferReply(false);
    }

    @Nonnull
    @CheckReturnValue
    ReplyCallbackAction deferReply(boolean ephemeral);

    @Nonnull
    @CheckReturnValue
    ReplyCallbackAction reply(String content);
}
