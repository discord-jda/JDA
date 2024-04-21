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
package net.dv8tion.jda.api.audit

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.Webhook
import net.dv8tion.jda.internal.entities.GuildImpl
import net.dv8tion.jda.internal.entities.UserImpl
import net.dv8tion.jda.internal.entities.WebhookImpl
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.EntityString
import java.util.*
import javax.annotation.Nonnull

/**
 * Single entry for an [AuditLogPaginationAction][net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction].
 * <br></br>This entry contains all options/changes and details for the action
 * that was logged by the [Guild][net.dv8tion.jda.api.entities.Guild] audit-logs.
 */
class AuditLogEntry(
    /**
     * The [ActionType][net.dv8tion.jda.api.audit.ActionType] defining what auditable
     * Action is referred to by this entry.
     *
     * @return The [ActionType][net.dv8tion.jda.api.audit.ActionType]
     */
    @get:Nonnull val type: ActionType,
    /**
     * The raw type value used to derive [.getType].
     * <br></br>This can be used when a new action type is not yet supported by JDA.
     *
     * @return The raw type value
     */
    val typeRaw: Int,
    protected val id: Long,
    /**
     * The id for the user that executed the action.
     *
     * @return The user id
     */
    val userIdLong: Long,
    /**
     * The id for the target entity.
     * <br></br>This references an entity based on the [TargetType][net.dv8tion.jda.api.audit.TargetType]
     * which is specified by [.getTargetType]!
     *
     * @return The target id
     */
    val targetIdLong: Long,
    protected val guild: GuildImpl,
    protected val user: UserImpl,
    protected val webhook: WebhookImpl,
    protected val reason: String,
    changes: Map<String?, AuditLogChange?>?,
    options: Map<String?, Any?>?
) : ISnowflake {

    /**
     * Key-Value [Map][java.util.Map] containing all [ AuditLogChanges][AuditLogChange] made in this entry.
     * The keys for the returned map are case-insensitive keys defined in the regarding AuditLogChange value.
     * <br></br>To iterate only the changes you can use [Map.values()][java.util.Map.values]!
     *
     * @return Key-Value Map of changes
     */
    @get:Nonnull
    val changes: Map<String?, AuditLogChange?>

    /**
     * Key-Value [Map][java.util.Map] containing all Options made in this entry. The keys for the returned map are
     * case-insensitive keys defined in the regarding AuditLogChange value.
     * <br></br>To iterate only the changes you can use [Map.values()][java.util.Map.values]!
     *
     *
     * Options may include secondary targets or details that do not qualify as "change".
     * <br></br>An example of that would be the `member` option
     * for [CHANNEL_OVERRIDE_UPDATE][net.dv8tion.jda.api.audit.ActionType.CHANNEL_OVERRIDE_UPDATE]
     * containing the user_id of a [Member][net.dv8tion.jda.api.entities.Member].
     *
     * @return Key-Value Map of changes
     */
    @get:Nonnull
    val options: Map<String?, Any?>

    init {
        this.changes =
            if (changes != null && !changes.isEmpty()) Collections.unmodifiableMap(changes) else emptyMap<String, AuditLogChange>()
        this.options =
            if (options != null && !options.isEmpty()) Collections.unmodifiableMap(options) else emptyMap<String, Any>()
    }

    override fun getIdLong(): Long {
        return id
    }

    /**
     * The id for the target entity.
     * <br></br>This references an entity based on the [TargetType][net.dv8tion.jda.api.audit.TargetType]
     * which is specified by [.getTargetType]!
     *
     * @return The target id
     */
    @Nonnull
    fun getTargetId(): String {
        return java.lang.Long.toUnsignedString(targetIdLong)
    }

    /**
     * The [Webhook][net.dv8tion.jda.api.entities.Webhook] that the target id of this audit-log entry refers to
     *
     * @return Possibly-null Webhook instance
     */
    fun getWebhook(): Webhook? {
        return webhook
    }

    /**
     * The [Guild][net.dv8tion.jda.api.entities.Guild] this audit-log entry refers to
     *
     * @return The Guild instance
     */
    @Nonnull
    fun getGuild(): Guild {
        return guild
    }

    /**
     * The id for the user that executed the action.
     *
     * @return The user id
     */
    @Nonnull
    fun getUserId(): String {
        return java.lang.Long.toUnsignedString(userIdLong)
    }

    /**
     * The [User] responsible for this action.
     *
     *
     * This will not be available for [GuildAuditLogEntryCreateEvent], you can use [.getUserIdLong] instead.
     *
     * @return Possibly-null User instance
     */
    fun getUser(): User? {
        return user
    }

    /**
     * The optional reason why this action was executed.
     *
     * @return Possibly-null reason String
     */
    fun getReason(): String? {
        return reason
    }

    @get:Nonnull
    val jDA: JDA
        /**
         * The corresponding JDA instance of the referring Guild
         *
         * @return The corresponding JDA instance
         */
        get() = guild.jda

    /**
     * Shortcut to `[getChanges()][.getChanges].get(key)` lookup!
     * <br></br>This lookup is case-insensitive!
     *
     * @param  key
     * The [AuditLogKey][net.dv8tion.jda.api.audit.AuditLogKey] to look for
     *
     * @return Possibly-null value corresponding to the specified key
     */
    fun getChangeByKey(key: AuditLogKey?): AuditLogChange? {
        return if (key == null) null else getChangeByKey(key.key)
    }

    /**
     * Shortcut to `[getChanges()][.getChanges].get(key)` lookup!
     * <br></br>This lookup is case-insensitive!
     *
     * @param  key
     * The key to look for
     *
     * @return Possibly-null value corresponding to the specified key
     */
    fun getChangeByKey(key: String?): AuditLogChange? {
        return changes[key]
    }

    /**
     * Filters all changes by the specified keys
     *
     * @param  keys
     * Varargs [AuditLogKeys][net.dv8tion.jda.api.audit.AuditLogKey] to look for
     *
     * @throws java.lang.IllegalArgumentException
     * If provided with null array
     *
     * @return Possibly-empty, never-null immutable list of [AuditLogChanges][AuditLogChange]
     */
    @Nonnull
    fun getChangesForKeys(@Nonnull vararg keys: AuditLogKey?): List<AuditLogChange> {
        Checks.notNull(keys, "Keys")
        val changes: MutableList<AuditLogChange> = ArrayList(keys.size)
        for (key in keys) {
            val change = getChangeByKey(key)
            if (change != null) changes.add(change)
        }
        return Collections.unmodifiableList(changes)
    }

    /**
     * Shortcut to `[getOptions()][.getOptions].get(name)` lookup!
     * <br></br>This lookup is case-insensitive!
     *
     * @param  <T>
     * The expected type for this option <br></br>Will be used for casting
     * @param  name
     * The field name to look for
     *
     * @throws java.lang.ClassCastException
     * If the type-cast failed for the generic type.
     *
     * @return Possibly-null value corresponding to the specified key
    </T> */
    fun <T> getOptionByName(name: String?): T? {
        return options[name] as T?
    }

    /**
     * Shortcut to `[getOptions()][.getOptions].get(name)` lookup!
     *
     * @param  <T>
     * The expected type for this option <br></br>Will be used for casting
     * @param  option
     * The [AuditLogOption][net.dv8tion.jda.api.audit.AuditLogOption]
     *
     * @throws java.lang.ClassCastException
     * If the type-cast failed for the generic type.
     * @throws java.lang.IllegalArgumentException
     * If provided with `null` option.
     *
     * @return Possibly-null value corresponding to the specified option constant
    </T> */
    fun <T> getOption(@Nonnull option: AuditLogOption): T? {
        Checks.notNull(option, "Option")
        return getOptionByName<T>(option.key)
    }

    /**
     * Constructs a filtered, immutable list of options corresponding to
     * the provided [AuditLogOptions][net.dv8tion.jda.api.audit.AuditLogOption].
     * <br></br>This will exclude options with `null` values!
     *
     * @param  options
     * The not-null [AuditLogOptions][net.dv8tion.jda.api.audit.AuditLogOption]
     * which will be used to gather option values via [getOption(AuditLogOption)][.getOption]!
     *
     * @throws java.lang.IllegalArgumentException
     * If provided with null options
     *
     * @return Unmodifiable list of representative values
     */
    @Nonnull
    fun getOptions(@Nonnull vararg options: AuditLogOption): List<Any> {
        Checks.notNull(options, "Options")
        val items: MutableList<Any> = ArrayList(options.size)
        for (option in options) {
            val obj = getOption<Any>(option)
            if (obj != null) items.add(obj)
        }
        return Collections.unmodifiableList(items)
    }

    @get:Nonnull
    val targetType: TargetType?
        /**
         * The [TargetType][net.dv8tion.jda.api.audit.TargetType] defining what kind of
         * entity was targeted by this action.
         * <br></br>Shortcut for `getType().getTargetType()`
         *
         * @return The [TargetType][net.dv8tion.jda.api.audit.TargetType]
         */
        get() = type.targetType

    override fun hashCode(): Int {
        return java.lang.Long.hashCode(id)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj !is AuditLogEntry) return false
        val other = obj
        return other.id == id && other.targetIdLong == targetIdLong
    }

    override fun toString(): String {
        return EntityString(this)
            .setType(type)
            .addMetadata("targetId", targetIdLong)
            .addMetadata("guild", guild)
            .toString()
    }
}
