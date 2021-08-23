package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.UserCommandInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UserCommandInteractionImpl extends CommandInteractionImpl implements UserCommandInteraction
{
    private User targetUser;
    private Member targetMember;

    public UserCommandInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
    }

    @Override
    protected void parseResolved(JDAImpl jda, DataObject resolveJson)
    {
        EntityBuilder entityBuilder = jda.getEntityBuilder();

        resolveJson.optObject("users").ifPresent(users ->
                users.keys().forEach(userId -> {
                    DataObject userJson = users.getObject(userId);
                    targetUser = entityBuilder.createUser(userJson); // Technically this can be only one user
                })
        );

        if (guild != null)
        {
            resolveJson.optObject("members").ifPresent(members ->
                    members.keys().forEach(memberId ->
                    {
                        DataObject userJson = resolveJson.getObject("users").getObject(memberId);
                        DataObject memberJson = members.getObject(memberId);
                        memberJson.put("user", userJson);
                        MemberImpl optionMember = entityBuilder.createMember((GuildImpl) guild, memberJson);
                        entityBuilder.updateMemberCache(optionMember);
                        targetMember = optionMember; // Technically this can be also only one member
                    })
            );
        }
    }

    @Override
    @Nonnull
    public User getTargetUser()
    {
        return targetUser;
    }

    @Override
    @Nullable
    public Member getTargetMember()
    {
        return targetMember;
    }
}
