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

package com.github.stephenc.javaisotools.vfs.provider.iso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileEntry;
import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileSystem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.VfsLog;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

/**
 * Implementation of {@link org.apache.commons.vfs.FileSystem} for ISO9660 (.iso) files.
 * <p/>
 * TODO: perf test with ISO files containing lots of entries; possibly optimize by creating FileObjects on-demand via
 * the createFile() method.
 */
public class IsoFileSystem extends AbstractFileSystem {

    private static final Log log = LogFactory.getLog(IsoFileSystem.class);

    private Iso9660FileSystem fileSystem;

    public IsoFileSystem(final FileName rootName, final FileObject parentLayer,
                         final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        super(rootName, parentLayer, fileSystemOptions);
    }

    public void init() throws FileSystemException {
        super.init();

        final File file = getParentLayer().getFileSystem().
                replicateFile(getParentLayer(), Selectors.SELECT_SELF);

        try {
            this.fileSystem = new Iso9660FileSystem(file, true);
        }
        catch (IOException ex) {
            throw new FileSystemException("vfs.provider.iso/open-iso-file.error", file, ex);
        }

        // Build the index
        final List strongRef = new ArrayList(100);

        boolean skipRoot = false;

        for (Iso9660FileEntry entry: this.fileSystem) {

            String name = entry.getPath();

            // skip entries without names (should only be one - the root entry)
            if ("".equals(name)) {
                if (!skipRoot) {
                    skipRoot = true;
                } else {
                    continue;
                }
            }

            final FileName filename = getFileSystemManager().
                    resolveName(getRootName(), UriParser.encode(name));

            // Create the file
            IsoFileObject fileObj;

            if (entry.isDirectory() && getFileFromCache(filename) != null) {
                fileObj = (IsoFileObject) getFileFromCache(filename);
                fileObj.setIsoEntry(entry);
                continue;
            }

            fileObj = new IsoFileObject(filename, entry, this);
            putFileToCache(fileObj);
            strongRef.add(fileObj);
            fileObj.holdObject(strongRef);

            // Make sure all ancestors exist
            IsoFileObject parent;

            for (FileName parentName = filename.getParent(); parentName != null;
                 fileObj = parent, parentName = parentName.getParent()) {

                // Locate the parent
                parent = (IsoFileObject) getFileFromCache(parentName);

                if (parent == null) {
                    parent = new IsoFileObject(parentName, this);
                    putFileToCache(parent);
                    strongRef.add(parent);
                    parent.holdObject(strongRef);
                }

                // Attach child to parent
                parent.attachChild(fileObj.getName());
            }
        }
    }

    protected void addCapabilities(final Collection caps) {
        caps.addAll(IsoFileProvider.capabilities);
    }

    /**
     * Since this method is only called for files that don't actually exist in the .iso file, it always returns
     * IMAGINARY files. Any attempt to access their content results in an exception.
     */
    protected FileObject createFile(final FileName name) throws Exception {
        return new IsoFileObject(name, this);
    }

    /**
     * Closes the underlying .iso file.
     */
    protected void doCloseCommunicationLink() {
        if (null != this.fileSystem && !this.fileSystem.isClosed()) {
            try {
                this.fileSystem.close();
            }
            catch (IOException ex) {
                VfsLog.warn(getLogger(), log,
                        "vfs.provider.iso/close-iso-file.error :" + this.fileSystem, ex);
            }
        }
    }

    public void close() {
        super.close();
        this.fileSystem = null;
    }

    /**
     * Returns an input stream for the specified Iso9660FileEntry. Called by {@link IsoFileObject#doGetInputStream()}.
     */
    InputStream getInputStream(final Iso9660FileEntry entry) {
        return this.fileSystem.getInputStream(entry);
    }
}