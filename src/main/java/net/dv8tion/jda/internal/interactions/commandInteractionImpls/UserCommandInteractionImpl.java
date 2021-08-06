package net.dv8tion.jda.internal.interactions.commandInteractionImpls;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.commandInteractions.UserCommandInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.UserImpl;

import java.util.Arrays;
import java.util.stream.Collectors;

public class UserCommandInteractionImpl extends CommandInteractionImpl implements UserCommandInteraction
{
    protected final long targetID;
    protected Member targetMember;

    public UserCommandInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
        DataObject commandData = data.getObject("data");

        this.targetID = commandData.getLong("target_id");
        this.targetMember =
                (MemberImpl) Arrays.stream(resolved.values()).filter(value -> value instanceof MemberImpl).collect(Collectors.toList()).get(0);
    }


    @Override
    public long getTargetIdLong()
    {
        return targetID;
    }

    @Override
    public User getTargetUser()
    {
        return targetMember.getUser();
    }

    @Override
    public Member getTargetMember()
    {
        return targetMember;
    }
}
