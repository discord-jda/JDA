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
package net.dv8tion.jda.api.requests

import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.EncodingUtil
import net.dv8tion.jda.internal.utils.EntityString
import net.dv8tion.jda.internal.utils.Helpers
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Routes for API endpoints.
 */
@Suppress("unused")
class Route private constructor(
    /**
     * The [Method] of this route template.
     * <br></br>Multiple routes with different HTTP methods can share a rate-limit.
     *
     * @return The HTTP method
     */
    @get:Nonnull val method: Method, route: String,
    /**
     * Whether this route is a route related to interactions.
     * <br></br>Interactions have some special handling, since they are exempt from global rate-limits and are limited to 15 minute uptime.
     *
     * @return True, if this route is for interactions
     */
    val isInteractionBucket: Boolean = false
) {
    object Misc {
        val GET_VOICE_REGIONS = Route(Method.GET, "voice/regions")
        val GATEWAY = Route(Method.GET, "gateway")
        @JvmField
        val GATEWAY_BOT = Route(Method.GET, "gateway/bot")
    }

    object Applications {
        @JvmField
        val GET_BOT_APPLICATION = Route(Method.GET, "oauth2/applications/@me")
        @JvmField
        val GET_ROLE_CONNECTION_METADATA = Route(Method.GET, "applications/{application_id}/role-connections/metadata")
        @JvmField
        val UPDATE_ROLE_CONNECTION_METADATA =
            Route(Method.PUT, "applications/{application_id}/role-connections/metadata")
        @JvmField
        val GET_ENTITLEMENTS = Route(Method.GET, "applications/{application_id}/entitlements")
    }

    object Interactions {
        @JvmField
        val GET_COMMANDS = Route(Method.GET, "applications/{application_id}/commands")
        @JvmField
        val GET_COMMAND = Route(Method.GET, "applications/{application_id}/commands/{command_id}")
        @JvmField
        val CREATE_COMMAND = Route(Method.POST, "applications/{application_id}/commands")
        @JvmField
        val UPDATE_COMMANDS = Route(Method.PUT, "applications/{application_id}/commands")
        @JvmField
        val EDIT_COMMAND = Route(Method.PATCH, "applications/{application_id}/commands/{command_id}")
        @JvmField
        val DELETE_COMMAND = Route(Method.DELETE, "applications/{application_id}/commands/{command_id}")
        @JvmField
        val GET_GUILD_COMMANDS = Route(Method.GET, "applications/{application_id}/guilds/{guild_id}/commands")
        @JvmField
        val GET_GUILD_COMMAND =
            Route(Method.GET, "applications/{application_id}/guilds/{guild_id}/commands/{command_id}")
        @JvmField
        val CREATE_GUILD_COMMAND = Route(Method.POST, "applications/{application_id}/guilds/{guild_id}/commands")
        @JvmField
        val UPDATE_GUILD_COMMANDS = Route(Method.PUT, "applications/{application_id}/guilds/{guild_id}/commands")
        @JvmField
        val EDIT_GUILD_COMMAND =
            Route(Method.PATCH, "applications/{application_id}/guilds/{guild_id}/commands/{command_id}")
        @JvmField
        val DELETE_GUILD_COMMAND =
            Route(Method.DELETE, "applications/{application_id}/guilds/{guild_id}/commands/{command_id}")
        @JvmField
        val GET_ALL_COMMAND_PERMISSIONS =
            Route(Method.GET, "applications/{application_id}/guilds/{guild_id}/commands/permissions")
        val EDIT_ALL_COMMAND_PERMISSIONS =
            Route(Method.PUT, "applications/{application_id}/guilds/{guild_id}/commands/permissions")
        @JvmField
        val GET_COMMAND_PERMISSIONS =
            Route(Method.GET, "applications/{application_id}/guilds/{guild_id}/commands/{command_id}/permissions")
        val EDIT_COMMAND_PERMISSIONS =
            Route(Method.PUT, "applications/{application_id}/guilds/{guild_id}/commands/{command_id}/permissions")
        @JvmField
        val CALLBACK = Route(Method.POST, "interactions/{interaction_id}/{interaction_token}/callback", true)
        @JvmField
        val CREATE_FOLLOWUP = Route(Method.POST, "webhooks/{application_id}/{interaction_token}", true)
        @JvmField
        val EDIT_FOLLOWUP =
            Route(Method.PATCH, "webhooks/{application_id}/{interaction_token}/messages/{message_id}", true)
        @JvmField
        val DELETE_FOLLOWUP =
            Route(Method.DELETE, "webhooks/{application_id}/{interaction_token}/messages/{message_id}", true)
        @JvmField
        val GET_MESSAGE = Route(Method.GET, "webhooks/{application_id}/{interaction_token}/messages/{message_id}", true)
    }

    object Self {
        @JvmField
        val GET_SELF = Route(Method.GET, "users/@me")
        @JvmField
        val MODIFY_SELF = Route(Method.PATCH, "users/@me")
        val GET_GUILDS = Route(Method.GET, "users/@me/guilds")
        @JvmField
        val LEAVE_GUILD = Route(Method.DELETE, "users/@me/guilds/{guild_id}")
        val GET_PRIVATE_CHANNELS = Route(Method.GET, "users/@me/channels")
        @JvmField
        val CREATE_PRIVATE_CHANNEL = Route(Method.POST, "users/@me/channels")
    }

    object Users {
        @JvmField
        val GET_USER = Route(Method.GET, "users/{user_id}")
    }

    object Guilds {
        @JvmField
        val GET_GUILD = Route(Method.GET, "guilds/{guild_id}")
        @JvmField
        val MODIFY_GUILD = Route(Method.PATCH, "guilds/{guild_id}")
        @JvmField
        val GET_VANITY_URL = Route(Method.GET, "guilds/{guild_id}/vanity-url")
        @JvmField
        val CREATE_CHANNEL = Route(Method.POST, "guilds/{guild_id}/channels")
        val GET_CHANNELS = Route(Method.GET, "guilds/{guild_id}/channels")
        @JvmField
        val MODIFY_CHANNELS = Route(Method.PATCH, "guilds/{guild_id}/channels")
        @JvmField
        val MODIFY_ROLES = Route(Method.PATCH, "guilds/{guild_id}/roles")
        @JvmField
        val GET_BANS = Route(Method.GET, "guilds/{guild_id}/bans")
        @JvmField
        val GET_BAN = Route(Method.GET, "guilds/{guild_id}/bans/{user_id}")
        @JvmField
        val UNBAN = Route(Method.DELETE, "guilds/{guild_id}/bans/{user_id}")
        @JvmField
        val BAN = Route(Method.PUT, "guilds/{guild_id}/bans/{user_id}")
        @JvmField
        val BULK_BAN = Route(Method.POST, "guilds/{guild_id}/bulk-ban")
        @JvmField
        val KICK_MEMBER = Route(Method.DELETE, "guilds/{guild_id}/members/{user_id}")
        @JvmField
        val MODIFY_MEMBER = Route(Method.PATCH, "guilds/{guild_id}/members/{user_id}")
        @JvmField
        val ADD_MEMBER = Route(Method.PUT, "guilds/{guild_id}/members/{user_id}")
        @JvmField
        val GET_MEMBER = Route(Method.GET, "guilds/{guild_id}/members/{user_id}")
        @JvmField
        val MODIFY_SELF = Route(Method.PATCH, "guilds/{guild_id}/members/@me")
        @JvmField
        val PRUNABLE_COUNT = Route(Method.GET, "guilds/{guild_id}/prune")
        @JvmField
        val PRUNE_MEMBERS = Route(Method.POST, "guilds/{guild_id}/prune")
        @JvmField
        val GET_WEBHOOKS = Route(Method.GET, "guilds/{guild_id}/webhooks")
        val GET_GUILD_EMBED = Route(Method.GET, "guilds/{guild_id}/embed")
        val MODIFY_GUILD_EMBED = Route(Method.PATCH, "guilds/{guild_id}/embed")
        val GET_GUILD_EMOJIS = Route(Method.GET, "guilds/{guild_id}/emojis")
        @JvmField
        val GET_AUDIT_LOGS = Route(Method.GET, "guilds/{guild_id}/audit-logs")
        @JvmField
        val GET_VOICE_REGIONS = Route(Method.GET, "guilds/{guild_id}/regions")
        @JvmField
        val UPDATE_VOICE_STATE = Route(Method.PATCH, "guilds/{guild_id}/voice-states/{user_id}")
        val GET_INTEGRATIONS = Route(Method.GET, "guilds/{guild_id}/integrations")
        val CREATE_INTEGRATION = Route(Method.POST, "guilds/{guild_id}/integrations")
        val DELETE_INTEGRATION = Route(Method.DELETE, "guilds/{guild_id}/integrations/{integration_id}")
        val MODIFY_INTEGRATION = Route(Method.PATCH, "guilds/{guild_id}/integrations/{integration_id}")
        val SYNC_INTEGRATION = Route(Method.POST, "guilds/{guild_id}/integrations/{integration_id}/sync")
        @JvmField
        val ADD_MEMBER_ROLE = Route(Method.PUT, "guilds/{guild_id}/members/{user_id}/roles/{role_id}")
        @JvmField
        val REMOVE_MEMBER_ROLE = Route(Method.DELETE, "guilds/{guild_id}/members/{user_id}/roles/{role_id}")
        @JvmField
        val LIST_ACTIVE_THREADS = Route(Method.GET, "guilds/{guild_id}/threads/active")
        @JvmField
        val GET_SCHEDULED_EVENT = Route(Method.GET, "guilds/{guild_id}/scheduled-events/{scheduled_event_id}")
        val GET_SCHEDULED_EVENTS = Route(Method.GET, "guilds/{guild_id}/scheduled-events")
        @JvmField
        val CREATE_SCHEDULED_EVENT = Route(Method.POST, "guilds/{guild_id}/scheduled-events")
        @JvmField
        val MODIFY_SCHEDULED_EVENT = Route(Method.PATCH, "guilds/{guild_id}/scheduled-events/{scheduled_event_id}")
        @JvmField
        val DELETE_SCHEDULED_EVENT = Route(Method.DELETE, "guilds/{guild_id}/scheduled-events/{scheduled_event_id}")
        @JvmField
        val GET_SCHEDULED_EVENT_USERS =
            Route(Method.GET, "guilds/{guild_id}/scheduled-events/{scheduled_event_id}/users")
        @JvmField
        val GET_WELCOME_SCREEN = Route(Method.GET, "guilds/{guild_id}/welcome-screen")
        @JvmField
        val MODIFY_WELCOME_SCREEN = Route(Method.PATCH, "guilds/{guild_id}/welcome-screen")
        @JvmField
        val CREATE_GUILD = Route(Method.POST, "guilds")
        @JvmField
        val DELETE_GUILD = Route(Method.POST, "guilds/{guild_id}/delete")
    }

    object Emojis {
        // These are all client endpoints and thus don't need defined major parameters
        @JvmField
        val MODIFY_EMOJI = Route(Method.PATCH, "guilds/{guild_id}/emojis/{emoji_id}")
        @JvmField
        val DELETE_EMOJI = Route(Method.DELETE, "guilds/{guild_id}/emojis/{emoji_id}")
        @JvmField
        val CREATE_EMOJI = Route(Method.POST, "guilds/{guild_id}/emojis")
        @JvmField
        val GET_EMOJIS = Route(Method.GET, "guilds/{guild_id}/emojis")
        @JvmField
        val GET_EMOJI = Route(Method.GET, "guilds/{guild_id}/emojis/{emoji_id}")
    }

    object Stickers {
        @JvmField
        val GET_GUILD_STICKERS = Route(Method.GET, "guilds/{guild_id}/stickers")
        @JvmField
        val GET_GUILD_STICKER = Route(Method.GET, "guilds/{guild_id}/stickers/{sticker_id}")
        @JvmField
        val MODIFY_GUILD_STICKER = Route(Method.PATCH, "guilds/{guild_id}/stickers/{sticker_id}")
        @JvmField
        val DELETE_GUILD_STICKER = Route(Method.DELETE, "guilds/{guild_id}/stickers/{sticker_id}")
        @JvmField
        val CREATE_GUILD_STICKER = Route(Method.POST, "guilds/{guild_id}/stickers")
        @JvmField
        val GET_STICKER = Route(Method.GET, "stickers/{sticker_id}")
        @JvmField
        val LIST_PACKS = Route(Method.GET, "sticker-packs")
    }

    object Webhooks {
        @JvmField
        val GET_WEBHOOK = Route(Method.GET, "webhooks/{webhook_id}")
        val GET_TOKEN_WEBHOOK = Route(Method.GET, "webhooks/{webhook_id}/{token}")
        @JvmField
        val DELETE_WEBHOOK = Route(Method.DELETE, "webhooks/{webhook_id}")
        @JvmField
        val DELETE_TOKEN_WEBHOOK = Route(Method.DELETE, "webhooks/{webhook_id}/{token}")
        @JvmField
        val MODIFY_WEBHOOK = Route(Method.PATCH, "webhooks/{webhook_id}")
        val MODIFY_TOKEN_WEBHOOK = Route(Method.PATCH, "webhooks/{webhook_id}/{token}")
        @JvmField
        val EXECUTE_WEBHOOK = Route(Method.POST, "webhooks/{webhook_id}/{token}")
        @JvmField
        val EXECUTE_WEBHOOK_FETCH = Route(Method.GET, "webhooks/{webhook_id}/{token}/messages/{message_id}")
        @JvmField
        val EXECUTE_WEBHOOK_EDIT = Route(Method.PATCH, "webhooks/{webhook_id}/{token}/messages/{message_id}")
        @JvmField
        val EXECUTE_WEBHOOK_DELETE = Route(Method.DELETE, "webhooks/{webhook_id}/{token}/messages/{message_id}")
        val EXECUTE_WEBHOOK_SLACK = Route(Method.POST, "webhooks/{webhook_id}/{token}/slack")
        val EXECUTE_WEBHOOK_GITHUB = Route(Method.POST, "webhooks/{webhook_id}/{token}/github")
    }

    object Roles {
        val GET_ROLES = Route(Method.GET, "guilds/{guild_id}/roles")
        @JvmField
        val CREATE_ROLE = Route(Method.POST, "guilds/{guild_id}/roles")
        val GET_ROLE = Route(Method.GET, "guilds/{guild_id}/roles/{role_id}")
        @JvmField
        val MODIFY_ROLE = Route(Method.PATCH, "guilds/{guild_id}/roles/{role_id}")
        @JvmField
        val DELETE_ROLE = Route(Method.DELETE, "guilds/{guild_id}/roles/{role_id}")
    }

    object Channels {
        @JvmField
        val DELETE_CHANNEL = Route(Method.DELETE, "channels/{channel_id}")
        @JvmField
        val MODIFY_CHANNEL = Route(Method.PATCH, "channels/{channel_id}")
        @JvmField
        val GET_CHANNEL = Route(Method.GET, "channels/{channel_id}")
        @JvmField
        val GET_WEBHOOKS = Route(Method.GET, "channels/{channel_id}/webhooks")
        @JvmField
        val CREATE_WEBHOOK = Route(Method.POST, "channels/{channel_id}/webhooks")
        @JvmField
        val CREATE_PERM_OVERRIDE = Route(Method.PUT, "channels/{channel_id}/permissions/{permoverride_id}")
        @JvmField
        val MODIFY_PERM_OVERRIDE = Route(Method.PUT, "channels/{channel_id}/permissions/{permoverride_id}")
        @JvmField
        val DELETE_PERM_OVERRIDE = Route(Method.DELETE, "channels/{channel_id}/permissions/{permoverride_id}")
        @JvmField
        val SET_STATUS = Route(Method.PUT, "channels/{channel_id}/voice-status")
        val SEND_TYPING = Route(Method.POST, "channels/{channel_id}/typing")
        val GET_PERMISSIONS = Route(Method.GET, "channels/{channel_id}/permissions")
        val GET_PERM_OVERRIDE = Route(Method.GET, "channels/{channel_id}/permissions/{permoverride_id}")
        @JvmField
        val FOLLOW_CHANNEL = Route(Method.POST, "channels/{channel_id}/followers")
        @JvmField
        val CREATE_THREAD_FROM_MESSAGE = Route(Method.POST, "channels/{channel_id}/messages/{message_id}/threads")
        @JvmField
        val CREATE_THREAD = Route(Method.POST, "channels/{channel_id}/threads")
        @JvmField
        val JOIN_THREAD = Route(Method.PUT, "channels/{channel_id}/thread-members/@me")
        @JvmField
        val ADD_THREAD_MEMBER = Route(Method.PUT, "channels/{channel_id}/thread-members/{user_id}")
        @JvmField
        val LEAVE_THREAD = Route(Method.DELETE, "channels/{channel_id}/thread-members/@me")
        @JvmField
        val REMOVE_THREAD_MEMBER = Route(Method.DELETE, "channels/{channel_id}/thread-members/{user_id}")
        @JvmField
        val GET_THREAD_MEMBER = Route(Method.GET, "channels/{channel_id}/thread-members/{user_id}")
        @JvmField
        val LIST_THREAD_MEMBERS = Route(Method.GET, "channels/{channel_id}/thread-members")
        @JvmField
        val LIST_PUBLIC_ARCHIVED_THREADS = Route(Method.GET, "channels/{channel_id}/threads/archived/public")
        @JvmField
        val LIST_PRIVATE_ARCHIVED_THREADS = Route(Method.GET, "channels/{channel_id}/threads/archived/private")
        @JvmField
        val LIST_JOINED_PRIVATE_ARCHIVED_THREADS =
            Route(Method.GET, "channels/{channel_id}/users/@me/threads/archived/private")
    }

    object StageInstances {
        val GET_INSTANCE = Route(Method.GET, "stage-instances/{channel_id}")
        @JvmField
        val DELETE_INSTANCE = Route(Method.DELETE, "stage-instances/{channel_id}")
        @JvmField
        val UPDATE_INSTANCE = Route(Method.PATCH, "stage-instances/{channel_id}")
        @JvmField
        val CREATE_INSTANCE = Route(Method.POST, "stage-instances")
    }

    object AutoModeration {
        @JvmField
        val LIST_RULES = Route(Method.GET, "guilds/{guild_id}/auto-moderation/rules")
        @JvmField
        val GET_RULE = Route(Method.GET, "guilds/{guild_id}/auto-moderation/rules/{rule_id}")
        @JvmField
        val CREATE_RULE = Route(Method.POST, "guilds/{guild_id}/auto-moderation/rules")
        @JvmField
        val UPDATE_RULE = Route(Method.PATCH, "guilds/{guild_id}/auto-moderation/rules/{rule_id}")
        @JvmField
        val DELETE_RULE = Route(Method.DELETE, "guilds/{guild_id}/auto-moderation/rules/{rule_id}")
    }

    object Messages {
        @JvmField
        val EDIT_MESSAGE = Route(
            Method.PATCH,
            "channels/{channel_id}/messages/{message_id}"
        ) // requires special handling, same bucket but different endpoints
        @JvmField
        val SEND_MESSAGE = Route(Method.POST, "channels/{channel_id}/messages")
        val GET_PINNED_MESSAGES = Route(Method.GET, "channels/{channel_id}/pins")
        @JvmField
        val ADD_PINNED_MESSAGE = Route(Method.PUT, "channels/{channel_id}/pins/{message_id}")
        @JvmField
        val REMOVE_PINNED_MESSAGE = Route(Method.DELETE, "channels/{channel_id}/pins/{message_id}")
        @JvmField
        val ADD_REACTION =
            Route(Method.PUT, "channels/{channel_id}/messages/{message_id}/reactions/{reaction_code}/{user_id}")
        @JvmField
        val REMOVE_REACTION =
            Route(Method.DELETE, "channels/{channel_id}/messages/{message_id}/reactions/{reaction_code}/{user_id}")
        @JvmField
        val REMOVE_ALL_REACTIONS = Route(Method.DELETE, "channels/{channel_id}/messages/{message_id}/reactions")
        @JvmField
        val GET_REACTION_USERS =
            Route(Method.GET, "channels/{channel_id}/messages/{message_id}/reactions/{reaction_code}")
        @JvmField
        val CLEAR_EMOJI_REACTIONS =
            Route(Method.DELETE, "channels/{channel_id}/messages/{message_id}/reactions/{reaction_code}")
        @JvmField
        val DELETE_MESSAGE = Route(Method.DELETE, "channels/{channel_id}/messages/{message_id}")
        @JvmField
        val GET_MESSAGE_HISTORY = Route(Method.GET, "channels/{channel_id}/messages")
        @JvmField
        val CROSSPOST_MESSAGE = Route(Method.POST, "channels/{channel_id}/messages/{message_id}/crosspost")
        val GET_MESSAGE = Route(Method.GET, "channels/{channel_id}/messages/{message_id}")
        @JvmField
        val DELETE_MESSAGES = Route(Method.POST, "channels/{channel_id}/messages/bulk-delete")
    }

    object Invites {
        @JvmField
        val GET_INVITE = Route(Method.GET, "invites/{code}")
        @JvmField
        val GET_GUILD_INVITES = Route(Method.GET, "guilds/{guild_id}/invites")
        @JvmField
        val GET_CHANNEL_INVITES = Route(Method.GET, "channels/{channel_id}/invites")
        @JvmField
        val CREATE_INVITE = Route(Method.POST, "channels/{channel_id}/invites")
        @JvmField
        val DELETE_INVITE = Route(Method.DELETE, "invites/{code}")
    }

    object Templates {
        val GET_TEMPLATE = Route(Method.GET, "guilds/templates/{code}")
        val SYNC_TEMPLATE = Route(Method.PUT, "guilds/{guild_id}/templates/{code}")
        @JvmField
        val CREATE_TEMPLATE = Route(Method.POST, "guilds/{guild_id}/templates")
        @JvmField
        val MODIFY_TEMPLATE = Route(Method.PATCH, "guilds/{guild_id}/templates/{code}")
        val DELETE_TEMPLATE = Route(Method.DELETE, "guilds/{guild_id}/templates/{code}")
        @JvmField
        val GET_GUILD_TEMPLATES = Route(Method.GET, "guilds/{guild_id}/templates")
        @JvmField
        val CREATE_GUILD_FROM_TEMPLATE = Route(Method.POST, "guilds/templates/{code}")
    }

    /**
     * The number of parameters for this route, not including query parameters.
     *
     * @return The parameter count
     */
    val paramCount: Int
    private val template: Array<String>

    init {
        template = Helpers.split(route, "/")

        // Validate route syntax
        var paramCount = 0
        for (element in template) {
            val opening = Helpers.countMatches(element, '{')
            val closing = Helpers.countMatches(element, '}')
            if (element.startsWith("{") && element.endsWith("}")) {
                // Ensure the brackets are only on the start and end
                // Valid: {guild_id}
                // Invalid: {guild_id}abc
                // Invalid: {{guild_id}}
                Checks.check(closing == 1 && opening == 1, "Route element has invalid syntax: '%s'", element)
                paramCount += 1
            } else require(!(opening > 0 || closing > 0)) {
                // Handle potential stray brackets
                // Invalid: guilds{/guild_id} -> ["guilds{", "guild_id}"]
                "Route element has invalid syntax: '$element'"
            }
        }
        this.paramCount = paramCount
    }

    @get:Nonnull
    val route: String
        /**
         * The route template with argument placeholders.
         *
         * @return The route template
         */
        get() = java.lang.String.join("/", *template)

    /**
     * Compile the route with provided parameters.
     * <br></br>The number of parameters must match the number of placeholders in the route template.
     * The provided arguments are positional and will replace the placeholders of the template in order of appearance.
     *
     *
     * Use [CompiledRoute.withQueryParams] to add query parameters to the route.
     *
     * @param  params
     * The parameters to compile the route with
     *
     * @throws IllegalArgumentException
     * If the number of parameters does not match the number of placeholders, or null is provided
     *
     * @return The compiled route, ready to use for rate-limit handling
     */
    @Nonnull
    fun compile(@Nonnull vararg params: String): CompiledRoute {
        Checks.noneNull(params, "Arguments")
        Checks.check(
            params.size == paramCount,
            "Error Compiling Route: [%s], incorrect amount of parameters provided. Expected: %d, Provided: %d",
            this, paramCount, params.size
        )
        val major = StringJoiner(":").setEmptyValue("n/a")
        val compiledRoute = StringJoiner("/")
        var paramIndex = 0
        for (element in template) {
            if (element[0] == '{') {
                val name = element.substring(1, element.length - 1)
                val value = params[paramIndex++]
                if (MAJOR_PARAMETER_NAMES.contains(name)) {
                    if (value.length > 30) // probably a long interaction_token, hash it to keep logs clean (not useful anyway)
                        major.add(name + "=" + Integer.toUnsignedString(value.hashCode())) else major.add("$name=$value")
                }
                compiledRoute.add(EncodingUtil.encodeUTF8(value))
            } else {
                compiledRoute.add(element)
            }
        }
        return CompiledRoute(this, compiledRoute.toString(), major.toString())
    }

    override fun hashCode(): Int {
        return Objects.hash(method, template.contentHashCode())
    }

    override fun equals(o: Any?): Boolean {
        if (o !is Route) return false
        val oRoute = o
        return method == oRoute.method && template.contentEquals(oRoute.template)
    }

    override fun toString(): String {
        return method.toString() + "/" + route
    }

    /**
     * A route compiled with arguments.
     *
     * @see Route.compile
     */
    inner class CompiledRoute {
        /**
         * The route template with the original placeholders.
         *
         * @return The route template with the original placeholders
         */
        @get:Nonnull
        val baseRoute: Route

        /**
         * The string of major parameters used by this route.
         * <br></br>This is important for rate-limit handling.
         *
         * @return The string of major parameters used by this route
         */
        @get:Nonnull
        val majorParameters: String
        private val compiledRoute: String
        private val query: List<String>?

        constructor(baseRoute: Route, compiledRoute: String, major: String) {
            this.baseRoute = baseRoute
            this.compiledRoute = compiledRoute
            majorParameters = major
            query = null
        }

        private constructor(original: CompiledRoute, query: List<String>) {
            baseRoute = original.baseRoute
            compiledRoute = original.compiledRoute
            majorParameters = original.majorParameters
            this.query = query
        }

        /**
         * Returns a copy of this CompiledRoute with the provided parameters added as query.
         * <br></br>This will use [percent-encoding](https://en.wikipedia.org/wiki/Percent-encoding)
         * for all provided *values* but not for the keys.
         *
         *
         * **Example Usage**<br></br>
         * <pre>`Route.CompiledRoute history = Route.GET_MESSAGE_HISTORY.compile(channelId);
         *
         * // returns a new route
         * route = history.withQueryParams(
         * "limit", 100
         * );
         * // adds another parameter ontop of limit
         * route = route.withQueryParams(
         * "after", messageId
         * );
         *
         * // now the route has both limit and after, you can also do this in one call:
         * route = history.withQueryParams(
         * "limit", 100,
         * "after", messageId
         * );
        `</pre> *
         *
         * @param  params
         * The parameters to add as query, alternating key and value (see example)
         *
         * @throws IllegalArgumentException
         * If the number of arguments is not even or null is provided
         *
         * @return A copy of this CompiledRoute with the provided parameters added as query
         */
        @Nonnull
        @CheckReturnValue
        fun withQueryParams(@Nonnull vararg params: String): CompiledRoute {
            Checks.notNull(params, "Params")
            Checks.check(params.size >= 2, "Params length must be at least 2")
            Checks.check(params.size and 1 == 0, "Params length must be a multiple of 2")
            val newQuery: MutableList<String>
            if (query == null) {
                newQuery = ArrayList(params.size / 2)
            } else {
                newQuery = ArrayList(query.size + params.size / 2)
                newQuery.addAll(query)
            }

            // Assuming names don't need encoding
            var i = 0
            while (i < params.size) {
                Checks.notEmpty(params[i], "Query key [" + i / 2 + "]")
                Checks.notNull(params[i + 1], "Query value [" + i / 2 + "]")
                newQuery.add(params[i] + '=' + EncodingUtil.encodeUTF8(params[i + 1]))
                i += 2
            }
            return CompiledRoute(this, newQuery)
        }

        /**
         * The compiled route string of the endpoint,
         * including all arguments and query parameters.
         *
         * @return The compiled route string of the endpoint
         */
        @Nonnull
        fun getCompiledRoute(): String {
            return if (query == null) compiledRoute else compiledRoute + '?' + java.lang.String.join("&", query)
            // Append query to url
        }

        /**
         * The HTTP method.
         *
         * @return The HTTP method
         */
        @Nonnull
        fun getMethod(): Method {
            return baseRoute.method
        }

        override fun hashCode(): Int {
            return (compiledRoute + method.toString()).hashCode()
        }

        override fun equals(o: Any?): Boolean {
            if (o !is CompiledRoute) return false
            val oCompiled = o
            return baseRoute == oCompiled.baseRoute && compiledRoute == oCompiled.compiledRoute
        }

        override fun toString(): String {
            return EntityString(this)
                .setType(method)
                .addMetadata("compiledRoute", compiledRoute)
                .toString()
        }
    }

    companion object {
        /**
         * Create a route template for the given HTTP method.
         *
         *
         * Route syntax should include valid argument placeholders of the format: `'{' argument_name '}'`
         * <br></br>The rate-limit handling in JDA relies on the correct names of major parameters:
         *
         *  * `channel_id` for channel routes
         *  * `guild_id` for guild routes
         *  * `webhook_id` for webhook routes
         *  * `interaction_token` for interaction routes
         *
         *
         * For example, to compose the route to create a message in a channel:
         * <pre>`Route route = Route.custom(Method.POST, "channels/{channel_id}/messages");
        `</pre> *
         *
         *
         * To compile the route, use [.compile] with the positional arguments.
         * <pre>`Route.CompiledRoute compiled = route.compile(channelId);
        `</pre> *
         *
         * @param  method
         * The HTTP method
         * @param  route
         * The route template with valid argument placeholders
         *
         * @throws IllegalArgumentException
         * If null is provided or the route is invalid (containing spaces or empty)
         *
         * @return The custom route template
         */
        @Nonnull
        fun custom(@Nonnull method: Method, @Nonnull route: String): Route {
            Checks.notNull(method, "Method")
            Checks.notEmpty(route, "Route")
            Checks.noWhitespace(route, "Route")
            return Route(method, route)
        }

        /**
         * Create a route template for the with the [DELETE][Method.DELETE] method.
         *
         *
         * Route syntax should include valid argument placeholders of the format: `'{' argument_name '}'`
         * <br></br>The rate-limit handling in JDA relies on the correct names of major parameters:
         *
         *  * `channel_id` for channel routes
         *  * `guild_id` for guild routes
         *  * `webhook_id` for webhook routes
         *  * `interaction_token` for interaction routes
         *
         *
         * For example, to compose the route to delete a message in a channel:
         * <pre>`Route route = Route.custom(Method.DELETE, "channels/{channel_id}/messages/{message_id}");
        `</pre> *
         *
         *
         * To compile the route, use [.compile] with the positional arguments.
         * <pre>`Route.CompiledRoute compiled = route.compile(channelId, messageId);
        `</pre> *
         *
         * @param  route
         * The route template with valid argument placeholders
         *
         * @throws IllegalArgumentException
         * If null is provided or the route is invalid (containing spaces or empty)
         *
         * @return The custom route template
         */
        @Nonnull
        fun delete(@Nonnull route: String): Route {
            return custom(Method.DELETE, route)
        }

        /**
         * Create a route template for the with the [POST][Method.POST] method.
         *
         *
         * Route syntax should include valid argument placeholders of the format: `'{' argument_name '}'`
         * <br></br>The rate-limit handling in JDA relies on the correct names of major parameters:
         *
         *  * `channel_id` for channel routes
         *  * `guild_id` for guild routes
         *  * `webhook_id` for webhook routes
         *  * `interaction_token` for interaction routes
         *
         *
         * For example, to compose the route to create a message in a channel:
         * <pre>`Route route = Route.custom(Method.POST, "channels/{channel_id}/messages");
        `</pre> *
         *
         *
         * To compile the route, use [.compile] with the positional arguments.
         * <pre>`Route.CompiledRoute compiled = route.compile(channelId);
        `</pre> *
         *
         * @param  route
         * The route template with valid argument placeholders
         *
         * @throws IllegalArgumentException
         * If null is provided or the route is invalid (containing spaces or empty)
         *
         * @return The custom route template
         */
        @Nonnull
        fun post(@Nonnull route: String): Route {
            return custom(Method.POST, route)
        }

        /**
         * Create a route template for the with the [PUT][Method.PUT] method.
         *
         *
         * Route syntax should include valid argument placeholders of the format: `'{' argument_name '}'`
         * <br></br>The rate-limit handling in JDA relies on the correct names of major parameters:
         *
         *  * `channel_id` for channel routes
         *  * `guild_id` for guild routes
         *  * `webhook_id` for webhook routes
         *  * `interaction_token` for interaction routes
         *
         *
         * For example, to compose the route to ban a user in a guild:
         * <pre>`Route route = Route.custom(Method.PUT, "guilds/{guild_id}/bans/{user_id}");
        `</pre> *
         *
         *
         * To compile the route, use [.compile] with the positional arguments.
         * <pre>`Route.CompiledRoute compiled = route.compile(guildId, userId);
        `</pre> *
         *
         * @param  route
         * The route template with valid argument placeholders
         *
         * @throws IllegalArgumentException
         * If null is provided or the route is invalid (containing spaces or empty)
         *
         * @return The custom route template
         */
        @Nonnull
        fun put(@Nonnull route: String): Route {
            return custom(Method.PUT, route)
        }

        /**
         * Create a route template for the with the [PATCH][Method.PATCH] method.
         *
         *
         * Route syntax should include valid argument placeholders of the format: `'{' argument_name '}'`
         * <br></br>The rate-limit handling in JDA relies on the correct names of major parameters:
         *
         *  * `channel_id` for channel routes
         *  * `guild_id` for guild routes
         *  * `webhook_id` for webhook routes
         *  * `interaction_token` for interaction routes
         *
         *
         * For example, to compose the route to edit a message in a channel:
         * <pre>`Route route = Route.custom(Method.PATCH, "channels/{channel_id}/messages/{message_id}");
        `</pre> *
         *
         *
         * To compile the route, use [.compile] with the positional arguments.
         * <pre>`Route.CompiledRoute compiled = route.compile(channelId, messageId);
        `</pre> *
         *
         * @param  route
         * The route template with valid argument placeholders
         *
         * @throws IllegalArgumentException
         * If null is provided or the route is invalid (containing spaces or empty)
         *
         * @return The custom route template
         */
        @Nonnull
        fun patch(@Nonnull route: String): Route {
            return custom(Method.PATCH, route)
        }

        /**
         * Create a route template for the with the [GET][Method.GET] method.
         *
         *
         * Route syntax should include valid argument placeholders of the format: `'{' argument_name '}'`
         * <br></br>The rate-limit handling in JDA relies on the correct names of major parameters:
         *
         *  * `channel_id` for channel routes
         *  * `guild_id` for guild routes
         *  * `webhook_id` for webhook routes
         *  * `interaction_token` for interaction routes
         *
         *
         * For example, to compose the route to get a message in a channel:
         * <pre>`Route route = Route.custom(Method.GET, "channels/{channel_id}/messages/{message_id}");
        `</pre> *
         *
         *
         * To compile the route, use [.compile] with the positional arguments.
         * <pre>`Route.CompiledRoute compiled = route.compile(channelId, messageId);
        `</pre> *
         *
         * @param  route
         * The route template with valid argument placeholders
         *
         * @throws IllegalArgumentException
         * If null is provided or the route is invalid (containing spaces or empty)
         *
         * @return The custom route template
         */
        @Nonnull
        operator fun get(@Nonnull route: String): Route {
            return custom(Method.GET, route)
        }

        /**
         * The known major parameters used for rate-limits.
         *
         *
         * Instead of `webhook_id + webhook_token`, we use `interaction_token` for interaction routes.
         *
         * @see [Rate Limit Documentation](https://discord.com/developers/docs/topics/rate-limits)
         */
        val MAJOR_PARAMETER_NAMES = Helpers.listOf(
            "guild_id", "channel_id", "webhook_id", "interaction_token"
        )
    }
}
