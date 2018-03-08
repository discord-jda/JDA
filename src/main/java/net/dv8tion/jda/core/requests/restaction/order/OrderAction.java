/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.requests.restaction.order;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Extension of {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: Void
 * that allows to modify the order of entities provided as an {@link java.util.ArrayList ArrayList}.
 * <br>This action contains a List or entities for the specified type {@code T} which
 * can be moved within the bounds but not removed, nor can any new entities be added.
 *
 * @param <T>
 *        The entity type for the {@link java.util.List List} of entities
 *        contained in the OrderAction's orderList
 * @param <M>
 *        The extension implementing the abstract operations of this OrderAction,
 *        this will be important for chaining convenience as it returns the specific
 *        implementation rather than a mask of this class. It allows us to implement
 *        chaining operations in this class instead of having to implement it in every
 *        inheriting class!
 *
 * @since 3.0
 */
public abstract class OrderAction<T, M extends OrderAction<T, M>> extends RestAction<Void>
{
    protected final List<T> orderList;
    protected final boolean ascendingOrder;
    protected int selectedPosition = -1;

    /**
     * Creates a new OrderAction instance
     *
     * @param api
     *        JDA instance which is associated with the entities contained
     *        in the order list
     * @param route
     *        The {@link net.dv8tion.jda.core.requests.Route.CompiledRoute CompiledRoute}
     *        which is provided to the {@link RestAction#RestAction(JDA, Route.CompiledRoute, okhttp3.RequestBody) RestAction Constructor}
     */
    public OrderAction(JDA api, Route.CompiledRoute route)
    {
        this(api, true, route);
    }

    /**
     * Creates a new OrderAction instance
     *
     * @param api
     *        JDA instance which is associated with the entities contained
     *        in the order list
     * @param ascendingOrder
     *        Whether or not the order of items should be ascending
     * @param route
     *        The {@link net.dv8tion.jda.core.requests.Route.CompiledRoute CompiledRoute}
     *        which is provided to the {@link RestAction#RestAction(JDA, Route.CompiledRoute, okhttp3.RequestBody) RestAction Constructor}
     */
    public OrderAction(JDA api, boolean ascendingOrder, Route.CompiledRoute route)
    {
        super(api, route);
        this.orderList = new ArrayList<>();
        this.ascendingOrder = ascendingOrder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public M setCheck(BooleanSupplier checks)
    {
        return (M) super.setCheck(checks);
    }

    /**
     * Immutable List representing the currently selected order
     * of entities in this OrderAction instance
     *
     * @return Immutable List representing the current order
     */
    public List<T> getCurrentOrder()
    {
        return Collections.unmodifiableList(orderList);
    }

    /**
     * Selects a new current entity at the specified index
     * <br>This index is in correlation to the {@link #getCurrentOrder() current order}
     *
     * @param  selectedPosition
     *         The index for the new position that will be in focus for all modification
     *         operations
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided position is out-of-bounds
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    #getSelectedPosition()
     * @see    #getSelectedEntity()
     */
    @SuppressWarnings("unchecked")
    public M selectPosition(int selectedPosition)
    {
        Checks.notNegative(selectedPosition, "Provided selectedPosition");
        Checks.check(selectedPosition < orderList.size(), "Provided selectedPosition is too big and is out of bounds. selectedPosition: " + selectedPosition);

        this.selectedPosition = selectedPosition;

        return (M) this;
    }

    /**
     * Selects a new current entity based on the index of
     * the specified entity in the {@link #getCurrentOrder() current order}
     * <br>This is a convenience function that uses {@link #selectPosition(int)} internally
     *
     * @param  selectedEntity
     *         The entity for the new position that will be in focus for all modification
     *         operations
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    #selectPosition(int)
     * @see    #getSelectedPosition()
     * @see    #getSelectedEntity()
     */
    public M selectPosition(T selectedEntity)
    {
        Checks.notNull(selectedEntity, "Channel");
        validateInput(selectedEntity);

        return selectPosition(orderList.indexOf(selectedEntity));
    }

    /**
     * The currently selected position
     * that is in focus for all modification operations of this OrderAction instance
     *
     * @return The currently selected index, or -1 if no position has been selected yet
     */
    public int getSelectedPosition()
    {
        return selectedPosition;
    }

    /**
     * The entity which is currently at the {@link #getSelectedPosition() selected position}
     *
     * @throws java.lang.IllegalStateException
     *         If no entity has been selected yet
     *
     * @return The currently selected entity
     */
    public T getSelectedEntity()
    {
        if (selectedPosition == -1)
            throw new IllegalStateException("No position has been selected yet");

        return orderList.get(selectedPosition);
    }

    /**
     * Moves the currently selected entity {@code amount} positions <b>UP</b>
     * in order by pushing all entities down by one position.
     *
     * @param  amount
     *         The amount of positions that should be moved
     *
     * @throws java.lang.IllegalStateException
     *         If no entity has been selected yet
     * @throws java.lang.IllegalArgumentException
     *         If the specified amount would cause the entity to go out-of-bounds
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    #moveTo(int)
     */
    public M moveUp(int amount)
    {
        Checks.notNegative(amount, "Provided amount");
        if (selectedPosition == -1)
            throw new IllegalStateException("Cannot move until an item has been selected. Use #selectPosition first.");
        if (ascendingOrder)
        {
            Checks.check(selectedPosition - amount >= 0,
                    "Amount provided to move up is too large and would be out of bounds." +
                            "Selected position: " + selectedPosition + " Amount: " + amount + " Largest Position: " + orderList.size());
        }
        else
        {
            Checks.check(selectedPosition + amount < orderList.size(),
                    "Amount provided to move up is too large and would be out of bounds." +
                            "Selected position: " + selectedPosition + " Amount: " + amount + " Largest Position: " + orderList.size());
        }

        if (ascendingOrder)
            return moveTo(selectedPosition - amount);
        else
            return moveTo(selectedPosition + amount);
    }

    /**
     * Moves the currently selected entity {@code amount} positions <b>DOWN</b>
     * in order by pushing all entities up by one position.
     *
     * @param  amount
     *         The amount of positions that should be moved
     *
     * @throws java.lang.IllegalStateException
     *         If no entity has been selected yet
     * @throws java.lang.IllegalArgumentException
     *         If the specified amount would cause the entity to go out-of-bounds
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    #moveTo(int)
     */
    public M moveDown(int amount)
    {
        Checks.notNegative(amount, "Provided amount");
        if (selectedPosition == -1)
            throw new IllegalStateException("Cannot move until an item has been selected. Use #selectPosition first.");

        if (ascendingOrder)
        {
            Checks.check(selectedPosition + amount < orderList.size(),
                    "Amount provided to move down is too large and would be out of bounds." +
                            "Selected position: " + selectedPosition + " Amount: " + amount + " Largest Position: " + orderList.size());
        }
        else
        {
            Checks.check(selectedPosition - amount >= orderList.size(),
                    "Amount provided to move down is too large and would be out of bounds." +
                            "Selected position: " + selectedPosition + " Amount: " + amount + " Largest Position: " + orderList.size());
        }

        if (ascendingOrder)
            return moveTo(selectedPosition + amount);
        else
            return moveTo(selectedPosition - amount);
    }

    /**
     * Moves the currently selected entity to the specified
     * position (0 based index). All entities are moved in the
     * direction of the left <i>hole</i> to fill the gap.
     *
     * @param  position
     *         The new not-negative position for the currently selected entity
     *
     * @throws java.lang.IllegalStateException
     *         If no entity has been selected yet
     * @throws java.lang.IllegalArgumentException
     *         If the specified position is out-of-bounds
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    #moveDown(int)
     * @see    #moveUp(int)
     */
    @SuppressWarnings("unchecked")
    public M moveTo(int position)
    {
        Checks.notNegative(position, "Provided position");
        Checks.check(position < orderList.size(), "Provided position is too big and is out of bounds.");

        T selectedItem = orderList.remove(selectedPosition);
        orderList.add(position, selectedItem);

        return (M) this;
    }

    /**
     * Swaps the currently selected entity with the entity located
     * at the specified position. No other entities are affected by this operation.
     *
     * @param  swapPosition
     *         0 based index of target position
     *
     * @throws java.lang.IllegalStateException
     *         If no entity has been selected yet
     * @throws java.lang.IllegalArgumentException
     *         If the specified position is out-of-bounds
     *
     * @return The current OrderAction sub-implementation instance
     */
    @SuppressWarnings("unchecked")
    public M swapPosition(int swapPosition)
    {
        Checks.notNegative(swapPosition, "Provided swapPosition");
        Checks.check(swapPosition < orderList.size(), "Provided swapPosition is too big and is out of bounds. swapPosition: "
                + swapPosition);

        T selectedItem = orderList.get(selectedPosition);
        T swapItem = orderList.get(swapPosition);
        orderList.set(swapPosition, selectedItem);
        orderList.set(selectedPosition, swapItem);

        return (M) this;
    }

    /**
     * Swaps the currently selected entity with the specified entity.
     * No other entities are affected by this operation.
     *
     * @param  swapEntity
     *         Target entity to switch positions with
     *
     * @throws java.lang.IllegalStateException
     *         If no entity has been selected yet
     * @throws java.lang.IllegalArgumentException
     *         If the specified position is out-of-bounds,
     *         or if the target entity is {@code null} or not
     *         available in this order action implementation
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    #swapPosition(int)
     */
    @SuppressWarnings("unchecked")
    public M swapPosition(T swapEntity)
    {
        Checks.notNull(swapEntity, "Provided swapEntity");
        validateInput(swapEntity);

        return swapPosition(orderList.indexOf(swapEntity));
    }

    /**
     * Reverses the {@link #getCurrentOrder() current order} by using
     * {@link java.util.Collections#reverse(java.util.List) Collections.reverse(orderList)}
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    java.util.Collections#reverse(java.util.List)
     */
    @SuppressWarnings("unchecked")
    public M reverseOrder()
    {
        Collections.reverse(this.orderList);
        return (M) this;
    }

    /**
     * Shuffles the {@link #getCurrentOrder() current order} by using
     * {@link java.util.Collections#shuffle(java.util.List) Collections.shuffle(orderList)}
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    java.util.Collections#shuffle(java.util.List)
     */
    @SuppressWarnings("unchecked")
    public M shuffleOrder()
    {
        Collections.shuffle(this.orderList);
        return (M) this;
    }

    /**
     * Sorts the {@link #getCurrentOrder() current order} based on
     * the specified {@link java.util.Comparator Comparator}.
     * <br>Using {@link java.util.ArrayList#sort(java.util.Comparator) ArrayList.sort(comparator)}
     *
     * @param  comparator
     *         Comparator used to sort the current order
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified comparator is {@code null}
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    java.util.ArrayList#sort(java.util.Comparator)
     */
    @SuppressWarnings("unchecked")
    public M sortOrder(final Comparator<T> comparator)
    {
        Checks.notNull(comparator, "Provided comparator");

        this.orderList.sort(comparator);
        return (M) this;
    }

    @Override
    protected void handleResponse(Response response, Request<Void> request)
    {
        if (response.isOk())
            request.onSuccess(null);
        else
            request.onFailure(response);
    }

    protected abstract void validateInput(T entity);
}
