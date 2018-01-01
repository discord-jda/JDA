/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
package net.dv8tion.jda.core.events.user;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nullable;

/**
 * <b><u>UserGameUpdateEvent</u></b><br>
 * Fired if the {@link net.dv8tion.jda.core.entities.Game Game} of a {@link net.dv8tion.jda.core.entities.User User} changes.<br>
 * <br>
 * Use: Retrieve the User who's Game changed and their previous Game.
 */
public class UserGameUpdateEvent extends GenericUserPresenceEvent
{
    protected final Game previousGame;

    public UserGameUpdateEvent(JDA api, long responseNumber, User user, @Nullable Guild guild, @Nullable Game previousGame)
    {
        super(api, responseNumber, user, guild);
        this.previousGame = previousGame;
    }

    @Nullable
    public Game getPreviousGame()
    {
        return previousGame;
    }

    @Nullable
    public Game getCurrentGame()
    {
        return isRelationshipUpdate() ? getFriend().getGame() : getMember().getGame();
    }
}
