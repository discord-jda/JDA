package net.dv8tion.jda.api.entities.subscription;

import javax.annotation.Nonnull;

/**
 * Representation of a Discord Subscription Status
 */
public enum SubscriptionStatus
{
    UNKNOWN(-1),ACTIVE(0), ENDING(1), INACTIVE(2);

    private final int id;

    SubscriptionStatus(int id){
        this.id = id;
    }

    @Nonnull
    public static SubscriptionStatus fromKey(int id){
        for (SubscriptionStatus status : values()){
            if(status.id == id){
                return status;
            }
        }
        return UNKNOWN;
    }

    public int getId(){
        return id;
    }
}
