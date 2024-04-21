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
package net.dv8tion.jda.api.requests.restaction.order

import net.dv8tion.jda.api.requests.RestAction
import java.util.concurrent.TimeUnit
import java.util.function.BooleanSupplier
import javax.annotation.Nonnull

/**
 * Extension of [RestAction][net.dv8tion.jda.api.requests.RestAction] - Type: Void
 * that allows to modify the order of entities provided as an [ArrayList][java.util.ArrayList].
 * <br></br>This action contains a List or entities for the specified type `T` which
 * can be moved within the bounds but not removed, nor can any new entities be added.
 *
 * @param <T>
 * The entity type for the [List][java.util.List] of entities
 * contained in the OrderAction's orderList
 * @param <M>
 * The extension implementing the abstract operations of this OrderAction,
 * this will be important for chaining convenience as it returns the specific
 * implementation rather than a mask of this class. It allows us to implement
 * chaining operations in this class instead of having to implement it in every
 * inheriting class!
 *
 * @since 3.0
</M></T> */
interface OrderAction<T, M : OrderAction<T, M>?> : RestAction<Void?> {
    @Nonnull
    override fun setCheck(checks: BooleanSupplier?): RestAction<Void?>
    @Nonnull
    override fun timeout(timeout: Long, @Nonnull unit: TimeUnit): RestAction<Void?>
    @Nonnull
    override fun deadline(timestamp: Long): RestAction<Void?>

    /**
     * Whether this instance uses ascending order, from the lowest
     * position to the highest.
     *
     * @return True, if this uses ascending order
     */
    val isAscendingOrder: Boolean

    @get:Nonnull
    val currentOrder: List<T>?

    /**
     * Selects a new current entity at the specified index
     * <br></br>This index is in correlation to the [current order][.getCurrentOrder]
     *
     * @param  selectedPosition
     * The index for the new position that will be in focus for all modification
     * operations
     *
     * @throws java.lang.IllegalArgumentException
     * If the provided position is out-of-bounds
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see .getSelectedPosition
     * @see .getSelectedEntity
     */
    @Nonnull
    fun selectPosition(selectedPosition: Int): M

    /**
     * Selects a new current entity based on the index of
     * the specified entity in the [current order][.getCurrentOrder]
     * <br></br>This is a convenience function that uses [.selectPosition] internally
     *
     * @param  selectedEntity
     * The entity for the new position that will be in focus for all modification
     * operations
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see .selectPosition
     * @see .getSelectedPosition
     * @see .getSelectedEntity
     */
    @Nonnull
    fun selectPosition(@Nonnull selectedEntity: T): M

    /**
     * The currently selected position
     * that is in focus for all modification operations of this OrderAction instance
     *
     * @return The currently selected index, or -1 if no position has been selected yet
     */
    val selectedPosition: Int

    @get:Nonnull
    val selectedEntity: T

    /**
     * Moves the currently selected entity `amount` positions **UP**
     * in order by pushing all entities down by one position.
     *
     * @param  amount
     * The amount of positions that should be moved
     *
     * @throws java.lang.IllegalStateException
     * If no entity has been selected yet, use [.selectPosition]
     * @throws java.lang.IllegalArgumentException
     * If the specified amount would cause the entity to go out-of-bounds
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see .moveTo
     */
    @Nonnull
    fun moveUp(amount: Int): M

    /**
     * Moves the currently selected entity `amount` positions **DOWN**
     * in order by pushing all entities up by one position.
     *
     * @param  amount
     * The amount of positions that should be moved
     *
     * @throws java.lang.IllegalStateException
     * If no entity has been selected yet, use [.selectPosition]
     * @throws java.lang.IllegalArgumentException
     * If the specified amount would cause the entity to go out-of-bounds
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see .moveTo
     */
    @Nonnull
    fun moveDown(amount: Int): M

    /**
     * Moves the currently selected entity to the specified
     * position (0 based index). All entities are moved in the
     * direction of the left *hole* to fill the gap.
     *
     * @param  position
     * The new not-negative position for the currently selected entity
     *
     * @throws java.lang.IllegalStateException
     * If no entity has been selected yet, use [.selectPosition]
     * @throws java.lang.IllegalArgumentException
     * If the specified position is out-of-bounds
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see .moveDown
     * @see .moveUp
     * @see .moveBelow
     * @see .moveAbove
     */
    @Nonnull
    fun moveTo(position: Int): M

    /**
     * Moves the currently selected entity below the specified target entity.
     *
     * @param  other
     * The reference entity that should end up above the selected entity
     *
     * @throws IllegalStateException
     * If no entity has been selected yet, use [.selectPosition]
     * @throws IllegalArgumentException
     * If the specified target entity is not managed by this instance or null
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see .moveUp
     * @see .moveDown
     * @see .moveAbove
     */
    @Nonnull
    fun moveBelow(@Nonnull other: T): M

    /**
     * Moves the currently selected entity above the specified target entity.
     *
     * @param  other
     * The reference entity that should end up below the selected entity
     *
     * @throws IllegalStateException
     * If no entity has been selected yet, use [.selectPosition]
     * @throws IllegalArgumentException
     * If the specified target entity is not managed by this instance or null
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see .moveUp
     * @see .moveDown
     * @see .moveBelow
     */
    @Nonnull
    fun moveAbove(@Nonnull other: T): M

    /**
     * Swaps the currently selected entity with the entity located
     * at the specified position. No other entities are affected by this operation.
     *
     * @param  swapPosition
     * 0 based index of target position
     *
     * @throws java.lang.IllegalStateException
     * If no entity has been selected yet, use [.selectPosition]
     * @throws java.lang.IllegalArgumentException
     * If the specified position is out-of-bounds
     *
     * @return The current OrderAction sub-implementation instance
     */
    @Nonnull
    fun swapPosition(swapPosition: Int): M

    /**
     * Swaps the currently selected entity with the specified entity.
     * No other entities are affected by this operation.
     *
     * @param  swapEntity
     * Target entity to switch positions with
     *
     * @throws java.lang.IllegalStateException
     * If no entity has been selected yet, use [.selectPosition]
     * @throws java.lang.IllegalArgumentException
     * If the specified position is out-of-bounds,
     * or if the target entity is `null` or not
     * available in this order action implementation
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see .swapPosition
     */
    @Nonnull
    fun swapPosition(@Nonnull swapEntity: T): M

    /**
     * Reverses the [current order][.getCurrentOrder] by using
     * [Collections.reverse(orderList)][java.util.Collections.reverse]
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see java.util.Collections.reverse
     */
    @Nonnull
    fun reverseOrder(): M

    /**
     * Shuffles the [current order][.getCurrentOrder] by using
     * [Collections.shuffle(orderList)][java.util.Collections.shuffle]
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see java.util.Collections.shuffle
     */
    @Nonnull
    fun shuffleOrder(): M

    /**
     * Sorts the [current order][.getCurrentOrder] based on
     * the specified [Comparator][java.util.Comparator] by using
     * [ArrayList.sort(comparator)][java.util.ArrayList.sort]
     *
     * @param  comparator
     * Comparator used to sort the current order
     *
     * @throws java.lang.IllegalArgumentException
     * If the specified comparator is `null`
     *
     * @return The current OrderAction sub-implementation instance
     *
     * @see java.util.ArrayList.sort
     */
    @Nonnull
    fun sortOrder(@Nonnull comparator: Comparator<T>?): M
}
