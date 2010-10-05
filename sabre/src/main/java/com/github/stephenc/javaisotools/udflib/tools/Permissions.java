/*
 * Copyright (c) 2010. Stephen Connolly.
 * Copyright (c) 2006. Björn Stickler <bjoern@stickler.de>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.github.stephenc.javaisotools.udflib.tools;

public class Permissions {

    public static int OTHER_Execute = 0x00000001;
    public static int OTHER_Write = 0x00000002;
    public static int OTHER_Read = 0x00000004;
    public static int OTHER_ChAttr = 0x00000008;
    public static int OTHER_Delete = 0x00000010;

    public static int GROUP_Execute = 0x00000020;
    public static int GROUP_Write = 0x00000040;
    public static int GROUP_Read = 0x00000080;
    public static int GROUP_ChAttr = 0x00000100;
    public static int GROUP_Delete = 0x00000200;

    public static int OWNER_Execute = 0x00000400;
    public static int OWNER_Write = 0x00000800;
    public static int OWNER_Read = 0x00001000;
    public static int OWNER_ChAttr = 0x00002000;
    public static int OWNER_Delete = 0x00004000;

}
