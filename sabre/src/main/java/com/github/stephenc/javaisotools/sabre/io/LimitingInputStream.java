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

/*
 * Created on 01.10.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package com.github.stephenc.javaisotools.sabre.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author hartle
 *         <p/>
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class LimitingInputStream extends FilterInputStream {

    protected int limit = -1;

    public LimitingInputStream(InputStream inputStream, int limit) {
        super(inputStream);
        this.limit = limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return this.limit;
    }

    public int read() throws IOException {
        int readByte = -1;

        // Limit the amount of bytes that can be read
        if ((this.limit > 0) || (this.limit == -1)) {
            readByte = super.read();
            if (this.limit > 0) {
                this.limit -= 1;
            }
        }

        return readByte;
    }

    public int read(byte[] buffer) throws IOException {
        int result = -1;

        if (this.limit > 0) {
            result = super.read(buffer, 0, (this.limit < buffer.length) ? this.limit : buffer.length);
        } else if (this.limit == -1) {
            result = super.read(buffer);
        }

        if (result != -1) {
            this.limit -= result;
        }

        return result;
    }

    public int read(byte[] buffer, int position, int length) throws IOException {
        int result = -1;

        if (this.limit > 0) {
            result = super.read(buffer, position, (this.limit < length) ? this.limit : length);
        } else if (this.limit == -1) {
            result = super.read(buffer, position, length);
        }

        if (result != -1) {
            this.limit -= result;
        }

        return result;
    }

    public int available() throws IOException {
        int available = 0;

        available = super.available();
        if (this.limit != -1) {
            if (this.limit < available) {
                available = this.limit;
            }
        }

        return available;
    }
}
