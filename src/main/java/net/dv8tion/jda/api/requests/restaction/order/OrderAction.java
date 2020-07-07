/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.requests.restaction.order;

import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Extension of {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: Void
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
@SuppressWarnings("unchecked")
public interface OrderAction<T, M extends OrderAction<T, M>> extends RestAction<Void>
{
    @Nonnull
    @Override
    M setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    M timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    M deadline(long timestamp);

    /**
     * Whether this instance uses ascending order, from the lowest
     * position to the highest.
     *
     * @return True, if this uses ascending order
     */
    boolean isAscendingOrder();

    /**
     * Immutable List representing the currently selected order
     * of entities in this OrderAction instance
     *
     * @return Immutable List representing the current order
     */
    @Nonnull
    List<T> getCurrentOrder();

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
    @Nonnull
    M selectPosition(int selectedPosition);

    /**
     * Selects a new current entity based on the index of
     * the specified entity in the {@link #getCurrentOrder() current order}
     * <br>This is a convenience function that uses {@link #selectPosition(int)} internally
     *
     * @param  selectedEntity
     *         The entity for the new position that will be in focus for all modification
     *         operations
     *
     * @throws IllegalArgumentException
     *         If the entity is null or not tracked by this order action
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    #selectPosition(int)
     * @see    #getSelectedPosition()
     * @see    #getSelectedEntity()
     */
    @Nonnull
    M selectPosition(@Nonnull T selectedEntity);

    /**
     * The currently selected position
     * that is in focus for all modification operations of this OrderAction instance
     *
     * @return The currently selected index, or -1 if no position has been selected yet
     */
    int getSelectedPosition();

    /**
     * The entity which is currently at the {@link #getSelectedPosition() selected position}
     *
     * @throws java.lang.IllegalStateException
     *         If no entity has been selected yet
     *
     * @return The currently selected entity
     */
    @Nonnull
    T getSelectedEntity();

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
    @Nonnull
    M moveUp(int amount);

    /**
     * Moves the entity at the specified position {@code amount} positions <b>UP</b>
     * in order by pushing all entities down by one position.
     *
     * @param  position
     *         The position of the entity which will be moved
     * @param  amount
     *         The amount of positions that should be moved
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified amount would cause the entity to go out-of-bounds,
     *         or if the target position is out-of-bounds
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    #moveTo(int)
     */
    @Nonnull
    default M moveUp(int position, int amount)
    {
        int currentPosition = getSelectedPosition();
        try
        {
            selectPosition(position).moveUp(amount);
        }
        finally
        {
            if (currentPosition > -1)
                selectPosition(currentPosition);
        }
        return (M) this;
    }

    /**
     * Moves the specified entity {@code amount} positions <b>UP</b>
     * in order by pushing all entities down by one position.
     *
     * @param  entity
     *         The entity which will be moved
     * @param  amount
     *         The amount of positions that should be moved
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified amount would cause the entity to go out-of-bounds,
     *         or if the target entity is null or not tracked by this order action
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    #moveTo(int)
     */
    @Nonnull
    default M moveUp(@Nonnull T entity, int amount)
    {
        int currentPosition = getSelectedPosition();
        try
        {
            selectPosition(entity).moveUp(amount);
        }
        finally
        {
            if (currentPosition > -1)
                selectPosition(currentPosition);
        }
        return (M) this;
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
    @Nonnull
    M moveDown(int amount);

    /**
     * Moves the entity at the specified position {@code amount} positions <b>DOWN</b>
     * in order by pushing all entities down by one position.
     *
     * @param  position
     *         The position of the entity which will be moved
     * @param  amount
     *         The amount of positions that should be moved
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified amount would cause the entity to go out-of-bounds,
     *         or if the target position is out-of-bounds
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    #moveTo(int)
     */
    @Nonnull
    default M moveDown(int position, int amount)
    {
        int currentPosition = getSelectedPosition();
        try
        {
            selectPosition(position).moveDown(amount);
        }
        finally
        {
            if (currentPosition > -1)
                selectPosition(currentPosition);
        }
        return (M) this;
    }

    /**
     * Moves the specified entity {@code amount} positions <b>DOWN</b>
     * in order by pushing all entities down by one position.
     *
     * @param  entity
     *         The entity which will be moved
     * @param  amount
     *         The amount of positions that should be moved
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified amount would cause the entity to go out-of-bounds,
     *         or if the target entity is null or not tracked by this order action
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    #moveTo(int)
     */
    @Nonnull
    default M moveDown(@Nonnull T entity, int amount)
    {
        int currentPosition = getSelectedPosition();
        try
        {
            selectPosition(entity).moveDown(amount);
        }
        finally
        {
            if (currentPosition > -1)
                selectPosition(currentPosition);
        }
        return (M) this;
    }

    /**
     * Moves the currently selected entity to the specified
     * position (0 based index). All entities are moved in the
     * direction of the left <em>hole</em> to fill the gap.
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
    @Nonnull
    M moveTo(int position);

    /**
     * Moves the entity at the specified position to the new position (0 based index).
     * All entities are moved in the direction of the left <em>hole</em> to fill the gap.
     *
     * @param  position
     *         The old position
     * @param  newPosition
     *         The new not-negative position for the currently selected entity
     *
     * @throws java.lang.IllegalArgumentException
     *         If either of the specified positions is out-of-bounds
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    #moveDown(int)
     * @see    #moveUp(int)
     */
    @Nonnull
    default M moveTo(int position, int newPosition)
    {
        int currentPosition = getSelectedPosition();
        try
        {
            selectPosition(position).moveTo(newPosition);
        }
        finally
        {
            if (currentPosition > -1)
                selectPosition(currentPosition);
        }
        return (M) this;
    }

    /**
     * Moves the specified entity to the new position (0 based index).
     * All entities are moved in the direction of the left <em>hole</em> to fill the gap.
     *
     * @param  entity
     *         The entity to move
     * @param  newPosition
     *         The new not-negative position for the currently selected entity
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified position is out-of-bounds,
     *         or the entity is null or not tracked by this order action
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    #moveDown(int)
     * @see    #moveUp(int)
     */
    @Nonnull
    default M moveTo(@Nonnull T entity, int newPosition)
    {
        int currentPosition = getSelectedPosition();
        try
        {
            selectPosition(entity).moveTo(newPosition);
        }
        finally
        {
            if (currentPosition > -1)
                selectPosition(currentPosition);
        }
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
    @Nonnull
    M swapPosition(int swapPosition);

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
    @Nonnull
    M swapPosition(@Nonnull T swapEntity);

    /**
     * Swaps the entities at the specified positions.
     * No other entities are affected by this operation.
     *
     * @param  position1
     *         0 based index of the first target position
     * @param  position2
     *         0 based index of the second target position
     *
     * @throws java.lang.IllegalArgumentException
     *         If either of the specified positions is out-of-bounds
     *
     * @return The current OrderAction sub-implementation instance
     */
    @Nonnull
    default M swapPosition(int position1, int position2)
    {
        int currentPosition = getSelectedPosition();
        try
        {
            selectPosition(position1).swapPosition(position2);
        }
        finally
        {
            if (currentPosition > -1)
                selectPosition(currentPosition);
        }
        return (M) this;
    }

    /**
     * Swaps the entities at the specified positions.
     * No other entities are affected by this operation.
     *
     * @param  entity
     *         The first entity to swap
     * @param  position2
     *         0 based index of the second target position
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified position is out-of-bounds,
     *         or if the entity is null or not tracked by this order action
     *
     * @return The current OrderAction sub-implementation instance
     */
    @Nonnull
    default M swapPosition(@Nonnull T entity, int position2)
    {
        int currentPosition = getSelectedPosition();
        try
        {
            selectPosition(entity).swapPosition(position2);
        }
        finally
        {
            if (currentPosition > -1)
                selectPosition(currentPosition);
        }
        return (M) this;
    }

    /**
     * Swaps the entities at the specified positions.
     * No other entities are affected by this operation.
     *
     * @param  entity1
     *         The first entity to swap
     * @param  entity2
     *         The second entity to swap
     *
     * @throws java.lang.IllegalArgumentException
     *         If either of the provided entities is null
     *         or not tracked by this order action
     *
     * @return The current OrderAction sub-implementation instance
     */
    @Nonnull
    default M swapPosition(@Nonnull T entity1, @Nonnull T entity2)
    {
        int currentPosition = getSelectedPosition();
        try
        {
            selectPosition(entity1).swapPosition(entity2);
        }
        finally
        {
            if (currentPosition > -1)
                selectPosition(currentPosition);
        }
        return (M) this;
    }

    /**
     * Reverses the {@link #getCurrentOrder() current order} by using
     * {@link java.util.Collections#reverse(java.util.List) Collections.reverse(orderList)}
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    java.util.Collections#reverse(java.util.List)
     */
    @Nonnull
    M reverseOrder();

    /**
     * Shuffles the {@link #getCurrentOrder() current order} by using
     * {@link java.util.Collections#shuffle(java.util.List) Collections.shuffle(orderList)}
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see    java.util.Collections#shuffle(java.util.List)
     */
    @Nonnull
    M shuffleOrder();

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
    @Nonnull
    M sortOrder(@Nonnull final Comparator<T> comparator);
}
