/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.managers.fields;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class Field<T, M>
{
    private final M manager;
    private final Supplier<T> originalValue;
    private T value;
    private boolean set;


    public Field(M manager, Supplier<T> originalValue)
    {
        this.manager = manager;
        this.originalValue = originalValue;
        this.value = null;
        this.set = false;
    }

    public T getValue()
    {
        return value;
    }

    public T getOriginalValue()
    {
        return originalValue.get();
    }

    public M setValue(T value)
    {
        checkValue(value);

        this.value = value;
        this.set = true;

        return manager;
    }

    public boolean isSet()
    {
        return set;
    }

    public boolean shouldUpdate()
    {
        return isSet() && !equals(getOriginalValue());
    }

    public M getManager()
    {
        return manager;
    }

    public M reset()
    {
        this.value = null;
        this.set = false;

        return manager;
    }

    //Hook method for custom value verification.
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

    protected static void checkNull(Object obj, String name)
    {
        if (obj == null)
            throw new NullPointerException("Provided " + name + " was null!");
    }
}
