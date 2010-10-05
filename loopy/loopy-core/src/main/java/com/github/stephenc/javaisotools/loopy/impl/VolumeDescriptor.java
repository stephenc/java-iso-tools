/*
 * Copyright (c) 2004-2006, Loudeye Corp. All Rights Reserved.
 * Last changed by: $Author: jdidion $
 * Last changed at: $DateTime$
 * Revision: $Revision: 1.1 $
 */

package com.github.stephenc.javaisotools.loopy.impl;

import java.io.IOException;

import com.github.stephenc.javaisotools.loopy.FileEntry;

public interface VolumeDescriptor {

    boolean read(byte[] buffer) throws IOException;

    FileEntry getRootEntry();
}
