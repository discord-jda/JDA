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

import net.dv8tion.jda.api.requests.Method;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Requester;
import org.jetbrains.annotations.Contract;
import org.mockito.ThrowingConsumer;

import javax.annotation.Nonnull;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.doNothing;

public class RestActionAssertions implements ThrowingConsumer<Request<?>>
{
    private final List<ThrowingConsumer<Request<?>>> assertions = new ArrayList<>();
    private Consumer<? super DataObject> normalizeRequestBody = (v) -> {};

    public static RestActionAssertions assertThatNextAction(Requester requester)
    {
        RestActionAssertions assertions = new RestActionAssertions();
        doNothing().when(requester).request(assertArg(assertions::acceptThrows));
        return assertions;
    }

    @Contract("_->this")
    public RestActionAssertions withNormalizedBody(@Nonnull Consumer<? super DataObject> normalizer)
    {
        this.normalizeRequestBody = normalizer;
        return this;
    }

    @Contract("_->this")
    public RestActionAssertions checkAssertions(@Nonnull ThrowingConsumer<Request<?>> assertion)
    {
        assertions.add(assertion);
        return this;
    }

    @Contract("_->this")
    public RestActionAssertions hasBodyEqualTo(@Nonnull DataObject expected)
    {
        return checkAssertions(request -> {
            Object body = request.getRawBody();
            assertThat(body)
                .isNotNull()
                .isInstanceOf(DataObject.class);

            DataObject dataObject = (DataObject) body;
            normalizeRequestBody.accept(dataObject);
            normalizeRequestBody.accept(expected);

            assertThat(dataObject.toPrettyString())
                .as("RestAction should send request using expected request body")
                .isEqualTo(expected.toPrettyString());
        });
    }

    @Contract("_->this")
    public RestActionAssertions hasMethod(@Nonnull Method method)
    {
        return checkAssertions(request ->
            assertThat(request.getRoute().getMethod())
                .as("RestAction should send request using expected HTTP Method")
                .isEqualTo(method)
        );
    }

    @Contract("_->this")
    public RestActionAssertions hasCompiledRoute(@Nonnull String route)
    {
        return checkAssertions(request ->
            assertThat(request.getRoute().getCompiledRoute())
                .as("RestAction should send request using expected REST endpoint")
                .isEqualTo(route)
        );
    }

    @Contract("_->this")
    public RestActionAssertions hasAuditReason(@Nonnull String reason)
    {
        return checkAssertions(request ->
            assertThat(request.getHeaders())
                .as("RestAction should set header")
                .contains(new AbstractMap.SimpleEntry<>("X-Audit-Log-Reason", reason))
        );
    }

    @Override
    public void acceptThrows(Request<?> request) throws Throwable
    {
        for (ThrowingConsumer<Request<?>> assertion : assertions)
        {
            assertion.acceptThrows(request);
        }
    }
}
