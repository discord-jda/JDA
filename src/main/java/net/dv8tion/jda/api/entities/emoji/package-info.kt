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
 * Types relating to unicode and custom emoji as used in the API.
 */
package net.dv8tion.jda.api.entities.emoji

import net.dv8tion.jda.api.entities.ISnowflake.id
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake
import net.dv8tion.jda.internal.entities.sticker.StickerSnowflakeImpl
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.utils.ImageProxy
import net.dv8tion.jda.api.entities.sticker.RichSticker
import net.dv8tion.jda.api.entities.sticker.StandardSticker
import net.dv8tion.jda.api.entities.sticker.StickerPack
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.utils.data.SerializableData
import java.util.FormattableFlags
import net.dv8tion.jda.internal.utils.EncodingUtil
import net.dv8tion.jda.internal.entities.emoji.UnicodeEmojiImpl
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.managers.CustomEmojiManager
import net.dv8tion.jda.internal.utils.PermissionUtil
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.managers.GuildStickerManager
import net.dv8tion.jda.api.entities.sticker.GuildSticker
import net.dv8tion.jda.api.entities.sticker.Sticker.StickerFormat
import net.dv8tion.jda.api.entities.sticker.Sticker
