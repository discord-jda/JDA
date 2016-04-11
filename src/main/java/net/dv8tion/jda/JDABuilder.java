/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.hooks.AnnotatedEventManager;
import net.dv8tion.jda.hooks.IEventManager;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.hooks.SubscribeEvent;

import javax.security.auth.login.LoginException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Used to create a new {@link net.dv8tion.jda.JDA} instance. This is useful for making sure all of
 * your {@link net.dv8tion.jda.hooks.EventListener EventListeners} as registered
 * before {@link net.dv8tion.jda.JDA} attempts to log in.
 * <p>
 * A single JDABuilder can be reused multiple times. Each call to
 * {@link net.dv8tion.jda.JDABuilder#buildAsync() buildAsync()} or
 * {@link net.dv8tion.jda.JDABuilder#buildBlocking() buildBlocking()}
 * creates a new {@link net.dv8tion.jda.JDA} instance using the same information.
 * This means that you can have listeners easily registered to multiple {@link net.dv8tion.jda.JDA} instances.
 */
public class JDABuilder
{
    protected static boolean proxySet = false;
    protected static boolean jdaCreated = false;
    protected static String proxyUrl = null;
    protected static int proxyPort = -1;
    final List<Object> listeners;
    String botToken = null;
    boolean enableVoice = true;
    boolean useAnnotatedManager = false;
    IEventManager eventManager = null;
    boolean reconnect = true;

    /**
     * Creates a completely empty JDABuilder.<br>
     * If you use this, you need to set the bot token using
     * {@link net.dv8tion.jda.JDABuilder#setBotToken(String) setBotToken(String)}
     * before calling {@link net.dv8tion.jda.JDABuilder#buildAsync() buildAsync()}
     * or {@link net.dv8tion.jda.JDABuilder#buildBlocking() buildBlocking()}
     */
    public JDABuilder()
    {
        this(null);
    }

    /**
     * Creates a new JDABuilder using the provided token that is received when creating a Bot-Account.
     *
     * @param botToken
     *          The token of a Bot-Account.
     */
    public JDABuilder(String botToken)
    {
        this.botToken = botToken;
        listeners = new LinkedList<>();
    }

    /**
     * Sets the botToken that will be used by the {@link net.dv8tion.jda.JDA} instance to log in when
     * {@link net.dv8tion.jda.JDABuilder#buildAsync() buildAsync()}
     * or {@link net.dv8tion.jda.JDABuilder#buildBlocking() buildBlocking()}
     * is called.
     *
     * @param botToken
     *          The token of the bot-account that you would like to login with.
     * @return
     *      Returns the {@link net.dv8tion.jda.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public JDABuilder setBotToken(String botToken) {
        this.botToken = botToken;
        return this;
    }

    /**
     * Sets the proxy that will be used by <b>ALL</b> JDA instances.<br>
     * Once this is set <b>IT CANNOT BE CHANGED.</b><br>
     * After a JDA instance as been created, this method can never be called again, even if you are creating a new JDA object.<br>
     * <b>Note:</b> currently this only supports HTTP proxies.
     *
     * @param proxyUrl
     *          The url of the proxy.
     * @param proxyPort
     *          The port of the proxy.  Usually this is 8080.
     * @return
     *      Returns the {@link net.dv8tion.jda.JDABuilder JDABuilder} instance. Useful for chaining.
     * @throws UnsupportedOperationException
     *          If this method is called after proxy settings have already been set or after at least 1 JDA object has been created.
     */
    public JDABuilder setProxy(String proxyUrl, int proxyPort)
    {
        if (proxySet || jdaCreated)
            throw new UnsupportedOperationException("You cannot change the proxy after a proxy has been set or a JDA object has been created. Proxy settings are global among all instances!");
        proxySet = true;
        JDABuilder.proxyUrl = proxyUrl;
        JDABuilder.proxyPort = proxyPort;
        return this;
    }

    /**
     * Enables/Disables Voice functionality.<br>
     * This is useful, if your current system doesn't support Voice and you do not need it.
     * <p>
     * Default: true
     *
     * @param enabled
     *          True - enables voice support.
     * @return
     *          Returns the {@link net.dv8tion.jda.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public JDABuilder setAudioEnabled(boolean enabled)
    {
        this.enableVoice = enabled;
        return this;
    }

    /**
     * Sets whether or not JDA should try to reconnect, if a connection-error occured.
     * This will use and incremental reconnect (timeouts are increased each time an attempt fails).
     *
     * Default is true.
     *
     * @param reconnect
     *      If true - enables autoReconnect
     * @return
     *      Returns the {@link net.dv8tion.jda.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public JDABuilder setAutoReconnect(boolean reconnect)
    {
        this.reconnect = reconnect;
        return this;
    }

    /**
     * <b>This method is deprecated! Please switch to {@link #setEventManager(IEventManager)}.</b>
     * <p>
     * Changes the internal EventManager.
     * The default EventManager is {@link net.dv8tion.jda.hooks.InterfacedEventManager InterfacedEventListener}.
     * There is also an {@link AnnotatedEventManager AnnotatedEventManager} available.
     *
     * @param useAnnotated
     *      Whether or not to use the {@link net.dv8tion.jda.hooks.AnnotatedEventManager AnnotatedEventManager}
     * @return
     *      Returns the {@link net.dv8tion.jda.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    @Deprecated
    public JDABuilder useAnnotatedEventManager(boolean useAnnotated)
    {
        this.useAnnotatedManager = useAnnotated;
        return this;
    }

    /**
     * Changes the internally used EventManager.
     * There are 2 provided Implementations:
     * <ul>
     *     <li>{@link net.dv8tion.jda.hooks.InterfacedEventManager} which uses the Interface {@link net.dv8tion.jda.hooks.EventListener}
     *     (tip: use the {@link net.dv8tion.jda.hooks.ListenerAdapter}). This is the default EventManager.</li>
     *     <li>{@link net.dv8tion.jda.hooks.AnnotatedEventManager} which uses the Annotation {@link net.dv8tion.jda.hooks.SubscribeEvent} to mark the methods that listen for events.</li>
     * </ul>
     * You can also create your own EventManager (See {@link net.dv8tion.jda.hooks.IEventManager}).
     *
     * @param manager
     *      The new {@link net.dv8tion.jda.hooks.IEventManager} to use
     * @return
     *      Returns the {@link net.dv8tion.jda.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public JDABuilder setEventManager(IEventManager manager)
    {
        this.eventManager = manager;
        return this;
    }

    /**
     * Adds a listener to the list of listeners that will be used to populate the {@link net.dv8tion.jda.JDA} object.
     * This uses the {@link net.dv8tion.jda.hooks.InterfacedEventManager InterfacedEventListener} by default.
     * To switch to the {@link net.dv8tion.jda.hooks.AnnotatedEventManager AnnotatedEventManager}, use {@link #useAnnotatedEventManager(boolean)}.
     *
     * Note: when using the {@link net.dv8tion.jda.hooks.InterfacedEventManager InterfacedEventListener} (default),
     * given listener <b>must</b> be instance of {@link net.dv8tion.jda.hooks.EventListener EventListener}!
     *
     * @param listener
     *          The listener to add to the list.
     * @return
     *      Returns the {@link net.dv8tion.jda.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public JDABuilder addListener(Object listener)
    {
        listeners.add(listener);
        return this;
    }

    /**
     * Removes a listener from the list of listeners.
     *
     * @param listener
     *          The listener to remove from the list.
     * @return
     *      Returns the {@link net.dv8tion.jda.JDABuilder JDABuilder} instance. Useful for chaining.
     */
    public JDABuilder removeListener(Object listener)
    {
        listeners.remove(listener);
        return this;
    }

    /**
     * Builds a new {@link net.dv8tion.jda.JDA} instance and uses the provided email and password to start the login process.<br>
     * The login process runs in a different thread, so while this will return immediately, {@link net.dv8tion.jda.JDA} has not
     * finished loading, thus many {@link net.dv8tion.jda.JDA} methods have the chance to return incorrect information.
     * <p>
     * If you wish to be sure that the {@link net.dv8tion.jda.JDA} information is correct, please use
     * {@link net.dv8tion.jda.JDABuilder#buildBlocking() buildBlocking()} or register a
     * {@link net.dv8tion.jda.events.ReadyEvent ReadyEvent} {@link net.dv8tion.jda.hooks.EventListener EventListener}.
     *
     * @return
     *      A {@link net.dv8tion.jda.JDA} instance that has started the login process. It is unknown as to whether or not loading has finished when this returns.
     * @throws LoginException
     *          If the provided email-password combination fails the Discord security authentication.
     * @throws IllegalArgumentException
     *          If either the provided email or password is empty or null.
     */
    public JDA buildAsync() throws LoginException, IllegalArgumentException
    {
        jdaCreated = true;
        JDAImpl jda;
        if (proxySet)
            jda = new JDAImpl(proxyUrl, proxyPort, enableVoice);
        else
            jda = new JDAImpl(enableVoice);
        jda.setAutoReconnect(reconnect);
        if (eventManager != null)
        {
            jda.setEventManager(eventManager);
        }
        else if (useAnnotatedManager)
        {
            jda.setEventManager(new AnnotatedEventManager());
        }
        listeners.forEach(jda::addEventListener);
        jda.login(botToken);
        return jda;
    }

    /**
     * Builds a new {@link net.dv8tion.jda.JDA} instance and uses the provided email and password to start the login process.<br>
     * This method will block until JDA has logged in and finished loading all resources. This is an alternative
     * to using {@link net.dv8tion.jda.events.ReadyEvent ReadyEvent}.
     *
     * @return
     *      A {@link net.dv8tion.jda.JDA} Object that is <b>guaranteed</b> to be logged in and finished loading.
     * @throws LoginException
     *          If the provided email-password combination fails the Discord security authentication.
     * @throws IllegalArgumentException
     *          If either the provided email or password is empty or null.
     * @throws InterruptedException
     *          If an interrupt request is received while waiting for {@link net.dv8tion.jda.JDA} to finish logging in.
     *          This would most likely be caused by a JVM shutdown request.
     */
    public JDA buildBlocking() throws LoginException, IllegalArgumentException, InterruptedException
    {
        //Create our ReadyListener and a thread safe Boolean.
        AtomicBoolean ready = new AtomicBoolean(false);
        ListenerAdapter readyListener = new ListenerAdapter()
        {
            @SubscribeEvent
            @Override
            public void onReady(ReadyEvent event)
            {
                ready.set(true);
            }
        };

        //Add it to our list of listeners, start the login process, wait for the ReadyEvent.
        listeners.add(readyListener);
        JDA jda = buildAsync();
        while(!ready.get())
        {
            Thread.sleep(50);
        }

        //We have logged in. Remove the temp ready listener from our local list and the jda listener list.
        listeners.remove(readyListener);
        jda.removeEventListener(readyListener);
        return jda;
    }
}
