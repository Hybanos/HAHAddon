package hybanos.addon;

import hybanos.addon.modules.*;
import hybanos.addon.hud.NoLeakPos;
import hybanos.addon.hud.Ducko;
import hybanos.addon.hud.Duck_logo;
import hybanos.addon.hud.Better_compass;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class HAHAddon extends MeteorAddon {
	public static final Logger LOG = LoggerFactory.getLogger(HAHAddon.class);
	public static final Category CATEGORY = new Category("HAHA", Items.GOLDEN_CARROT.getDefaultStack());
    public static final Category SKID = new Category("HEHE", Items.COOKIE.getDefaultStack());

	@Override
	public void onInitialize() {
		LOG.info("Initializing HAHAddon");

		// Required when using @EventHandler
		MeteorClient.EVENT_BUS.registerLambdaFactory("hybanos.addon", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

		// Modules
        Modules.get().add(new Auto_grow());
		Modules.get().add(new Auto_sex());
        Modules.get().add(new More_tracers());
        Modules.get().add(new Stay_above());
        Modules.get().add(new StashFinder());
        Modules.get().add(new No_bed_interact());
        Modules.get().add(new Block_rotation());
        Modules.get().add(new SpamP());
        Modules.get().add(new Trash_can());
        Modules.get().add(new Photoshoot());
        Modules.get().add(new Highway_Builder());
        Modules.get().add(new Villager_Aura());
        Modules.get().add(new F3_crosshair());

        // HUD
        HUD hud = meteordevelopment.meteorclient.systems.Systems.get(HUD.class);
        hud.bottomRight.add(new NoLeakPos(hud));
        // hud.bottomRight.add(new Ducko(hud));
        hud.bottomRight.add(new Duck_logo(hud));
        hud.bottomRight.add(new Better_compass(hud));
	}

	@Override
	public void onRegisterCategories() {
		Modules.registerCategory(CATEGORY);
        Modules.registerCategory(SKID);
	}
}
