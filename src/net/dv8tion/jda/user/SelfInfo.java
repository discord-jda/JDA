package net.dv8tion.jda.user;

import java.util.List;

public interface SelfInfo extends User
{
    /**
     * Returns the email of the connected account.
     * @return
     */
    public String getEmail();

    /**
     * A list of Discord Ids of Channels that have been muted on this account.
     * @return
     */
    public List<String> getMutedChannelIds();

    /**
     * The status of this account's verification.
     * (Have you accepted the verification email)
     * @return
     */
    public boolean isVerified();
}
