/*
 *	UniqueIdDisposer.java
 *
 *	2006-06-14
 *
 *	Bj�rn Stickler <bjoern@stickler.de>
 */

package com.github.stephenc.javaisotools.udflib.tools;

public class UniqueIdDisposer {

    private long currentUniqueId;

    public UniqueIdDisposer() {
        currentUniqueId = 15;
    }

    public long getNextUniqueId() {
        currentUniqueId++;

        if ((currentUniqueId & 0xFFFFFFFF) == 0) {
            currentUniqueId |= 0x00000010;
        }

        return currentUniqueId;
    }

}
