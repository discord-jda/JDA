package net.dv8tion.jda.internal.interactions.commandInteractionImpls;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.commandInteractions.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.commandInteractions.SlashCommandInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.UserImpl;
import net.dv8tion.jda.internal.interactions.InteractionImpl;

import javax.annotation.Nonnull;
import java.util.Objects;

public class CommandInteractionImpl extends InteractionImpl implements CommandInteraction
{
    protected final long commandId;
    protected final TLongObjectMap<Object> resolved = new TLongObjectHashMap<>();
    protected final String name;

    public CommandInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
        DataObject commandData = data.getObject("data");
        this.commandId = commandData.getUnsignedLong("id");
        this.name = commandData.getString("name");
        DataObject resolveJson = commandData.optObject("resolved").orElseGet(DataObject::empty);

        parseResolved(jda, resolveJson);
    }

    private void parseResolved(JDAImpl jda, DataObject resolveJson)
    {
        EntityBuilder entityBuilder = jda.getEntityBuilder();

        resolveJson.optObject("users").ifPresent(users ->
                users.keys().forEach(userId -> {
                    DataObject userJson = users.getObject(userId);
                    UserImpl userArg = entityBuilder.createUser(userJson);
                    resolved.put(userArg.getIdLong(), userArg);
                })
        );

        if (guild != null) // Technically these can function in DMs too ...
        {
            resolveJson.optObject("members").ifPresent(members ->
                    members.keys().forEach(memberId -> {
                        DataObject userJson = resolveJson.getObject("users").getObject(memberId);
                        DataObject memberJson = members.getObject(memberId);
                        memberJson.put("user", userJson);
                        MemberImpl optionMember = entityBuilder.createMember((GuildImpl) guild, memberJson);
                        entityBuilder.updateMemberCache(optionMember);
                        resolved.put(optionMember.getIdLong(), optionMember); // This basically upgrades user to member
                    })
            );
            resolveJson.optObject("roles").ifPresent(roles ->
                    roles.keys()
                            .stream()
                            .map(guild::getRoleById)
                            .filter(Objects::nonNull)
                            .forEach(role -> resolved.put(role.getIdLong(), role))
            );
            resolveJson.optObject("channels").ifPresent(channels -> {
                channels.keys().forEach(id -> {
                    ISnowflake channelObj = jda.getGuildChannelById(id);
                    if (channelObj != null)
                        resolved.put(channelObj.getIdLong(), channelObj);
                });
            });
            resolveJson.optObject("messages").ifPresent(message ->
                    message.keys().forEach(messageId -> {
                        DataObject messageJson = message.getObject(messageId);
                        Message messageArg = entityBuilder.createMessage(messageJson);
                        resolved.put(messageArg.getIdLong(), messageArg);
                    })
            );
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public MessageChannel getChannel()
    {
        return (MessageChannel) super.getChannel();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public long getCommandIdLong()
    {
        return commandId;
    }
}
