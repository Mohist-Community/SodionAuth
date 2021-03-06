/*
 * Copyright 2020 Mohist-Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package red.mohist.sodionauth.bukkit.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import red.mohist.sodionauth.bukkit.BukkitLoader;
import red.mohist.sodionauth.core.SodionAuthCore;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class ListenerProtocolEvent {
    private final ProtocolManager protocolManager;

    public ListenerProtocolEvent() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(
                new PacketAdapter(BukkitLoader.instance, ListenerPriority.LOWEST,
                        PacketType.Play.Server.WINDOW_ITEMS, PacketType.Play.Server.SET_SLOT) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (event.getPacket().getIntegers().read(0) == 0
                                && SodionAuthCore.instance.needCancelled(BukkitLoader.instance.player2info(event.getPlayer()))) {
                            event.setCancelled(true);
                        }
                    }
                });
    }

    public void sendBlankInventoryPacket(Player player) {
        PacketContainer inventoryPacket = protocolManager.createPacket(PacketType.Play.Server.WINDOW_ITEMS);
        inventoryPacket.getIntegers().write(0, 0);
        ItemStack[] blankInventory = new ItemStack[45];
        int k = blankInventory.length;
        for (int i = 0; i < k; i++) {
            blankInventory[i] = new ItemStack(Material.AIR);
        }
        StructureModifier<ItemStack[]> itemArrayModifier = inventoryPacket.getItemArrayModifier();
        if (itemArrayModifier.size() > 0) {
            itemArrayModifier.write(0, blankInventory);
        } else {
            StructureModifier<List<ItemStack>> itemListModifier = inventoryPacket.getItemListModifier();
            itemListModifier.write(0, Arrays.asList(blankInventory));
        }
        try {
            protocolManager.sendServerPacket(player, inventoryPacket, false);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
