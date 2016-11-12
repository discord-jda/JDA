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
package net.dv8tion.jda.utils;

import net.dv8tion.jda.entities.impl.JDAImpl;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

public class SimpleLog
{
    /**
     * The global LOG-level that is used as standard if not overwritten
     */
    public static Level LEVEL = Level.INFO;

    /**
     * If this boolean is set to true, if there is no console present, messages are shown as message-dialogs
     */
    public static boolean ENABLE_GUI = false;

    private static final String FORMAT = "[%time%] [%level%] [%name%]: %text%";
    private static final String MSGFORMAT = "%text%";
    private static final SimpleDateFormat DFORMAT = new SimpleDateFormat("HH:mm:ss");

    private static final Map<String, SimpleLog> LOGS = new HashMap<>();
    private static final Set<LogListener> listeners = new HashSet<>();

    /**
     * Will get the LOG with the given LOG-name or create one if it didn't exist
     *
     * @param name the name of the LOG
     * @return SimpleLog with given LOG-name
     */
    public static SimpleLog getLog(String name) {
        synchronized (LOGS) {
            if(!LOGS.containsKey(name.toLowerCase())) {
                LOGS.put(name.toLowerCase(), new SimpleLog(name));
            }
        }
        return LOGS.get(name.toLowerCase());
    }

    private static PrintStream origStd = null;
    private static PrintStream origErr = null;
    private static FileOutputStream stdOut = null;
    private static FileOutputStream errOut = null;
    private static Map<Level, Set<File>> fileLogs = new HashMap<>();

    /**
     * Will duplicate the output-streams to the specified Files.
     * This will catch everything that is sent to sout and serr (even stuff not logged via SimpleLog).
     *
     * @param std
     *      The file to use for System.out logging, or null to not LOG System.out to a file
     * @param err
     *      The file to use for System.err logging, or null to not LOG System.err to a file
     * @throws IOException
     *      If an IO error is encountered while dealing with the file. Most likely
     *      to be caused by a lack of permissions when creating the log folders or files.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void addFileLogs(File std, File err) throws IOException {
        if(std != null) {
            if (origStd == null)
                origStd = System.out;
            if(!std.getAbsoluteFile().getParentFile().exists()) {
                std.getAbsoluteFile().getParentFile().mkdirs();
            }
            if(!std.exists()) {
                std.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(std, true);
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    origStd.write(b);
                    fOut.write(b);
                }
            }));
            if (stdOut != null)
                stdOut.close();
            stdOut = fOut;
        }
        else if (origStd != null)
        {
            System.setOut(origStd);
            stdOut.close();
            origStd = null;
        }
        if(err != null) {
            if (origErr == null)
                origErr = System.err;
            if(!err.getAbsoluteFile().getParentFile().exists()) {
                err.getAbsoluteFile().getParentFile().mkdirs();
            }
            if(!err.exists()) {
                err.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(err, true);
            System.setErr(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    origErr.write(b);
                    fOut.write(b);
                }
            }));
            if (errOut != null)
                errOut.close();
            errOut = fOut;
        }
        else if (origErr != null)
        {
            System.setErr(origErr);
            errOut.close();
            origErr = null;
        }
    }

    /**
     * Sets up a File to log all messages that are not visible via sout and serr and meet a given log-level criteria.
     * All logs that would not be printed and are above given logLevel are printed to that File.
     * If you want to log Logs printed to sout and serr to file(s), use {@link #addFileLogs(File, File)} instead.
     *
     * @param logLevel
     *      The log-level criteria. Only logs equal or above this level are printed to the File
     * @param file
     *      The File where the logs should be printed
     * @throws IOException
     *      If the File can't be canonically resolved (access denied)
     */
    public static void addFileLog(Level logLevel, File file) throws IOException
    {
        File canonicalFile = file.getCanonicalFile();
        if (!fileLogs.containsKey(logLevel))
        {
            fileLogs.put(logLevel, new HashSet<>());
        }
        fileLogs.get(logLevel).add(canonicalFile);
    }

    /**
     * Removes all File-logs created via {@link #addFileLog(Level, File)} with given Level.
     * To remove the sout and serr logs, call {@link #addFileLogs(File, File)} with null args.
     *
     * @param logLevel
     *      The level for which all fileLogs should be removed
     */
    public static void removeFileLog(Level logLevel)
    {
        fileLogs.remove(logLevel);
    }

    /**
     * Removes all File-logs created via {@link #addFileLog(Level, File)} with given File.
     * To remove the sout and serr logs, call {@link #addFileLogs(File, File)} with null args.
     *
     * @param file
     *      The file to remove from all FileLogs (except sout and serr logs)
     * @throws IOException
     *      If the File can't be canonically resolved (access denied)
     */
    public static void removeFileLog(File file) throws IOException
    {
        File canonicalFile = file.getCanonicalFile();
        Iterator<Map.Entry<Level, Set<File>>> setIter = fileLogs.entrySet().iterator();
        while (setIter.hasNext())
        {
            Map.Entry<Level, Set<File>> set = setIter.next();
            Iterator<File> fileIter = set.getValue().iterator();
            while (fileIter.hasNext())
            {
                File logFile = fileIter.next();
                if(logFile.equals(canonicalFile))
                {
                    fileIter.remove();
                    break;
                }
            }
            if (set.getValue().isEmpty())
            {
                setIter.remove();
            }
        }
    }

    private static Set<File> collectFiles(Level level)
    {
        Set<File> out = new HashSet<>();
        for (Map.Entry<Level, Set<File>> mapEntry : fileLogs.entrySet())
        {
            if(mapEntry.getKey().getPriority() <= level.getPriority())
                out.addAll(mapEntry.getValue());
        }
        return out;
    }

    private static void logToFiles(String msg, Level level)
    {
        Set<File> files = collectFiles(level);
        for (File file : files)
        {
            try
            {
                Files.write(file.toPath(), (msg + '\n').getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
            catch (IOException e)
            {
                JDAImpl.LOG.fatal("Could not write log to logFile...");
                JDAImpl.LOG.log(e);
            }
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

    public final String name;
    private Level level = null;

    private SimpleLog(String name) {
        this.name = name;
    }

    /**
     * Set the LOG-level
     * All messages with lower LOG-level will not be printed
     * If this level is set to null, the global Log-level ({@link SimpleLog#LEVEL}) will be used
     *
     * @param lev the new LOG-level
     */
    public void setLevel(Level lev) {
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
        return level == null ? SimpleLog.LEVEL : level;
    }

    /**
     * Will LOG a message with given LOG-level
     *
     * @param level The level of the Log
     * @param msg   The message to LOG
     */
    public void log(Level level, Object msg) {
        synchronized (listeners)
        {
            for (LogListener listener : listeners)
            {
                listener.onLog(this, level, msg);
            }
        }
        String format = (ENABLE_GUI && !isConsolePresent()) ? MSGFORMAT : FORMAT;
        format = format.replace("%time%", DFORMAT.format(new Date())).replace("%level%", level.getTag()).replace("%name%", name).replace("%text%", String.valueOf(msg));
        if(level == Level.OFF || level.getPriority() < ((this.level == null) ? SimpleLog.LEVEL.getPriority() : this.level.getPriority())) {
            logToFiles(format, level);
        }
        else
        {
            print(format, level);
        }
    }

    public void log(Throwable ex)
    {
        synchronized (listeners)
        {
            for (LogListener listener : listeners)
            {
                listener.onError(this, ex);
            }
        }
        log(Level.FATAL, "Encountered an exception:");
        log(Level.FATAL, ExceptionUtils.getStackTrace(ex));
    }

    /**
     * Will LOG a message with trace level.
     *
     * @param msg the object, which should be logged
     */
    public void trace(Object msg) {
        log(Level.TRACE, msg);
    }

    /**
     * Will LOG a message with debug level
     *
     * @param msg the object, which should be logged
     */
    public void debug(Object msg) {
        log(Level.DEBUG, msg);
    }

    /**
     * Will LOG a message with info level
     *
     * @param msg the object, which should be logged
     */
    public void info(Object msg) {
        log(Level.INFO, msg);
    }

    /**
     * Will LOG a message with warning level
     *
     * @param msg the object, which should be logged
     */
    public void warn(Object msg) {
        log(Level.WARNING, msg);
    }

    /**
     * Will LOG a message with fatal level
     *
     * @param msg the object, which should be logged
     */
    public void fatal(Object msg) {
        log(Level.FATAL, msg);
    }

    /**
     * prints a message to the console or as message-box.
     *
     * @param msg   the message, that should be displayed
     * @param level the LOG level of the message
     */
    private void print(String msg, Level level) {
        if(ENABLE_GUI && !isConsolePresent()) {
            if(level.isError()) {
                JOptionPane.showMessageDialog(null, msg, "An Error occurred!", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, msg, level.getTag(), JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            if(level.isError()) {
                System.err.println(msg);
            } else {
                System.out.println(msg);
            }
        }
    }

    /**
     * Will return whether the program has a console present, or was launched without
     *
     * @return boolean true, if console is present
     */
    public static boolean isConsolePresent() {
        return System.console() != null;
    }

    /**
     * This interface has to be able to register (via {@link SimpleLog#addListener(LogListener)}) and listen to log-messages.
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

    /**
     * Enum containing all the LOG-levels
     */
    public enum Level {
        ALL("Finest", 0, false),
        TRACE("Trace", 1, false),
        DEBUG("Debug", 2, false),
        INFO("Info", 3, false),
        WARNING("Warning", 4, true),
        FATAL("Fatal", 5, true),
        OFF("NO-LOGGING", 6, true);

        private String msg;
        private int pri;
        private boolean isError;

        Level(String message, int priority, boolean isError) {
            this.msg = message;
            this.pri = priority;
            this.isError = isError;
        }

        /**
         * Returns the Log-Tag (e.g. Fatal)
         *
         * @return the logTag
         */
        public String getTag() {
            return msg;
        }

        /**
         * Returns the numeric priority of this loglevel, with 0 being the lowest
         *
         * @return the level-priority
         */
        public int getPriority() {
            return pri;
        }

        /**
         * Returns whether this LOG-level should be treated like an error or not
         *
         * @return boolean true, if this LOG-level is an error-level
         */
        public boolean isError() {
            return isError;
        }
    }
}
