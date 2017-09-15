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

import net.dv8tion.jda.core.entities.ISnowflake;

import java.util.function.Function;

public class SnowflakeCacheViewImpl<T extends ISnowflake> extends AbstractCacheView<T> implements SnowflakeCacheView<T>
{
    public SnowflakeCacheViewImpl(Function<T, String> nameMapper)
    {
        super(nameMapper);
    }

    @Override
    public T getElementById(long id)
    {
        return elements.get(id);
    }
}
