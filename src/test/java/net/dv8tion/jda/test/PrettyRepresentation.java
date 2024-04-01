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

package net.dv8tion.jda.test;

import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.assertj.core.presentation.StandardRepresentation;

public class PrettyRepresentation extends StandardRepresentation
{
    @Override
    protected String fallbackToStringOf(Object object)
    {
        if (object instanceof DataObject)
            return ((DataObject) object).toPrettyString();
        else if (object instanceof DataArray)
            return ((DataArray) object).toPrettyString();
        return object.toString();
    }
}
