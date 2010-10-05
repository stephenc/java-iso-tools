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

/**
 * Contains all the volume descriptors for a block file system.
 */
public interface VolumeDescriptorSet<T extends FileEntry> {

    /**
     * Load a volume descriptor from the specified byte array.
     *
     * @param volumeDescriptor the volume descriptor to deserialize
     *
     * @return true if the volume descriptor is a terminator
     *
     * @throws IOException if there is an error deserializing the volume descriptor
     */
    boolean deserialize(byte[] volumeDescriptor) throws IOException;

    /**
     * Returns the root file entry for the file system.
     *
     * @return the root file entry for the file system
     */
    T getRootEntry();
}
