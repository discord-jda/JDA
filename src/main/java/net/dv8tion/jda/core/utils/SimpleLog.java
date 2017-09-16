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
package net.dv8tion.jda.core.utils;

import net.dv8tion.jda.core.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.text.SimpleDateFormat;
import java.util.*;

public class SimpleLog
{
    /**
     * The global LOG-level that is used as standard if not overwritten
     */
    public static Level LEVEL = Level.INFO;

    public static final boolean SLF4J_ENABLED;
    static
    {
        boolean tmp = false;
        try
        {
            Class.forName("org.slf4j.impl.StaticLoggerBinder");
            tmp = true;
        }
        catch (ClassNotFoundException e)
        {
            //prints warning of missing implementation
            LoggerFactory.getLogger(JDA.class);
        }
        SLF4J_ENABLED = tmp;
    }

    private static final String FORMAT = "[%time%] [%level%] [%name%]: %text%";
    private static final SimpleDateFormat DFORMAT = new SimpleDateFormat("HH:mm:ss");

    private static final Map<String, SimpleLog> LOGS = new HashMap<>();
    private static final Set<LogListener> listeners = new HashSet<>();

    private final String name;
    private final Logger logger;
    private Level level = null;

    private SimpleLog(Class<?> clazz)
    {
        this.name = clazz.getName();
        this.logger = SLF4J_ENABLED ? LoggerFactory.getLogger(clazz) : null;
    }

    private SimpleLog(String name)
    {
        this.name = name;
        this.logger = SLF4J_ENABLED ? LoggerFactory.getLogger(name) : null;
    }

    public String getName()
    {
        return name;
    }

    /**
     * Set the LOG-level
     * All messages with lower LOG-level will not be printed
     * If this level is set to null, the global Log-level ({@link net.dv8tion.jda.core.utils.SimpleLog#LEVEL}) will be used
     *
     * @param lev the new LOG-level
     */
    public void setLevel(Level lev)
    {
        this.level = lev;
    }

    /**
     * Gets the current logging-level of this Logger.
     * This might return null, if the global logging-level is used.
     *
     * @return the logging-level of this Logger or null
     */
    public Level getLevel()
    {
        return level;
    }

    /**
     * Gets the effective logging-level of this Logger.
     * This considers the global logging-level.
     *
     * @return the effective logging-level of this Logger
     */
    public Level getEffectiveLevel()
    {
        if (logger != null)
        {
            if (logger.isTraceEnabled())
                return Level.TRACE;
            if (logger.isDebugEnabled())
                return Level.DEBUG;
            if (logger.isInfoEnabled())
                return Level.INFO;
            if (logger.isWarnEnabled())
                return Level.WARN;
            return Level.ERROR;
        }
        return level == null ? SimpleLog.LEVEL : level;
    }

    private void slf4j(Level level, Object obj)
    {
        if (obj instanceof Throwable)
        {
            Throwable t = (Throwable) obj;
            switch (level)
            {
                case ERROR:
                    logger.error("Encountered an Exception ", t);
                    break;
                case WARN:
                    logger.warn("Encountered an Exception ", t);
                    break;
                case INFO:
                    logger.info("Encountered an Exception ", t);
                    break;
                case DEBUG:
                    logger.debug("Encountered an Exception ", t);
                    break;
                case TRACE:
                    logger.trace("Encountered an Exception ", t);
                    break;
                default:
                    break;
            }
            return;
        }
        String msg = String.valueOf(obj);
        switch (level)
        {
            case ERROR:
                logger.error(msg);
                break;
            case WARN:
                logger.warn(msg);
                break;
            case INFO:
                logger.info(msg);
                break;
            case DEBUG:
                logger.debug(msg);
                break;
            case TRACE:
                logger.trace(msg);
                break;
        }
    }

    /**
     * Will LOG a message with given LOG-level
     *
     * @param level The level of the Log
     * @param obj   The message to LOG
     */
    public void log(Level level, Object obj)
    {
        if (logger != null)
        {
            slf4j(level, obj);
            return;
        }
        if (getEffectiveLevel().ordinal() < level.ordinal())
            return;
        String msg;
        if (obj instanceof Throwable)
            msg = "Encountered an Exception: \n" + Helpers.getStackTrace((Throwable) obj);
        else
            msg = String.valueOf(obj);

        synchronized (listeners)
        {
            for (LogListener listener : listeners)
            {
                if (obj instanceof Throwable)
                    listener.onError(this, (Throwable) obj);
                else
                    listener.onLog(this, level, msg);
            }
        }
        print(FORMAT.replace("%time%", DFORMAT.format(new Date()))
                    .replace("%level%", level.toString())
                    .replace("%name%", name)
                    .replace("%text%", msg), level);
    }

    /**
     * Will LOG a message with trace level.
     *
     * @param msg the object, which should be logged
     */
    public void trace(Object msg)
    {
        log(Level.TRACE, msg);
    }

    /**
     * Will LOG a message with debug level
     *
     * @param msg the object, which should be logged
     */
    public void debug(Object msg)
    {
        log(Level.DEBUG, msg);
    }

    /**
     * Will LOG a message with info level
     *
     * @param msg the object, which should be logged
     */
    public void info(Object msg)
    {
        log(Level.INFO, msg);
    }

    /**
     * Will LOG a message with warning level
     *
     * @param msg the object, which should be logged
     */
    public void warn(Object msg)
    {
        log(Level.WARN, msg);
    }

    /**
     * Will LOG a message with fatal level
     *
     * @param msg the object, which should be logged
     */
    public void fatal(Object msg)
    {
        log(Level.ERROR, msg);
    }

    /**
     * prints a message to the console or as message-box.
     *
     * @param msg   the message, that should be displayed
     * @param level the LOG level of the message
     */
    private void print(String msg, Level level)
    {
        if (level == Level.ERROR || level == Level.WARN)
            System.err.println(msg);
        else
            System.out.println(msg);
    }

    // STATIC ACCESS

    /**
     * Will get the LOG with the given LOG-name or create one if it didn't exist
     *
     * @param name the name of the LOG
     * @return SimpleLog with given LOG-name
     */
    public static SimpleLog getLog(String name)
    {
        synchronized (LOGS)
        {
            return LOGS.computeIfAbsent(name.toLowerCase(), (n) -> new SimpleLog(name));
        }
    }

    public static SimpleLog getLog(Class<?> clazz)
    {
        synchronized (LOGS)
        {
            return LOGS.computeIfAbsent(clazz.getName().toLowerCase(), (name) -> new SimpleLog(clazz));
        }
    }

    /**
     * Adds a custom Listener that receives all logs
     * @param listener the listener to add
     */
    public static void addListener(LogListener listener)
    {
        synchronized (listeners)
        {
            listeners.add(listener);
        }
    }

    /**
     * Removes a custom Listener
     * @param listener the listener to remove
     */
    public static void removeListener(LogListener listener)
    {
        synchronized (listeners)
        {
            listeners.remove(listener);
        }
    }

    /**
     * This interface has to be able to register (via {@link net.dv8tion.jda.core.utils.SimpleLog#addListener(net.dv8tion.jda.core.utils.SimpleLog.LogListener)}) and listen to log-messages.
     */
    public interface LogListener
    {
        /**
         * Called on any incoming log-messages (including stacktraces).
         * This is also called on log-messages that would normally not print to console due to log-level.
         * @param log
         *      the log this message was sent to
         * @param logLevel
         *      the level of the message sent
         * @param message
         *      the message as object
         */
        void onLog(SimpleLog log, Level logLevel, Object message);

        /**
         * Called on any incoming error-message (Throwable).
         * Note: Throwables are also logged with FATAL level.
         * This is just a convenience Method to do special Throwable handling.
         * @param log
         *      the log this error was sent to
         * @param err
         *      the error as Throwable
         */
        void onError(SimpleLog log, Throwable err);
    }
}
