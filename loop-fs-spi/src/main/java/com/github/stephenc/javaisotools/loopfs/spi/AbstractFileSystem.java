/*
 * Copyright (c) 2010. Stephen Connolly.
 * Copyright (c) 2006-2007. loopy project (http://loopy.sourceforge.net).
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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.github.stephenc.javaisotools.loopfs.spi;

import java.io.IOException;

import com.github.stephenc.javaisotools.loopfs.api.FileEntry;
import com.github.stephenc.javaisotools.loopfs.api.FileSystem;

/**
 * Implementation of FileSystem that is backed by a {@link SeekableInput}.
 */
public abstract class AbstractFileSystem<T extends FileEntry> implements FileSystem<T> {

    /**
     * Channel to the open file.
     */
    private SeekableInput channel;

    protected AbstractFileSystem(final SeekableInput seekable, final boolean readOnly) throws IOException {
        if (!readOnly) {
            throw new IllegalArgumentException("Currently, only read-only is supported");
        }
        this.channel = seekable;
    }

    // TODO: close open streams automatically

    public synchronized void close() throws IOException {
        if (isClosed()) {
            return;
        }
        try {
            this.channel.close();
        }
        finally {
            this.channel = null;
        }
    }

    public synchronized boolean isClosed() {
        return (null == this.channel);
    }

    /**
     * Throws an exception if the underlying file is closed.
     */
    protected final void ensureOpen() throws IllegalStateException {
        if (isClosed()) {
            throw new IllegalStateException("File has been closed");
        }
    }

    /**
     * Moves the pointer in the underlying file to the specified position.
     */
    protected final void seek(long pos) throws IOException {
        ensureOpen();
        this.channel.seek(pos);
    }

    /**
     * Reads up to <code>length</code> bytes into the specified buffer, starting at the specified offset. The actual
     * number of bytes read will be less than <code>length</code> if there are not enough available bytes to read, or if
     * the buffer is not large enough.
     *
     * @return the number of bytes read into the buffer
     */
    protected final int read(byte[] buffer, int offset, int length) throws IOException {
        ensureOpen();
        return readFully(buffer, offset, length);
    }

    private int readFully(byte[] buffer, int offset, int length) throws IOException {
        int bytesRead;
        int remaining = length;

        // read doesn't guarantee a full buffer is read. Reading until we have a full buffer or end of stream
        while (remaining != 0 &&
                (bytesRead = this.channel.read(buffer, offset, remaining)) != -1) {
            offset += bytesRead;
            remaining -= bytesRead;
        }
        int totalBytesRead = length - remaining;
        return totalBytesRead;
    }

}