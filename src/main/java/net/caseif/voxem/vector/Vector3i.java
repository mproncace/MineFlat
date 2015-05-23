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
package net.caseif.voxem.vector;

/**
 * Represents a vector with 3 int elements.
 */
public class Vector3i implements Vector3 {

    protected int x;
    protected int y;
    protected int z;

    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public Vector3i add(Vector3i vector) {
        return add(vector.getX(), vector.getY(), vector.getZ());
    }

    public Vector3i add(int x, int y, int z) {
        return new Vector3i(this.x + x, this.y + y, this.z + z);
    }

    public Vector3i subtract(Vector3i vector) {
        return subtract(vector.getX(), vector.getY(), vector.getZ());
    }

    public Vector3i subtract(int x, int y, int z) {
        return new Vector3i(this.x - x, this.y - y, this.z + z);
    }

    public boolean equals(Object o) {
        if (o instanceof Vector3i) {
            Vector3i v = (Vector3i) o;
            return v.getX() == this.x && v.getY() == this.y && this.z == z;
        }
        return false;
    }

    @Override
    public Vector3i clone() {
        return new Vector3i(x, y, z);
    }

    @Override
    public String toString() {
        return "Vector3i{" + x + ", " + y + ", " + z + "}";
    }

}
