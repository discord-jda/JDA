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
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.utils.Checks;

import java.util.Objects;

/**
 * Represents a Discord {@link net.dv8tion.jda.core.entities.Game Game}.
 * <br>This should contain all information provided from Discord about a Game.
 *
 * @since  2.1
 * @author John A. Grosh
 */
public class Game
{
    protected final String name;
    protected final String url;
    protected final Game.GameType type;

    protected Game(String name)
    {
        this(name, null, GameType.DEFAULT);
    }

    protected Game(String name, String url)
    {
        this(name, url, GameType.STREAMING);
    }

    protected Game(String name, String url, GameType type)
    {
        this.name = name;
        this.url = url;
        this.type = type;
    }

    /**
     * The displayed name of the {@link net.dv8tion.jda.core.entities.Game Game}. If no name has been set, this returns null.
     *
     * @return Possibly-null String containing the Game's name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * The URL of the {@link net.dv8tion.jda.core.entities.Game Game} if the game is actually a Stream.
     * <br>This will return null for regular games.
     *
     * @return Possibly-null String containing the Game's URL.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * The type of {@link net.dv8tion.jda.core.entities.Game Game}.
     *
     * @return Never-null {@link net.dv8tion.jda.core.entities.Game.GameType GameType} representing the type of Game
     */
    public GameType getType()
    {
        return type;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Game))
            return false;
        if (o == this)
            return true;

        Game oGame = (Game) o;
        return oGame.getType() == type
            && Objects.equals(name, oGame.getName())
            && Objects.equals(url, oGame.getUrl());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, type, url);
    }

    @Override
    public String toString()
    {
        if (url != null)
            return String.format("Game(%s | %s)", name, url);
        else
            return String.format("Game(%s)", name);
    }

    /**
     * Creates a new Game instance with the specified name.
     *
     * @param  name
     *         The not-null name of the newly created game
     *
     * @throws IllegalArgumentException
     *         if the specified name is null or empty
     *
     * @return A valid Game instance with the provided name with {@link GameType#DEFAULT}
     */
    public static Game of(String name)
    {
        return of(name, null);
    }

    /**
     * Creates a new Game instance with the specified name and url.
     *
     * @param  name
     *         The not-null name of the newly created game
     * @param  url
     *         The streaming url to use, invalid for {@link GameType#DEFAULT GameType#DEFAULT}
     *
     * @throws IllegalArgumentException
     *         if the specified name is null or empty
     *
     * @return A valid Game instance with the provided name and url
     *
     * @see    #isValidStreamingUrl(String)
     */
    public static Game of(String name, String url)
    {
        Checks.notEmpty(name, "Provided game name");
        GameType type;
        if (isValidStreamingUrl(url))
            type = GameType.STREAMING;
        else
            type = GameType.DEFAULT;
        return new Game(name, url, type);
    }

    /**
     * Checks if a given String is a valid Twitch url (ie, one that will display "Streaming" on the Discord client).
     *
     * @param  url
     *         The url to check.
     *
     * @return True if the provided url is valid for triggering Discord's streaming status
     */
    public static boolean isValidStreamingUrl(String url)
    {
        return url != null && url.matches("https?://(www\\.)?twitch\\.tv/.+");
    }

    /**
     * The type game being played, differentiating between a game and stream types.
     */
    public enum GameType
    {
        /**
         * The GameType used to represent a normal {@link net.dv8tion.jda.core.entities.Game Game} status.
         */
        DEFAULT(0),
        /**
         * Used to indicate that the {@link net.dv8tion.jda.core.entities.Game Game} is a stream, specifically for
         * <br>This type is displayed as "Streaming" in the discord client.
         */
        STREAMING(1),
        /**
         * Used to indicate that the {@link net.dv8tion.jda.core.entities.Game Game} is a stream, specifically for
         * <a href="https://www.twitch.tv">https://www.twitch.tv</a>.
         * <br>This type is displayed as "Streaming" in the discord client.
         *
         * @deprecated This might be removed when discord introduces more streaming methods. Use {@link #STREAMING} instead.
         */
        @Deprecated
        TWITCH(1);

        private final int key;

        GameType(int key)
        {
            this.key = key;
        }

        /**
         * The Discord defined id key for this GameType.
         *
         * @return the id key.
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Gets the GameType related to the provided key.
         * <br>If an unknown key is provided, this returns {@link #DEFAULT}
         *
         * @param  key
         *         The Discord key referencing a GameType.
         *
         * @return The GameType that has the key provided, or {@link #DEFAULT} for unknown key.
         */
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
