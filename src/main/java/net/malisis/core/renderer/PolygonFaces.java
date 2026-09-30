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

package net.malisis.core.renderer;

import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.util.Vector;

final class PolygonFaces {

    private PolygonFaces() {}

    static void normal(Vertex[] vertices, Vector result) {
        result.set(0, 0, 0);
        for (int i = 0; i < vertices.length; i++) {
            final Vertex a = vertices[i];
            final Vertex b = vertices[(i + 1) % vertices.length];
            result.x += (a.getY() - b.getY()) * (a.getZ() + b.getZ());
            result.y += (a.getZ() - b.getZ()) * (a.getX() + b.getX());
            result.z += (a.getX() - b.getX()) * (a.getY() + b.getY());
        }
    }

    static int[] triangulate(Vertex[] vertices, Vector normal) {
        final int count = vertices.length;
        final double[] u = new double[count];
        final double[] v = new double[count];
        final double nx = Math.abs(normal.x), ny = Math.abs(normal.y), nz = Math.abs(normal.z);
        final int droppedAxis = nx >= ny && nx >= nz ? 0 : ny >= nz ? 1 : 2;
        for (int i = 0; i < count; i++) {
            final Vertex vertex = vertices[i];
            u[i] = droppedAxis == 0 ? vertex.getY() : vertex.getX();
            v[i] = droppedAxis == 2 ? vertex.getY() : vertex.getZ();
        }

        double area = 0;
        for (int i = 0; i < count; i++) {
            final int j = (i + 1) % count;
            area += u[i] * v[j] - u[j] * v[i];
        }
        if (area == 0) return null;
        final double winding = Math.signum(area);
        final double epsilon = Math.abs(area) * 1.0e-10;
        final int[] remaining = new int[count];
        for (int i = 0; i < count; i++) remaining[i] = i;
        final int[] triangles = new int[(count - 2) * 3];
        int size = count;
        int output = 0;
        while (size > 3) {
            boolean clipped = false;
            for (int i = 0; i < size; i++) {
                final int a = remaining[(i + size - 1) % size];
                final int b = remaining[i];
                final int c = remaining[(i + 1) % size];
                if (cross(u, v, a, b, c) * winding <= epsilon) continue;

                boolean containsVertex = false;
                for (int j = 0; j < size; j++) {
                    final int p = remaining[j];
                    if (p == a || p == b || p == c) continue;
                    if (cross(u, v, a, b, p) * winding >= -epsilon && cross(u, v, b, c, p) * winding >= -epsilon
                        && cross(u, v, c, a, p) * winding >= -epsilon) {
                        containsVertex = true;
                        break;
                    }
                }
                if (containsVertex) continue;

                triangles[output++] = a;
                triangles[output++] = b;
                triangles[output++] = c;
                System.arraycopy(remaining, i + 1, remaining, i, size - i - 1);
                size--;
                clipped = true;
                break;
            }
            if (!clipped) return null;
        }
        triangles[output++] = remaining[0];
        triangles[output++] = remaining[1];
        triangles[output] = remaining[2];
        return triangles;
    }

    private static double cross(double[] u, double[] v, int a, int b, int c) {
        return (u[b] - u[a]) * (v[c] - v[a]) - (v[b] - v[a]) * (u[c] - u[a]);
    }
}
