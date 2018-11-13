package net.dv8tion.jda.core.events.user.update;

import net.dv8tion.jda.core.entities.Activity;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.internal.JDAImpl;

import java.util.List;

public class UserUpdateActivityOrderEvent extends GenericUserPresenceEvent<List<Activity>> // TODO: Finish this
{
    public static final String IDENTIFIER = "activity_order";

    public UserUpdateActivityOrderEvent(JDAImpl api, long responseNumber, List<Activity> previous, Member member)
    {
        super(api, responseNumber, member.getUser(), member.getGuild(), previous, member.getActivities(), IDENTIFIER);
    }
}
