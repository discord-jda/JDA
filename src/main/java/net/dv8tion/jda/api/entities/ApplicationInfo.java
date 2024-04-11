/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Represents a Discord Application from its bot's point of view.
 * 
 * @since  3.0
 * @author Aljoscha Grebe
 * 
 * @see    net.dv8tion.jda.api.JDA#retrieveApplicationInfo()
 */
public interface ApplicationInfo extends ISnowflake
{
    /**
     * Whether the bot requires code grant to invite or not. 
     * 
     * <p>This means that additional OAuth2 steps are required to authorize the application to make a bot join a guild 
     * like {@code &response_type=code} together with a valid {@code &redirect_uri}. 
     * <br>For more information look at the <a href="https://discord.com/developers/docs/topics/oauth2">Discord OAuth2 documentation</a>.
     * 
     * @return Whether the bot requires code grant
     */
    boolean doesBotRequireCodeGrant();

    /**
     * The description of the bot's application.
     * 
     * @return The description of the bot's application or an empty {@link String} if no description is defined
     */
    @Nonnull
    String getDescription();

    /**
     * The URL for the application's terms of service.
     *
     * @return The URL for the application's terms of service or {@code null} if none is set
     */
    @Nullable
    String getTermsOfServiceUrl();

    /**
     * The URL for the application's privacy policy.
     *
     * @return The URL for the application's privacy policy or {@code null} if none is set
     */
    @Nullable
    String getPrivacyPolicyUrl();

    /**
     * The icon id of the bot's application.
     * <br>The application icon is <b>not</b> necessarily the same as the bot's avatar!
     * 
     * @return The icon id of the bot's application or null if no icon is defined
     */
    @Nullable
    String getIconId();

    /**
     * The icon-url of the bot's application.
     * <br>The application icon is <b>not</b> necessarily the same as the bot's avatar!
     * 
     * @return The icon-url of the bot's application or null if no icon is defined
     */
    @Nullable
    String getIconUrl();

    /**
     * Returns an {@link ImageProxy} for this application info's icon.
     *
     * @return The {@link ImageProxy} of this application info's icon or null if no icon is defined
     *
     * @see    #getIconUrl()
     */
    @Nullable
    default ImageProxy getIcon()
    {
        final String iconUrl = getIconUrl();
        return iconUrl == null ? null : new ImageProxy(iconUrl);
    }

    /**
     * The team information for this application.
     *
     * @return The {@link net.dv8tion.jda.api.entities.ApplicationTeam}, or null if this application is not in a team.
     */
    @Nullable
    ApplicationTeam getTeam();

    /**
     * Configures the required scopes applied to the {@link #getInviteUrl(Permission...)} and similar methods.
     * <br>The scope {@code "bot"} is always applied.
     *
     * @param  scopes
     *         The scopes to use with {@link #getInviteUrl(Permission...)} and the likes
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The current ApplicationInfo instance
     */
    @Nonnull
    default ApplicationInfo setRequiredScopes(@Nonnull String... scopes)
    {
        Checks.noneNull(scopes, "Scopes");
        return setRequiredScopes(Arrays.asList(scopes));
    }

    /**
     * Configures the required scopes applied to the {@link #getInviteUrl(Permission...)} and similar methods.
     * <br>The scope {@code "bot"} is always applied.
     *
     * @param  scopes
     *         The scopes to use with {@link #getInviteUrl(Permission...)} and the likes
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The current ApplicationInfo instance
     */
    @Nonnull
    ApplicationInfo setRequiredScopes(@Nonnull Collection<String> scopes);

    /**
     * Creates a OAuth invite-link used to invite the bot.
     * 
     * <p>The link is provided in the following format:
     * <br>{@code https://discord.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS}
     * <br>Unnecessary query parameters are stripped.
     *
     * @param  permissions
     *         Possibly empty {@link java.util.Collection Collection} of {@link net.dv8tion.jda.api.Permission Permissions}
     *         that should be requested via invite.
     * 
     * @return The link used to invite the bot
     */
    @Nonnull
    default String getInviteUrl(@Nullable Collection<Permission> permissions)
    {
        return getInviteUrl(null, permissions);
    }

    /**
     * Creates a OAuth invite-link used to invite the bot.
     * 
     * <p>The link is provided in the following format:
     * <br>{@code https://discord.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS}
     * <br>Unnecessary query parameters are stripped.
     * 
     * @param  permissions
     *         {@link net.dv8tion.jda.api.Permission Permissions} that should be requested via invite.
     * 
     * @return The link used to invite the bot
     */
    @Nonnull
    default String getInviteUrl(@Nullable Permission... permissions)
    {
        return getInviteUrl(null, permissions);
    }

    /**
     * Creates a OAuth invite-link used to invite the bot.
     * 
     * <p>The link is provided in the following format:
     * <br>{@code https://discord.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS&guild_id=GUILD_ID}
     * <br>Unnecessary query parameters are stripped.
     * 
     * @param  guildId
     *         The id of the pre-selected guild.
     * @param  permissions
     *         Possibly empty {@link java.util.Collection Collection} of {@link net.dv8tion.jda.api.Permission Permissions}
     *         that should be requested via invite.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * 
     * @return The link used to invite the bot
     */
    @Nonnull
    String getInviteUrl(@Nullable String guildId, @Nullable Collection<Permission> permissions);

    /**
     * Creates a OAuth invite-link used to invite the bot.
     *
     * <p>The link is provided in the following format:
     * <br>{@code https://discord.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS&guild_id=GUILD_ID}
     * <br>Unnecessary query parameters are stripped.
     *
     * @param  guildId
     *         The id of the pre-selected guild.
     * @param  permissions
     *         Possibly empty {@link java.util.Collection Collection} of {@link net.dv8tion.jda.api.Permission Permissions}
     *         that should be requested via invite.
     *
     * @return The link used to invite the bot
     */
    @Nonnull
    default String getInviteUrl(long guildId, @Nullable Collection<Permission> permissions)
    {
        return getInviteUrl(Long.toUnsignedString(guildId), permissions);
    }

    /**
     * Creates a OAuth invite-link used to invite the bot.
     * 
     * <p>The link is provided in the following format:
     * <br>{@code https://discord.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS&guild_id=GUILD_ID}
     * <br>Unnecessary query parameters are stripped.
     * 
     * @param  guildId 
     *         The id of the pre-selected guild.
     * @param  permissions 
     *         Possibly empty array of {@link net.dv8tion.jda.api.Permission Permissions}
     *         that should be requested via invite.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * 
     * @return The link used to invite the bot
     */
    @Nonnull
    default String getInviteUrl(@Nullable String guildId, @Nullable Permission... permissions)
    {
        return getInviteUrl(guildId, permissions == null ? null : Arrays.asList(permissions));
    }

    /**
     * Creates a OAuth invite-link used to invite the bot.
     *
     * <p>The link is provided in the following format:
     * <br>{@code https://discord.com/oauth2/authorize?client_id=APPLICATION_ID&scope=bot&permissions=PERMISSIONS&guild_id=GUILD_ID}
     * <br>Unnecessary query parameters are stripped.
     *
     * @param  guildId
     *         The id of the pre-selected guild.
     * @param  permissions
     *         Possibly empty array of {@link net.dv8tion.jda.api.Permission Permissions}
     *         that should be requested via invite.
     *
     * @return The link used to invite the bot
     */
    @Nonnull
    default String getInviteUrl(long guildId, @Nullable Permission... permissions)
    {
        return getInviteUrl(Long.toUnsignedString(guildId), permissions);
    }

    /**
     * The {@link net.dv8tion.jda.api.JDA JDA} instance of this ApplicationInfo
     * (the one logged into this application's bot account).
     * 
     * @return The JDA instance of this ApplicationInfo
     */
    @Nonnull
    JDA getJDA();

    /**
     * The name of the bot's application.
     * <br>The application name is <b>not</b> necessarily the same as the bot's name!
     * 
     * @return The name of the bot's application.
     */
    @Nonnull
    String getName();

    /**
     * The owner of the bot's application.
     * 
     * @return The owner of the bot's application
     */
    @Nonnull
    User getOwner();

    /**
     * Whether the bot is public or not. 
     * Public bots can be added by anyone. When false only the owner can invite the bot to guilds.
     * 
     * @return Whether the bot is public
     */
    boolean isBotPublic();

    /**
     * A {@link java.util.List} containing the tags of this bot's application.
     *
     * <p>This List is empty if no tags are set in the <a href="https://discord.com/developers/applications" target="_blank">Developer Portal</a>.
     *
     * @return Immutable list containing the tags of this bot's application
     */
    @Nonnull
    @Unmodifiable
    List<String> getTags();

    /**
     * A {@link java.util.List} containing the OAuth2 redirect URIs of this bot's application.
     *
     * <p>This List is empty if no redirect URIs are set in the <a href="https://discord.com/developers/applications" target="_blank">Developer Portal</a>.
     *
     * @return Immutable list containing the OAuth2 redirect URIs of this bot's application
     */
    @Nonnull
    @Unmodifiable
    List<String> getRedirectUris();

    /**
     * The interaction endpoint URL of this bot's application.
     *
     * <p>This returns {@code null} if no interaction endpoint URL is set in the <a href="https://discord.com/developers/applications" target="_blank">Developer Portal</a>.
     *
     * <p>A non-null value means your bot will no longer receive {@link net.dv8tion.jda.api.interactions.Interaction interactions}
     * through JDA, such as slash commands, components and modals.
     *
     * @return Interaction endpoint URL of this bot's application, or {@code null} if it has not been set
     */
    @Nullable
    String getInteractionsEndpointUrl();

    /**
     * The role connections (linked roles) verification URL of this bot's application.
     *
     * <p>This returns {@code null} if no role connection verification URL is set in the <a href="https://discord.com/developers/applications" target="_blank">Developer Portal</a>.
     *
     * @return Role connections verification URL of this bot's application, or {@code null} if it has not been set
     */
    @Nullable
    String getRoleConnectionsVerificationUrl();

    /**
     * The custom Authorization URL of this bot's application.
     *
     * <p>This returns {@code null} if no custom URL is set in the <a href="https://discord.com/developers/applications" target="_blank">Developer Portal</a> or if In-app Authorization is enabled.
     *
     * @return Custom Authorization URL, or null if it has not been set
     */
    @Nullable
    String getCustomAuthorizationUrl();

    /**
     * A {@link java.util.List} of scopes the default authorization URL is set up with.
     *
     * <p>This List is empty if you set a custom URL in the <a href="https://discord.com/developers/applications" target="_blank">Developer Portal</a>.
     *
     * @return Immutable list of scopes the default authorization URL is set up with.
     */
    @Nonnull
    @Unmodifiable
    List<String> getScopes();

    /**
     * An {@link java.util.EnumSet} of permissions the default authorization URL is set up with.
     *
     * <p>This is empty if you set a custom URL in the <a href="https://discord.com/developers/applications" target="_blank">Developer Portal</a>.
     *
     * @return Set of permissions the default authorization URL is set up with.
     */
    @Nonnull
    EnumSet<Permission> getPermissions();

    /**
     * The {@code long} representation of the literal permissions the default authorization URL is set up with.
     *
     * @return Never-negative long containing offset permissions the default authorization URL is set up with.
     */
    long getPermissionsRaw();

    /**
     * The {@link Flag Flags} set for the application.
     * <br>Modifying the returned EnumSet will have not actually change the flags of the application.
     *
     * @return {@link EnumSet} of {@link Flag}
     */
    @Nonnull
    default EnumSet<Flag> getFlags()
    {
        return Flag.fromRaw(getFlagsRaw());
    }

    /**
     * The raw bitset representing this application's flags.
     *
     * @return The bitset
     */
    long getFlagsRaw();

    /**
     * The configurations for each {@link IntegrationType} set on the application.
     *
     * @return The configurations for each integration type
     */
    @Nonnull
    Map<IntegrationType, IntegrationTypeConfiguration> getIntegrationTypesConfig();

    /**
     * Configuration of a single {@link IntegrationType}.
     *
     * @see ApplicationInfo#getIntegrationTypesConfig()
     */
    interface IntegrationTypeConfiguration
    {
        /**
         * The OAuth2 install parameters for the default in-app authorization link.
         * <br>When a user invites your application in the Discord app, these will be the parameters of the invite url.
         *
         * @return The OAuth2 install parameters for the default in-app authorization link
         */
        @Nullable
        InstallParameters getInstallParameters();
    }

    /**
     * OAuth2 install parameter for the default in-app authorization link.
     *
     * @see IntegrationTypeConfiguration#getInstallParameters()
     */
    interface InstallParameters
    {
        /**
         * Gets the required scopes granted to the bot when invited.
         *
         * @return The required scopes granted to the bot when invited
         */
        @Nonnull
        List<String> getScopes();

        /**
         * Gets the permissions your bot asks for when invited.
         * <br><b>Note:</b> Users can choose to disable permissions before and after inviting your bot.
         *
         * @return The permissions your bot asks for when invited
         */
        @Nonnull
        Set<Permission> getPermissions();
    }

    /**
     * Flag constants corresponding to the <a href="https://discord.com/developers/docs/resources/application#application-object-application-flags" target="_blank">Discord Enum</a>
     *
     * @see #getFlags()
     */
    enum Flag
    {
        /** Bot can use {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_PRESENCES GatewayIntent.GUILD_PRESENCES} in 100 or more guilds */
        GATEWAY_PRESENCE(1 << 12),
        /** Bot can use {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_PRESENCES GatewayIntent.GUILD_PRESENCES} in under 100 guilds */
        GATEWAY_PRESENCE_LIMITED(1 << 13),
        /** Bot can use {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} in 100 or more guilds */
        GATEWAY_GUILD_MEMBERS(1 << 14),
        /** Bot can use {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GatewayIntent.GUILD_MEMBERS} in under 100 guilds */
        GATEWAY_GUILD_MEMBERS_LIMITED(1 << 15),
        /** Indicates unusual growth of an app that prevents verification */
        VERIFICATION_PENDING_GUILD_LIMIT(1 << 16),
        /** Indicates if an app is embedded within the Discord client (currently unavailable publicly) */
        EMBEDDED(1 << 17),
        /** Bot can use {@link net.dv8tion.jda.api.requests.GatewayIntent#MESSAGE_CONTENT GatewayIntent.MESSAGE_CONTENT} in 100 or more guilds */
        GATEWAY_MESSAGE_CONTENT(1 << 18),
        /** Bot can use {@link net.dv8tion.jda.api.requests.GatewayIntent#MESSAGE_CONTENT GatewayIntent.MESSAGE_CONTENT} in under 100 guilds */
        GATEWAY_MESSAGE_CONTENT_LIMITED(1 << 19),
        ;

        private final long value;

        Flag(long value)
        {
            this.value = value;
        }

        /**
         * Converts the provided bitset to the corresponding enum constants.
         *
         * @param  raw
         *         The bitset of flags
         *
         * @return {@link EnumSet} of {@link Flag}
         */
        @Nonnull
        public static EnumSet<Flag> fromRaw(long raw)
        {
            EnumSet<Flag> set = EnumSet.noneOf(Flag.class);
            for (Flag flag : values())
            {
                if ((raw & flag.value) != 0)
                    set.add(flag);
            }
            return set;
        }
    }
}
