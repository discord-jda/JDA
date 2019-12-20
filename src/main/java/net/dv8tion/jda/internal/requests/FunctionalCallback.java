/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.internal.requests;

import net.dv8tion.jda.api.utils.IOBiConsumer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.function.BiConsumer;

public class FunctionalCallback implements Callback
{
    private final BiConsumer<Call, IOException> failure;
    private final IOBiConsumer<Call, Response> success;

    public FunctionalCallback(BiConsumer<Call, IOException> failure, IOBiConsumer<Call, Response> success)
    {
        this.failure = failure;
        this.success = success;
    }

    public static Builder onSuccess(IOBiConsumer<Call, Response> callback)
    {
        return new Builder().onSuccess(callback);
    }

    public static Builder onFailure(BiConsumer<Call, IOException> callback)
    {
        return new Builder().onFailure(callback);
    }

    @Override
    public void onFailure(@Nonnull Call call, @Nonnull IOException e)
    {
        if (failure != null)
            failure.accept(call, e);
    }

    @Override
    public void onResponse(@Nonnull Call call, @Nonnull Response response) throws IOException
    {
        if (success != null)
            success.accept(call, response);
    }

    public static class Builder
    {
        private BiConsumer<Call, IOException> failure;
        private IOBiConsumer<Call, Response> success;

        public Builder onSuccess(IOBiConsumer<Call, Response> callback)
        {
            this.success = callback;
            return this;
        }

        public Builder onFailure(BiConsumer<Call, IOException> callback)
        {
            this.failure = callback;
            return this;
        }

        public FunctionalCallback build()
        {
            return new FunctionalCallback(failure, success);
        }
    }
}
