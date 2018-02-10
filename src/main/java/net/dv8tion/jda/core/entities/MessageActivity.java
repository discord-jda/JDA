/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.utils.Checks;

import javax.annotation.Nullable;

/**
 * Represents a {@link net.dv8tion.jda.core.entities.Message} activity.
 */
public class MessageActivity
{
    private final ActivityType type;
    private final String partyId;
    private final Application application;
    private String sessionId = null;

    /**
     *
     * @param  type the {@link net.dv8tion.jda.core.entities.MessageActivity.ActivityType}
     *         for the {@link MessageActivity}.
     *
     * @param  partyId the partyId for the {@link MessageActivity}.
     *
     * @param  application the {@link net.dv8tion.jda.core.entities.MessageActivity.Application}
     *         for the {@link MessageActivity}.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If {@link net.dv8tion.jda.core.entities.MessageActivity.ActivityType ActivityType} requires a not null {@link net.dv8tion.jda.core.entities.MessageActivity.Application Application}.</li>
     *             <li>If {@link net.dv8tion.jda.core.entities.MessageActivity.ActivityType ActivityType} gets a not required not null {@link net.dv8tion.jda.core.entities.MessageActivity.Application Application}.</li>
     *         </ul>
     */
    public MessageActivity(ActivityType type, String partyId, Application application)
    {
        this.type = type;
        this.partyId = partyId;
        Checks.check(!(type != ActivityType.LISTENING && application == null), "Either the ActivityType is wrong or the Application is null!");
        this.application = application;
    }

    /**
     *
     * @param  type the {@link net.dv8tion.jda.core.entities.MessageActivity.ActivityType}
     *         for the {@link MessageActivity}.
     *
     * @param  partyId the partyId for the {@link net.dv8tion.jda.core.entities.MessageActivity}.
     *
     * @param  sessionId the send-able sessionId for the {@link net.dv8tion.jda.core.entities.MessageActivity}.
     *
     * @param  application the {@link net.dv8tion.jda.core.entities.MessageActivity.Application}
     *         for the {@link MessageActivity}.
     *
     * @throws java.lang.IllegalArgumentException
     *         if the arguments match following expressions:
     *         <ul>
     *             <li>{@link net.dv8tion.jda.core.entities.MessageActivity.ActivityType ActivityType} requires a not null {@link net.dv8tion.jda.core.entities.MessageActivity.Application Application}.</li>
     *             <li>{@link net.dv8tion.jda.core.entities.MessageActivity.ActivityType ActivityType} gets a not required not null {@link net.dv8tion.jda.core.entities.MessageActivity.Application Application}.</li>
     *             <li>The {@code sessionId} is {@code null}.</li>
     *             <li>The {@code sessionId} {@link java.lang.String#matches(String) sessionId#matches()} an party id.</li>
     *         </ul>
     */
    public MessageActivity(ActivityType type, String partyId, String sessionId, Application application)
    {
        this(type, partyId, application);
        Checks.notBlank(sessionId, "The provided sessionId");
        Checks.check(!sessionId.matches("(spotify:\\d{17,20}|\\d{4,5}\\|[0-9a-f]{32})"),
            "The provided session id may not be a party id. Provided string: %s", sessionId);
        this.sessionId = sessionId;
    }

    /**
     * The current {@link net.dv8tion.jda.core.entities.MessageActivity.ActivityType ActivityType}
     *
     * @return the type of the activity.
     */
    public ActivityType getType()
    {
        return type;
    }

    /**
     * The party id discord uses internally, it may be {@code null}.
     *
     * @return the parties id.
     */
    @Nullable public String getPartyId()
    {
        return partyId;
    }

    /**
     * The session id that is required for sending the {@link net.dv8tion.jda.core.entities.MessageActivity MessageActivity}.
     * <br>Its default value is {@code null}
     *
     * @return the session id and {@code null} if it is not set.
     */
    @Nullable public String getSessionId()
    {
        return sessionId;
    }

    /**
     * Sets the sessionId that allows us to make this {@link net.dv8tion.jda.core.entities.MessageActivity MessageActivity} reusable.
     *
     * @throws java.lang.IllegalArgumentException
     *         if the provided {@code sessionId} matches a spotify or osu! party id.
     *
     * @param  sessionId the sessionId that
     *
     * @return the current {@link net.dv8tion.jda.core.entities.MessageActivity MessageActivity} for reusing it.
     */
    public MessageActivity setSessionId(String sessionId)
    {
        Checks.notBlank(sessionId, "The provided sessionId");
        Checks.check(!sessionId.matches("(spotify:\\d{17,20}|\\d{4,5}\\|[0-9a-f]{32})"),
            "The provided session id may not be a party id. Provided string: %s", sessionId);
        this.sessionId = sessionId;
        return this;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.MessageActivity.Application Application} this {@link net.dv8tion.jda.core.entities.MessageActivity MessageActivity} may have.
     *
     * @return A possibly {@code null} {@link net.dv8tion.jda.core.entities.MessageActivity.Application}.
     */
    public MessageActivity.Application getApplication()
    {
        return application;
    }

    /**
     * Represents the {@link net.dv8tion.jda.core.entities.Message} application if the
     * {@link net.dv8tion.jda.core.entities.MessageActivity.ActivityType ActivityType} of the
     * {@link net.dv8tion.jda.core.entities.MessageActivity MessageActivity} not equals
     * {@link net.dv8tion.jda.core.entities.MessageActivity.ActivityType#LISTENING ActivityType.LISTENING}.
     *
     * <br>Many applications can be found at:
     * <a href="https://discordapp.com/api/v7/games" target="_blank">https://discordapp.com/api/v7/games</a>.
     */
    public static class Application
    {
        private final String name;
        private final String description;
        private final String iconId;
        private final String coverId;
        private final long id;

        public Application(String name, String description, String iconId, String coverId, long id)
        {
            this.name = name;
            this.description = description;
            this.iconId = iconId;
            this.coverId = coverId;
            this.id = id;
        }

        /**
         * The name of this {@link net.dv8tion.jda.core.entities.MessageActivity.Application}.
         *
         * @return the applications name.
         */
        public String getName()
        {
            return name;
        }

        /**
         * A short description of this {@link net.dv8tion.jda.core.entities.MessageActivity.Application}.
         *
         * @return the applications description.
         */
        public String getDescription()
        {
            return description;
        }

        /**
         * The icon id of this {@link net.dv8tion.jda.core.entities.MessageActivity.Application}.
         *
         * @return the applications icon id.
         */
        public String getIconId()
        {
            return iconId;
        }

        /**
         * The url of the icon image for this application.
         *
         * @return the url of the icon
         */
        public String getIconUrl()
        {
            return "https://cdn.discordapp.com/application/" + id + "/" + iconId + ".png";
        }

        /**
         * The cover aka splash id of this {@link net.dv8tion.jda.core.entities.MessageActivity.Application}.
         *
         * @return the applications cover image/id.
         */
        public String getCoverId()
        {
            return coverId;
        }

        /**
         * The url of the cover image for this application.
         *
         * @return the url of the cover/splash
         */
        public String getCoverUrl()
        {
            return "https://cdn.discordapp.com/application/" + id + "/" + coverId + ".png";
        }

        /**
         * The id of the current {@link net.dv8tion.jda.core.entities.MessageActivity.Application} as a {@link java.lang.String}.
         *
         * @return the applications id.
         */
        public String getId()
        {
            return Long.toUnsignedString(id);
        }

        /**
         * The id of the current {@link net.dv8tion.jda.core.entities.MessageActivity.Application}.
         *
         * @return the applications id.
         */
        public long getIdLong()
        {
            return id;
        }
    }

    /**
     * An enum containing {@link net.dv8tion.jda.core.entities.MessageActivity MessageActivity} types.
     */
    public enum ActivityType
    {
        /**
         * The {@link net.dv8tion.jda.core.entities.MessageActivity MessageActivity} type used for inviting people to join a game.
         */
        JOIN(1),
        /**
         * The {@link net.dv8tion.jda.core.entities.MessageActivity MessageActivity} type used for inviting people to spectate a game.
         */
        SPECTATE(2),
        /**
         * The {@link net.dv8tion.jda.core.entities.MessageActivity MessageActivity} type used for inviting people to listen Spotify together.
         */
        LISTENING(3),
        /**
         * The {@link net.dv8tion.jda.core.entities.MessageActivity MessageActivity} type used for requesting to join a game.
         */
        JOIN_REQUEST(5),
        /**
         * Unknown Discord {@link net.dv8tion.jda.core.entities.MessageActivity MessageActivity} type. Should never happen and would only possibly happen if Discord implemented a new
         * {@link net.dv8tion.jda.core.entities.MessageActivity MessageActivity} type and JDA had yet to implement support for it.
         */
        UNKNOWN(-1);

        private final int id;

        ActivityType(int id)
        {
            this.id = id;
        }

        /**
         * The id of this {@link net.dv8tion.jda.core.entities.MessageActivity.ActivityType ActivityType}.
         *
         * @return the id of the type.
         */
        public int getId()
        {
            return id;
        }

        public static ActivityType fromId(int id)
        {
            for (ActivityType activityType : values()) {
                if (activityType.id == id)
                    return activityType;
            }
            return UNKNOWN;
        }
    }
}
