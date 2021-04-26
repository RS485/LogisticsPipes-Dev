/*
 * Copyright (c) 2021  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2021  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.logisticspipes.integration

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.time.withTimeoutOrNull
import logisticspipes.LogisticsPipes
import net.minecraft.world.WorldServer
import net.minecraftforge.common.ForgeChunkManager
import network.rs485.grow.ServerTickDispatcher
import network.rs485.minecraft.BlockPosSelector
import network.rs485.minecraft.WorldBuilder
import java.time.Duration

class TestWorldBuilder(override val world: WorldServer) : WorldBuilder {

    private val _tickets: HashSet<ForgeChunkManager.Ticket> = HashSet()
    override val tickets: Set<ForgeChunkManager.Ticket>
        get() = _tickets

    fun newSelector() = BlockPosSelector(this, 0, 100, 0)

    init {
        ForgeChunkManager.setForcedChunkLoadingCallback(LogisticsPipes.instance) { ticketsIn, world ->
            if (world == this@TestWorldBuilder.world) _tickets.addAll(ticketsIn!!)
        }
        _tickets.add(ForgeChunkManager.requestTicket(LogisticsPipes.instance, world, ForgeChunkManager.Type.NORMAL)!!)
    }
}

suspend fun waitForOrNull(timeout: Duration, check: () -> Boolean): Unit? =
    withTimeoutOrNull(timeout) {
        val response = CompletableDeferred<Unit>()

        fun schedule() {
            ServerTickDispatcher.scheduleNextTick {
                try {
                    if (check()) {
                        response.complete(Unit)
                    } else {
                        schedule()
                    }
                } catch (e: Exception) {
                    response.completeExceptionally(e)
                }
            }
        }

        schedule()
        response.await()
    }

suspend fun waitFor(timeout: Duration, check: () -> Boolean, lazyErrorMessage: () -> Any) =
    waitForOrNull(timeout, check) ?: error(lazyErrorMessage())
