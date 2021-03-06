/*
 * Voxem
 * Copyright (c) 2014-2015, Maxim Roncacé <caseif@caseif.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.caseif.voxem.event.block;

import net.caseif.voxem.util.VboUtil;
import net.caseif.voxem.world.Block;
import net.caseif.voxem.world.Location;

public class BlockBreakEvent extends BlockEvent {

    public BlockBreakEvent(Location l, Block block) {
        this.location = l;
        this.oldBlock = block;
        this.newBlock = null;
        block.getLocation().getChunk();
        block.destroy();
        block.updateLight();
        VboUtil.updateChunkArray(l.getLevel(), l.getChunk());
        if (l.getChunk() == 1) {
            if (l.getLevel().isChunkGenerated(l.getChunk() - 2)) {
                VboUtil.updateChunkArray(l.getLevel(), l.getChunk() - 2);
            }
        } else {
            if (l.getLevel().isChunkGenerated(l.getChunk() - 1)) {
                VboUtil.updateChunkArray(l.getLevel(), l.getChunk() - 1);
            }
        }
        if (l.getChunk() == -1) {
            if (l.getLevel().isChunkGenerated(l.getChunk() + 2)) {
                VboUtil.updateChunkArray(l.getLevel(), l.getChunk() + 2);
            }
        } else {
            if (l.getLevel().isChunkGenerated(l.getChunk() + 1)) {
                VboUtil.updateChunkArray(l.getLevel(), l.getChunk() + 1);
            }
        }
        VboUtil.prepareBindArray();
    }

    //TODO: Implement Cancellable

}
