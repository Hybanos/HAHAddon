package hybanos.addon;

import hybanos.addon.modules.Auto_grow;
import hybanos.addon.modules.Auto_sex;
import hybanos.addon.modules.More_tracers;
import hybanos.addon.modules.Stay_above;
import hybanos.addon.modules.StashFinder;
import hybanos.addon.modules.No_bed_interact;
import hybanos.addon.modules.Block_rotation;
import hybanos.addon.hud.NoLeakPos;
import hybanos.addon.hud.Ducko;
import hybanos.addon.hud.Duck_logo;
import hybanos.addon.hud.Better_compass;
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
	}
}
