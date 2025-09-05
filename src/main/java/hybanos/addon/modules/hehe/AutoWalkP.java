package hybanos.addon.modules.hehe;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;


// Made by Cookie
// I suck at code 
// probably a mess, good luck

public class AutoWalkP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgWalk = settings.createGroup("Delay");

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Walking mode.")
        .defaultValue(Mode.Simple)
        .onChanged(mode1 -> {
            if (isActive()) {
                if (mode1 == Mode.Simple) {
                } else {
                }
            }
        })
        .build()
    );

    private final Setting<Direction> direction = sgGeneral.add(new EnumSetting.Builder<Direction>()
        .name("direction 1")
        .description("The first direction to walk in any mode.")
        .defaultValue(Direction.Forwards)
        .onChanged(direction1 -> {
            if (isActive()) unpress();
        })
        .build()
    );

    private final Setting<Direction> direction2 = sgGeneral.add(new EnumSetting.Builder<Direction>()
        .name("direction 2")
        .description("The second direction to walk in double mode.")
        .defaultValue(Direction.Left)
        .onChanged(direction2 -> {
            if (isActive()) unpress();
        })
        .visible(() -> mode.get() == Mode.Double)
        .build()
    );	

    // Delay

    private final Setting<Boolean> walk_active = sgWalk.add(new BoolSetting.Builder()
        .name("Walk Delay")
        .description("Enables key press delay.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> trigger = sgWalk.add(new IntSetting.Builder()
        .name("Delay")
        .description("Delay of pressing the movement keys.")
        .defaultValue(5)
		.sliderMin(1)
        .sliderMax(20)
        .build()
    );
	
    private final Setting<Integer> stop = sgWalk.add(new IntSetting.Builder()
        .name("Stop")
        .description("Time it stops until it moves again.")
        .defaultValue(5)
		.sliderMin(1)
        .sliderMax(20)
        .build()
    );

    public AutoWalkP() {
        super(HAHAddon.COOKIE, "Auto Walk+", "Auto Walk but you can press 2 keys with delay.");
    }

    private int timer;

    private void setPressed(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }
	
    private void unpress() {
        setPressed(mc.options.forwardKey, false);
        setPressed(mc.options.backKey, false);
        setPressed(mc.options.leftKey, false);
        setPressed(mc.options.rightKey, false);
    }

	@Override
    public void onDeactivate() {
        if (mode.get() == Mode.Simple) unpress();
        if (mode.get() == Mode.Double) unpress();
    }
	
    @EventHandler
    private void onTick(TickEvent.Post event) {
        switch (direction.get()) {
            case Forwards -> setPressed(mc.options.forwardKey, true);
            case Backwards -> setPressed(mc.options.backKey, true); 
            case Left -> setPressed(mc.options.leftKey, true);
            case Right -> setPressed(mc.options.rightKey, true);
        }
	
		if (mode.get() == Mode.Double) {
            switch (direction2.get()) {
                case Forwards -> setPressed(mc.options.forwardKey, true);
                case Backwards -> setPressed(mc.options.backKey, true); 
                case Left -> setPressed(mc.options.leftKey, true);
                case Right -> setPressed(mc.options.rightKey, true);				
            }
		}
        
		if (walk_active.get() == (true)){
			if (timer < trigger.get()) {
				timer++;			
				return;	
			} else {
                unpress();	
                if (timer - trigger.get() < stop.get()) {
                    timer++;			
                    return;	
                } else {
                    timer = 0;
                }
			}
		}		
	}	

    public enum Mode {
        Simple,
		Double
    }

    public enum Direction {
        Forwards,
        Backwards,
        Left,
        Right
    }
}
