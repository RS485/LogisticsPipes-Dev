/*
 * Copyright (c) 2020  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020  RS485
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

package network.rs485.logisticspipes.gui.guidebook

import net.minecraft.nbt.NBTTagCompound
import network.rs485.logisticspipes.guidebook.BookContents
import network.rs485.logisticspipes.guidebook.BookContents.MAIN_MENU_FILE
import network.rs485.logisticspipes.util.LPDataInput
import network.rs485.logisticspipes.util.LPDataOutput
import network.rs485.logisticspipes.util.math.Rectangle

private const val PAGE_VERTICAL_PADDING = 5

class SavedPage constructor(val page: String = MAIN_MENU_FILE, var color: Int = 0, var progress: Float = 0.0F) {

    val loadedPage = BookContents.get(page)
    var height: Int = 0
        private set

    constructor(page: SavedPage) : this(page.page, page.color, page.progress)

    fun draw(mouseX: Int, mouseY: Int, delta: Float, visibleArea: Rectangle) {
        val yOffset = ((height - visibleArea.height) * progress).toInt()
        loadedPage.drawableParagraphs.forEach { paragraph ->
            paragraph.draw(mouseX, mouseY, delta, yOffset, visibleArea)
        }
    }

    fun initDrawables(area: Rectangle) {
        loadedPage.drawableParagraphs.fold(area.y0 + PAGE_VERTICAL_PADDING) { currentY, paragraph ->
            currentY + paragraph.setPos(area.x0 + 1, currentY + 1, area.width - 2) + 3
        }
        height = loadedPage.drawableParagraphs.last().area.y1 - loadedPage.drawableParagraphs.first().area.y0 + PAGE_VERTICAL_PADDING * 2
    }

    /**
     * Takes in an LPDataInput buffer and turns the buffered bytes object into a SavedPage object.
     * @param input the received data
     * @return SavedPage object created from the buffered data
     */
    fun fromBytes(input: LPDataInput): SavedPage {
        return SavedPage(
                input.readUTF() ?: error("Oh fuck you!"),
                input.readInt(),
                input.readFloat())
    }

    /**
     * Takes in an LPDataOutput buffer and turns a SavedPage object into bytes and puts them inside the buffer.
     * @param output data to send
     */
    fun toBytes(output: LPDataOutput) {
        output.writeUTF(page)
        output.writeInt(color)
        output.writeFloat(progress)
    }

    fun fromTag(nbt: NBTTagCompound): SavedPage {
        return SavedPage(
                nbt.getString("page"),
                nbt.getInteger("color"),
                nbt.getFloat("progress"))
    }

    fun toTag(): NBTTagCompound {
        val nbt = NBTTagCompound()
        nbt.setString("page", page)
        nbt.setInteger("color", color)
        nbt.setFloat("progress", progress)
        return nbt
    }

    fun isEqual(b: SavedPage): Boolean = this.page == b.page

    fun cycleColor(invert: Boolean = false) {
        if (invert) color--
        else color++
        if (color == 16) color = 0
        if (color == -1) color = 15
    }
}