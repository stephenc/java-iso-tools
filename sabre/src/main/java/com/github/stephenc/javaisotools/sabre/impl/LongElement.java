/*
 * Copyright (c) 2010. Stephen Connolly
 * Copyright (c) 2006. Michael Hartle <mhartle@rbg.informatik.tu-darmstadt.de>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.github.stephenc.javaisotools.sabre.impl;

import com.github.stephenc.javaisotools.sabre.Element;

public class LongElement extends Element {

    private long id = 0;

    public LongElement(long id) {
        this.id = id;
    }

    public Object getId() {
        return new Long(id);
    }

    public String toString() {
        String result = null;

        result = new String();
        result += (char) ((id & 0xFF000000) >> 24);
        result += (char) ((id & 0x00FF0000) >> 16);
        result += (char) ((id & 0x0000FF00) >> 8);
        result += (char) ((id & 0x000000FF));

        return result;
    }
}
