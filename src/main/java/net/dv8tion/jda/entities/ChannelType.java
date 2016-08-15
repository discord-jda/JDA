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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.entities;

public enum ChannelType
{
    TEXT(0),
    PRIVATE(1),
    VOICE(2),
    GROUP(3),
    UNKNOWN(-1);

    protected final int id;

    ChannelType(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    public static ChannelType fromId(int id)
    {
        for (ChannelType type : values())
        {
            if (type.id == id)
                return type;
        }
        return UNKNOWN;
    }
}
