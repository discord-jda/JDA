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

package net.dv8tion.jda.core.utils.cache;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    public long size()
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
        Checks.notEmpty(name, "Name");
        if (elements.isEmpty())
            return Collections.emptyList();
        Iterator<T> iter = iterator();
        T elem = iter.next();
        Method method = getNameGetter(elem);
        if (method == null) // no getName method available
            throw new UnsupportedOperationException("The contained elements are not assigned with names.");

        List<T> list = new LinkedList<>();
        do
        {
            String elementName = getName(elem, method);
            if (elementName != null)
            {
                if (ignoreCase)
                {
                    if (elementName.equalsIgnoreCase(name))
                        list.add(elem);
                }
                else
                {
                    if (elementName.equals(name))
                        list.add(elem);
                }
            }

            if (!iter.hasNext())
                break;
            elem = iter.next();
        }
        while (elem != null);

        return list;
    }

    @Override
    public T getElementById(long id)
    {
        return elements.get(id);
    }

    protected Method getNameGetter(T element)
    {
        try
        {
            return element.getClass().getMethod("getName");
        }
        catch (Exception e)
        {
            return null;
        }
    }

    protected String getName(T element, Method method)
    {
        try
        {
            return (String) method.invoke(element);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            return null;
        }
    }
}
