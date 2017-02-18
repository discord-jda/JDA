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

import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;

import java.util.function.Supplier;

/**
 * Specification AMF for
 * an {@link net.dv8tion.jda.core.managers.ChannelManagerUpdatable ChannelManagerUpdatable}
 * field.
 *
 * <p><b>This class is abstract and requires an implementation
 * for {@link #checkValue(Object)}</b>
 *
 * @param  <T>
 *         The Field-Type for this ChannelField
 *
 * @since  3.0
 */
public abstract class ChannelField<T> extends Field<T, ChannelManagerUpdatable>
{
    public ChannelField(ChannelManagerUpdatable manager, Supplier<T> originalValue)
    {
        super(manager, originalValue);
    }
}
