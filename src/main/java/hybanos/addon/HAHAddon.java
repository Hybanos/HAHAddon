package hybanos.addon;

import hybanos.addon.modules.*;
import hybanos.addon.hud.*;
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
    public static final Category COOKIE = new Category("HEHE", Items.COOKIE.getDefaultStack());
    public static final Category SKID = new Category("HIHI", Items.CHORUS_FRUIT.getDefaultStack());

	@Override
	public void onInitialize() {
		LOG.info("Initializing HAHAddon");

		// Required when using @EventHandler
		MeteorClient.EVENT_BUS.registerLambdaFactory("hybanos.addon", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        MeteorClient.EVENT_BUS.subscribe(New_chunks.class);

		// Modules
        Modules.get().add(new AntiAntiAFK());
        Modules.get().add(new AntiMob());
        Modules.get().add(new Auto_grow());
		Modules.get().add(new Auto_sex());
        Modules.get().add(new AutoWalkP());
        Modules.get().add(new Block_rotation());
        Modules.get().add(new Duck_Icon());
        Modules.get().add(new F3_crosshair());
        Modules.get().add(new Highway_Builder());
        Modules.get().add(new More_tracers());
        Modules.get().add(new No_bed_interact());
        Modules.get().add(new Photoshoot());
        Modules.get().add(new RedstonePlacement());
        Modules.get().add(new SCAFFOLD());
        Modules.get().add(new SpamP());
        Modules.get().add(new Stay_above());
        Modules.get().add(new StashFinder());
        // Modules.get().add(new Test());
        Modules.get().add(new Trash_can());
        Modules.get().add(new TPSLog());
        Modules.get().add(new Villager_Aura());

        HUD hud = meteordevelopment.meteorclient.systems.Systems.get(HUD.class);
        // HUD
        hud.topLeft.add(new NoLeakPos(hud));
        hud.topLeft.add(new Duck_logo(hud));
        hud.topLeft.add(new Better_compass(hud));
        hud.topLeft.add(new New_chunks(hud));
        // hud.topLeft.add(new Ducko(hud));
	}

	@Override
	public void onRegisterCategories() {
		Modules.registerCategory(CATEGORY);
        Modules.registerCategory(COOKIE);
        Modules.registerCategory(SKID);
	}
}
