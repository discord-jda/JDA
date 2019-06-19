package net.dv8tion.jda.api.entities;

import javax.annotation.Nonnull;

public interface TeamMember
{
    @Nonnull
    User getUser();

    @Nonnull
    MembershipState getMembershipState();

    @Nonnull
    default String getTeamId()
    {
        return Long.toUnsignedString(getTeamIdLong());
    }

    long getTeamIdLong();

    enum MembershipState
    {
        INVITED(1),
        ACCEPTED(2),
        UNKNOWN(-1);

        private final int key;

        MembershipState(int key)
        {
            this.key = key;
        }

        public int getKey()
        {
            return key;
        }

        public static MembershipState fromKey(int key)
        {
            for (MembershipState state : values())
            {
                if (state.key == key)
                    return state;
            }
            return UNKNOWN;
        }
    }
}
