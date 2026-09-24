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

package net.dv8tion.jda.internal.utils

import org.slf4j.Marker
import org.slf4j.event.Level
import org.slf4j.helpers.LegacyAbstractLogger
import org.slf4j.helpers.MessageFormatter
import java.time.LocalDateTime

class FallbackLogger(
    name: String,
) : LegacyAbstractLogger() {
    init {
        this.name = name
    }

    override fun getFullyQualifiedCallerName(): String? = null

    override fun handleNormalizedLoggingCall(
        level: Level?,
        marker: Marker?,
        messagePattern: String?,
        arguments: Array<out Any?>?,
        throwable: Throwable?,
    ) {
        val now = LocalDateTime.now()
        val result = MessageFormatter.arrayFormat(messagePattern, arguments)
        System.err.printf("%1\$tF %1\$tT [%2\$s] [%3\$s] %4\$s%n", now, name, level, result.message)
        throwable?.printStackTrace(System.err)
    }

    override fun isTraceEnabled(): Boolean = false

    override fun isDebugEnabled(): Boolean = false

    override fun isInfoEnabled(): Boolean = true

    override fun isWarnEnabled(): Boolean = true

    override fun isErrorEnabled(): Boolean = true
}
