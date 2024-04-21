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

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.requests.RestRateLimiter.GlobalRateLimit.Companion.create
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.requests.Requester
import okhttp3.MultipartBody.Builder.addFormDataPart
import okhttp3.MultipartBody.Builder.setType
import okhttp3.RequestBody
import java.io.*
import java.nio.file.OpenOption
import java.nio.file.Path
import javax.annotation.Nonnull

/**
 * Represents files that are attached to requests.
 */
interface AttachedFile : Closeable {
    /**
     * Used internally to build the multipart request.
     *
     *
     * The index can be used as a unique identifier for the multipart name, which is required to be unique by Discord.
     *
     * @param builder
     * The [MultipartBody.Builder] used for the request body
     * @param index
     * The index of the attachment, ignored for [AttachmentUpdate]
     */
    fun addPart(@Nonnull builder: Builder, index: Int)

    /**
     * Used internally to build attachment descriptions for requests.
     * <br></br>This contains the id/index of the attachment, and the name of the file.
     *
     * @param  index
     * The reference index (should be same as [.addPart])
     *
     * @return [DataObject] for the attachment
     */
    @Nonnull
    fun toAttachmentData(index: Int): DataObject

    /**
     * Forces the underlying resource to be closed, even if the file is already handled by a request.
     *
     * @throws IOException
     * If an IOException is thrown while closing the resource
     */
    @Throws(IOException::class)
    fun forceClose()

    companion object {
        /**
         * Create a new [FileUpload] for an input stream.
         * <br></br>This is used to upload data to discord for various purposes.
         *
         *
         * The [InputStream] will be closed on consumption by the request.
         * You can use [FileUpload.close] to close the stream manually.
         *
         * @param  data
         * The [InputStream] to upload
         * @param  name
         * The representative name to use for the file
         *
         * @throws IllegalArgumentException
         * If null is provided or the name is empty
         *
         * @return [FileUpload]
         *
         * @see java.io.FileInputStream FileInputStream
         */
        @Nonnull
        fun fromData(@Nonnull data: InputStream?, @Nonnull name: String): FileUpload? {
            return FileUpload.Companion.fromData(data, name)
        }

        /**
         * Create a new [FileUpload] for a byte array.
         * <br></br>This is used to upload data to discord for various purposes.
         *
         * @param  data
         * The `byte[]` to upload
         * @param  name
         * The representative name to use for the file
         *
         * @throws IllegalArgumentException
         * If null is provided or the name is empty
         *
         * @return [FileUpload]
         */
        @Nonnull
        fun fromData(@Nonnull data: ByteArray?, @Nonnull name: String): FileUpload? {
            return FileUpload.Companion.fromData(data, name)
        }

        /**
         * Create a new [FileUpload] for a local file.
         * <br></br>This is used to upload data to discord for various purposes.
         *
         *
         * This opens a [FileInputStream], which will be closed on consumption by the request.
         * You can use [FileUpload.close] to close the stream manually.
         *
         * @param  file
         * The [File] to upload
         * @param  name
         * The representative name to use for the file
         *
         * @throws IllegalArgumentException
         * If null is provided or the name is empty
         * @throws UncheckedIOException
         * If an IOException is thrown while opening the file
         *
         * @return [FileUpload]
         *
         * @see java.io.FileInputStream FileInputStream
         */
        @Nonnull
        fun fromData(@Nonnull file: File?, @Nonnull name: String): FileUpload? {
            return FileUpload.Companion.fromData(file, name)
        }

        /**
         * Create a new [FileUpload] for a local file.
         * <br></br>This is used to upload data to discord for various purposes.
         *
         *
         * This opens a [FileInputStream], which will be closed on consumption by the request.
         * You can use [FileUpload.close] to close the stream manually.
         *
         * @param  file
         * The [File] to upload
         *
         * @throws IllegalArgumentException
         * If null is provided
         * @throws UncheckedIOException
         * If an IOException is thrown while opening the file
         *
         * @return [FileUpload]
         *
         * @see java.io.FileInputStream FileInputStream
         *
         * @see .fromData
         */
        @Nonnull
        fun fromData(@Nonnull file: File): FileUpload? {
            return FileUpload.Companion.fromData(file)
        }

        /**
         * Create a new [FileUpload] for a local file.
         * <br></br>This is used to upload data to discord for various purposes.
         *
         *
         * This opens the path using [Files.newInputStream], which will be closed on consumption by the request.
         * You can use [FileUpload.close] to close the stream manually.
         *
         * @param  path
         * The [Path] of the file to upload
         * @param  name
         * The representative name to use for the file
         * @param  options
         * The [OpenOptions][OpenOption] specifying how the file is opened
         *
         * @throws IllegalArgumentException
         * If null is provided or the name is empty
         * @throws UncheckedIOException
         * If an IOException is thrown while opening the file
         *
         * @return [FileUpload]
         */
        @Nonnull
        fun fromData(@Nonnull path: Path, @Nonnull name: String, @Nonnull vararg options: OpenOption?): FileUpload? {
            return FileUpload.Companion.fromData(path, name, *options)
        }

        /**
         * Create a new [FileUpload] for a local file.
         * <br></br>This is used to upload data to discord for various purposes.
         * Uses [Path.getFileName] to specify the name of the file, to customize the filename use [.fromData].
         *
         *
         * This opens the path using [Files.newInputStream], which will be closed on consumption by the request.
         * You can use [FileUpload.close] to close the stream manually.
         *
         * @param  path
         * The [Path] of the file to upload
         * @param  options
         * The [OpenOptions][OpenOption] specifying how the file is opened
         *
         * @throws IllegalArgumentException
         * If null is provided
         * @throws UncheckedIOException
         * If an IOException is thrown while opening the file
         *
         * @return [FileUpload]
         */
        @Nonnull
        fun fromData(@Nonnull path: Path, @Nonnull vararg options: OpenOption?): FileUpload? {
            return FileUpload.Companion.fromData(path, *options)
        }

        /**
         * Creates an [AttachmentUpdate] with the given attachment id.
         * <br></br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
         *
         * @param  id
         * The id of the attachment to retain
         *
         * @return [AttachmentUpdate]
         */
        @Nonnull
        fun fromAttachment(id: Long): AttachmentUpdate? {
            return AttachmentUpdate.Companion.fromAttachment(id)
        }

        /**
         * Creates an [AttachmentUpdate] with the given attachment id.
         * <br></br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
         *
         * @param  id
         * The id of the attachment to retain
         *
         * @throws IllegalArgumentException
         * If the id is not a valid snowflake
         *
         * @return [AttachmentUpdate]
         */
        @Nonnull
        fun fromAttachment(@Nonnull id: String): AttachmentUpdate? {
            return AttachmentUpdate.Companion.fromAttachment(id)
        }

        /**
         * Creates an [AttachmentUpdate] with the given attachment.
         * <br></br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
         *
         * @param  attachment
         * The attachment to retain
         *
         * @return [AttachmentUpdate]
         */
        @Nonnull
        fun fromAttachment(@Nonnull attachment: Message.Attachment): AttachmentUpdate? {
            return AttachmentUpdate.Companion.fromAttachment(attachment)
        }

        /**
         * Build a complete request using the provided files and payload data.
         *
         * @param  files
         * The files to upload/edit
         *
         * @throws IllegalArgumentException
         * If the file list is null
         *
         * @return [MultipartBody.Builder]
         */
        @Nonnull
        fun createMultipartBody(@Nonnull files: List<AttachedFile>): Builder? {
            return createMultipartBody(files, null as RequestBody?)
        }

        /**
         * Build a complete request using the provided files and payload data.
         *
         * @param  files
         * The files to upload/edit
         * @param  payloadJson
         * The payload data to send, null to not add a payload_json part
         *
         * @throws IllegalArgumentException
         * If the file list is null
         *
         * @return [MultipartBody.Builder]
         */
        @Nonnull
        fun createMultipartBody(@Nonnull files: List<AttachedFile>, payloadJson: DataObject?): Builder? {
            val body: RequestBody? =
                if (payloadJson != null) create.create(payloadJson.toJson(), Requester.MEDIA_TYPE_JSON) else null
            return createMultipartBody(files, body)
        }

        /**
         * Build a complete request using the provided files and payload data.
         *
         * @param  files
         * The files to upload/edit
         * @param  payloadJson
         * The payload data to send, null to not add a payload_json part
         *
         * @throws IllegalArgumentException
         * If the file list is null
         *
         * @return [MultipartBody.Builder]
         */
        @Nonnull
        fun createMultipartBody(@Nonnull files: List<AttachedFile>, payloadJson: RequestBody?): Builder? {
            val builder: Builder = Builder().setType(FORM)
            for (i in files.indices) {
                val file = files[i]
                file.addPart(builder, i)
            }
            if (payloadJson != null) builder.addFormDataPart("payload_json", null, payloadJson)
            return builder
        }

        /**
         * The maximum length a [description][FileUpload.setDescription] can be ({@value}).
         */
        const val MAX_DESCRIPTION_LENGTH = 1024
    }
}
