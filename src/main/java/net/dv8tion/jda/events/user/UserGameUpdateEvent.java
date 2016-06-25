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
package net.dv8tion.jda.events.user;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Game;
import net.dv8tion.jda.entities.User;

/**
 * <b><u>UserGameUpdateEvent</u></b><br/>
 * Fired if the {@link net.dv8tion.jda.entities.Game Game} of a {@link net.dv8tion.jda.entities.User User} changes.<br/>
 * <br/>
 * Use: Retrieve the User who's Game changed and their previous Game.
 */
public class UserGameUpdateEvent extends GenericUserEvent
{
    private final Game previousGame;

    public UserGameUpdateEvent(JDA api, int responseNumber, User user, Game previousGame)
    {
        super(api, responseNumber, user);
        this.previousGame = previousGame;
    }

    public Game getPreviousGame()
    {
        return previousGame;
    }
}
