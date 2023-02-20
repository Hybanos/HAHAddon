package hybanos.addon.modules.haha;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;

public class F3_crosshair extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> perspective = sgGeneral.add(new BoolSetting.Builder()
        .name("Third person")
        .description("Displays the crosshair even in third person.")
        .defaultValue(true)
        .build()
    );

    public F3_crosshair() {
        super(HAHAddon.CATEGORY, "F3 Crosshair", "Renders the f3 crosshair.");
    }

    public boolean getPerspective() {
        return perspective.get();
    }
}
