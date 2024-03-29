/*
 * The MIT License (MIT) Copyright (c) 2014 Ordinastie Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions: The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.malisis.core.renderer.element.face;

import static net.minecraftforge.common.util.ForgeDirection.*;

import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Vertex;

/**
 * @author Ordinastie
 *
 */
public class NorthFace extends Face {

    private static final Vertex[] DEFAULT = { new Vertex.TopNorthEast(), new Vertex.BottomNorthEast(),
        new Vertex.BottomNorthWest(), new Vertex.TopNorthWest() };

    @Override
    public void reset() {
        for (int i = 0; i < 4; ++i) {
            this.vertexes[i].setState(DEFAULT[i]);
        }
    }

    public NorthFace() {
        super(
            new Vertex.TopNorthEast(),
            new Vertex.BottomNorthEast(),
            new Vertex.BottomNorthWest(),
            new Vertex.TopNorthWest());

        params.direction.set(NORTH);
        params.textureSide.set(NORTH);
        params.colorFactor.set(0.8F);
        params.aoMatrix.set(calculateAoMatrix(NORTH));
        setStandardUV();
    }
}
