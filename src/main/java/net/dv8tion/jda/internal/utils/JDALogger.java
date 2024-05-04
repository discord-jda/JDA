/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.internal.utils;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.SLF4JServiceProvider;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * This class serves as a LoggerFactory for JDA's internals.
 * <br>It will either return a Logger from a SLF4J implementation via {@link org.slf4j.LoggerFactory} if present,
 * or an instance of a custom {@link FallbackLogger}.
 * <p>
 * It also has the utility method {@link #getLazyString(LazyEvaluation)} which is used to lazily construct Strings for Logging.
 */
public class JDALogger
{
    /**
     * Marks whether or not a SLF4J <code>StaticLoggerBinder</code> (pre 1.8.x) or
     * <code>SLF4JServiceProvider</code> implementation (1.8.x+) was found. If false, JDA will use its fallback logger.
     * <br>This variable is initialized during static class initialization.
     */
    public static final boolean SLF4J_ENABLED;
    static
    {
        boolean tmp = false;

        try
        {
            Class.forName("org.slf4j.impl.StaticLoggerBinder");

            tmp = true;
        }
        catch (ClassNotFoundException eStatic)
        {
            // there was no static logger binder (SLF4J pre-1.8.x)
            // check if there is a service implementation for the service, indicating a provider for SLF4J 1.8.x+ is installed
            tmp = ServiceLoader.load(SLF4JServiceProvider.class).iterator().hasNext();
        }

        SLF4J_ENABLED = tmp;
    }

    private static final Map<String, Logger> LOGS = new CaseInsensitiveMap<>();

    private JDALogger() {}

    /**
     * Will get the {@link org.slf4j.Logger} with the given log-name
     * or create and cache a fallback logger if there is no SLF4J implementation present.
     * <p>
     * The fallback logger uses a constant logging configuration and prints directly to {@link System#err}.
     *
     * @param  name
     *         The name of the Logger
     *
     * @return Logger with given log name
     */
    public static Logger getLog(String name)
    {
        synchronized (LOGS)
        {
            if (SLF4J_ENABLED)
                return LoggerFactory.getLogger(name);
            return LOGS.computeIfAbsent(name, FallbackLogger::new);
        }
    }

    /**
     * Will get the {@link org.slf4j.Logger} for the given Class
     * or create and cache a fallback logger if there is no SLF4J implementation present.
     * <p>
     * The fallback logger uses a constant logging configuration and prints directly to {@link System#err}.
     *
     * @param  clazz
     *         The class used for the Logger name
     *
     * @return Logger for given Class
     */
    public static Logger getLog(Class<?> clazz)
    {
        synchronized (LOGS)
        {
            if (SLF4J_ENABLED)
                return LoggerFactory.getLogger(clazz);
            return LOGS.computeIfAbsent(clazz.getName(), (n) -> new FallbackLogger(clazz.getSimpleName()));
        }
    }

    /**
     * Utility function to enable logging of complex statements more efficiently (lazy).
     *
     * @param  lazyLambda
     *         The Supplier used when evaluating the expression
     *
     * @return An Object that can be passed to SLF4J's logging methods as lazy parameter
     */
    public static Object getLazyString(LazyEvaluation lazyLambda)
    {
        return new Object()
        {
            @Override
            public String toString()
            {
                try
                {
                    return lazyLambda.getString();
                }
                catch (Exception ex)
                {
                    StringWriter sw = new StringWriter();
                    ex.printStackTrace(new PrintWriter(sw));
                    return "Error while evaluating lazy String... " + sw;
                }
            }
        };
    }

    /**
     * Functional interface used for {@link #getLazyString(LazyEvaluation)} to lazily construct a String.
     */
    @FunctionalInterface
    public interface LazyEvaluation
    {
        /**
         * This method is used by {@link #getLazyString(LazyEvaluation)}
         * when SLF4J requests String construction.
         * <br>The String returned by this is used to construct the log message.
         *
         * @throws Exception
         *         To allow lazy evaluation of methods that might throw exceptions
         *
         * @return The String for log message
         */
        String getString() throws Exception;
    }
}

