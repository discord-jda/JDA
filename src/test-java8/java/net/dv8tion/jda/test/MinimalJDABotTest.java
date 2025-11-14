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

package net.dv8tion.jda.test;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MinimalJDABotTest {
    @Test
    void testCurrentJavaVersion() {
        assertThat(System.getProperty("java.version")).startsWith("1.8");
    }

    @Test
    void testFailedLogin() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new RequestInterceptor())
                .build();

        assertThatExceptionOfType(ErrorResponseException.class)
                .isThrownBy(() -> JDABuilder.createLight("INVALID_TOKEN")
                        .setHttpClient(httpClient)
                        .build())
                .withCauseExactlyInstanceOf(RequestInterceptedException.class);
    }

    private static class RequestInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) {
            throw new RequestInterceptedException();
        }
    }

    private static class RequestInterceptedException extends RuntimeException {}
}
