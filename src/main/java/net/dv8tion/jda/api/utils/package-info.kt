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
/**
 * Package which contains all utilities for the JDA library.
 * These are used by JDA itself and can also be useful for the library user!
 *
 *
 * List of utilities:
 *
 *  * [MiscUtil][net.dv8tion.jda.api.utils.MiscUtil]
 * <br></br>Various operations that don't have specific utility classes yet, mostly internals that are accessible from JDA entities
 *
 *  * [WidgetUtil][net.dv8tion.jda.api.utils.WidgetUtil]
 * <br></br>This is not bound to a JDA instance and can view the [Widget][net.dv8tion.jda.api.entities.Widget]
 * for a specified Guild. (by id)
 *
 *  * [MarkdownSanitizer][net.dv8tion.jda.api.utils.MarkdownSanitizer]
 * <br></br>Parser for Discord markdown that can either escape or strip markdown from a string
 *
 *  * [SessionController][net.dv8tion.jda.api.utils.SessionController]
 * <br></br>Special handler for session (re-)connects and global rate-limits
 *
 *  * [TimeUtil][net.dv8tion.jda.api.utils.TimeUtil]
 * <br></br>Useful time conversion methods related to Discord
 *
 */
package net.dv8tion.jda.api.utils

import net.dv8tion.jda.api.utils.data.DataObject.toJson
import okhttp3.MultipartBody.Builder.setType
import okhttp3.MultipartBody.Builder.addFormDataPart
import net.dv8tion.jda.api.entities.Icon.Companion.from
import net.dv8tion.jda.api.entities.Message.Attachment.getIdLong
import net.dv8tion.jda.api.utils.data.DataObject.Companion.empty
import net.dv8tion.jda.api.utils.data.DataObject.put
import net.dv8tion.jda.api.entities.ISnowflake.id
import okhttp3.Request.Builder.url
import okhttp3.Request.Builder.addHeader
import okhttp3.Request.Builder.build
import okhttp3.OkHttpClient.newCall
import okhttp3.Call.enqueue
import okhttp3.Response.isSuccessful
import okhttp3.Call.cancel
import okio.source
import okio.buffer
import okio.BufferedSource.inputStream
import net.dv8tion.jda.api.requests.RestRateLimiter.GlobalRateLimit.classic
import net.dv8tion.jda.api.requests.RestRateLimiter.GlobalRateLimit.Companion.create
import net.dv8tion.jda.api.requests.Route.compile
import net.dv8tion.jda.api.requests.Response.isOk
import net.dv8tion.jda.api.requests.Response.`object`
import net.dv8tion.jda.api.utils.data.DataObject.getString
import net.dv8tion.jda.api.utils.data.DataObject.getInt
import net.dv8tion.jda.api.utils.data.DataObject.getObject
import net.dv8tion.jda.api.requests.Request.onSuccess
import net.dv8tion.jda.api.requests.Request.onFailure
import net.dv8tion.jda.api.requests.RestAction.complete
import okhttp3.OkHttpClient.Builder.build
import okhttp3.Request.Builder.method
import okhttp3.Request.Builder.header
import okhttp3.Call.execute
import net.dv8tion.jda.api.utils.data.DataObject.Companion.fromJson
import net.dv8tion.jda.api.utils.data.DataObject.getLong
import net.dv8tion.jda.api.utils.data.DataObject
import java.io.IOException
import net.dv8tion.jda.api.utils.FileUpload
import java.nio.file.OpenOption
import net.dv8tion.jda.api.utils.AttachmentUpdate
import net.dv8tion.jda.api.utils.AttachedFile
import net.dv8tion.jda.internal.requests.Requester
import net.dv8tion.jda.internal.utils.FutureUtil
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.internal.utils.EntityString
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.SessionControllerAdapter
import net.dv8tion.jda.api.utils.SessionController
import net.dv8tion.jda.api.utils.SessionController.SessionConnectNode
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.NoSuchElementException
import com.neovisionaries.ws.client.OpeningHandshakeException
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.RestConfig
import net.dv8tion.jda.api.utils.FileProxy.DownloadTask
import net.dv8tion.jda.internal.requests.FunctionalCallback
import net.dv8tion.jda.api.utils.IOBiConsumer
import net.dv8tion.jda.internal.utils.requestbody.TypedBody
import net.dv8tion.jda.internal.utils.requestbody.DataSupplierBody
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.FileNotFoundException
import net.dv8tion.jda.api.utils.ClosableIterator
import net.dv8tion.jda.api.utils.LockIterator
import net.dv8tion.jda.internal.utils.JDALogger
import net.dv8tion.jda.api.utils.MarkdownSanitizer.SanitizationStrategy
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import gnu.trove.map.TIntObjectMap
import net.dv8tion.jda.api.utils.MarkdownUtil
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.LRUMemberCachePolicy
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.GuildVoiceState
import gnu.trove.map.TLongObjectMap
import gnu.trove.impl.sync.TSynchronizedLongObjectMap
import java.util.concurrent.locks.ReentrantLock
import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.api.requests.RestRateLimiter.GlobalRateLimit
import net.dv8tion.jda.api.utils.SessionController.GlobalRateLimitAdapter
import net.dv8tion.jda.api.utils.SessionController.ShardedGateway
import net.dv8tion.jda.api.JDA.ShardInfo
import net.dv8tion.jda.api.requests.RestRateLimiter
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.api.exceptions.InvalidTokenException
import net.dv8tion.jda.api.utils.SplitUtil
import java.util.LinkedList
import java.time.temporal.TemporalAccessor
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.TimeUtil
import java.time.OffsetDateTime
import java.util.Calendar
import java.util.TimeZone
import net.dv8tion.jda.api.utils.WidgetUtil.BannerType
import net.dv8tion.jda.api.utils.WidgetUtil
import java.util.Locale
import net.dv8tion.jda.api.utils.WidgetUtil.WidgetTheme
import net.dv8tion.jda.api.exceptions.RateLimitedException
import net.dv8tion.jda.internal.entities.WidgetImpl
