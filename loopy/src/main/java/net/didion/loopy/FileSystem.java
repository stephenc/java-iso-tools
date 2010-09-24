package net.didion.loopy;

import java.io.InputStream;
import java.util.Enumeration;

public interface FileSystem {

    /**
     * Get an enumeration of the entries within this file system.
     *
     * @return an enumeration of the entries within this file system.
     */
    Enumeration getEntries();

    /**
     * Get an input stream that reads the data for the given entry.
     *
     * @return an input stream that reads the contents of the given entry.
     */
    InputStream getInputStream(FileEntry entry);

    /**
     * Close the file system. This automatically closes all input streams opened via {@link
     * FileSystem#getInputStream(FileEntry entry)}.
     *
     * @throws LoopyException if there was an error closing the FileSystem.
     */
    void close() throws LoopyException;

    /**
     * Returns whether or not this FileSystem has been closed.
     *
     * @return true if {@link FileSystem#close()} has been called on this FileSystem, otherwise false.
     */
    boolean isClosed();
}