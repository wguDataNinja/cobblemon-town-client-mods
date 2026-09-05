package com.cobbletown.eggbadges;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/** Cached client-only egg badges. Lore is never read from the render callback. */
public final class EggBadgeRenderer {
    private static final Cache CACHE = new Cache();
    private EggBadgeRenderer() {}
    public static void tick(MinecraftClient client) { CACHE.tick(client.currentScreen); }
    public static void render(Screen screen, DrawContext context, int mouseX, int mouseY) {
        long started = System.nanoTime();
        if (screen instanceof HandledScreen<?> handled && CACHE.matches(screen, handled.getScreenHandler())) {
            for (int slot = CACHE.next(0); slot >= 0; slot = CACHE.next(slot + 1)) {
                Slot itemSlot = handled.getScreenHandler().slots.get(slot);
                if (!CACHE.slotStillMatches(slot, itemSlot.getStack())) { CACHE.invalidateActive(slot); continue; }
                drawMask(context, CACHE.mask(slot), handled.x + itemSlot.x, handled.y + itemSlot.y, false);
            }
            if (CACHE.cursorMask() != 0) drawMask(context, CACHE.cursorMask(), mouseX - 1, mouseY - 1, true);
        }
        CACHE.draw(System.nanoTime() - started);
    }
    private static void drawMask(DrawContext context, int mask, int x, int y, boolean cursor) {
        BadgeRule[] rules = EggBadgesConfig.INSTANCE.badges; int lane = 0;
        for (int badge = 0; rules != null && badge < Math.min(6, rules.length); badge++) {
            if ((mask & (1 << badge)) == 0) continue;
            BadgeRule rule = rules[badge];
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, rule.glyph,
                    x + (cursor ? 0 : (lane % 3) * 11 - 1), y + (cursor ? 0 : (lane / 3) * 9 - 1), rule.color);
            lane++;
        }
    }

    static final class Cache {
        private static final int STABLE_TICKS = BadgeClassificationScheduler.STABLE_TICKS_REQUIRED, SLOTS_PER_TICK = 6, DEFENSIVE_TICKS = 20;
        private final BadgeMaskBuffers buffers = new BadgeMaskBuffers();
        private int[] counts = new int[0]; private Object[] refs, components;
        private Object screen, handler, cursorRef, cursorComponents;
        private int revision = Integer.MIN_VALUE, rulesHash, stable, nextSlot, defensive, cursorCount, cursorMask;
        private boolean scanning, complete; private EggBadgePerformance performance;

        void tick(Screen current) {
            if (!(current instanceof HandledScreen<?> handled)) { finish(); return; }
            var currentHandler = handled.getScreenHandler(); int hash = rulesHash(EggBadgesConfig.INSTANCE.badges);
            if (performance == null) performance = new EggBadgePerformance();
            long started = System.nanoTime();
            boolean identityChanged = screen != current || handler != currentHandler;
            boolean configChanged = !identityChanged && rulesHash != hash;
            boolean revisionChanged = !identityChanged && !configChanged && revision != currentHandler.getRevision();
            if (identityChanged || configChanged) {
                screen = current; handler = currentHandler; revision = currentHandler.getRevision(); rulesHash = hash;
                restart(identityChanged ? BadgeClassificationScheduler.RestartReason.SCREEN : BadgeClassificationScheduler.RestartReason.CONFIG, currentHandler.slots.size());
            } else if (revisionChanged) {
                revision = currentHandler.getRevision();
                if (complete && reconcileChangedSlots(currentHandler.slots)) defensive = 0;
                else { invalidateChangedSlots(currentHandler.slots); restartScan(BadgeClassificationScheduler.RestartReason.REVISION, currentHandler.slots.size()); }
            } else if (!scanning && !complete && ++stable >= STABLE_TICKS) scanning = true;
            refreshCursor(currentHandler.getCursorStack());
            if (scanning) scan(currentHandler.slots);
            else if (complete && ++defensive >= DEFENSIVE_TICKS) {
                defensive = 0; long changedStarted = System.nanoTime();
                if (contentChanged(currentHandler.slots)) { invalidateChangedSlots(currentHandler.slots); restartScan(BadgeClassificationScheduler.RestartReason.CONTENT, currentHandler.slots.size()); }
                performance.changed(System.nanoTime() - changedStarted);
            }
            performance.refresh(System.nanoTime() - started);
        }
        private void restart(BadgeClassificationScheduler.RestartReason reason, int size) { performance.restart(reason); stable = 0; nextSlot = 0; scanning = complete = false; defensive = 0; clear(size); }
        private void restartScan(BadgeClassificationScheduler.RestartReason reason, int size) { performance.restart(reason); stable = 0; nextSlot = 0; scanning = complete = false; defensive = 0; buffers.resize(size); buffers.clearPending(); }
        private void scan(List<Slot> list) { long started = System.nanoTime(); int end = Math.min(list.size(), nextSlot + SLOTS_PER_TICK); for (; nextSlot < end; nextSlot++) classifySlot(nextSlot, list.get(nextSlot).getStack(), false); performance.scan(System.nanoTime() - started); if (nextSlot >= list.size()) { scanning = false; complete = true; buffers.swap(); performance.complete(); } }
        private void classifySlot(int index, ItemStack stack, boolean active) { refs[index] = stack; counts[index] = stack.getCount(); components[index] = stack.getComponents(); int mask = classify(stack); if (active) buffers.setActive(index, mask); else buffers.setPending(index, mask); }
        private int classify(ItemStack stack) { EggClassifier.ReadResult read = EggClassifier.read(stack, EggBadgesConfig.INSTANCE.badges); performance.classified(read.egg(), read.loreLines()); int mask = 0; BadgeRule[] rules = EggBadgesConfig.INSTANCE.badges; if (read.attributes() != null && rules != null) for (int b = 0; b < Math.min(6, rules.length); b++) if (EggClassifier.matches(read.attributes(), rules[b])) mask |= 1 << b; return mask; }
        private boolean reconcileChangedSlots(List<Slot> list) { if (refs == null || refs.length != list.size()) return false; long started = System.nanoTime(); for (int i = 0; i < list.size(); i++) { ItemStack s = list.get(i).getStack(); if (refs[i] != s || counts[i] != s.getCount() || components[i] != s.getComponents()) classifySlot(i, s, true); } performance.changed(System.nanoTime() - started); return true; }
        private void invalidateChangedSlots(List<Slot> list) { if (refs == null || refs.length != list.size()) { buffers.clearAll(); return; } for (int i = 0; i < list.size(); i++) { ItemStack s = list.get(i).getStack(); if (refs[i] != s || counts[i] != s.getCount() || components[i] != s.getComponents()) buffers.clearActive(i); } }
        private boolean contentChanged(List<Slot> list) { if (refs == null || refs.length != list.size()) return true; for (int i = 0; i < list.size(); i++) { ItemStack s = list.get(i).getStack(); if (refs[i] != s || counts[i] != s.getCount() || components[i] != s.getComponents()) return true; } return false; }
        private void refreshCursor(ItemStack stack) { if (cursorRef == stack && cursorCount == stack.getCount() && cursorComponents == stack.getComponents()) return; cursorRef = stack; cursorCount = stack.getCount(); cursorComponents = stack.getComponents(); cursorMask = classify(stack); }
        private void clear(int size) { buffers.resize(size); buffers.clearAll(); if (refs == null || refs.length != size) { refs = new Object[size]; components = new Object[size]; counts = new int[size]; } else { Arrays.fill(refs, null); Arrays.fill(components, null); Arrays.fill(counts, 0); } }
        boolean matches(Object value, Object valueHandler) { return screen == value && handler == valueHandler; }
        int next(int from) { int n = buffers.nextActive(from); return n < 0 ? -1 : n; } int mask(int slot) { return buffers.activeMask(slot); } int cursorMask() { return cursorMask; }
        boolean slotStillMatches(int slot, ItemStack stack) { return refs != null && slot < refs.length && refs[slot] == stack && counts[slot] == stack.getCount() && components[slot] == stack.getComponents(); }
        void invalidateActive(int slot) { buffers.clearActive(slot); }
        void draw(long ns) { if (performance != null) { performance.draw(ns); performance.frame(ns); } }
        void finish() { if (performance != null) performance.emit(); performance = null; screen = handler = cursorRef = cursorComponents = null; revision = Integer.MIN_VALUE; refs = components = null; counts = new int[0]; stable = nextSlot = defensive = cursorCount = cursorMask = 0; scanning = complete = false; }
        static int rulesHash(BadgeRule[] rules) { int hash = 1; if (rules == null) return hash; for (BadgeRule r : rules) hash = 31 * hash + (r == null ? 0 : java.util.Objects.hash(r.id, r.label, r.glyph, r.color, r.enabled, r.species, r.form, r.nature, r.gender, r.preset, Arrays.hashCode(r.operators), Arrays.hashCode(r.values))); return hash; }
    }
}
