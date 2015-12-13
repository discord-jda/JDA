package net.dv8tion.jda.entities;

/**
 * Created by Michael Ritter on 13.12.2015.
 */
public interface PrivateChannel
{
    /**
     * The Id of the Channel. This is typically 18 characters long.
     * @return
     */
    String getId();

    /**
     * The User that this PrivateChannel communicates with.
     * @return
     */
    User getUser();
}
