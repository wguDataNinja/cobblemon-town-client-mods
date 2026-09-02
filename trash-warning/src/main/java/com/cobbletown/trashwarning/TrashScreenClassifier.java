package com.cobbletown.trashwarning;

import com.mojang.serialization.JsonOps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.TextCodecs;

/** Reads a screen once at initialization; it has no render or interaction side effects. */
public final class TrashScreenClassifier {
    private TrashScreenClassifier() { }

    public static boolean isObservedTrash(Screen screen) {
        // Exact classes and topology deliberately reject lookalike server/plugin screens.
        if (screen == null || screen.getClass() != GenericContainerScreen.class) return false;
        GenericContainerScreen genericScreen = (GenericContainerScreen) screen;
        GenericContainerScreenHandler handler = genericScreen.getScreenHandler();
        if (handler.getClass() != GenericContainerScreenHandler.class) return false;
        return TrashFingerprint.matches(snapshot(handler, genericScreen.getTitle()));
    }

    static TrashFingerprint.Snapshot snapshot(GenericContainerScreenHandler handler, net.minecraft.text.Text title) {
        int upper = 0;
        int player = 0;
        // Verify slot ownership and coordinates once; render must never rescan slots.
        boolean topology = handler.slots.size() == TrashFingerprint.SLOT_COUNT;
        PlayerInventory playerInventory = MinecraftClient.getInstance().player == null
            ? null : MinecraftClient.getInstance().player.getInventory();
        for (int index = 0; index < handler.slots.size(); index++) {
            Slot slot = handler.slots.get(index);
            boolean isPlayer = playerInventory != null && slot.inventory == playerInventory;
            if (isPlayer) player++; else upper++;
            topology &= expectedSlot(index, slot, isPlayer);
        }
        String structured = TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, title).result()
            .map(Object::toString).orElse("");
        return new TrashFingerprint.Snapshot(
            true,
            true,
            handler.getRows(),
            handler.slots.size(),
            upper,
            player,
            topology,
            title.getString(),
            structured
        );
    }

    private static boolean expectedSlot(int index, Slot slot, boolean isPlayer) {
        if (index < TrashFingerprint.UPPER_SLOTS) {
            return !isPlayer && slot.x == 8 + (index % 9) * 18 && slot.y == 18 + (index / 9) * 18;
        }
        if (index < 54) {
            int playerIndex = index - 27;
            return isPlayer && slot.x == 8 + (playerIndex % 9) * 18 && slot.y == 85 + (playerIndex / 9) * 18;
        }
        int hotbarIndex = index - 54;
        return isPlayer && slot.x == 8 + hotbarIndex * 18 && slot.y == 143;
    }
}
