package hybanos.addon.settings;


import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import net.minecraft.client.MinecraftClient;

    // Holy fuck what a mess

public class FactoryInjector extends DefaultSettingsWidgetFactory {
    public FactoryInjector(GuiTheme theme) {
        super(theme);
    }

    public static void injectFactories(FactoryInjectorAgain factory) {
        factory.additions$putFactory(Item2IntMapSetting.class, (Factory)(table, setting) -> item2intW(factory, table, (Item2IntMapSetting) setting));
    }

    private static void item2intW(FactoryInjectorAgain factory, WTable table, Item2IntMapSetting setting) {
        factory.additions$selectW(table, setting, () -> MinecraftClient.getInstance().setScreen(new Item2IntMapSettingScreen(factory.additions$getTheme(), setting)));
    }
}
