package net.didion.loopy;

public interface FileEntry {

    /**
     * Get the entry name (the last entry of the path).
     *
     * @return the entry name
     */
    String getName();

    /**
     * Get the relative entry path. The path does NOT begin with any separators.
     *
     * @return the entry path
     */
    String getPath();

    /**
     * Get the last modified time for this entry.
     *
     * @return the last modified time
     */
    long getLastModified();

    /**
     * Get whether this entry represents a directory.
     *
     * @return true if this entry represents a directory, otherwise false.
     */
    boolean isDirectory();

    /**
     * Get the size, in bytes, of the data represented by this entry.
     *
     * @return the entry size
     */
    int getSize();
}