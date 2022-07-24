package hybanos.addon.settings;

import hybanos.addon.HAHAddon;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WIntEdit;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.WItemWithLabel;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.item.Item;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Item2IntMapSettingScreen extends WindowScreen {
    private final Setting<Object2IntMap<Item>> setting;

    private WTable table;

    private WTextBox filter;
    private String filterText = "";

    public Item2IntMapSettingScreen(GuiTheme theme, Setting<Object2IntMap<Item>> setting) {
        super(theme, "Item");

        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        filter = add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initTable();
        };

        table = add(theme.table()).expandX().widget();
        initTable();
    }

    private void initTable() {
        List<Item> items = new ArrayList<>(setting.get().keySet());
        items.sort(Comparator.comparing(Item::getRawId));

        for (Item item : items) {
            String name = Names.get(item);
            if (!StringUtils.containsIgnoreCase(name, filterText)) continue;

            WItemWithLabel itemLabel = theme.itemWithLabel(item.getDefaultStack(), name);

            table.add(itemLabel).expandCellX();

            WIntEdit number = theme.intEdit(setting.get().getInt(item), 0, Integer.MAX_VALUE, true);
            number.action = () -> {
                setting.get().put(item, number.get());
                setting.onChanged();
            };

            table.add(number).minWidth(50);
            table.row();
        }
    }
}
