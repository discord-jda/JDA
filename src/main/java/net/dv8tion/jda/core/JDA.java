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
import org.apache.http.HttpHost;

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

    /**
     * The login token that is currently being used for Discord authentication.
     *
     * @return
     *      Never-null, 18 character length string containing the auth token.
     */
    String getAuthToken();


    /**
     * The proxy settings used by all JDA instances.
     *
     * @return
     *      The proxy settings used by all JDA instances. If JDA currently isn't using a proxy, {@link java.net.Proxy#NO_PROXY Proxy.NO_PROXY} is returned.
     */
    HttpHost getGlobalProxy();

    /**
     * Sets whether or not JDA should try to reconnect, if a connection-error occured.
     * This will use and incremental reconnect (timeouts are increased each time an attempt fails).
     *
     * Default is true.
     *
     * @param reconnect
     *      If true - enables autoReconnect
     */
    void setAutoReconnect(boolean reconnect);

    /**
     * Returns whether or not autoReconnect is enabled for JDA.
     *
     * @return
     *      True if JDA attempts to autoReconnect
     */
    boolean isAutoReconnect();

    /**
     * Used to determine whether the instance of JDA supports audio and has it enabled.
     *
     * @return
     *      True if JDA can currently utilize the audio system.
     */
    boolean isAudioEnabled();

    /**
     * Used to determine if JDA will process MESSAGE_DELETE_BULK messages received from Discord as a single
     * {@link net.dv8tion.jda.events.message.MessageBulkDeleteEvent MessageBulkDeleteEvent} or split
     * the deleted messages up and fire multiple {@link net.dv8tion.jda.events.message.MessageDeleteEvent MessageDeleteEvents},
     * one for each deleted message.
     * <p>
     * By default, JDA will separate the bulk delete event into individual delete events, but this isn't as efficient as
     * handling a single event would be. It is recommended that BulkDelete Splitting be disabled and that the developer
     * should instead handle the {@link net.dv8tion.jda.events.message.MessageBulkDeleteEvent MessageBulkDeleteEvent}
     *
     * @return
     *      Whether or not JDA currently handles the BULK_MESSAGE_DELETE event by splitting it into individual MessageDeleteEvents or not.
     */
    boolean isBulkDeleteSplittingEnabled();

    /**
     * Shuts down JDA, closing all its connections.
     * After this command is issued the JDA Instance can not be used anymore.
     * This will also close the background-thread used for requests (which is required for further api calls of other JDA instances).
     * If this is not desired, use {@link #shutdown(boolean)} instead.
     * To reconnect, just create a new JDA instance.
     */
    void shutdown();

    /**
     * Shuts down JDA, closing all its connections.
     * After this command is issued the JDA Instance can not be used anymore.
     * Depending on the free-parameter, this will also close the background-thread used for requests.
     * If the background-thread is closed, the system can exit properly, but no further JDA requests are possible (includes other JDA instances).
     * If you want to reconnect, and the request-thread was not freed, just create a new JDA instance.
     *
     * @param free
     *          If true, shuts down JDA's rest system permanently.
     */
    void shutdown(boolean free);

    /**
     * Installs an auxiliary cable into your system.
     *
     * @param port the port
     * @throws UnsupportedOperationException
     */
    void installAuxiliaryCable(int port) throws UnsupportedOperationException;

    AccountType getAccountType();

    JDAClient asClient();

    JDABot asBot();
}
