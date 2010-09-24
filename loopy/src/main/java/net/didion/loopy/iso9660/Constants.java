package net.didion.loopy.iso9660;

interface Constants {

    int BLOCK_SIZE = 2 * 1024;
    int RESERVED_BYTES = 16 * BLOCK_SIZE;

    char SEPARATOR1 = '.';
    char SEPARATOR2 = ';';

    String DEFAULT_ENCODING = "US-ASCII";

    interface VolumeDescriptorType {

        int BOOTRECORD = 0;
        int PRIMARY_DESCRIPTOR = 1;
        int SUPPLEMENTARY_DESCRIPTOR = 2;
        int PARTITION_DESCRIPTOR = 3;
        int TERMINATOR = 255;
    }
}