package net.dv8tion.jda.test.entities.scheduledevent;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.ScheduledEventImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ScheduledEventTest{

    @Test
    void testGetJumpUrl(){

        long guildId = 7777777;
        long eventId = 333;

        String expected = "https://discord.com/events/" + guildId + "/" + eventId;

        Guild guild = new GuildImpl(new JDAImpl(null), guildId);

        ScheduledEvent scheduledEvent = new ScheduledEventImpl(eventId, guild);

        Assertions.assertEquals(expected, scheduledEvent.getJumpUrl());

    }

}
