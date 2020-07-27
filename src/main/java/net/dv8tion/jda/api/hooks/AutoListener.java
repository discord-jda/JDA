/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.hooks;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Automatically calls any method which fulfills these requirements:
 *
 * <ol>
 *     <li>Public Member (non-static)</li>
 *     <li>Single Parameter</li>
 *     <li>Parameter-type inherits from {@link GenericEvent}</li>
 *     <li>Method not annotated with {@link IgnoreEvent}</li>
 * </ol>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * public class MyListener extends AutoListener {
 *     public static final Logger LOG = LoggerFactory.getLogger(MyListener.class);
 *
 *     // Ignored: Not a public member method
 *     private void log(MessageReceivedEvent event) {
 *         LOG.info("[{}#{}] {}: {}",
 *             event.getGuild().getName(), event.getChannel().getName(),
 *             event.getAuthor().getAsTag(), event.getMessage().getContentDisplay()
 *         );
 *     }
 *
 *     // Ignored: Not a public member method
 *     public static void logPrivate(MessageReceivedEvent event) {
 *         LOG.info("[private] {}: {}",
 *             event.getAuthor().getAsTag(), event.getMessage().getContentDisplay()
 *         );
 *     }
 *
 *     // Called: It's a public member method
 *     public void onLog(MessageReceivedEvent event) {
 *         if (event.getChannelType().isGuild())
 *             log(event);
 *         else
 *             logPrivate(event);
 *     }
 * }</pre>
 */
public abstract class AutoListener implements EventListener
{
    private final Map<Class<?>, List<Method>> listeners;

    /**
     * Creates an AutoListener which handles inheritance and calls methods for any subclass.
     *
     * <p>You can use {@link #AutoListener(boolean)} to disable inheritance.
     */
    public AutoListener()
    {
        this(true);
    }

    /**
     * Creates an AutoListener.
     *
     * @param inheritance
     *        Whether to call methods at deeper subclasses.
     */
    public AutoListener(boolean inheritance)
    {
        this.listeners = new HashMap<>();
        Method[] methods;
        if (inheritance)
            methods = getClass().getMethods(); // returns public member methods, including inherited ones
        else
            methods = getClass().getDeclaredMethods(); // returns all methods declared by the instance class, no inheritance
        for (Method method : methods)
        {
            int modifiers = method.getModifiers();
            // We only want to call public member methods
            if ((modifiers & (Modifier.STATIC | Modifier.PUBLIC)) != Modifier.PUBLIC)
                continue;
            Class<?>[] parameters = method.getParameterTypes();
            // We only want to check methods which have a single event parameter
            if (parameters.length != 1 || !GenericEvent.class.isAssignableFrom(parameters[0]))
                continue;
            // We don't want to call methods which are explicitly ignored
            IgnoreEvent annotation = method.getAnnotation(IgnoreEvent.class);
            if (annotation != null)
                continue;

            // Accumulate all methods that match this event class
            listeners.computeIfAbsent(parameters[0], (k) -> new ArrayList<>())
                    .add(method);
        }
    }

    @Override
    @IgnoreEvent
    public final void onEvent(@Nonnull GenericEvent event)
    {
        for (Map.Entry<Class<?>, List<Method>> entry : listeners.entrySet())
        {
            if (!entry.getKey().isInstance(event))
                continue;

            for (Method method : entry.getValue())
            {
                try
                {
                    method.invoke(this, event);
                }
                catch (IllegalAccessException e)
                {
                    JDAImpl.LOG.error("Could not invoke method for event handling", e);
                }
                catch (InvocationTargetException e)
                {
                    JDAImpl.LOG.error("Uncaught exception in event listener", e.getCause());
                }
            }
        }
    }
}
