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

package net.dv8tion.jda.api.components.attachmentupload;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.attribute.ICustomId;
import net.dv8tion.jda.api.components.label.LabelChildComponent;
import net.dv8tion.jda.internal.components.attachmentupload.AttachmentUploadImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Component accepting files from users.
 *
 * <p>The user can send up to {@value #MAX_UPLOADS} files, the requested number of files can be adjusted or be completely optional.
 *
 * <p>Can only be used inside {@link net.dv8tion.jda.api.components.label.Label Labels}!
 */
public interface AttachmentUpload extends Component, ICustomId, LabelChildComponent {
    /** The maximum number of files the user can upload at once ({@value}) */
    int MAX_UPLOADS = 10;

    /** The maximum length a custom ID can have ({@value}) */
    int ID_MAX_LENGTH = 100;

    /**
     * Creates a new {@link AttachmentUpload.Builder} with the provided custom ID.
     * <br>By default, the user will be <b>required</b> to submit a <b>single</b> attachment.
     *
     * @param  customId
     *         The custom ID of this component, can be used to pass data, then read in an interaction
     *
     * @throws IllegalArgumentException
     *         If {@code customId} is {@code null}, blank, or longer than {@value #ID_MAX_LENGTH} characters
     *
     * @return The new {@link AttachmentUpload.Builder}
     */
    @Nonnull
    static AttachmentUpload.Builder create(@Nonnull String customId) {
        return new AttachmentUpload.Builder(customId);
    }

    /**
     * Creates a new {@link AttachmentUpload} with the provided custom ID.
     * <br>The user will be <b>required</b> to submit a <b>single</b> attachment.
     *
     * @param  customId
     *         The custom ID of this component, can be used to pass data, then read in an interaction
     *
     * @throws IllegalArgumentException
     *         If {@code customId} is {@code null}, blank, or longer than {@value #ID_MAX_LENGTH} characters
     *
     * @return The new {@link AttachmentUpload}
     */
    @Nonnull
    static AttachmentUpload of(@Nonnull String customId) {
        return create(customId).build();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    AttachmentUpload withUniqueId(int uniqueId);

    /**
     * The minimum amount of attachments the user must send.
     *
     * @return Minimum amount of attachments the user must send
     */
    int getMinValues();

    /**
     * The maximum amount of attachments the user can send.
     *
     * @return Maximum amount of attachments the user can send
     */
    int getMaxValues();

    /**
     * Whether the user must send attachments.
     *
     * <p>This attribute is completely separate from the value range,
     * for example you can have an optional {@link AttachmentUpload} with the range set to {@code [2 ; 2]},
     * meaning you accept either 0 attachments, or 2.
     *
     * @return {@code true} if files must be uploaded, {@code false} if not
     */
    boolean isRequired();

    /**
     * Builder for {@link AttachmentUpload AttachmentUploads}.
     */
    class Builder {
        protected int uniqueId = -1;
        protected String customId;
        protected int minValues = 1;
        protected int maxValues = 1;
        protected boolean required = true;

        protected Builder(@Nonnull String customId) {
            setCustomId(customId);
        }

        @Nonnull
        public Builder setUniqueId(int uniqueId) {
            Checks.positive(uniqueId, "Unique ID");
            this.uniqueId = uniqueId;
            return this;
        }

        /**
         * Changes the custom ID.
         * <br>This ID can be used to pass data, then read in an interaction.
         *
         * @param  customId
         *         The new custom ID
         *
         * @throws IllegalArgumentException
         *         If {@code customId} is {@code null}, blank, or longer than {@value #ID_MAX_LENGTH} characters
         *
         * @return The same instance, for chaining purposes
         */
        @Nonnull
        public Builder setCustomId(@Nonnull String customId) {
            Checks.notBlank(customId, "Custom ID");
            Checks.notLonger(customId, ID_MAX_LENGTH, "Custom ID");
            this.customId = customId;
            return this;
        }

        /**
         * Changes the minimum amount of attachments the user has to send.
         * <br>Default: {@code 1}
         *
         * @param  minValues
         *         The new minimum amount of attachments the user has to send, must be >= 0 and less than {@value #MAX_UPLOADS}
         *
         * @throws IllegalArgumentException
         *         If {@code minValues} is negative or larger than {@value #MAX_UPLOADS}
         *
         * @return The same instance, for chaining purposes
         */
        @Nonnull
        public Builder setMinValues(int minValues) {
            Checks.notNegative(minValues, "Min values");
            Checks.check(minValues <= MAX_UPLOADS, "Min values (%s) must be lower than %s", minValues, MAX_UPLOADS);
            this.minValues = minValues;
            return this;
        }

        /**
         * Changes the maximum amount of attachments the user can send.
         * <br>Default: {@code 1}
         *
         * @param  maxValues
         *         The new maximum amount of attachments the user can send, must be positive and less than {@value #MAX_UPLOADS}
         *
         * @throws IllegalArgumentException
         *         If {@code maxValues} is negative, zero, or larger than {@value #MAX_UPLOADS}
         *
         * @return The same instance, for chaining purposes
         */
        @Nonnull
        public Builder setMaxValues(int maxValues) {
            Checks.positive(maxValues, "Max values");
            Checks.check(maxValues <= MAX_UPLOADS, "Max values (%s) must be lower than %s", maxValues, MAX_UPLOADS);
            this.maxValues = maxValues;
            return this;
        }

        /**
         * Changes the amounts of attachments the user can send.
         * <br>Default: {@code [1 ; 1]}
         *
         * @param  min
         *         The new minimum amount of attachments the user must send, must be >= 0 and less than {@value #MAX_UPLOADS}
         * @param  max
         *         The new maximum amount of attachments the user can send, must be positive and less than {@value #MAX_UPLOADS}
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If {@code min} is negative or larger than {@value #MAX_UPLOADS}</li>
         *             <li>If {@code max} is negative, zero, or larger than {@value #MAX_UPLOADS}</li>
         *         </ul>
         *
         * @return The same instance, for chaining purposes
         */
        @Nonnull
        public Builder setRequiredRange(int min, int max) {
            return setMinValues(min).setMaxValues(max);
        }

        /**
         * Changes whether the user must upload files.
         * <br>Default: {@code true}
         *
         * <p>This attribute is completely separate from the value range,
         * for example you can have an optional {@link AttachmentUpload} with the range set to {@code [2 ; 2]},
         * meaning you accept either 0 attachments, or 2.
         *
         * @param  required
         *         The new required status
         *
         * @return The same instance, for chaining purposes
         */
        @Nonnull
        public Builder setRequired(boolean required) {
            this.required = required;
            return this;
        }

        /**
         * The unique, numeric identifier of this component.
         * <br>Can be set manually or automatically assigned by Discord (starting from {@code 1}).
         * If it has not been assigned yet, this will return {@code -1}.
         *
         * @return The unique identifier of this component, or {@code -1} if not assigned yet
         */
        public int getUniqueId() {
            return uniqueId;
        }

        /**
         * Returns the unique custom ID, this can be used to pass data, then read in an interaction.
         *
         * @return The custom ID
         */
        @Nonnull
        public String getCustomId() {
            return customId;
        }

        /**
         * The minimum amount of attachments the user must send.
         *
         * @return Minimum amount of attachments the user must send
         */
        public int getMinValues() {
            return minValues;
        }

        /**
         * The maximum amount of attachments the user can send.
         *
         * @return Maximum amount of attachments the user can send
         */
        public int getMaxValues() {
            return maxValues;
        }

        /**
         * Whether the user must send attachments.
         *
         * <p>This attribute is completely separate from the value range,
         * for example you can have an optional {@link AttachmentUpload} with the range set to {@code [2 ; 2]},
         * meaning you accept either 0 attachments, or 2.
         *
         * @return {@code true} if files must be uploaded, {@code false} if not
         */
        public boolean isRequired() {
            return required;
        }

        /**
         * Builds a new {@link AttachmentUpload}.
         *
         * @throws IllegalArgumentException
         *         If {@linkplain #setMinValues(int) min values} is larger than {@linkplain #setMaxValues(int) max values}
         *
         * @return The new {@link AttachmentUpload}
         */
        @Nonnull
        public AttachmentUpload build() {
            Checks.check(maxValues >= minValues, "Max (%s) must be higher or equal to min (%s)", maxValues, minValues);
            return new AttachmentUploadImpl(uniqueId, customId, minValues, maxValues, required);
        }
    }
}
