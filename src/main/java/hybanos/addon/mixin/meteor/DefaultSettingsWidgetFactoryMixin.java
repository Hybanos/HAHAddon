package hybanos.addon.mixin.meteor;

import hybanos.addon.settings.FactoryInjector;
import hybanos.addon.settings.FactoryInjectorAgain;
import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.settings.Setting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(DefaultSettingsWidgetFactory.class)
public abstract class DefaultSettingsWidgetFactoryMixin extends SettingsWidgetFactory implements FactoryInjectorAgain {
    //@Shadow(remap = false) @Final private Map<Class<?>, Object> factories;

    // @Shadow(remap = false) @Final private GuiTheme theme;

    public DefaultSettingsWidgetFactoryMixin(GuiTheme theme) {
        super(theme);
    }

    @Shadow(remap = false) protected abstract void selectW(WContainer c, Setting<?> setting, Runnable action);

    @Inject(at = @At("TAIL"), method = "<init>(Lmeteordevelopment/meteorclient/gui/GuiTheme;)V", remap = false)
    private void addCustomFactories(GuiTheme theme, CallbackInfo ci) {
        FactoryInjector.injectFactories(this);
    }

    @Override
    public void additions$putFactory(Class<?> klazz, Object factory) {
        factories.put(klazz, (Factory) factory);
    }

    @Override
    public GuiTheme additions$getTheme() {
        return theme;
    }

    @Override
    public void additions$selectW(WContainer c, Setting<?> setting, Runnable action) {
        selectW(c, setting, action);
    }
}
