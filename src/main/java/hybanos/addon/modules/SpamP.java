package hybanos.addon.modules;

import hybanos.addon.HAHAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.orbit.EventHandler;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.io.FileReader;
import java.io.IOException;

public class SpamP extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The amount of ticks between lines.")
        .defaultValue(5)
        .build()
    );

    private File file = new File(MeteorClient.FOLDER, "spam.txt");
    private final PointerBuffer filters;
    private ArrayList<String> lines = new ArrayList<String>();

    private int timer = 0;
    private Boolean shouldToggle = false;

    public SpamP() {
        super(HAHAddon.CATEGORY, "Spam+", "Import text from a file.");

        if (!file.exists()) {
            file = null;
        }

        filters = BufferUtils.createPointerBuffer(1);

        ByteBuffer txtFilter = MemoryUtil.memASCII("*.txt");

        filters.put(txtFilter);
        filters.rewind();
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WHorizontalList list = theme.horizontalList();

        WButton selectFile = list.add(theme.button("Select File")).widget();

        WLabel fileName = list.add(theme.label((file != null && file.exists()) ? file.getName() : "No file selected.")).widget();

        selectFile.action = () -> {
            String path = TinyFileDialogs.tinyfd_openFileDialog(
                "Select File",
                new File(MeteorClient.FOLDER, "spam.txt").getAbsolutePath(),
                filters,
                null,
                false
            );

            if (path != null) {
                file = new File(path);
                fileName.set(file.getName());
            }
        };

        return list;
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (file == null) {
            info("Please select a file.");
            toggle();
            return;
        }

        if (lines.size() == 0 && !shouldToggle) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }

                reader.close();
            } catch (IOException ignored) {
                error("Failed to read the file.");
            }
        } else if (lines.size() == 0 && shouldToggle) {
            toggle();
            return;
        }

        if (timer < delay.get()) {
            timer++;
            return;
        } else {
            timer = 0;
        }

        mc.player.sendChatMessage(lines.get(0));
        lines.remove(0);

        shouldToggle = true;
    }

    @Override
    public void onActivate() {
        shouldToggle = false;
    }

    public enum Mode {
        SPAM,
        CSV
    }

}
