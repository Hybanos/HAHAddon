package hybanos.addon.settings;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.settings.Setting;

public interface FactoryInjectorAgain {
    void additions$putFactory(Class<?> klazz, Object factory);
    GuiTheme additions$getTheme();
    void additions$selectW(WContainer c, Setting<?> setting, Runnable action);
}
