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
    private final Game updatedGame;

    public GuildMemberGameUpdateEvent(JDA api, long responseNumber, Guild guild, Member member, Game oldGame, Game newGame) {
        super(api, responseNumber, guild, member);
        previousGame = oldGame;
        updatedGame = newGame;
    }

    public Game getPreviousGame() {
        return previousGame;
    }


    /**
     *
     * @return the game of which the member's presence had updated to.
     */
    public Game getUpdatedGame() {
        return updatedGame;
    }
}
