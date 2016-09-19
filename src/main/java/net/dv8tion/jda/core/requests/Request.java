/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.requests;

import com.mashape.unirest.http.HttpMethod;

import java.util.function.Consumer;

public class Request<T>
{
    final RestAction<T> restAction;
    final Consumer<T> onSuccess;
    final Consumer<Throwable> onFailure;
    final boolean shouldQueue;

    Request(RestAction<T> restAction, Consumer<T> onSuccess, Consumer<Throwable> onFailure, boolean shouldQueue)
    {
        this.restAction = restAction;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
        this.shouldQueue = shouldQueue;
    }

    public RestAction<T> getRestAction()
    {
        return restAction;
    }

    public Consumer<T> getOnSuccess()
    {
        return onSuccess;
    }

    public Consumer<Throwable> getOnFailure()
    {
        return onFailure;
    }

    public Route.CompiledRoute getRoute()
    {
        return restAction.route;
    }

    public Object getData()
    {
        return restAction.data;
    }

    public boolean shouldQueue()
    {
        return shouldQueue;
    }
}
