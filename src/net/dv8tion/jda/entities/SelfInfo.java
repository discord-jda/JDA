package net.dv8tion.jda.entities;

import java.util.List;

public interface SelfInfo extends User
{
    /**
     * Returns the email of the connected account.
     * @return
     */
    String getEmail();

    /**
     * A list of Discord Ids of Channels that have been muted on this account.
     * @return
     */
    List<TextChannel> getMutedChannels();

    /**
     * The status of this account's verification.
     * (Have you accepted the verification email)
     * @return
     */
    boolean isVerified();
}
