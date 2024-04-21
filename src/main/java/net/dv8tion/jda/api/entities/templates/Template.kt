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
package net.dv8tion.jda.api.entities.templates

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.managers.TemplateManager
import net.dv8tion.jda.api.requests.Request
import net.dv8tion.jda.api.requests.Response
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.managers.TemplateManagerImpl
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.EntityString
import java.time.OffsetDateTime
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Representation of a Discord Guild Template
 * <br></br>This class is immutable.
 *
 * @since  4.3.0
 *
 * @see .resolve
 * @see net.dv8tion.jda.api.entities.Guild.retrieveTemplates
 */
class Template(
    private val api: JDAImpl,
    /**
     * The template code.
     *
     * @return The template code
     */
    @JvmField @get:Nonnull val code: String,
    /**
     * The template name.
     *
     * @return The template name
     */
    @get:Nonnull val name: String, private val description: String,
    /**
     * How often this template has been used.
     *
     * @return The uses of this template
     */
    val uses: Int,
    /**
     * The user who created this template.
     *
     * @return The user who created this template
     */
    @get:Nonnull val creator: User,
    /**
     * Returns creation date of this template.
     *
     * @return The creation date of this template
     */
    @get:Nonnull val timeCreated: OffsetDateTime,
    /**
     * Returns the last update date of this template.
     * <br></br>If this template has never been updated, this returns the date of creation.
     *
     * @return The last update date of this template
     *
     * @see .getTimeCreated
     */
    @get:Nonnull val timeUpdated: OffsetDateTime,
    /**
     * A [Template.Guild][TemplateGuild] object
     * containing information about this template's origin guild.
     *
     * @return Information about this template's origin guild
     *
     * @see TemplateGuild
     */
    @JvmField @get:Nonnull val guild: TemplateGuild,
    /**
     * Whether or not this template is synced.
     *
     * @return True, if this template matches the current guild structure
     */
    val isSynced: Boolean
) {

    /**
     * Syncs this template.
     * <br></br>Requires [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] in the template's guild.
     * Will throw an [InsufficientPermissionException][net.dv8tion.jda.api.exceptions.InsufficientPermissionException] otherwise.
     *
     * @throws IllegalStateException
     * If the account is not in the template's guild
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the account does not have [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] in the template's guild
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: [Template][net.dv8tion.jda.api.entities.templates.Template]
     * <br></br>The synced Template object
     */
    @Nonnull
    @CheckReturnValue
    fun sync(): RestAction<Template?> {
        checkInteraction()
        val route = Route.Templates.SYNC_TEMPLATE.compile(guild.id, this.code)
        return RestActionImpl(
            api,
            route
        ) { response: Response, request: Request<Template?>? -> api.entityBuilder.createTemplate(response.getObject()) }
    }

    /**
     * Deletes this template.
     * <br></br>Requires [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] in the template's guild.
     * Will throw an [InsufficientPermissionException][net.dv8tion.jda.api.exceptions.InsufficientPermissionException] otherwise.
     *
     * @throws IllegalStateException
     * If the account is not in the template's guild
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     * If the account does not have [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER] in the template's guild
     *
     * @return [RestAction][net.dv8tion.jda.api.requests.RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun delete(): RestAction<Void> {
        checkInteraction()
        val route = Route.Templates.DELETE_TEMPLATE.compile(guild.id, this.code)
        return RestActionImpl(api, route)
    }

    /**
     * The template description.
     *
     * @return The template description
     */
    fun getDescription(): String? {
        return description
    }

    @get:Nonnull
    val manager: TemplateManager
        /**
         * Returns the [TemplateManager][net.dv8tion.jda.api.managers.TemplateManager] for this Template.
         * <br></br>In the TemplateManager, you can modify the name or description of the template.
         * You modify multiple fields in one request by chaining setters before calling [RestAction.queue()][net.dv8tion.jda.api.requests.RestAction.queue].
         *
         * @throws IllegalStateException
         * If the account is not in the template's guild
         * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
         * If the currently logged in account does not have [MANAGE_SERVER][net.dv8tion.jda.api.Permission.MANAGE_SERVER]
         *
         * @return The TemplateManager of this Template
         */
        get() {
            checkInteraction()
            return TemplateManagerImpl(this)
        }

    private fun checkInteraction() {
        val guild = api.getGuildById(guild.idLong)
            ?: throw IllegalStateException("Cannot interact with a template without shared guild")
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) throw InsufficientPermissionException(
            guild,
            Permission.MANAGE_SERVER
        )
    }

    @get:Nonnull
    val jDA: JDA
        /**
         * The [JDA][net.dv8tion.jda.api.JDA] instance used to create this Template instance.
         *
         * @return The corresponding JDA instance
         */
        get() = api

    override fun hashCode(): Int {
        return code.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) return true
        if (obj !is Template) return false
        return obj.code == this.code
    }

    override fun toString(): String {
        return EntityString(this)
            .addMetadata("code", code)
            .toString()
    }

    companion object {
        /**
         * Retrieves a new [Template][net.dv8tion.jda.api.entities.templates.Template] instance for the given template code.
         *
         *
         * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
         *
         *  * [Unknown Guild Template][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_GUILD_TEMPLATE]
         * <br></br>The template doesn't exist.
         *
         *
         * @param  api
         * The JDA instance
         * @param  code
         * A valid template code
         *
         * @throws java.lang.IllegalArgumentException
         *
         *  * If the provided code is null or empty
         *  * If the provided code contains a whitespace
         *  * If the provided JDA object is null
         *
         *
         * @return [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: [Template][net.dv8tion.jda.api.entities.templates.Template]
         * <br></br>The Template object
         */
        @Nonnull
        fun resolve(api: JDA, code: String?): RestAction<Template?> {
            Checks.notEmpty(code, "code")
            Checks.noWhitespace(code, "code")
            Checks.notNull(api, "api")
            val route = Route.Templates.GET_TEMPLATE.compile(code)
            val jda = api as JDAImpl
            return RestActionImpl(
                api,
                route
            ) { response: Response, request: Request<Template?>? -> jda.entityBuilder.createTemplate(response.getObject()) }
        }
    }
}
