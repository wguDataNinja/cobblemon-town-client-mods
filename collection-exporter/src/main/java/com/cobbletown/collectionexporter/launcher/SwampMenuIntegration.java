package com.cobbletown.collectionexporter.launcher;

import com.cobbletown.collectionexporter.CollectionExporterMod;
import com.cobbletown.collectionexporter.CollectionExporterRuntime;
import com.cobbletown.collectionexporter.ui.CollectionExporterScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hosted implementation of the Swamp Mods launcher contract.
 *
 * <p>This JAR presently hosts the launcher because the feature mods are independent and the
 * Egg Badges settings work is concurrent. Exactly one button is injected into
 * {@link GameMenuScreen}. One available entry opens directly; multiple entries open the
 * native selector. Egg Badges participates only when the installed mod exposes its stable
 * settings factory; otherwise it is silently omitted rather than advertised as broken.</p>
 */
public final class SwampMenuIntegration {
    private static final int LAUNCHER_WIDTH = 204;
    private static final Map<Screen, List<ClickableWidget>> INJECTED = new IdentityHashMap<>();
    private static boolean registered;

    private SwampMenuIntegration() {
    }

    public static synchronized void register(CollectionExporterRuntime runtime) {
        if (registered) {
            return;
        }
        registered = true;

        SwampModRegistry.register(new SwampModEntry(
                new SwampModDescriptor(
                        "collection-exporter",
                        "Collection Exporter",
                        "Copy a readable report of your Cobblemon PC collection.",
                        100),
                parent -> new CollectionExporterScreen(parent, runtime.controller())));

        registerEggBadgesIfAvailable();

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof GameMenuScreen) {
                attach(screen, width, height);
            }
        });
    }

    private static void registerEggBadgesIfAvailable() {
        FabricLoader loader = FabricLoader.getInstance();
        if (!loader.isModLoaded("egg-badges")) {
            return;
        }
        try {
            Class<?> type = Class.forName("com.cobbletown.eggbadges.EggBadgeSettingsScreen");
            Method resolvedCreate;
            try {
                resolvedCreate = type.getMethod("create", Screen.class);
            } catch (NoSuchMethodException ignored) {
                // The currently installed 0.4.x build exposes its stable public constructor.
                resolvedCreate = null;
            }
            Method create = resolvedCreate;
            Constructor<?> constructor = create == null
                    ? type.getConstructor(Screen.class)
                    : null;
            SwampModRegistry.register(new SwampModEntry(
                    new SwampModDescriptor(
                            "egg-badges",
                            "Egg Badges",
                            "Open the Egg Badges settings screen.",
                            0),
                    create != null
                            ? parent -> invokeFactory(create, parent)
                            : parent -> invokeConstructor(constructor, parent)));
        } catch (ReflectiveOperationException | LinkageError failure) {
            CollectionExporterMod.LOGGER.warn(
                    "Egg Badges is installed but its settings factory is unavailable; omitting it from the launcher",
                    failure);
        }
    }

    private static Screen invokeFactory(Method create, Screen parent) {
        try {
            Object screen = create.invoke(null, parent);
            return screen instanceof Screen value ? value : null;
        } catch (IllegalAccessException | InvocationTargetException failure) {
            throw new IllegalStateException("Egg Badges settings screen could not be opened", failure);
        }
    }

    private static Screen invokeConstructor(Constructor<?> constructor, Screen parent) {
        try {
            Object screen = constructor.newInstance(parent);
            return screen instanceof Screen value ? value : null;
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Egg Badges settings screen could not be opened", failure);
        }
    }

    private static void attach(Screen screen, int width, int height) {
        SwampLauncherModel model = SwampLauncherModel.installed(
                SwampModRegistry.descriptors(),
                FabricLoader.getInstance()::isModLoaded);
        if (model.mode() == SwampLauncherModel.Mode.NONE) {
            return;
        }

        remove(screen);
        ButtonWidget exit = findExitButton(screen);
        SwampLauncherPlacement.Bounds exitBounds = exit == null
                ? new SwampLauncherPlacement.Bounds(
                        Math.max(4, width / 2 - LAUNCHER_WIDTH / 2),
                        Math.max(4, height - 28),
                        Math.min(LAUNCHER_WIDTH, Math.max(1, width - 8)),
                        SwampLauncherPlacement.BUTTON_HEIGHT)
                : new SwampLauncherPlacement.Bounds(
                        exit.getX(), exit.getY(), exit.getWidth(), exit.getHeight());
        List<SwampLauncherPlacement.Bounds> occupied = Screens.getButtons(screen).stream()
                .map(widget -> new SwampLauncherPlacement.Bounds(
                        widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight()))
                .toList();

        SwampLauncherPlacement.choose(width, height, exitBounds, occupied, LAUNCHER_WIDTH)
                .ifPresent(placement -> {
                    ButtonWidget launcher = ButtonWidget.builder(
                                    Text.literal(model.buttonLabel()),
                                    button -> open(model, screen))
                            .dimensions(placement.x(), placement.y(), placement.width(), placement.height())
                            .build();
                    Screens.getButtons(screen).add(launcher);
                    INJECTED.put(screen, new ArrayList<>(List.of(launcher)));
                    ScreenEvents.remove(screen).register(SwampMenuIntegration::remove);
                });
    }

    private static void open(SwampLauncherModel model, Screen parent) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (model.mode() == SwampLauncherModel.Mode.DIRECT) {
            model.directEntry().ifPresent(descriptor -> open(client, parent, descriptor));
            return;
        }
        client.setScreen(new SwampModsScreen(parent, model.entries()));
    }

    static void open(MinecraftClient client, Screen parent, SwampModDescriptor descriptor) {
        SwampModEntry entry = SwampModRegistry.find(descriptor.modId());
        if (entry == null) {
            return;
        }
        try {
            Screen child = entry.screenFactory().create(parent);
            if (child != null) {
                client.setScreen(child);
            }
        } catch (RuntimeException | LinkageError failure) {
            CollectionExporterMod.LOGGER.error(
                    "Could not open the {} screen", descriptor.displayName(), failure);
        }
    }

    private static ButtonWidget findExitButton(Screen screen) {
        ButtonWidget bottomMost = null;
        for (ClickableWidget widget : Screens.getButtons(screen)) {
            if (!(widget instanceof ButtonWidget button)) {
                continue;
            }
            String label = button.getMessage().getString();
            if (label.equals("Disconnect") || label.startsWith("Save and Quit")) {
                return button;
            }
            if (bottomMost == null || button.getY() > bottomMost.getY()) {
                bottomMost = button;
            }
        }
        return bottomMost;
    }

    private static void remove(Screen screen) {
        List<ClickableWidget> widgets = INJECTED.remove(screen);
        if (widgets != null) {
            Screens.getButtons(screen).removeAll(widgets);
        }
    }
}
