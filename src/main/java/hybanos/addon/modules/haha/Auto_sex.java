package hybanos.addon.modules.haha;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.mixin.KeyBindingAccessor;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import baritone.api.BaritoneAPI;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class Auto_sex extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSneaking = settings.createGroup("Sneaking");
    private final SettingGroup sgBaritone = settings.createGroup("Baritone");

    public enum Mode {
        PlayerName,
        ClosestPlayer
    }

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Which method to sex.")
        .defaultValue(Mode.PlayerName)
        .build()
    );

    private final Setting<String> user = sgGeneral.add(new StringSetting.Builder()
        .name("Player")
        .description("The player to sex (obviously Hybanos)")
        .defaultValue("Hybanos")
        .visible(() -> mode.get() == Mode.PlayerName)
        .build()
    );

    private final Setting<List<String>> messages = sgGeneral.add(new StringListSetting.Builder()
        .name("messages")
        .description("Messages to use for sex.")
        .defaultValue(List.of("Fuck me hard daddy %s","Spill your white cum all over me %s","Seggs me %s","uwu rawr~ %s","Tonight I'm all yours %s", "Let me swallow your babies %s"))
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay between specified messages in ticks.")
        .defaultValue(100)
        .min(0)
        .sliderMax(200)
        .build()
    );

    private final Setting<Boolean> sneaking_active = sgSneaking.add(new BoolSetting.Builder()
        .name("sneaking")
        .description("Enables sneaking.")
        .build()
    );

    private final Setting<Integer> sneak_delay = sgSneaking.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay between specified messages in ticks.")
        .defaultValue(1)
        .min(1)
        .sliderMax(200)
        .build()
    );

    private final Setting<Boolean> baritone_active = sgBaritone.add(new BoolSetting.Builder()
        .name("Follow player")
        .description("Enables following.")
        .build()
    );

    public Auto_sex() {
        super(HAHAddon.CATEGORY, "Auto Sex", "%s will be replaced by sex partner");
    }

    private final List<AbstractClientPlayerEntity> players = new ArrayList<>();
    private int tickcount, timer, sneak_timer;
    private boolean first;
    private String player_name;

    @Override
    public void onActivate() {
        first = true;
        tickcount = 0;
        timer = delay.get();
        sneak_timer = sneak_delay.get();
    }

    @Override
    public void onDeactivate() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("stop");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        tickcount++;
        if (messages.get().isEmpty()) return;
        getPlayers();
        if (mode.get() == Mode.PlayerName) {
            player_name = user.get();
        }
        else {
            if (players.size() <= 1) player_name = "";
            else player_name = players.get(1).getEntityName();
        }

        if (tickcount % timer == 0) {
            int i;
            i = Utils.random(0, messages.get().size());

            String text = messages.get().get(i);

            text = String.format(text, player_name);

            mc.player.networkHandler.sendChatMessage(text);
        }

        if (sneaking_active.get()) {
            if (tickcount % sneak_timer == 0) {
                set(mc.options.sneakKey, true);
                sneak_timer = sneak_delay.get();
            }
            else {
                set(mc.options.sneakKey, false);
            }
        }

        if (baritone_active.get()) {
            if (tickcount % 400 == 0 || first) {
                first = false;
                String command = String.format("follow player %s", player_name);
                BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute(command);
            }
        }
    }




    private void set(KeyBinding bind, boolean pressed) {
        boolean wasPressed = bind.isPressed();
        bind.setPressed(pressed);

        InputUtil.Key key = ((KeyBindingAccessor) bind).getKey();
        if (wasPressed != pressed && key.getCategory() == InputUtil.Type.KEYSYM) {
            MeteorClient.EVENT_BUS.post(KeyEvent.get(key.getCode(), 0, pressed ? KeyAction.Press : KeyAction.Release));
        }
    }

    private List<AbstractClientPlayerEntity> getPlayers() {
        players.clear();
        players.addAll(mc.world.getPlayers());
        players.sort(Comparator.comparingDouble(e -> e.distanceTo(mc.getCameraEntity())));

        return players;
    }
}
