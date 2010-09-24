package net.didion.loopy.iso9660;

import java.io.IOException;
import java.io.InputStream;

class EntryInputStream extends InputStream {

    // entry within the IsoFile
    private ISO9660FileEntry entry;
    // current position within entry data
    private int pos;
    // number of remaining bytes within entry
    private int rem;
    // the source IsoFile
    private ISO9660FileSystem isoFile;

    EntryInputStream(ISO9660FileEntry entry, ISO9660FileSystem file) {
        this.pos = 0;
        this.rem = entry.getSize();
        this.entry = entry;
        this.isoFile = file;
    }

    public int read(byte b[], int off, int len) throws IOException {
        if (this.rem == 0) {
            return -1;
        }
        if (len <= 0) {
            return 0;
        }
        if (len > this.rem) {
            len = this.rem;
        }

        synchronized (this.isoFile) {
            if (this.isoFile.isClosed()) {
                throw new IOException("ZipFile closed.");
            }
            len = this.isoFile.readData(this.entry, this.pos, b, off, len);
        }

        if (len > 0) {
            this.pos += len;
            this.rem -= len;
        }

        if (this.rem == 0) {
            close();
        }

        return len;
    }

    public int read() throws IOException {
        byte[] b = new byte[1];
        if (read(b, 0, 1) == 1) {
            return b[0] & 0xff;
        } else {
            return -1;
        }
    }

    public long skip(long n) {
        int len = n > rem ? rem : (int) n;
        this.pos += len;
        this.rem -= len;
        if (this.rem == 0) {
            close();
        }
        return len;
    }

    public int available() {
        return this.rem;
    }

    public int size() {
        return this.entry.getSize();
    }

    public void close() {
        this.rem = 0;
        this.entry = null;
        this.isoFile = null;
    }
}