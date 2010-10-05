/*
 * Copyright (c) 2010. Stephen Connolly
 * Copyright (C) 2006. Michael Hartle <mhartle@rbg.informatik.tu-darmstadt.de>.
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.github.stephenc.javaisotools.sabre.io.LimitingInputStream;
import com.github.stephenc.javaisotools.sabre.DataReference;

public class URLDataReference implements DataReference {

    private URL url = null;
    private long position = 0;
    private long length = 0;

    public URLDataReference(URL url, long position, long length) {
        this.url = url;
        this.position = position;
        this.length = length;
    }

    public long getLength() {
        return this.length;
    }

    public InputStream createInputStream() throws IOException {
        InputStream urlInputStream = null;
        InputStream limitedInputStream = null;

        urlInputStream = this.url.openStream();
        urlInputStream.skip(this.position);
        limitedInputStream = new LimitingInputStream(urlInputStream, (int) this.length);

        return limitedInputStream;
    }
}
