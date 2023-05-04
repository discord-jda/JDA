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

package net.dv8tion.jda.api.events.self;

import net.dv8tion.jda.api.JDA;

import javax.annotation.Nullable;

public class SelfUpdateGlobalNameEvent extends GenericSelfUpdateEvent<String>
{
    public static final String IDENTIFIER = "global_name";

    public SelfUpdateGlobalNameEvent(JDA api, long responseNumber, String oldName)
    {
        super(api, responseNumber, oldName, api.getSelfUser().getGlobalName(), IDENTIFIER);
    }

    /**
     * The old global name
     *
     * @return The old global name
     */
    @Nullable
    public String getOldGlobalName()
    {
        return getOldValue();
    }

    /**
     * The new global name
     *
     * @return The new global name
     */
    @Nullable
    public String getNewGlobalName()
    {
        return getNewValue();
    }

    @Override
    public String toString()
    {
        return "SelfUpdateGlobalName(" + getOldValue() + "->" + getNewValue() + ')';
    }
}
