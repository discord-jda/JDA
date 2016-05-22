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
 *
 */
package net.dv8tion.jda.entities;

/**
 * Represents a Discord {@link net.dv8tion.jda.entities.Game Game}. This should contain all information provided from Discord about a Game.
 */
public interface Game
{

    /**
     * The displayed name of the {@link net.dv8tion.jda.entities.Game Game}. If no name has been set, this returns null.
     *
     * @return
     *      Possibly-null String containing the Game's name.
     */
    String getName();

    /**
     * The URL of the {@link net.dv8tion.jda.entities.Game Game}. This will return null for regular games.
     *
     * @return
     *      Possibly-null String containing the Game's URL.
     */
    String getUrl();

    /**
     * The type of {@link net.dv8tion.jda.entities.Game Game}. This will return null for regular games.
     *
     * @return
     *      Possibly-null int representing the type of Game
     */
    GameType getType();


    /**
     * Checks if a given String is a valid Twitch url (ie, one that will display "Streaming" on the Discord client.
     */
    static boolean isValidStreamingUrl(String url)
    {
        return url != null && url.matches("^https?:\\/\\/(www\\.)?twitch\\.tv\\/.+");
    }

    enum GameType
    {
        DEFAULT(0),
        TWITCH(1);

        private final int key;

        GameType(int key)
        {
            this.key = key;
        }

        public int getKey()
        {
            return key;
        }

        public static GameType fromKey(int key)
        {
            for (GameType level : GameType.values())
            {
                if(level.getKey() == key)
                    return level;
            }
            return DEFAULT;
        }
    }
}
