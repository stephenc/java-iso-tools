/*
 * Copyright (c) 2004-2006, Loudeye Corp. All Rights Reserved.
 * Last changed by: $Author: jdidion $
 * Last changed at: $DateTime$
 * Revision: $Revision: 1.1.1.1 $
 */

package net.didion.loopy.udf;

import java.io.IOException;

import net.didion.loopy.FileEntry;
import net.didion.loopy.impl.VolumeDescriptor;

public class UDFVolumeDescriptor implements VolumeDescriptor {

    private UDFFileSystem fs;

    public UDFVolumeDescriptor(UDFFileSystem fs) {
        this.fs = fs;
    }

    public boolean read(byte[] buffer) throws IOException {
        return false;
    }

    public FileEntry getRootEntry() {
        return null;
    }
}