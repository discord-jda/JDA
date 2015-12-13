package net.dv8tion.jda.entities;

/**
 * Created by Michael Ritter on 13.12.2015.
 */
public interface PrivateChannel extends Channel
{
    /**
     * The User that this PrivateChannel communicates with.
     * @return
     */
    User getUser();
}
