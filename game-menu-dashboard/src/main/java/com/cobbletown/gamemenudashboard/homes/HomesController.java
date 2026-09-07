package com.cobbletown.gamemenudashboard.homes;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.ArrayList;

/** Owns Homes truthfulness and the sole ordinary-command dispatch point. */
public final class HomesController {
    private static final Duration LIST_TIMEOUT = Duration.ofSeconds(5);

    public enum Truth { UNLOADED, AWAITING_LIST, FRESH, STALE, FAILED }

    private HomeSnapshot snapshot;
    private Truth truth = Truth.UNLOADED;
    private Instant requestedAt;
    private long version;

    public HomeSnapshot snapshot() { return snapshot; }
    public Truth truth() { return truth; }
    public long version() { return version; }

    /** Restored local cache is useful but deliberately not represented as fresh server state. */
    public void restore(HomeSnapshot cached) {
        if (cached == null) return;
        snapshot = cached;
        truth = Truth.STALE;
        version++;
    }

    public void refresh() {
        if (dispatch("home list")) {
            truth = Truth.AWAITING_LIST;
            requestedAt = Instant.now();
            version++;
        }
    }

    public void go(String name) {
        if (HomeListParser.isSafeName(name)) dispatch("home " + name);
    }

    public void setHome(String name) {
        if (HomeListParser.isSafeName(name) && dispatch("sethome " + name)) {
            if (snapshot != null && snapshot.count() < snapshot.limit() && !snapshot.names().contains(name)) {
                ArrayList<String> names = new ArrayList<>(snapshot.names());
                names.add(name);
                snapshot = new HomeSnapshot(names, snapshot.count() + 1, snapshot.limit(), Instant.now());
            }
            markStale();
        }
    }

    public void delete(String name) {
        if (HomeListParser.isSafeName(name) && dispatch("delhome " + name)) {
            if (snapshot != null && snapshot.names().contains(name)) {
                ArrayList<String> names = new ArrayList<>(snapshot.names());
                names.remove(name);
                snapshot = new HomeSnapshot(names, snapshot.count() - 1, snapshot.limit(), Instant.now());
            }
            markStale();
        }
    }

    public void onGameMessage(Text message, boolean overlay) {
        if (overlay) return;
        Optional<HomeSnapshot> parsed = HomeListParser.parse(message.getString(), Instant.now());
        if (parsed.isPresent()) {
            snapshot = parsed.get();
            truth = Truth.FRESH;
            requestedAt = null;
            version++;
        }
    }

    public void tick() {
        if (truth == Truth.AWAITING_LIST && requestedAt != null
            && Instant.now().isAfter(requestedAt.plus(LIST_TIMEOUT))) {
            truth = Truth.FAILED;
            requestedAt = null;
            version++;
        }
    }

    private void markStale() {
        truth = snapshot == null ? Truth.UNLOADED : Truth.STALE;
        version++;
    }

    private boolean dispatch(String command) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) {
            truth = Truth.FAILED;
            version++;
            return false;
        }
        client.getNetworkHandler().sendChatCommand(command);
        return true;
    }
}
