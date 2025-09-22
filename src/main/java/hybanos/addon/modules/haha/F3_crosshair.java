package hybanos.addon.modules.haha;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;

public class F3_crosshair extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public F3_crosshair() {
        super(HAHAddon.CATEGORY, "F3 Crosshair", "Renders the f3 crosshair.");
    }
}
