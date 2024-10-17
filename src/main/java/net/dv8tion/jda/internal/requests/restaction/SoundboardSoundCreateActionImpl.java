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

package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.SoundboardSound;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.SoundboardSoundCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class SoundboardSoundCreateActionImpl extends AuditableRestActionImpl<SoundboardSound> implements SoundboardSoundCreateAction
{
    private final String name;
    private final FileUpload file;
    private double volume = 1;
    private Emoji emoji;

    public SoundboardSoundCreateActionImpl(JDA api, Route.CompiledRoute route, String name, FileUpload file)
    {
        super(api, route);
        this.name = name;
        this.file = file;
    }

    @Nonnull
    @Override
    public SoundboardSoundCreateAction timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (SoundboardSoundCreateAction) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public SoundboardSoundCreateAction addCheck(@Nonnull BooleanSupplier checks)
    {
        return (SoundboardSoundCreateAction) super.addCheck(checks);
    }

    @Nonnull
    @Override
    public SoundboardSoundCreateAction setCheck(BooleanSupplier checks)
    {
        return (SoundboardSoundCreateAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public SoundboardSoundCreateAction deadline(long timestamp)
    {
        return (SoundboardSoundCreateAction) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public SoundboardSoundCreateAction setVolume(double volume)
    {
        Checks.check(volume >= 0 && volume <= 1, "Volume must be between 0 and 1");
        this.volume = volume;
        return this;
    }

    @Nonnull
    @Override
    public SoundboardSoundCreateAction setEmoji(@Nullable Emoji emoji)
    {
        this.emoji = emoji;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        try
        {
            final DataObject json = DataObject.empty()
                    .put("name", name)
                    .put("sound", "data:" + getMime() + ";base64," + getBase64Sound())
                    .put("volume", volume);

            if (emoji instanceof UnicodeEmoji) {
                json.put("emoji_name", emoji.getName());
            } else if (emoji instanceof CustomEmoji) {
                json.put("emoji_id", ((CustomEmoji) emoji).getId());
            }

            return getRequestBody(json);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException("Unable to get request body when creating a guild soundboard sound", e);
        }
    }

    @Nonnull
    private String getBase64Sound() throws IOException
    {
        final byte[] data = IOUtil.readFully(file.getData());
        final byte[] b64 = Base64.getEncoder().encode(data);
        return new String(b64, StandardCharsets.UTF_8);
    }

    @Nonnull
    private String getMime()
    {
        int index = file.getName().lastIndexOf('.');
        Checks.check(index > -1, "Filename for soundboard sound is missing file extension. Provided: '" + file.getName() + "'. Must be MP3 or OGG.");

        String extension = file.getName().substring(index + 1).toLowerCase(Locale.ROOT);
        String mime;
        switch (extension)
        {
        case "mp3":
            mime = "audio/mpeg";
            break;
        case "ogg":
            mime = "audio/ogg";
            break;
        default:
            throw new IllegalArgumentException("Unsupported file extension: '." + extension + "', must be MP3 or OGG.");
        }
        return mime;
    }

    @Override
    protected void handleSuccess(Response response, Request<SoundboardSound> request)
    {
        request.onSuccess(api.getEntityBuilder().createSoundboardSound(response.getObject()));
    }
}
