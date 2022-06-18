package net.dv8tion.jda.api.entities;

public interface ActionMetadata {
    /**
     * The channel were the logging of the auto-moderation rule breaking should be sent to.
     *
     * @return {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     */
    TextChannel getChannel();

    /**
     * The duration of the timeout in seconds.
     *
     * @return {@link java.lang.Integer Integer}
     */
    int getDuration();
}
