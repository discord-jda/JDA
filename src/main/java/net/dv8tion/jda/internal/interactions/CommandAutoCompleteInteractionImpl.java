package net.dv8tion.jda.internal.interactions;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.interactions.ChoiceAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.UserImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.ChoiceActionImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CommandAutoCompleteInteractionImpl extends InteractionImpl implements CommandAutoCompleteInteraction
{
    private final long commandId;
    private final List<OptionMapping> options = new ArrayList<>();
    private final TLongObjectMap<Object> resolved = new TLongObjectHashMap<>();
    private final String name;
    private String subcommand;
    private String group;

    public CommandAutoCompleteInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
        DataObject commandData = data.getObject("data");
        this.commandId = commandData.getUnsignedLong("id");
        this.name = commandData.getString("name");

        DataArray options = commandData.optArray("options").orElseGet(DataArray::empty);
        DataObject resolveJson = commandData.optObject("resolved").orElseGet(DataObject::empty);

        if (options.length() == 1)
        {
            DataObject option = options.getObject(0);
            switch (OptionType.fromKey(option.getInt("type")))
            {
            case SUB_COMMAND_GROUP:
                group = option.getString("name");
                options = option.getArray("options");
                option = options.getObject(0);
            case SUB_COMMAND:
                subcommand = option.getString("name");
                options = option.optArray("options").orElseGet(DataArray::empty); // Flatten options
                break;
            }
        }

        parseResolved(jda, resolveJson);
        parseOptions(options);
    }

    private void parseOptions(DataArray options)
    {
        options.stream(DataArray::getObject)
                .map(json -> new OptionMapping(json, resolved))
                .forEach(this.options::add);
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
        }
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getSubcommandName()
    {
        return subcommand;
    }

    @Override
    public String getSubcommandGroup()
    {
        return group;
    }

    @Override
    public long getCommandIdLong()
    {
        return commandId;
    }

    @Nonnull
    @Override
    public List<OptionMapping> getOptions()
    {
        return options;
    }

    @Nonnull
    public ChoiceAction respondChoice(@Nonnull String name, @Nonnull String value)
    {
        return new ChoiceActionImpl(this.hook, getFocusedOption().getType()).respondChoice(name, value);
    }

    @Nonnull
    public ChoiceAction respondChoice(@Nonnull String name, double value)
    {
        return new ChoiceActionImpl(this.hook, getFocusedOption().getType()).respondChoice(name, value);
    }

    @Nonnull
    public ChoiceAction respondChoice(@Nonnull String name, long value)
    {
        return new ChoiceActionImpl(this.hook, getFocusedOption().getType()).respondChoice(name, value);
    }

    @NotNull
    @Override
    public ChoiceAction respondChoices(@Nonnull Command.Choice... choices)
    {
        return new ChoiceActionImpl(this.hook, getFocusedOption().getType()).respondChoices(choices);
    }

    @NotNull
    @Override
    public ChoiceAction respondChoices(@Nonnull Collection<? extends Command.Choice> choices)
    {
        return new ChoiceActionImpl(this.hook, getFocusedOption().getType()).respondChoices(choices);
    }

    @Override
    @Nonnull
    public OptionMapping getFocusedOption()
    {
        return getOptions().stream()
                .filter(OptionMapping::isFocused)
                .findFirst()
                .get();
    }
}
