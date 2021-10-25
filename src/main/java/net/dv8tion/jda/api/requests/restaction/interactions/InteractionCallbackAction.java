package net.dv8tion.jda.api.requests.restaction.interactions;

import net.dv8tion.jda.api.interactions.ChannelInteraction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;

/**
 * A callback action is used to <b>acknowledge</b> a {@link net.dv8tion.jda.api.interactions.Interaction ChannelInteraction}.
 */
public interface InteractionCallbackAction extends RestAction<InteractionHook>
{
    /**
     * The possible types of interaction responses.
     * <br>This is currently only used internally to reduce interface complexity.
     */
    enum ResponseType
    {
        /** Immediately respond to an interaction with a message */
        CHANNEL_MESSAGE_WITH_SOURCE(4),
        /** Delayed or Deferred response to an interaction, this sends a "Thinking..." message to the channel */
        DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE(5),
        /** Defer the update of the message for a component interaction */
        DEFERRED_MESSAGE_UPDATE(6),
        /** Update the message for a component interaction */
        MESSAGE_UPDATE(7),
        /**
         * Update an application command's options
         */
        COMMAND_AUTOCOMPLETE_RESULT(8),
        ;
        private final int raw;

        ResponseType(int raw)
        {
            this.raw = raw;
        }

        /**
         * The raw integer key for this response type
         *
         * @return The raw key
         */
        public int getRaw()
        {
            return raw;
        }
    }
}
