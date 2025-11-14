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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;
import org.slf4j.spi.SLF4JServiceProvider;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * This class serves as a LoggerFactory for JDA's internals.
 * <br>It will either return a Logger from a SLF4J implementation via {@link org.slf4j.LoggerFactory} if present,
 * or an instance of a custom {@link FallbackLogger}.
 * <p>
 * It also has the utility method {@link #getLazyString(LazyEvaluation)} which is used to lazily construct Strings for Logging.
 *
 * @see #setFallbackLoggerEnabled(boolean)
 */
public class JDALogger {
    /**
     * The name of the system property, which controls whether the fallback logger is disabled.
     *
     * <p>{@value}
     */
    public static final String DISABLE_FALLBACK_PROPERTY_NAME = "net.dv8tion.jda.disableFallbackLogger";

    /**
     * Whether an implementation of {@link SLF4JServiceProvider} was found.
     * <br>If false, JDA will use its fallback logger.
     *
     * <p>The fallback logger can be disabled with {@link #setFallbackLoggerEnabled(boolean)}
     * or using the system property {@value #DISABLE_FALLBACK_PROPERTY_NAME}.
     */
    public static final boolean SLF4J_ENABLED;

    private static boolean disableFallback = Boolean.getBoolean(DISABLE_FALLBACK_PROPERTY_NAME);
    private static final MethodHandle fallbackLoggerConstructor;

    static {
        boolean hasLoggerImpl = false;
        try {
            Class<?> provider = Class.forName("org.slf4j.spi.SLF4JServiceProvider");
            hasLoggerImpl = ServiceLoader.load(provider).iterator().hasNext();
        } catch (ClassNotFoundException ignored) {
            disableFallback = true; // only works with SLF4J 2.0+
        }

        SLF4J_ENABLED = hasLoggerImpl;

        // Dynamically load fallback logger to avoid static initializer errors

        MethodHandle constructor = null;
        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            Class<?> fallbackLoggerClass = Class.forName("net.dv8tion.jda.internal.utils.FallbackLogger");
            constructor = lookup.findConstructor(fallbackLoggerClass, MethodType.methodType(void.class, String.class));
        } catch (ClassNotFoundException
                | ExceptionInInitializerError
                | IllegalAccessException
                | NoClassDefFoundError
                | NoSuchMethodException ignored) {
        }

        fallbackLoggerConstructor = constructor;
    }

    private static final Map<String, Logger> LOGS = new HashMap<>();

    private JDALogger() {}

    /**
     * Disables the automatic fallback logger that JDA uses when no SLF4J implementation is found.
     *
     * @param enabled
     *        False, to disable the fallback logger
     */
    public static void setFallbackLoggerEnabled(boolean enabled) {
        disableFallback = !enabled;
    }

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
    public static Logger getLog(String name) {
        synchronized (LOGS) {
            if (SLF4J_ENABLED || disableFallback) {
                return LoggerFactory.getLogger(name);
            }
            return newFallbackLogger(name);
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
    public static Logger getLog(Class<?> clazz) {
        synchronized (LOGS) {
            if (SLF4J_ENABLED || disableFallback) {
                return LoggerFactory.getLogger(clazz);
            }
            return newFallbackLogger(clazz.getSimpleName());
        }
    }

    private static void printFallbackWarning() {
        Logger logger = newFallbackLogger(JDALogger.class.getSimpleName());
        logger.warn("Using fallback logger due to missing SLF4J implementation.");
        logger.warn("Please setup a logging framework to use JDA.");
        logger.warn("You can use our logging setup guide https://jda.wiki/setup/logging/");
        logger.warn(
                "To disable the fallback logger, add the slf4j-nop dependency or use JDALogger.setFallbackLoggerEnabled(false)");
    }

    private static Logger newFallbackLogger(String name) {
        if (disableFallback || fallbackLoggerConstructor == null) {
            return NOPLogger.NOP_LOGGER;
        }

        try {
            synchronized (LOGS) {
                if (LOGS.containsKey(name)) {
                    return LOGS.get(name);
                }
                Logger logger = (Logger) fallbackLoggerConstructor.invoke(name);
                boolean isFirstFallback = LOGS.isEmpty();
                LOGS.put(name, logger);
                if (isFirstFallback) {
                    printFallbackWarning();
                }
                return logger;
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to initialize fallback logger", e);
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
    public static Object getLazyString(LazyEvaluation lazyLambda) {
        return new Object() {
            @Override
            public String toString() {
                try {
                    return lazyLambda.getString();
                } catch (Exception ex) {
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
    public interface LazyEvaluation {
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
