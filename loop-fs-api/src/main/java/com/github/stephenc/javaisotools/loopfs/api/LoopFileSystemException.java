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

package com.github.stephenc.javaisotools.loopfs.api;

import java.io.IOException;

/**
 * General loop-fs exception.
 */
public class LoopFileSystemException extends IOException {

    public LoopFileSystemException() {
    }

    public LoopFileSystemException(final String message) {
        super(message);
    }

    public LoopFileSystemException(final Throwable cause) {
        super(cause);
    }

    public LoopFileSystemException(final String message, final Throwable cause) {
        super(message, cause);
    }
}