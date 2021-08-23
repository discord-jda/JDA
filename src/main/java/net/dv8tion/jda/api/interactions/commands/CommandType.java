package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;

public enum CommandType
{
    UNKNOWN(-1),
    /**
     *  Slash commands; a text-based command that shows up when a user types /
     */
    CHAT_INPUT(1) {
        @Override
        public Command create(JDAImpl api, Guild guild, DataObject json)
        {
            return new SlashCommand(api, guild, json);
        }
    },
    /**
     * 	A UI-based command that shows up when you right click or tap on a user
     */
    USER(2) {
        @Override
        public Command create(JDAImpl api, Guild guild, DataObject json)
        {
            return new UserCommand(api, guild, json);
        }
    },
    /**
     * 	A UI-based command that shows up when you right click or tap on a message
     */
    MESSAGE(3) {
        @Override
        public Command create(JDAImpl api, Guild guild, DataObject json)
        {
            return new MessageCommand(api, guild, json);
        }
    }
    ;

    private final int key;

    CommandType(int key)
    {
        this.key = key;
    }


    /**
     * The raw value for this type or -1 for {@link #UNKNOWN}
     *
     * @return The raw value
     */
    public int getKey()
    {
        return key;
    }

    public Command create(JDAImpl api, Guild guild, DataObject json) {
        return null;
    }

    /**
     * Maps the provided type id to the respective enum instance.
     *
     * @param  type
     *         The raw type id
     *
     * @return The Type or {@link #UNKNOWN}
     */
    @Nonnull
    public static CommandType fromKey(int type)
    {
        for (CommandType t : values())
        {
            if (t.key == type)
                return t;
        }
        return UNKNOWN;
    }
}
