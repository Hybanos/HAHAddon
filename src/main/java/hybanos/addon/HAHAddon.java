package hybanos.addon;

import hybanos.addon.modules.Auto_grow;
import hybanos.addon.modules.Auto_sex;
import hybanos.addon.modules.Nav_tracer;
import hybanos.addon.modules.Highway_viewer;
import hybanos.addon.modules.Stay_above;
import hybanos.addon.modules.StashFinder;
import hybanos.addon.modules.No_bed_interact;
import hybanos.addon.hud.NoLeakPos;
import meteordevelopment.meteorclient.systems.hud.HUD;
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
        Modules.get().add(new Highway_viewer());
        Modules.get().add(new Nav_tracer());
        Modules.get().add(new Stay_above());
        Modules.get().add(new StashFinder());
        Modules.get().add(new No_bed_interact());

        // HUD
        HUD hud = meteordevelopment.meteorclient.systems.Systems.get(HUD.class);
        hud.bottomRight.add(new NoLeakPos(hud));
	}

	@Override
	public void onRegisterCategories() {
		Modules.registerCategory(CATEGORY);
	}
}
