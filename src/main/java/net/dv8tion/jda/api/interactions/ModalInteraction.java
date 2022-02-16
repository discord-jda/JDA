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

import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Interaction on a {@link net.dv8tion.jda.api.interactions.components.text.Modal Modal}
 *
 * If the modal of this interaction was a reply to a {@link net.dv8tion.jda.api.interactions.components.ComponentInteraction ComponentInteraction}, you can also use {@link #deferEdit()} to edit the message instead of replying.
 *
 * @see    net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
 */
public interface ModalInteraction extends IReplyCallback, IMessageEditCallback
{
    /**
     * Returns the custom id of the Modal in question
     *
     * @return Custom id
     * 
     * @see net.dv8tion.jda.api.interactions.components.text.Modal.Builder#setId(String) 
     */
    @Nonnull
    String getModalId();

    /**
     * Returns a List of {@link ModalMapping ModalMappings} the modal in question contains
     *
     * @return List of {@link ModalMapping ModalMappings}
     *
     * @see #getValue(String)
     */
    @Nonnull
    List<ModalMapping> getValues();

    /**
     * Convenience method to get a {@link ModalMapping ModalMapping} by its id from the List of {@link ModalMapping ModalMappings}
     *
     * <p>Returns null if no component with that id has been found
     * 
     * <p>You can use the second and third parameter overloads to handle optional arguments gracefully.
     * See {@link #getValue(String, Function)} and {@link #getValue(String, Object, Function)}
     *
     * @param  id
     *         The custom id
     *
     * @throws IllegalArgumentException
     *         If the provided id is null
     *
     * @return ModalMapping with this id, or null if not found
     *
     * @see    #getValues()
     * @see    #getValue(String, Function) 
     * @see    #getValue(String, Object, Function) 
     * @see    #getValue(String, Supplier, Function) 
     */
    @Nullable
    default ModalMapping getValue(@Nonnull String id)
    {
        Checks.notNull(id, "ID");
        return getValues().stream()
                .filter(mapping -> mapping.getId().equals(id))
                .findFirst().orElse(null);
    }

    /**
     * Finds the first value with the specified id.
     * <br>A resolver is used to get the value if it is provided.
     * If no value is provided for the given id, this will simply return null instead.
     * You can use {@link #getValue(String, Object, Function)} to provide a fallback for missing values.
     * 
     * <p><b>Example</b>
     * <br>>ou can understand this as a shortcut for these lines of code:
     * <pre>{@code 
     * ModalMapping mapping = event.getValue("email");
     * String email = mapping == null ? null : mapping.getAsString();
     * }</pre>
     * Which can be written with this resolver as:
     * <pre>{@code
     * String email = event.getValue("email", ModalMapping::getAsString);
     * }</pre>
     * 
     * @param  id
     *         The value id
     * @param  resolver
     *         The mapping resolver function to use if there is a mapping available,
     *         the provided mapping will never be null!
     * @param  <T>
     *         The type of the resolved value
     *         
     * @throws IllegalArgumentException
     *         If the id or resolver is null
     *         
     * @return The resolved value with the provided id, or null if that value is not provided.
     * 
     * @see    #getValue(String, Object, Function) 
     * @see    #getValue(String, Supplier, Function)
     */
    default <T> T getValue(@Nonnull String id,
                           @Nonnull Function<? super ModalMapping, ? extends T> resolver)
    {
        return getValue(id, null, resolver);
    }

    /**
     * Finds the first value with the specified id.
     * <br>A resolver is used to get the value if it is provided.
     * If no value is provided for the given id, this will simply return your provided fallback instead.
     * You can use {@link #getValue(String, Function)} to fall back to {@code null}.
     *
     * <p><b>Example</b>
     * <br>You can understand this as a shortcut for these lines of code:
     * <pre>{@code
     * ModalMapping mapping = event.getValue("email");
     * String email = mapping == null ? "No email provided" : mapping.getAsString();
     * }</pre>
     * Which can be written with this resolver as:
     * <pre>{@code
     * String email = event.getValue("email", "No email provided", ModalMapping::getAsString);
     * }</pre>
     *
     * @param  id
     *         The value id
     * @param  fallback
     *         The fallback to use if the value is not provided, meaning {@link #getValue(String)} returns null
     * @param  resolver
     *         The mapping resolver function to use if there is a mapping available,
     *         the provided mapping will never be null!
     * @param  <T>
     *         The type of the resolved value
     *
     * @throws IllegalArgumentException
     *         If the id or resolver is null
     *
     * @return The resolved option with the provided id, or {@code fallback} if that option is not provided
     *
     * @see    #getValue(String, Function)
     * @see    #getValue(String, Supplier, Function)
     */
    default <T> T getValue(@Nonnull String id,
                           @Nullable T fallback,
                           @Nonnull Function<? super ModalMapping, ? extends T> resolver)
    {
        Checks.notNull(resolver, "Resolver");
        ModalMapping mapping = getValue(id);
        if (mapping != null)
            return resolver.apply(mapping);
        return fallback;
    }

    /**
     * Finds the first value with the specified id.
     * <br>A resolver is used to get the value if it is provided.
     * If no value is provided for the given id, this will simply return your provided fallback instead.
     * You can use {@link #getValue(String, Function)} to fall back to {@code null}.
     *
     * <p><b>Example</b>
     * <br>You can understand this as a shortcut for these lines of code:
     * <pre>{@code
     * ModalMapping mapping = event.getValue("email");
     * String email = mapping == null ? context.getMissingEmailString() : mapping.getAsString();
     * }</pre>
     * Which can be written with this resolver as:
     * <pre>{@code
     * String email = event.getValue("email", context::getMissingEmailString , ModalMapping::getAsString);
     * }</pre>
     *
     * @param  id
     *         The option id
     * @param  fallback
     *         The fallback supplier to use if the value is not provided, meaning {@link #getValue(String)} returns null
     * @param  resolver
     *         The mapping resolver function to use if there is a mapping available,
     *         the provided mapping will never be null!
     * @param  <T>
     *         The type of the resolved value
     *
     * @throws IllegalArgumentException
     *         If the id or resolver is null
     *
     * @return The resolved option with the provided id, or {@code fallback} if that option is not provided
     *
     * @see    #getValue(String, Function)
     * @see    #getValue(String, Object, Function)
     */
    default <T> T getValue(@Nonnull String id,
                           @Nullable Supplier<? extends T> fallback,
                           @Nonnull Function<? super ModalMapping, ? extends T> resolver)
    {
        Checks.notNull(resolver, "Resolver");
        ModalMapping mapping = getValue(id);
        if (mapping != null)
            return resolver.apply(mapping);
        return fallback == null ? null : fallback.get();
    }
}
