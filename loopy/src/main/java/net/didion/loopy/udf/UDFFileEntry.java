/*
 * Copyright (c) 2004-2006, Loudeye Corp. All Rights Reserved.
 * Last changed by: $Author: jdidion $
 * Last changed at: $DateTime$
 * Revision: $Revision: 1.1.1.1 $
 */

package net.didion.loopy.udf;

import net.didion.loopy.impl.AbstractBlockFileEntry;

public class UDFFileEntry extends AbstractBlockFileEntry {

    public String getName() {
        return null;
    }

    public String getPath() {
        return null;
    }

    public long getLastModified() {
        return 0;
    }

    public boolean isDirectory() {
        return false;
    }

    public int getSize() {
        return 0;
    }
}