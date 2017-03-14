package net.dv8tion.jda.core.events.guild.member;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

/**
 * <b><u>UserGameUpdateEvent</u></b><br>
 * Fired if the {@link net.dv8tion.jda.core.entities.Game Game} of a {@link net.dv8tion.jda.core.entities.Member User} changes.<br>
 * <br>
 * Use: Retrieve the Member whose Game is changed and their previous Game.
 */
public class GuildMemberGameUpdateEvent extends GenericGuildMemberEvent {

    private final Game previousGame;

    public GuildMemberGameUpdateEvent(JDA api, long responseNumber, Guild guild, Member member, Game oldGame) {
        super(api, responseNumber, guild, member);
        previousGame = oldGame;
    }

    public Game getPreviousGame() {
        return previousGame;
    }

}
