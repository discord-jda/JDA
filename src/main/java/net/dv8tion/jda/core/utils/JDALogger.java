package net.dv8tion.jda.core.utils;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

import java.util.Map;

public class JDALogger {

    /**
     * Marks whether or not a slf4j StaticLoggerBinder was found. If false, JDA will use its fallback logger.
     * This variable is initialized during static class initialization.
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
        catch (ClassNotFoundException e)
        {
            //prints warning of missing implementation
            LoggerFactory.getLogger(JDALogger.class);
        }
        SLF4J_ENABLED = tmp;
    }

    private static final Map<String, Logger> LOGS = new CaseInsensitiveMap<>();

    /**
     * Will get the {@link Logger} with the given log-name
     * or create and cache a fallback logger if there is no SLF4J implementation present
     *
     * The fallback logger will be an instance of {@link SimpleLogger} - a slightly modified version of SLF4Js Loggerger
     *
     * @param name the name of the Logger
     * @return Logger with given log-name
     */
    public static Logger getLog(String name)
    {
        synchronized (LOGS)
        {
            if(SLF4J_ENABLED)
                return LoggerFactory.getLogger(name);
            return LOGS.computeIfAbsent(name, (n) -> new SimpleLogger(name));
        }
    }

    /**
     * Will get the {@link Logger} for the given Class
     * or create and cache a fallback logger if there is no SLF4J implementation present
     *
     * The fallback logger will be an instance of {@link SimpleLogger} - a slightly modified version of SLF4Js Loggerger
     *
     * @param clazz the class used for the Logger name
     * @return Logger for given Class
     */
    public static Logger getLog(Class<?> clazz)
    {
        synchronized (LOGS)
        {
            if(SLF4J_ENABLED)
                return LoggerFactory.getLogger(clazz);
            return LOGS.computeIfAbsent(clazz.getName(), (n) -> new SimpleLogger(clazz.getSimpleName()));
        }
    }
}

