package com.cobbletown.gamemenudashboard.claims;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Owns the bounded OWN-list → Info → Details navigation contract.
 * Server callback strings are held only in memory for the current response and are never cached.
 */
public final class ClaimsController {
    private static final Logger LOGGER = LoggerFactory.getLogger("game-menu-dashboard/claims");
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(8);
    private static final Pattern OWNER = Pattern.compile("(?i)owner\\s*:?\\s*([^\\n\\r]+)");
    private static final Pattern TYPE = Pattern.compile("(?i)type\\s*:?\\s*([^\\n\\r]+)");
    private static final Pattern AREA = Pattern.compile("(?i)area\\s*:?\\s*([^\\n\\r]+)");
    private static final Pattern BLOCKS = Pattern.compile("(?i)blocks?\\s*:?\\s*([0-9,]+)");
    private static final Pattern WORLD = Pattern.compile("(?i)world\\s*:?\\s*([^\\n\\r]+)");
    private static final Pattern LESSER = Pattern.compile("(?i)lesser\\s+boundary\\s+corner[^-0-9]*\\(?\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)");
    private static final Pattern GREATER = Pattern.compile("(?i)greater\\s+boundary\\s+corner[^-0-9]*\\(?\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)");

    public enum Truth { UNLOADED, AWAITING_LIST, FRESH, STALE, FAILED }
    private enum Pending { NONE, LIST, INFO, DETAILS, TRUST_PANEL, TRUST_ALL, TRUST_MUTATION }
    public enum TrustTruth { UNLOADED, AWAITING_PANEL, READY_FOR_ALL, AWAITING_ALL, FRESH, STALE, FAILED }

    private final ClaimCacheStore store = new ClaimCacheStore();
    private final Map<String, String> rowCallbacks = new HashMap<>();
    private List<ClaimEntry> claims = List.of();
    private Truth truth = Truth.UNLOADED;
    private Pending pending = Pending.NONE;
    private String selectedKey;
    private String detailsCallback;
    private Instant requestedAt;
    private Instant receivedAt;
    private DetailAccumulator details;
    private String trustClaimKey;
    private String trustAllCallback;
    private List<TrustedPlayer> trustedPlayers = List.of();
    private TrustTruth trustTruth = TrustTruth.UNLOADED;
    /** Kept only until the one command's matching server confirmation arrives. */
    private TrustMutation pendingTrustMutation;
    private long version;

    public void restore() {
        ClaimCacheStore.Cached cached = store.load();
        if (cached == null) return;
        claims = cached.claims(); receivedAt = cached.receivedAt(); truth = Truth.STALE; version++;
    }
    public List<ClaimEntry> claims() { return claims; }
    public Truth truth() { return truth; }
    public long version() { return version; }
    public String selectedKey() { return selectedKey; }
    public ClaimEntry selected() { return claims.stream().filter(entry -> entry.key().equals(selectedKey)).findFirst().orElse(null); }
    public boolean detailsAvailable() { return detailsCallback != null && selectedKey != null; }
    public List<TrustedPlayer> trustedPlayers() { return trustedPlayers; }
    public TrustTruth trustTruth() { return trustTruth; }
    public Instant receivedAt() { return receivedAt; }
    public void toggleDisplay(String key) {
        ClaimEntry selected = claims.stream().filter(entry -> entry.key().equals(key) && entry.geometry() != null).findFirst().orElse(null);
        if (selected == null) return;
        boolean enable = !selected.displayEnabled();
        List<ClaimEntry> next = new ArrayList<>(claims.size()); boolean changed = false;
        for (ClaimEntry entry : claims) {
            boolean nextEnabled = entry.key().equals(key) ? enable : entry.displayEnabled();
            next.add(entry.withDisplayEnabled(nextEnabled));
            changed |= entry.displayEnabled() != nextEnabled;
        }
        if (changed) { claims = List.copyOf(next); store.save(claims, receivedAt == null ? Instant.now() : receivedAt); version++; }
    }
    public void select(String key) {
        if (claims.stream().anyMatch(entry -> entry.key().equals(key) && entry.geometry() != null)) { selectedKey = key; version++; }
    }

    public void refresh() {
        if (!dispatch("claimslist")) return;
        rowCallbacks.clear(); detailsCallback = null; selectedKey = null; details = null;
        claims = List.of(); pending = Pending.LIST; truth = Truth.AWAITING_LIST; requestedAt = Instant.now(); version++;
    }
    public void openInfo(String key) {
        String callback = rowCallbacks.get(key);
        if (callback == null || !dispatchCallback(callback)) return;
        selectedKey = key; detailsCallback = null; details = null; pending = Pending.INFO; requestedAt = Instant.now(); version++;
    }
    public void loadDetails() {
        if (detailsCallback == null || selectedKey == null || !dispatchCallback(detailsCallback)) return;
        details = new DetailAccumulator(selectedKey); pending = Pending.DETAILS; requestedAt = Instant.now(); version++;
    }
    /** One ordinary command. The resulting ALL callback is deliberately a later UI action. */
    public void loadTrustList() {
        if (selectedKey == null || !dispatch("trustlist")) return;
        // A refresh must not make already-known access disappear while the ordinary
        // server response is in flight. A different Claim has no shared roster.
        if (!selectedKey.equals(trustClaimKey)) trustedPlayers = List.of();
        trustClaimKey = selectedKey; trustAllCallback = null;
        pending = Pending.TRUST_PANEL; trustTruth = TrustTruth.AWAITING_PANEL; requestedAt = Instant.now(); version++;
    }
    /** One fresh server navigation callback, held only from the current Trust response. */
    public void loadAllTrusted() {
        if (trustAllCallback == null || !dispatchCallback(trustAllCallback)) return;
        trustedPlayers = List.of(); pending = Pending.TRUST_ALL; trustTruth = TrustTruth.AWAITING_ALL; requestedAt = Instant.now(); version++;
    }
    /** Builder is the only currently proven direct Dashboard grant command. */
    public void grantBuilder(String player) {
        if (player == null || !player.matches("[A-Za-z0-9_]{3,16}") || !dispatch("trust " + player)) return;
        pendingTrustMutation = new TrustMutation(player, "Builder", true);
        pending = Pending.TRUST_MUTATION; requestedAt = Instant.now(); version++;
    }
    /** One direct current-claim revoke command; does not replay a stored row callback. */
    public void revokeTrust(String player) {
        if (player == null || player.isBlank() || !dispatch("untrust " + player)) return;
        pendingTrustMutation = new TrustMutation(player, "", false);
        pending = Pending.TRUST_MUTATION; requestedAt = Instant.now(); version++;
    }

    public void onGameMessage(Text message, boolean overlay) {
        if (overlay || pending == Pending.NONE) return;
        if (pending == Pending.LIST) consumeList(message);
        else if (pending == Pending.INFO) consumeInfo(message);
        else if (pending == Pending.DETAILS) consumeDetails(message.getString());
        else if (pending == Pending.TRUST_PANEL) consumeTrustPanel(message);
        else if (pending == Pending.TRUST_ALL) consumeTrustedRow(message);
        else if (pending == Pending.TRUST_MUTATION) consumeTrustMutation(message.getString());
    }

    public void tick() {
        if (pending != Pending.NONE && requestedAt != null && Instant.now().isAfter(requestedAt.plus(RESPONSE_TIMEOUT))) {
            if (pending == Pending.LIST) truth = claims.isEmpty() ? Truth.FAILED : Truth.FRESH;
            if (pending == Pending.TRUST_PANEL) trustTruth = TrustTruth.FAILED;
            if (pending == Pending.TRUST_ALL) trustTruth = TrustTruth.FRESH;
            pending = Pending.NONE; requestedAt = null; version++;
        }
    }

    private void consumeList(Text message) {
        List<StyledSegment> segments = segments(message);
        boolean changed = false; int callbacks = 0;
        for (StyledSegment segment : segments) {
            String callback = callback(segment.style());
            if (callback == null) continue;
            callbacks++;
            String hover = hover(segment.style());
            ClaimParts parts = claimParts(segment.text(), hover);
            if (parts == null) continue;
            String key = uniqueKey(parts, claims);
            if (rowCallbacks.putIfAbsent(key, callback) == null) {
                List<ClaimEntry> next = new ArrayList<>(claims);
                next.add(new ClaimEntry(key, parts.label(), parts.owner(), parts.type(), parts.area(), parts.blocks(), null, false, false));
                claims = List.copyOf(next); changed = true;
            }
        }
        if (changed) {
            claims = mergeCachedGeometry(claims);
            receivedAt = Instant.now(); truth = Truth.FRESH; requestedAt = Instant.now(); version++;
            LOGGER.info("Accepted {} OWN Claims rows from current structured list response", claims.size());
        } else if (callbacks > 0) {
            LOGGER.info("Claims list response had {} callback segments but no recognized OWN row hover shape", callbacks);
        }
    }

    private void consumeInfo(Text message) {
        for (StyledSegment segment : segments(message)) {
            if (!segment.text().toUpperCase(Locale.ROOT).contains("DETAIL")) continue;
            String callback = callback(segment.style());
            if (callback != null) {
                detailsCallback = callback; pending = Pending.NONE; requestedAt = null; version++;
                LOGGER.info("Received current Claim Info DETAILS navigation action for selected OWN row");
                return;
            }
        }
    }

    private void consumeDetails(String plain) {
        if (details == null) return;
        details.accept(plain);
        ClaimGeometry geometry = details.geometry();
        if (geometry == null) return;
        List<ClaimEntry> next = new ArrayList<>(claims.size());
        boolean priorMapChoice = store.displayEnabledFor(geometry);
        for (ClaimEntry entry : claims) next.add(entry.key().equals(selectedKey) ? entry.withGeometry(geometry, false).withDisplayEnabled(priorMapChoice) : entry);
        claims = List.copyOf(next); receivedAt = Instant.now(); truth = Truth.FRESH;
        pending = Pending.NONE; requestedAt = null; detailsCallback = null; details = null; version++;
        store.save(claims, receivedAt);
        LOGGER.info("Normalized and cached Details geometry for selected OWN claim (callback discarded)");
    }
    private void consumeTrustPanel(Text message) {
        for (StyledSegment segment : segments(message)) {
            if (!segment.text().strip().equalsIgnoreCase("ALL")) continue;
            String callback = callback(segment.style());
            if (callback == null) continue;
            trustAllCallback = callback; pending = Pending.NONE; requestedAt = null; trustTruth = TrustTruth.READY_FOR_ALL; version++;
            LOGGER.info("Received current Trust panel ALL navigation action (not persisted)");
            return;
        }
    }
    private void consumeTrustedRow(Text message) {
        String plain = message.getString().strip();
        if (!plain.startsWith("[-]")) return;
        String player = plain.substring(3).strip();
        if (player.isBlank() || player.length() > 64) return;
        boolean remove = segments(message).stream().anyMatch(segment -> hover(segment.style()).toLowerCase(Locale.ROOT).contains("remove") && callback(segment.style()) != null);
        if (!remove || trustedPlayers.stream().anyMatch(row -> row.player().equalsIgnoreCase(player))) return;
        List<TrustedPlayer> next = new ArrayList<>(trustedPlayers); next.add(new TrustedPlayer(player)); trustedPlayers = List.copyOf(next); version++;
    }
    private void consumeTrustMutation(String plain) {
        String normalized = plain.toLowerCase(Locale.ROOT);
        if (!normalized.contains("granted") && !normalized.contains("revoked")) return;
        if (pendingTrustMutation != null) {
            List<TrustedPlayer> next = new ArrayList<>(trustedPlayers);
            if (pendingTrustMutation.grant()) {
                next.removeIf(row -> row.player().equalsIgnoreCase(pendingTrustMutation.player()));
                next.add(new TrustedPlayer(pendingTrustMutation.player(), pendingTrustMutation.permission()));
            } else {
                next.removeIf(row -> row.player().equalsIgnoreCase(pendingTrustMutation.player()));
            }
            trustedPlayers = List.copyOf(next);
        }
        pendingTrustMutation = null;
        pending = Pending.NONE; requestedAt = null; trustTruth = TrustTruth.STALE; version++;
        LOGGER.info("Observed current-claim Trust mutation result; roster marked stale for deliberate reload");
    }

    /** Cache merge is intentionally conservative; ambiguous default labels get no geometry match. */
    private List<ClaimEntry> mergeCachedGeometry(List<ClaimEntry> latest) {
        ClaimCacheStore.Cached cached = store.load();
        if (cached == null) return latest;
        List<ClaimEntry> merged = new ArrayList<>(latest.size());
        for (ClaimEntry entry : latest) {
            List<ClaimEntry> matches = cached.claims().stream().filter(old -> sameListIdentity(old, entry) && old.geometry() != null).toList();
            merged.add(matches.size() == 1 ? entry.withGeometry(matches.getFirst().geometry(), false).withDisplayEnabled(matches.getFirst().displayEnabled()) : entry);
        }
        return List.copyOf(merged);
    }
    private static boolean sameListIdentity(ClaimEntry a, ClaimEntry b) {
        return a.label().equalsIgnoreCase(b.label()) && a.owner().equalsIgnoreCase(b.owner())
            && a.type().equalsIgnoreCase(b.type()) && a.area().equalsIgnoreCase(b.area()) && a.blocks().equals(b.blocks());
    }

    private boolean dispatchCallback(String callback) { return dispatch(callback.startsWith("/") ? callback.substring(1) : callback); }
    private boolean dispatch(String command) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) { truth = Truth.FAILED; version++; return false; }
        client.getNetworkHandler().sendChatCommand(command); return true;
    }

    private static List<StyledSegment> segments(Text message) {
        List<StyledSegment> found = new ArrayList<>();
        message.visit((style, text) -> { if (!text.isBlank()) found.add(new StyledSegment(style, text)); return Optional.empty(); }, Style.EMPTY);
        return found;
    }
    private static String callback(Style style) {
        ClickEvent click = style.getClickEvent();
        return click != null && click.getAction() == ClickEvent.Action.RUN_COMMAND && click.getValue().startsWith("/gd:callback") ? click.getValue() : null;
    }
    private static String hover(Style style) { return style.getHoverEvent() == null ? "" : style.getHoverEvent().getValue(net.minecraft.text.HoverEvent.Action.SHOW_TEXT).getString(); }
    private static ClaimParts claimParts(String visible, String hover) {
        String label = visible.strip();
        if (label.isBlank() || label.length() > 96 || !hover.toLowerCase(Locale.ROOT).contains("owner")) return null;
        String owner = group(OWNER, hover), type = group(TYPE, hover), area = group(AREA, hover), blocks = group(BLOCKS, hover);
        if (owner.isBlank() || type.isBlank() || area.isBlank()) return null;
        return new ClaimParts(label, owner, type, area, blocks);
    }
    private static String uniqueKey(ClaimParts parts, List<ClaimEntry> entries) {
        String base = (parts.label() + "|" + parts.owner() + "|" + parts.type() + "|" + parts.area() + "|" + parts.blocks()).toLowerCase(Locale.ROOT);
        int duplicate = 1; String key = base;
        while (containsKey(entries, key)) key = base + "#" + (++duplicate);
        return key;
    }
    private static boolean containsKey(List<ClaimEntry> entries, String key) {
        for (ClaimEntry entry : entries) if (entry.key().equals(key)) return true;
        return false;
    }
    private static String group(Pattern pattern, String value) { Matcher matcher = pattern.matcher(value); return matcher.find() ? matcher.group(1).strip() : ""; }
    private record StyledSegment(Style style, String text) { }
    private record ClaimParts(String label, String owner, String type, String area, String blocks) { }
    public record TrustedPlayer(String player, String permission) {
        public TrustedPlayer(String player) { this(player, ""); }
    }
    private record TrustMutation(String player, String permission, boolean grant) { }
    private static final class DetailAccumulator {
        private final String key; private String world; private int[] lesser, greater;
        DetailAccumulator(String key) { this.key = key; }
        void accept(String text) {
            if (world == null) { String found = group(WORLD, text); if (!found.isBlank()) world = found.split("\\s+")[0]; }
            if (lesser == null) lesser = corner(LESSER, text);
            if (greater == null) greater = corner(GREATER, text);
        }
        ClaimGeometry geometry() { return world != null && lesser != null && greater != null ? new ClaimGeometry(world, lesser[0], lesser[1], lesser[2], greater[0], greater[1], greater[2]) : null; }
        private static int[] corner(Pattern pattern, String text) {
            Matcher match = pattern.matcher(text); if (!match.find()) return null;
            try { return new int[] {Integer.parseInt(match.group(1)), Integer.parseInt(match.group(2)), Integer.parseInt(match.group(3))}; }
            catch (NumberFormatException ignored) { return null; }
        }
    }
}
