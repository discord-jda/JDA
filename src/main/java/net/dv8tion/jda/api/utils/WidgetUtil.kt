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
package net.dv8tion.jda.api.utils

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Widget
import net.dv8tion.jda.api.exceptions.RateLimitedException
import net.dv8tion.jda.api.requests.RestConfig
import net.dv8tion.jda.api.utils.data.DataObject.Companion.fromJson
import net.dv8tion.jda.internal.entities.WidgetImpl
import net.dv8tion.jda.internal.utils.*
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder.build
import okhttp3.Request
import okhttp3.Request.Builder.build
import okhttp3.Request.Builder.header
import okhttp3.Request.Builder.method
import okhttp3.Request.Builder.url
import java.io.*
import java.net.HttpURLConnection
import java.util.*
import javax.annotation.Nonnull

/**
 * The WidgetUtil is a class for interacting with various facets of Discord's
 * guild widgets
 *
 * @since  3.0
 * @author John A. Grosh
 */
object WidgetUtil {
    const val WIDGET_PNG = RestConfig.DEFAULT_BASE_URL + "guilds/%s/widget.png?style=%s"
    const val WIDGET_URL = RestConfig.DEFAULT_BASE_URL + "guilds/%s/widget.json"
    const val WIDGET_HTML =
        "<iframe src=\"https://discord.com/widget?id=%s&theme=%s\" width=\"%d\" height=\"%d\" allowtransparency=\"true\" frameborder=\"0\"></iframe>"

    /**
     * Gets the banner image for the specified guild of the specified type.
     * <br></br>This banner will only be available if the guild in question has the
     * Widget enabled.
     *
     * @param  guild
     * The guild
     * @param  type
     * The type (visual style) of the banner
     *
     * @return A String containing the URL of the banner image
     */
    @Nonnull
    fun getWidgetBanner(@Nonnull guild: Guild, @Nonnull type: BannerType): String {
        Checks.notNull(guild, "Guild")
        return getWidgetBanner(guild.id, type)
    }

    /**
     * Gets the banner image for the specified guild of the specified type.
     * <br></br>This banner will only be available if the guild in question has the
     * Widget enabled. Additionally, this method can be used independently of
     * being on the guild in question.
     *
     * @param  guildId
     * the guild ID
     * @param  type
     * The type (visual style) of the banner
     *
     * @return A String containing the URL of the banner image
     */
    @Nonnull
    fun getWidgetBanner(@Nonnull guildId: String?, @Nonnull type: BannerType): String {
        Checks.notNull(guildId, "GuildId")
        Checks.notNull(type, "BannerType")
        return String.format(WIDGET_PNG, guildId, type.name.lowercase(Locale.getDefault()))
    }

    /**
     * Gets the pre-made HTML Widget for the specified guild using the specified
     * settings. The widget will only display correctly if the guild in question
     * has the Widget enabled.
     *
     * @param  guild
     * the guild
     * @param  theme
     * the theme, light or dark
     * @param  width
     * the width of the widget
     * @param  height
     * the height of the widget
     *
     * @return a String containing the pre-made widget with the supplied settings
     */
    @Nonnull
    fun getPremadeWidgetHtml(@Nonnull guild: Guild, @Nonnull theme: WidgetTheme, width: Int, height: Int): String {
        Checks.notNull(guild, "Guild")
        return getPremadeWidgetHtml(guild.id, theme, width, height)
    }

    /**
     * Gets the pre-made HTML Widget for the specified guild using the specified
     * settings. The widget will only display correctly if the guild in question
     * has the Widget enabled. Additionally, this method can be used independently
     * of being on the guild in question.
     *
     * @param  guildId
     * the guild ID
     * @param  theme
     * the theme, light or dark
     * @param  width
     * the width of the widget
     * @param  height
     * the height of the widget
     *
     * @return a String containing the pre-made widget with the supplied settings
     */
    @Nonnull
    fun getPremadeWidgetHtml(@Nonnull guildId: String?, @Nonnull theme: WidgetTheme, width: Int, height: Int): String {
        Checks.notNull(guildId, "GuildId")
        Checks.notNull(theme, "WidgetTheme")
        Checks.notNegative(width, "Width")
        Checks.notNegative(height, "Height")
        return Helpers.format(WIDGET_HTML, guildId, theme.name.lowercase(Locale.getDefault()), width, height)
    }

    /**
     * Makes a GET request to get the information for a Guild's widget. This
     * widget (if available) contains information about the guild, including the
     * Guild's name, an invite code (if set), a list of voice channels, and a
     * list of online members (plus the voice states of any members in voice
     * channels).
     *
     *
     * This Widget can be obtained from any valid guild ID that has
     * it enabled; no accounts need to be on the server to access this information.
     *
     * @param  guildId
     * The id of the Guild
     *
     * @throws net.dv8tion.jda.api.exceptions.RateLimitedException
     * If the request was rate limited, **respect the timeout**!
     * @throws java.lang.NumberFormatException
     * If the provided `guildId` cannot be parsed by [Long.parseLong]
     *
     * @return `null` if the provided guild ID is not a valid Discord guild ID
     * <br></br>a Widget object with null fields and isAvailable() returning
     * false if the guild ID is valid but the guild in question does not
     * have the widget enabled
     * <br></br>a filled-in Widget object if the guild ID is valid and the guild
     * in question has the widget enabled.
     */
    @Throws(RateLimitedException::class)
    fun getWidget(@Nonnull guildId: String): Widget? {
        return getWidget(MiscUtil.parseSnowflake(guildId))
    }

    /**
     * Makes a GET request to get the information for a Guild's widget. This
     * widget (if available) contains information about the guild, including the
     * Guild's name, an invite code (if set), a list of voice channels, and a
     * list of online members (plus the voice states of any members in voice
     * channels).
     *
     *
     * This Widget can be obtained from any valid guild ID that has
     * it enabled; no accounts need to be on the server to access this information.
     *
     * @param  guildId
     * The id of the Guild
     *
     * @throws java.io.UncheckedIOException
     * If an I/O error occurs
     * @throws net.dv8tion.jda.api.exceptions.RateLimitedException
     * If the request was rate limited, **respect the timeout**!
     *
     * @return `null` if the provided guild ID is not a valid Discord guild ID
     * <br></br>a Widget object with null fields and isAvailable() returning
     * false if the guild ID is valid but the guild in question does not
     * have the widget enabled
     * <br></br>a filled-in Widget object if the guild ID is valid and the guild
     * in question has the widget enabled.
     */
    @Throws(RateLimitedException::class)
    fun getWidget(guildId: Long): Widget? {
        Checks.notNull(guildId, "GuildId")
        var connection: HttpURLConnection
        val client: OkHttpClient = Builder().build()
        val request: Request = Builder()
            .url(String.format(WIDGET_URL, guildId))
            .method("GET", null)
            .header("user-agent", RestConfig.USER_AGENT)
            .header("accept-encoding", "gzip")
            .build()
        try {
            client.newCall(request).execute().use { response ->
                val code = response.code()
                val data = IOUtil.getBody(response)
                return when (code) {
                    200 -> {
                        run {
                            try {
                                data.use { stream -> return WidgetImpl(fromJson(stream)) }
                            } catch (e: IOException) {
                                throw UncheckedIOException(e)
                            }
                        }
                        null
                    }

                    400, 404 -> null
                    403 -> WidgetImpl(guildId)
                    429 -> {
                        var retryAfter: Long
                        try {
                            data.use { stream -> retryAfter = fromJson(stream).getLong("retry_after") }
                        } catch (e: Exception) {
                            retryAfter = 0
                        }
                        throw RateLimitedException(WIDGET_URL, retryAfter)
                    }

                    else -> throw IllegalStateException("An unknown status was returned: " + code + " " + response.message())
                }
            }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    /**
     * Represents the available banner types
     * <br></br>Each of these has a different appearance:
     *
     *
     *
     * <br></br>**Shield** - tiny, only contains Discord logo and online count
     * <br></br>**Banner1** - medium, contains server name, icon, and online count, and a "Powered by Discord" bar on the bottom
     * <br></br>**Banner2** - small, contains server name, icon, and online count, and a Discord logo on the side
     * <br></br>**Banner3** - medium, contains server name, icon, and online count, and a Discord logo with a "Chat Now" bar on the bottom
     * <br></br>**Banner4** - large, contains a very big Discord logo, server name, icon, and online count, and a big "Join My Server" button
     */
    enum class BannerType {
        SHIELD,
        BANNER1,
        BANNER2,
        BANNER3,
        BANNER4
    }

    /**
     * Represents the color scheme of the widget
     * <br></br>These color themes match Discord's dark and light themes
     */
    enum class WidgetTheme {
        LIGHT,
        DARK
    }
}
