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

import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.DataObject.Companion.empty
import net.dv8tion.jda.internal.requests.Requester
import net.dv8tion.jda.internal.utils.*
import net.dv8tion.jda.internal.utils.requestbody.DataSupplierBody
import net.dv8tion.jda.internal.utils.requestbody.TypedBody
import okhttp3.MediaType
import okhttp3.MultipartBody.Builder.addFormDataPart
import okhttp3.RequestBody
import okio.Source
import okio.buffer
import okio.source
import java.io.*
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path
import java.util.function.Supplier
import javax.annotation.Nonnull

/**
 * Represents a file that is intended to be uploaded to Discord for arbitrary requests.
 * <br></br>This is used to upload data to discord for various purposes.
 *
 *
 * The [InputStream] will be closed on consumption by the request.
 * You can use [.close] to close the stream manually.
 */
class FileUpload : Closeable, AttachedFile {
    private val resource: InputStream?
    private val resourceSupplier: Supplier<out Source>?

    /**
     * The filename for the file.
     *
     * @return The filename
     */
    @get:Nonnull
    var name: String
        private set
    private var body: TypedBody<*>? = null

    /**
     * The description for the file.
     *
     * @return The description
     */
    var description: String? = null
        private set

    protected constructor(resource: InputStream?, name: String) {
        this.resource = resource
        resourceSupplier = null
        this.name = name
    }

    protected constructor(resourceSupplier: Supplier<out Source>?, name: String) {
        this.resourceSupplier = resourceSupplier
        resource = null
        this.name = name
    }

    /**
     * Changes the name of this file, to be prefixed as `SPOILER_`.
     * <br></br>This will cause the file to be rendered as a spoiler attachment in the client.
     *
     * @return The updated FileUpload instance
     */
    @Nonnull
    fun asSpoiler(): FileUpload {
        return if (name.startsWith("SPOILER_")) this else setName("SPOILER_$name")
    }

    /**
     * Changes the name of this file.
     *
     * @param  name
     * The new filename
     *
     * @throws IllegalArgumentException
     * If the name is null, blank, or empty
     *
     * @return The updated FileUpload instance
     */
    @Nonnull
    fun setName(@Nonnull name: String): FileUpload {
        Checks.notBlank(name, "Name")
        this.name = name
        return this
    }

    /**
     * Set the file description used as ALT text for screenreaders.
     *
     * @param  description
     * The alt text describing this file attachment (up to {@value MAX_DESCRIPTION_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     * If the description is longer than {@value MAX_DESCRIPTION_LENGTH} characters
     *
     * @return The same FileUpload instance with the new description
     */
    @Nonnull
    fun setDescription(description: String?): FileUpload {
        var description = description
        if (description != null) Checks.notLonger(description.trim { it <= ' ' }
            .also { description = it }, AttachedFile.Companion.MAX_DESCRIPTION_LENGTH, "Description")
        this.description = description
        return this
    }

    @get:Nonnull
    val data: InputStream
        /**
         * The [InputStream] representing the data to upload as a file.
         *
         * @return The [InputStream]
         */
        get() = resource ?: resourceSupplier!!.get().buffer().inputStream()

    /**
     * Creates a re-usable instance of [RequestBody] with the specified content-type.
     *
     *
     * This body will automatically close the [resource][.getData] when the request is done.
     * However, since the body buffers the data, it can be used multiple times regardless.
     *
     * @param  type
     * The content-type to use for the body (e.g. `"application/octet-stream"`)
     *
     * @throws IllegalArgumentException
     * If the content-type is null
     *
     * @return [RequestBody]
     */
    @Nonnull
    @Synchronized
    fun getRequestBody(@Nonnull type: MediaType?): RequestBody {
        Checks.notNull(type, "Type")
        if (body != null) // This allows FileUpload to be used more than once!
            return body!!.withType(type!!)
        return if (resource == null) DataSupplierBody(type, resourceSupplier).also {
            body = it
        } else IOUtil.createRequestBody(type, resource).also { body = it }
    }

    @Synchronized
    override fun addPart(@Nonnull builder: Builder, index: Int) {
        builder.addFormDataPart("files[$index]", name, getRequestBody(Requester.MEDIA_TYPE_OCTET))
    }

    @Nonnull
    override fun toAttachmentData(index: Int): DataObject {
        return empty()
            .put("id", index)
            .put("description", if (description == null) "" else description)
            .put("filename", name)
    }

    @Synchronized
    @Throws(IOException::class)
    override fun close() {
        if (body == null) forceClose()
    }

    @Throws(IOException::class)
    override fun forceClose() {
        resource?.close()
    }

    @Suppress("deprecation")
    protected fun finalize() {
        if (body == null && resource != null) // Only close if the resource was never used
            IOUtil.silentClose(resource)
    }

    override fun toString(): String {
        return EntityString("AttachedFile")
            .setType("Data")
            .setName(name)
            .toString()
    }

    companion object {
        /**
         * Creates a FileUpload that sources its data from the supplier.
         * <br></br>The supplier *must* return a new stream on every call.
         *
         *
         * The streams are expected to always be at the beginning, when they are taken from the supplier.
         * If the supplier returned the same stream instance, the reader would start at the wrong position when re-attempting a request.
         *
         *
         * When this supplier factory is used, [.getData] will return a new instance on each call.
         * It is the responsibility of the caller to close that stream.
         *
         * @param  name
         * The file name
         * @param  supplier
         * The resource supplier, which returns a new stream on each call
         *
         * @throws IllegalArgumentException
         * If null is provided or the name is blank
         *
         * @return [FileUpload]
         */
        @Nonnull
        fun fromStreamSupplier(@Nonnull name: String, @Nonnull supplier: Supplier<out InputStream>): FileUpload {
            Checks.notNull(supplier, "Supplier")
            return fromSourceSupplier(name) { supplier.get().source() }
        }

        /**
         * Creates a FileUpload that sources its data from the supplier.
         * <br></br>The supplier *must* return a new stream on every call.
         *
         *
         * The streams are expected to always be at the beginning, when they are taken from the supplier.
         * If the supplier returned the same stream instance, the reader would start at the wrong position when re-attempting a request.
         *
         *
         * When this supplier factory is used, [.getData] will return a new instance on each call.
         * It is the responsibility of the caller to close that stream.
         *
         * @param  name
         * The file name
         * @param  supplier
         * The resource supplier, which returns a new [Source] on each call
         *
         * @throws IllegalArgumentException
         * If null is provided or the name is blank
         *
         * @return [FileUpload]
         */
        @Nonnull
        fun fromSourceSupplier(@Nonnull name: String, @Nonnull supplier: Supplier<out Source>?): FileUpload {
            Checks.notNull(supplier, "Supplier")
            Checks.notBlank(name, "Name")
            return FileUpload(supplier, name)
        }

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
        fun fromData(@Nonnull data: InputStream?, @Nonnull name: String): FileUpload {
            Checks.notNull(data, "Data")
            Checks.notBlank(name, "Name")
            return FileUpload(data, name)
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
        fun fromData(@Nonnull data: ByteArray?, @Nonnull name: String): FileUpload {
            Checks.notNull(data, "Data")
            Checks.notNull(name, "Name")
            return fromData(ByteArrayInputStream(data), name)
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
        fun fromData(@Nonnull file: File?, @Nonnull name: String): FileUpload {
            Checks.notNull(file, "File")
            return try {
                fromData(FileInputStream(file), name)
            } catch (e: FileNotFoundException) {
                throw UncheckedIOException(e)
            }
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
        fun fromData(@Nonnull file: File): FileUpload {
            Checks.notNull(file, "File")
            return try {
                fromData(FileInputStream(file), file.getName())
            } catch (e: FileNotFoundException) {
                throw UncheckedIOException(e)
            }
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
        fun fromData(@Nonnull path: Path, @Nonnull name: String, @Nonnull vararg options: OpenOption?): FileUpload {
            Checks.notNull(path, "Path")
            Checks.noneNull(options, "Options")
            Checks.check(Files.isReadable(path), "File for specified path cannot be read. Path: %s", path)
            return try {
                fromData(Files.newInputStream(path, *options), name)
            } catch (e: IOException) {
                throw UncheckedIOException("Could not open file for specified path. Path: $path", e)
            }
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
        fun fromData(@Nonnull path: Path, @Nonnull vararg options: OpenOption?): FileUpload {
            Checks.notNull(path, "Path")
            val fileName = path.fileName
            Checks.check(fileName != null, "Path does not have a file name. Path: %s", path)
            return fromData(path, fileName.toString(), *options)
        }
    }
}
