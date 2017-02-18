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

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.apache.http.util.Args;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class OrderAction<T, M extends OrderAction> extends RestAction<Void>
{
    protected final JDA api;
    protected final List<T> orderList;
    protected final boolean ascendingOrder;
    protected int selectedPosition = -1;

    public OrderAction(JDA api, Route.CompiledRoute route)
    {
        this(api, true, route);
    }

    public OrderAction(JDA api, boolean ascendingOrder, Route.CompiledRoute route)
    {
        super(api, route, null);
        this.api = api;
        this.orderList = new ArrayList<>();
        this.ascendingOrder = ascendingOrder;
    }

    public JDA getJDA()
    {
        return api;
    }

    public List<T> getCurrentOrder()
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

    public M selectPosition(T selectedEntity)
    {
        Args.notNull(selectedEntity, "Channel");
        validateInput(selectedEntity);

        return selectPosition(orderList.indexOf(selectedEntity));
    }

    public int getSelectedPosition()
    {
        return selectedPosition;
    }

    public T getSelectedEntity()
    {
        if (selectedPosition == -1)
            throw new IllegalStateException("No position has been selected yet");

        return orderList.get(selectedPosition);
    }

    public M moveUp(int amount)
    {
        Args.notNegative(amount, "Provided amount");
        if (selectedPosition == -1)
            throw new IllegalStateException("Cannot move until an item has been selected. Use #selectPosition first.");
        if (ascendingOrder)
        {
            Args.check(selectedPosition - amount >= 0,
                    "Amount provided to move up is too large and would be out of bounds." +
                            "Selected position: " + selectedPosition + " Amount: " + amount + " Largest Position: " + orderList.size());
        }
        else
        {
            Args.check(selectedPosition + amount < orderList.size(),
                    "Amount provided to move up is too large and would be out of bounds." +
                            "Selected position: " + selectedPosition + " Amount: " + amount + " Largest Position: " + orderList.size());
        }

        if (ascendingOrder)
            return moveTo(selectedPosition - amount);
        else
            return moveTo(selectedPosition + amount);
    }

    public M moveDown(int amount)
    {
        Args.notNegative(amount, "Provided amount");
        if (selectedPosition == -1)
            throw new IllegalStateException("Cannot move until an item has been selected. Use #selectPosition first.");

        if (ascendingOrder)
        {
            Args.check(selectedPosition + amount < orderList.size(),
                    "Amount provided to move down is too large and would be out of bounds." +
                            "Selected position: " + selectedPosition + " Amount: " + amount + " Largest Position: " + orderList.size());
        }
        else
        {
            Args.check(selectedPosition - amount >= orderList.size(),
                    "Amount provided to move down is too large and would be out of bounds." +
                            "Selected position: " + selectedPosition + " Amount: " + amount + " Largest Position: " + orderList.size());
        }

        if (ascendingOrder)
            return moveTo(selectedPosition + amount);
        else
            return moveTo(selectedPosition - amount);
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

    public M swapPosition(T swapEntity)
    {
        Args.notNull(swapEntity, "Provided swapEntity");
        validateInput(swapEntity);

        return swapPosition(orderList.indexOf(swapEntity));
    }

    public M reverseOrder()
    {
        Collections.reverse(this.orderList);
        return (M) this;
    }

    public M shuffleOrder()
    {
        Collections.shuffle(this.orderList);
        return (M) this;
    }

    public M sortOrder(final Comparator<T> comparator)
    {
        Args.notNull(comparator, "Provided comparator");

        this.orderList.sort(comparator);
        return (M) this;
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
    protected abstract void validateInput(T entity);
}
