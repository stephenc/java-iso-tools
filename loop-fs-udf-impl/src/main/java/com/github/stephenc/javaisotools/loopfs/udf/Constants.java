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

package com.github.stephenc.javaisotools.loopfs.udf;

public interface Constants {

    /**
     * ISO sector size. This does not include the 288 bytes reserved for synchronization, header, and EC on CD-ROMs
     * because this information is not used in .iso files.
     */
    int DEFAULT_BLOCK_SIZE = 2 * 1024;

    /**
     * The number of reserved sectors at the beginning of the file.
     */
    int RESERVED_SECTORS = 16;

    /**
     * The number of reserved bytes at the beginning of the file.
     */
    int RESERVED_BYTES = RESERVED_SECTORS * DEFAULT_BLOCK_SIZE;

    /**
     * Default character encoding.
     */
    String DEFAULT_ENCODING = "US-ASCII";
}