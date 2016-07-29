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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core;

import net.dv8tion.jda.bot.JDABot;
import net.dv8tion.jda.client.JDAClient;
import net.dv8tion.jda.core.exceptions.AccountTypeException;

public interface JDA
{
    /**
     * The current status of the JDA instance.
     */
    enum Status
    {
        /**JDA is currently setting up supporting systems like the AudioSystem.*/
        INITIALIZING,
        /**JDA has finished setting up supporting systems and is ready to log in.*/
        INITIALIZED,
        /**JDA is currently attempting to log in.*/
        LOGGING_IN,
        /**JDA is currently attempting to connect it's websocket to Discord.*/
        CONNECTING_TO_WEBSOCKET,
        /**JDA has successfully connected it's websocket to Discord and is populating internal objects.
         * This process often takes the longest of all Statuses (besides CONNECTED)*/
        LOADING_SUBSYSTEMS,
        /**JDA has finished loading everything, is receiving information from Discord and is firing events.*/
        CONNECTED,
        /**JDA's main websocket has been disconnected. This <b>DOES NOT</b> mean JDA has shutdown permanently.
         * This is an in-between status. Most likely ATTEMPTING_TO_RECONNECT or SHUTTING_DOWN/SHUTDOWN will soon follow.*/
        DISCONNECTED,
        /**When trying to reconnect to Discord JDA encountered an issue, most likely related to a lack of internet connection,
         * and is waiting to try reconnecting again.*/
        WAITING_TO_RECONNECT,
        /**JDA has been disconnected from Discord and is currently trying to reestablish the connection.*/
        ATTEMPTING_TO_RECONNECT,
        /**JDA has received a shutdown request or has been disconnected from Discord and reconnect is disabled, thus,
         * JDA is in the process of shutting down*/
        SHUTTING_DOWN,
        /**JDA has finished shutting down and this instance can no longer be used to communicate with the Discord servers.*/
        SHUTDOWN,
        /**While attempting to authenticate, Discord reported that the provided authentication information was invalid.*/
        FAILED_TO_LOGIN,
    }

    /**
     * Gets the current {@link net.dv8tion.jda.core.JDA.Status Status} of the JDA instance.
     *
     * @return
     *      Current JDA status.
     */
    Status getStatus();

    AccountType getAccountType();

    default JDAClient asClient()
    {
        throw new AccountTypeException(AccountType.CLIENT);
    }

    default JDABot asBot()
    {
        throw new AccountTypeException(AccountType.BOT);
    }
}
