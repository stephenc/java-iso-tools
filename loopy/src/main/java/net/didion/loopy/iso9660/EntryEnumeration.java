package net.didion.loopy.iso9660;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import net.didion.loopy.LoopyException;
import net.didion.loopy.util.LittleEndian;

class EntryEnumeration implements Enumeration {

    private ISO9660FileSystem isoFile;
    private List queue;

    public EntryEnumeration(ISO9660FileSystem isoFile, ISO9660FileEntry rootEntry) {
        this.isoFile = isoFile;
        this.queue = new LinkedList();
        this.queue.add(rootEntry);
    }

    public boolean hasMoreElements() {
        return !this.queue.isEmpty();
    }

    public Object nextElement() {
        if (!hasMoreElements()) {
            throw new NoSuchElementException();
        }
        // pop next entry from the queue
        ISO9660FileEntry entry = (ISO9660FileEntry) this.queue.remove(0);
        // if the entry is a directory, queue all its children
        if (entry.isDirectory()) {
            int offset = 0;
            final byte[] buffer;
            try {
                buffer = entry.getFileData();
            } catch (LoopyException ex) {
                throw new RuntimeException(ex);
            }
            while (offset < buffer.length && LittleEndian.getUInt8(buffer, offset) > 0) {
                ISO9660FileEntry child = new ISO9660FileEntry(
                        this.isoFile, entry.getPath(), buffer, offset + 1, entry.getEncoding());
                offset += child.getEntryLength();
                // It doesn't seem useful to include the . and .. entries
                if (!".".equals(child.getName()) && !"..".equals(child.getName())) {
                    this.queue.add(child);
                }
            }
        }
        return entry;
    }
}
