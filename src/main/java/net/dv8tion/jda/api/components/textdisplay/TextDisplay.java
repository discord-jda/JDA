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

package net.dv8tion.jda.api.components.textdisplay;

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.section.SectionContentComponent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import net.dv8tion.jda.internal.components.textdisplay.TextDisplayImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * A component to display text, supports Markdown.
 *
 * <p>This component has no content length limit, however,
 * you are still limited to the {@value Message#MAX_CONTENT_LENGTH_COMPONENT_V2} total characters,
 * as imposed by {@linkplain MessageRequest#useComponentsV2() components V2}.
 *
 * <p><b>Requirements:</b> {@linkplain MessageRequest#useComponentsV2() Components V2} needs to be enabled!
 */
public interface TextDisplay
        extends Component,
                MessageTopLevelComponent,
                ModalTopLevelComponent,
                ContainerChildComponent,
                SectionContentComponent {
    /**
     * Constructs a new {@link TextDisplay} from the given content.
     *
     * @param  content
     *         The content of the text display
     *
     * @throws IllegalArgumentException
     *         If the content is {@code null}, blank or empty
     *
     * @return The new {@link TextDisplay}
     */
    @Nonnull
    static TextDisplay of(@Nonnull String content) {
        Checks.notBlank(content, "Content");
        return new TextDisplayImpl(content);
    }

    /**
     * Constructs a new {@link TextDisplay} from the given format string and arguments.
     *
     * @param  format
     *         The content to format with the following arguments
     * @param  args
     *         The arguments to format the content with,
     *         if there are more arguments than format specifiers, the extra arguments are ignored.
     *         The number of arguments is variable and may be zero.
     *         The behaviour on a {@code null} argument depends on the <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#syntax" target="_blank">conversion</a>.
     *
     * @throws IllegalArgumentException
     *         If the format string is {@code null}, or the resulting content is blank
     * @throws java.util.IllegalFormatException
     *         If the format string contains an illegal syntax,
     *         a format specifier that is incompatible with the given arguments,
     *         insufficient arguments given the format string, or other illegal conditions.
     *         For specification of all possible formatting errors,
     *         see the <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#detail" target="_blank">Details</a> section of the formatter class specification.
     *
     * @return The new {@link TextDisplay}
     */
    @Nonnull
    @FormatMethod
    static TextDisplay ofFormat(@Nonnull @FormatString String format, @Nonnull Object... args) {
        Checks.notNull(format, "Format string");
        Checks.notNull(args, "Format args"); // Null array elements are allowed
        return of(String.format(format, args));
    }

    @Nonnull
    @Override
    @CheckReturnValue
    TextDisplay withUniqueId(int uniqueId);

    /**
     * Creates a new {@link TextDisplay} with the specified content.
     *
     * <p>While there are no per-component limit,
     * you are still limited to the {@value Message#MAX_CONTENT_LENGTH_COMPONENT_V2} total character limit.
     *
     * @param  content
     *         The new content
     *
     * @return The new {@link TextDisplay}
     */
    @Nonnull
    @CheckReturnValue
    TextDisplay withContent(@Nonnull String content);

    /**
     * The content of this {@link TextDisplay}.
     *
     * @return The content
     */
    @Nonnull
    String getContent();
}
