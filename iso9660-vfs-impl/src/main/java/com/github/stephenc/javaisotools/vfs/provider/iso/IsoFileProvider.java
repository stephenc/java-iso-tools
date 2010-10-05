/*
Copyright (C) 2006-2007 loopy project (http://loopy.sourceforge.net)

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

package com.github.stephenc.javaisotools.vfs.provider.iso;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractLayeredFileProvider;
import org.apache.commons.vfs.provider.LayeredFileName;

/**
 * Implementation of {@link org.apache.commons.vfs.provider.FileProvider} for ISO9660 (.iso) files. Currently, .iso
 * files are read-only.
 */
public class IsoFileProvider extends AbstractLayeredFileProvider {

    public static final Collection capabilities = Arrays.asList(new Capability[]{
            Capability.GET_LAST_MODIFIED,
            Capability.GET_TYPE,
            Capability.LIST_CHILDREN,
            Capability.READ_CONTENT,
            Capability.URI,
            Capability.VIRTUAL
    });

    public Collection getCapabilities() {
        return IsoFileProvider.capabilities;
    }

    protected FileSystem doCreateFileSystem(final String scheme, final FileObject file,
                                            final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        final FileName rootName = new LayeredFileName(
                scheme, file.getName(), FileName.ROOT_PATH, FileType.FOLDER);
        return new IsoFileSystem(rootName, file, fileSystemOptions);
    }
}