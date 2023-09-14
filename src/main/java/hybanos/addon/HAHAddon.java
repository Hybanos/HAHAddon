package hybanos.addon;

import hybanos.addon.modules.haha.*;
import hybanos.addon.modules.hehe.*;
import hybanos.addon.modules.hihi.*;
import hybanos.addon.hud.*;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.hud.*;
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

    public static final HudGroup HUD_GROUP = new HudGroup("HAHA");

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
        Modules.get().add(new NotifierP());
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

        // Hud
        Hud.get().register(BetterCompass.INFO);
        Hud.get().register(NoLeakPos.INFO);
        Hud.get().register(New_chunks.INFO);
        Hud.get().register(Duck_logo.INFO);
	}

	@Override
	public void onRegisterCategories() {
		Modules.registerCategory(CATEGORY);
        Modules.registerCategory(COOKIE);
        Modules.registerCategory(SKID);
	}

    @Override
    public String getPackage() {
        return "hybanos.addon";
    }
}
