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
package net.dv8tion.jda;

/**
 * Represents the online presence of a {@link net.dv8tion.jda.entities.User User}.
 */
public enum OnlineStatus
{
    ONLINE("online"),
    AWAY("idle"),
    OFFLINE("offline"),
    UNKNOWN("");

    private final String key;

    OnlineStatus(String key)
    {
        this.key = key;
    }

    /**
     * Will get the {@link net.dv8tion.jda.OnlineStatus OnlineStatus} from the provided key.<br>
     * If the provided key does no match a presence, this will return {@link net.dv8tion.jda.OnlineStatus#UNKNOWN UNKONWN}
     *
     * @param key
     *          The key relating to the {@link net.dv8tion.jda.OnlineStatus OnlineStatus} we wish to retrieve.
     * @return
     *      The matching {@link net.dv8tion.jda.OnlineStatus OnlineStatus}. If there is no match, returns {@link net.dv8tion.jda.OnlineStatus#UNKNOWN UNKONWN}.
     */
    public static OnlineStatus fromKey(String key)
    {
        for (OnlineStatus onlineStatus : values())
        {
            if (onlineStatus.key.equalsIgnoreCase(key))
            {
                return onlineStatus;
            }
        }
        return UNKNOWN;
    }
}
