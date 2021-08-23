package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.MessageCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.UserCommandInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MessageCommandInteractionImpl extends CommandInteractionImpl implements MessageCommandInteraction
{
    private Message targetMessage;

    public MessageCommandInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
    }

    @Override
    protected void parseResolved(JDAImpl jda, DataObject resolveJson)
    {
        EntityBuilder entityBuilder = jda.getEntityBuilder();

        resolveJson.optObject("messages").ifPresent(messages ->
                messages.keys().forEach(messageId -> {
                    DataObject messageJson = messages.getObject(messageId);
                    targetMessage = entityBuilder.createMessage(messageJson); // Technically this can be only one message
                })
        );
    }

    @Nonnull
    @Override
    public Message getTargetMessage()
    {
        return targetMessage;
    }
}
