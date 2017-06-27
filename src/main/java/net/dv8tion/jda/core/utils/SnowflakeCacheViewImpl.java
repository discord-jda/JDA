/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.utils;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.core.entities.ISnowflake;
import org.apache.http.util.Args;

import java.util.*;

public class SnowflakeCacheViewImpl<T extends ISnowflake> implements SnowflakeCacheView<T>
{
    protected final TLongObjectMap<T> elements = MiscUtil.newLongMap();

    public void clear()
    {
        elements.clear();
    }

    public TLongObjectMap<T> getMap()
    {
        return elements;
    }

    @Override
    public List<T> asList()
    {
        return Collections.unmodifiableList(new ArrayList<>(elements.valueCollection()));
    }

    @Override
    public Set<T> asSet()
    {
        return Collections.unmodifiableSet(new HashSet<>(elements.valueCollection()));
    }

    @Override
    public int size()
    {
        return elements.size();
    }

    @Override
    public boolean isEmpty()
    {
        return elements.isEmpty();
    }

    @Override
    public List<T> getElementsByName(String name, boolean ignoreCase)
    {
        Args.notEmpty(name, "Name");
        List<T> list = new LinkedList<>();
        for (T e : elements.valueCollection())
        {
            String elementName = getName(e);
            if (elementName == null) // no getName method available
                throw new UnsupportedOperationException("The contained elements are not assigned with names.");
            if (ignoreCase)
            {
                if (elementName.equalsIgnoreCase(name))
                    list.add(e);
            }
            else
            {
                if (elementName.equals(name))
                    list.add(e);
            }
        }

        return list;
    }

    @Override
    public T getElementById(long id)
    {
        return elements.get(id);
    }

    protected String getName(T element)
    {
        try
        {
            return (String) element.getClass().getMethod("getName").invoke(element);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
