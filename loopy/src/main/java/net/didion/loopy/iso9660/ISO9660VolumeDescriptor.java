package net.didion.loopy.iso9660;

import java.io.IOException;

import net.didion.loopy.FileEntry;
import net.didion.loopy.impl.VolumeDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Layout of Primary Volume Descriptor: <pre/> length     pos     contents ---------  ------
 * --------------------------------------------------------- 1          1       1 6          2       67, 68, 48, 48, 49
 * and 1, respectively (same as Volume Descriptor Set Terminator) 1          8       0 32         9       system
 * identifier 32         41      volume identifier 8          73      zeros 8          81      total number of sectors,
 * as a both endian double word 32         89      zeros 4          121     1, as a both endian word [volume set size] 4
 * 125     1, as a both endian word [volume sequence number] 4          129     2048 (the sector size), as a both endian
 * word 8          133     path table length in bytes, as a both endian double word 4          141 number of first
 * sector in first little endian path table, as a little endian double word 4          145     number of first sector in
 * second little endian path table, as a little endian double word, or zero if there is no second little endian path
 * table 4          149     number of first sector in first big endian path table, as a big endian double word 4
 *  153     number of first sector in second big endian path table, as a big endian double word, or zero if there is no
 * second big endian path table 34         157     root directory record, as described below 128 191     volume set
 * identifier 128        319     publisher identifier 128        447     data preparer identifier 128 575
 * application identifier 37         703     copyright file identifier 37         740     abstract file identifier 37
 *      777     bibliographical file identifier 17         814     date and time of volume creation 17 831     date and
 * time of most recent modification 17         848     date and time when volume expires 17 865     date and time when
 * volume is effective 1          882     1 1          883     0 512        884 reserved for application use (usually
 * zeros) 653        1396    zeros </pre>
 */
class ISO9660VolumeDescriptor implements VolumeDescriptor, Constants {

    private static final Log log = LogFactory.getLog(ISO9660VolumeDescriptor.class);

    private ISO9660FileSystem isoFile;

    // common
    private String systemIdentifier;
    private String volumeSetIdentifier;
    private String volumeIdentifier;
    private String publisher;
    private String preparer;
    private String application;
    private ISO9660FileEntry rootDirectoryEntry;

    // primary only
    private String standardIdentifier;
    private long totalBlocks;
    private int volumeSetSize;
    private int volumeSequenceNumber;
    private long creationTime;
    private long mostRecentModificationTime;
    private long expirationTime;
    private long effectiveTime;
    private long pathTableSize;
    private long locationOfLittleEndianPathTable;
    private long locationOfOptionalLittleEndianPathTable;
    private long locationOfBigEndianPathTable;
    private long locationOfOptionalBigEndianPathTable;

    // supplementary only
    public String encoding = DEFAULT_ENCODING;
    public String escapeSequences;

    private boolean hasPrimary = false;
    private boolean hasSupplementary = false;

    /**
     * Initialize this instance.
     */
    public ISO9660VolumeDescriptor(ISO9660FileSystem isoFile) {
        this.isoFile = isoFile;
    }

    public boolean read(byte[] buffer) throws IOException {
        final int type = Util.getType(buffer);
        boolean notTerminated = true;
        switch (type) {
            case VolumeDescriptorType.TERMINATOR:
                if (!hasPrimary) {
                    throw new IOException("No primary volume descriptor found");
                }
                notTerminated = false;
                break;
            case VolumeDescriptorType.BOOTRECORD:
                log.debug("Found boot record");
                break;
            case VolumeDescriptorType.PRIMARY_DESCRIPTOR:
                log.debug("Found primary descriptor");
                readPrimary(buffer);
                break;
            case VolumeDescriptorType.SUPPLEMENTARY_DESCRIPTOR:
                log.debug("Found supplementatory descriptor");
                readSupplementary(buffer);
                break;
            case VolumeDescriptorType.PARTITION_DESCRIPTOR:
                log.debug("Found partition descriptor");
                break;
            default:
                log.debug("Found unknown descriptor with type " + type);
        }
        return notTerminated;
    }

    void readPrimary(byte[] buffer) throws IOException {
        validateBlockSize(buffer);
        if (!this.hasSupplementary) {
            readCommon(buffer, DEFAULT_ENCODING);
        }
        this.standardIdentifier = Util.getDChars(buffer, 2, 5);
        this.volumeSetSize = Util.getUInt16Both(buffer, 121);
        this.volumeSequenceNumber = Util.getUInt16Both(buffer, 125);
        this.totalBlocks = Util.getUInt32Both(buffer, 81);
        this.publisher = Util.getDChars(buffer, 319, 128);
        this.preparer = Util.getDChars(buffer, 447, 128);
        this.application = Util.getDChars(buffer, 575, 128);
        //this.copyrightFile = Descriptor.get(buffer, 703, 37)
        //this.abstractFile = Descriptor.get(buffer, 740, 37)
        //this.bibliographicalFile = Descriptor.get(buffer, 777, 37)
        this.creationTime = Util.getStringDate(buffer, 814);
        this.mostRecentModificationTime = Util.getStringDate(buffer, 831);
        this.expirationTime = Util.getStringDate(buffer, 848);
        this.effectiveTime = Util.getStringDate(buffer, 865);
        this.pathTableSize = Util.getUInt32Both(buffer, 133);
        this.locationOfLittleEndianPathTable = Util.getUInt32LE(buffer, 141);
        this.locationOfOptionalLittleEndianPathTable = Util.getUInt32LE(buffer, 145);
        this.locationOfBigEndianPathTable = Util.getUInt32BE(buffer, 149);
        this.locationOfOptionalBigEndianPathTable = Util.getUInt32BE(buffer, 153);
        this.hasPrimary = true;
    }

    void readSupplementary(byte[] buffer) throws IOException {
        if (this.hasSupplementary) {
            return;
        }
        validateBlockSize(buffer);
        String escapeSequences = Util.getDChars(buffer, 89, 32);
        String enc = getEncoding(escapeSequences);
        if (null != enc) {
            this.encoding = enc;
            this.escapeSequences = escapeSequences;
            readCommon(buffer, this.encoding);
            this.hasSupplementary = true;
        } else {
            log.warn("Unsupported encoding, escapeSequences: '" + this.escapeSequences + "'");
        }
    }

    private void readCommon(byte[] buffer, String encoding) throws IOException {
        this.systemIdentifier = Util.getAChars(buffer, 9, 32, encoding);
        this.volumeIdentifier = Util.getDChars(buffer, 41, 32, encoding);
        this.volumeSetIdentifier = Util.getDChars(buffer, 191, 128, encoding);
        this.rootDirectoryEntry = new ISO9660FileEntry(this.isoFile, buffer, 157, encoding);
    }

    private void validateBlockSize(byte[] buffer) throws IOException {
        int blockSize = Util.getUInt16Both(buffer, 129);
        if (blockSize != BLOCK_SIZE) {
            throw new IOException("Invalid block size: " + blockSize);
        }
    }

    /**
     * Gets a derived encoding name from the given escape sequences.
     */
    private String getEncoding(String escapeSequences) {
        String encoding = null;
        if (escapeSequences.equals("%/@")) {
            // UCS-2 level 1
            encoding = "UTF-16BE";
        } else if (escapeSequences.equals("%/C")) {
            // UCS-2 level 2
            encoding = "UTF-16BE";
        } else if (escapeSequences.equals("%/E")) {
            // UCS-2 level 3
            encoding = "UTF-16BE";
        }
        return encoding;
    }

    public ISO9660FileSystem getIsoFile() {
        return isoFile;
    }

    public String getSystemIdentifier() {
        return systemIdentifier;
    }

    public String getVolumeSetIdentifier() {
        return volumeSetIdentifier;
    }

    public String getVolumeIdentifier() {
        return volumeIdentifier;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getPreparer() {
        return preparer;
    }

    public String getApplication() {
        return application;
    }

    public FileEntry getRootEntry() {
        return rootDirectoryEntry;
    }

    public String getStandardIdentifier() {
        return standardIdentifier;
    }

    public long getTotalBlocks() {
        return totalBlocks;
    }

    public int getVolumeSetSize() {
        return volumeSetSize;
    }

    public int getVolumeSequenceNumber() {
        return volumeSequenceNumber;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getMostRecentModificationTime() {
        return mostRecentModificationTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public long getEffectiveTime() {
        return effectiveTime;
    }

    public long getPathTableSize() {
        return pathTableSize;
    }

    public long getLocationOfLittleEndianPathTable() {
        return locationOfLittleEndianPathTable;
    }

    public long getLocationOfOptionalLittleEndianPathTable() {
        return locationOfOptionalLittleEndianPathTable;
    }

    public long getLocationOfBigEndianPathTable() {
        return locationOfBigEndianPathTable;
    }

    public long getLocationOfOptionalBigEndianPathTable() {
        return locationOfOptionalBigEndianPathTable;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getEscapeSequences() {
        return escapeSequences;
    }
}