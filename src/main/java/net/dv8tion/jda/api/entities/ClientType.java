/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.entities;

/**
 * The type of client a user might be active on.
 *
 * @see net.dv8tion.jda.api.entities.Member#getOnlineStatus(ClientType) Member.getOnlineStatus(type)
 */
public enum ClientType
{
    /** The official discord desktop client */
    DESKTOP("desktop"),
    /** The official discord mobile app */
    MOBILE("mobile"),
    /** Discord from the browser (or bot) */
    WEB("web"),
    /** Placeholder for a new type that is not yet supported here */
    UNKNOWN("unknown"),
    ;

    private final String key;

    ClientType(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    public static ClientType fromKey(String key)
    {
        for (ClientType type : values())
        {
            if (type.key.equals(key))
                return type;
        }
        return UNKNOWN;
    }
}
