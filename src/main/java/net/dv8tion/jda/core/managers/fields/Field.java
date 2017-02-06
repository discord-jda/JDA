/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.managers.fields;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Abstract Manager-Field (AMF)
 * <br>This is utilized in updatable Managers. Managers that allow
 * users to modify multiple {@code Field}s of an entity.
 *
 * <p>Most managers have specific implementations that provide
 * needed functionality and specify behaviour.
 *
 * <p><b>This class is abstract and requires an implementation
 * for {@link #checkValue(Object)}</b>
 *
 * @param  <T>
 *         The Field-Type for this Field.
 *         This Type represents the value that will be modified by the manager
 * @param  <M>
 *         The Manager-Type for this Field.
 *         This Type represents the Manager that is returned by {@link #setValue(Object)}
 *         for chaining convenience.
 *
 * @since  3.0
 */
public abstract class Field<T, M>
{
    protected final M manager;
    protected final Supplier<T> originalValue;
    protected T value;
    protected boolean set;


    /**
     * Creates a new Field instance
     *
     * @param manager
     *        The updatable manager instance this field
     *        is used in. This will be returned by {@link #setValue(Object)} for chaining convenience
     * @param originalValue
     *        The original value, represented with a {@link java.util.function.Supplier Supplier}
     *        access function.
     */
    public Field(M manager, Supplier<T> originalValue)
    {
        this.manager = manager;
        this.originalValue = originalValue;
        this.value = null;
        this.set = false;
    }

    /**
     * The currently set value, null if no value has been set.
     *
     * @return The current value, null if no value has been set
     *
     * @see    #getOriginalValue()
     */
    public T getValue()
    {
        return value;
    }

    /**
     * Resolves the original value for the underlying entity.
     * This might not work for some field implementations and will throw
     * an {@link java.lang.UnsupportedOperationException UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException
     *         If the original value is not accessible
     *
     * @return The original value for the underlying entity
     *
     * @see    #getValue()
     */
    public T getOriginalValue()
    {
        return originalValue.get();
    }

    /**
     * Sets the value which should be used in the update
     * operation for the Manager instance.
     * <br>This will cause {@link #isSet()} to return {@code true}!
     *
     * <p>Values might be checked differently depending on the Field implementation.
     * <br>The check criteria are specified in the field getter of the updatable manager.
     *
     * @param  value
     *         The value that should be used by the update operation
     *
     * @throws IllegalArgumentException
     *         If the provided value does not pass the specified checks
     *
     * @return The specific manager instance for chaining convenience
     *
     * @see    #isSet()
     */
    public M setValue(T value)
    {
        checkValue(value);

        this.value = value;
        this.set = true;

        return manager;
    }

    /**
     * Whether the value for this Field has been set
     *
     * @return Whether the value for this Field has been set
     */
    public boolean isSet()
    {
        return set;
    }

    /**
     * Whether the field should be update or not
     *
     * @return Whether the field should be update or not
     *         <br>This (in most cases) is {@code true} when the value has been set and is
     *         different from the value returned by the original value {@link java.util.function.Supplier Supplier}
     */
    public boolean shouldUpdate()
    {
        return isSet() && !equals(getOriginalValue());
    }

    /**
     * The Manager of this specific Field instance
     *
     * @return The Manager of this specific Field instance
     */
    public M getManager()
    {
        return manager;
    }

    /**
     * Resets this Field
     * <br>This will cause {@link #isSet()} to return {@code false}
     *
     * @return The specific manager instance for chaining convenience
     *         <br>Similar to {@link #setValue(Object)}
     */
    public M reset()
    {
        this.value = null;
        this.set = false;

        return manager;
    }

    /**
     * Hook method for custom value verification.
     *
     * <p><b>This method is abstract and requires specific implementation</b>
     *
     * @param  value
     *         The value that should be checked
     *
     * @throws IllegalArgumentException
     *         If the specified value does not pass the specified checks
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If this value requires specific {@link net.dv8tion.jda.core.Permission Permissions}
     *         that are not fulfilled
     */
    public abstract void checkValue(T value);

    @Override
    public boolean equals(Object o)
    {
        return isSet() && Objects.equals(o, value);
    }

    @Override
    public String toString()
    {
        throw new UnsupportedOperationException("toString is disabled for Fields due to possible, accidental usage in JSON bodies.");
    }
}
