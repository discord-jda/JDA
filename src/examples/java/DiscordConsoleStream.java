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

import net.dv8tion.jda.entities.TextChannel;

import java.io.PrintStream;

/**
 * Completed as a request for a possible implementation that would redirect all
 * console output to a Discord {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
 * <p>
 * Usage: create a new instance, providing the destination {@link net.dv8tion.jda.entities.TextChannel TextChannel},
 * and when you want to start redirecting to the {@link net.dv8tion.jda.entities.TextChannel TextChannel}
 * call {@link DiscordConsoleStream#enableRedirect(boolean) enableRedirect(true)}.
 */
public class DiscordConsoleStream
{
    private TextChannel channel;
    private RedirectStream systemOut;
    private RedirectStream systemErr;

    /**
     * Creates a new instance of the DiscordConsoleStream along with
     * with the required RedirectStreams.
     * <p>
     * @param channel
     *          The Discord {@link net.dv8tion.jda.entities.TextChannel TextChannel} to redirect output to.
     * @throws NullPointerException
     *          If the provided TextChannel is null.
     */
    public DiscordConsoleStream(TextChannel channel)
    {
        this(channel, false);
    }

    /**
     * Creates a new instance of the DiscordConsoleStream along with
     * with the required RedirectStreams. Automatically redirects STDout and STDerr.
     * <p>
     * @param channel
     *          The Discord {@link net.dv8tion.jda.entities.TextChannel TextChannel} to redirect output to.
     * @param enable
     *          Used to start redirecting immediately instead of needing an additional call to {@link DiscordConsoleStream#enableRedirect(boolean) enableRedirect(true)}.
     * @throws NullPointerException
     *          If the provided TextChannel is null.
     */
    public DiscordConsoleStream(TextChannel channel, boolean enable)
    {
        setChannel(channel);
        systemOut = new RedirectStream(System.out)
        {
            @Override
            protected void enableRedirect(boolean enable)
            {
                if (enable)
                {
                    System.setOut(this);
                }
                else
                {
                    System.setOut(getOut());
                }
            }
        };
        systemErr = new RedirectStream(System.err)
        {
            @Override
            protected void enableRedirect(boolean enable)
            {
                if (enable)
                {
                    System.setErr(this);
                }
                else
                {
                    System.setErr(getOut());
                }
            }
        };
        enableRedirect(enable);
    }

    /**
     * Sends a message to both the Console and to the Discord {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     *
     * @param string
     *          The message which to send to the Console and Discord.
     */
    public void print(String string)
    {
        systemOut.print(string);
    }

    /**
     * Sends a message with a break line to both the Console and to the Discord {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     *
     * @param string
     *          The message which to send to the Console and Discord.
     */
    public void println(String string)
    {
        systemOut.println(string);
    }

    /**
     * Sends a break line to both the Console and to the Discord {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     */
    public void println()
    {
        systemOut.println();
    }

    /**
     * The {@link net.dv8tion.jda.entities.TextChannel TextChannel} which {@link System#out System.out} and {@link System#err System.err}
     * are redirected to.
     *
     * @return
     *      The console output {@link net.dv8tion.jda.entities.TextChannel TextChannel}.
     */
    public TextChannel getChannel()
    {
        return channel;
    }

    /**
     * Sets the {@link net.dv8tion.jda.entities.TextChannel TextChannel} that the Console output should be redirected to.
     *
     * @param channel
     *          The TextChannel to redirect console output to.
     * @throws NullPointerException
     *          If the provided TextChannel is null.
     */
    public void setChannel(TextChannel channel)
    {
        if (channel == null) throw new NullPointerException("Cannot redirect Console output to a null TextChannel!");
        this.channel = channel;
    }

    /**
     * Enables the redirection of {@link System#out System.out} and {@link System#err System.err}
     * if the boolean param is true.<br>
     * If it is false, this method will restore {@link System#out System.out} and {@link System#err System.err}
     * to their original PrintStream instances and stop redirecting to Discord.
     *
     * @param enable
     *          True: Redirect output to Discord, False: Do not redirect to Discord.
     */
    public void enableRedirect(boolean enable)
    {
        systemOut.enableRedirect(enable);
        systemErr.enableRedirect(enable);
    }

    private void printToDiscord(String s)
    {
        channel.sendMessage(s);
    }

    private abstract class RedirectStream extends PrintStream
    {

        public RedirectStream(PrintStream out)
        {
            super(out);
        }

        protected abstract void enableRedirect(boolean enable);

        @Override
        public void println()
        {
            ((PrintStream) out).println();
            DiscordConsoleStream.this.printToDiscord("\n");
        }

        @Override
        public void println(String s)
        {
            s = s == null ? "null" : s;
            ((PrintStream) out).println(s);
            DiscordConsoleStream.this.printToDiscord(s + "\n");
        }

        @Override
        public void print(String s)
        {
            s = s == null ? "null" : s;
            ((PrintStream) out).print(s);
            DiscordConsoleStream.this.printToDiscord(s);
        }

        public PrintStream getOut()
        {
            return (PrintStream) out;
        }
    }
}
