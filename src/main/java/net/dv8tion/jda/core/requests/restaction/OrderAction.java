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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class OrderAction<T, M extends OrderAction> extends RestAction<Void>
{
    protected final Guild guild;
    protected final List<T> orderList;
    protected int selectedPosition = -1;

    public OrderAction(Guild guild, Route.CompiledRoute route)
    {
        super(guild.getJDA(), route, null);

        this.guild = guild;
        this.orderList = new ArrayList<>();
    }

    public List<T> currentOrder()
    {
        return Collections.unmodifiableList(orderList);
    }

    public M selectPosition(int selectedPosition)
    {
        Args.notNegative(selectedPosition, "Provided selectedPosition");
        Args.check(selectedPosition < orderList.size(), "Provided selectedPosition is too big and is out of bounds. selectedPosition: " + selectedPosition);

        this.selectedPosition = selectedPosition;

        return (M) this;
    }

    public M moveUp(int amount)
    {
        Args.notNegative(amount, "Provided amount");
        if (selectedPosition == -1)
            throw new IllegalStateException("Cannot move until an item has been selected. Use #selectPosition first.");
        Args.check(selectedPosition - amount >= 0,
                "Amount provided to move up is too large and would be out of bounds." +
                        "Selected position: " + selectedPosition + " Amount: " + amount + " Largest Position: " + orderList.size());

        return moveTo(selectedPosition - amount);
    }

    public M moveDown(int amount)
    {
        Args.notNegative(amount, "Provided amount");
        if (selectedPosition == -1)
            throw new IllegalStateException("Cannot move until an item has been selected. Use #selectPosition first.");

        Args.check(selectedPosition + amount < orderList.size(),
                "Amount provided to move down is too large and would be out of bounds." +
                        "Selected position: " + selectedPosition + " Amount: " + amount + " Largest Position: " + orderList.size());

        return moveTo(selectedPosition + amount);
    }

    public M moveTo(int position)
    {
        Args.notNegative(position, "Provided position");
        Args.check(position < orderList.size(), "Provided position is too big and is out of bounds.");

        T selectedItem = orderList.remove(selectedPosition);
        orderList.add(position, selectedItem);

        return (M) this;
    }

    public M swapPosition(int swapPosition)
    {
        Args.notNegative(swapPosition, "Provided swapPosition");
        Args.check(swapPosition < orderList.size(), "Provided swapPosition is too big and is out of bounds. swapPosition: "
                + swapPosition);

        T selectedItem = orderList.get(selectedPosition);
        T swapItem = orderList.get(swapPosition);
        orderList.set(swapPosition, selectedItem);
        orderList.set(selectedPosition, swapItem);

        return (M) this;
    }

    public Guild getGuild()
    {
        return guild;
    }

    public int getSelectedPosition()
    {
        return selectedPosition;
    }

    @Override
    protected void handleResponse(Response response, Request request)
    {
        if (response.isOk())
            request.onSuccess(null);
        else
            request.onFailure(response);
    }

    @Override
    protected abstract void finalizeData();
}
