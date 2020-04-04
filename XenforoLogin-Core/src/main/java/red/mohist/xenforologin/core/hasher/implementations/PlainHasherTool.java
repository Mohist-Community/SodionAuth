/*
 * This file is part of XenforoLogin, licensed under the GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You are not permitted to interfere any protection that prevents loading in CatServer
 *
 * Copyright (c) 2020 Mohist-Community.
 *
 */

package red.mohist.xenforologin.core.hasher.implementations;

import red.mohist.xenforologin.core.hasher.HasherTool;

public class PlainHasherTool extends HasherTool {
    public PlainHasherTool(int saltLength) {
        super(saltLength);
    }

    public boolean verify(String hash, String data) {
        return hash(data).equals(hash);
    }
}