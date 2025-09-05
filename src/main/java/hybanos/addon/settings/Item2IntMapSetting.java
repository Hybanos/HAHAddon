/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package hybanos.addon.settings;

import hybanos.addon.HAHAddon;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.util.function.Consumer;

public class Item2IntMapSetting extends Setting<Object2IntMap<Item>> {

    public Item2IntMapSetting(String name, String description, Object2IntMap<Item> defaultValue, Consumer<Object2IntMap<Item>> onChanged, Consumer<Setting<Object2IntMap<Item>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        value = new Object2IntArrayMap<>(defaultValue);
    }

    @Override
    protected Object2IntMap<Item> parseImpl(String str) {
        String[] values = str.split(",");
        Object2IntMap<Item> items = createItemMap();
        try {
            for (String value : values) {
                String[] split = value.split(" ");
                Item item = parseId(Registries.ITEM, split[0]);
                int number = Integer.parseInt(split[1]);

                items.put(item, number);
            }
        } catch (Exception ignored) {}

        return items;
    }

    @Override
    protected boolean isValueValid(Object2IntMap<Item> value) {
        return true;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtCompound valueTag = new NbtCompound();
        for (Item item : get().keySet()) {
            Identifier id = Registries.ITEM.getId(item);
            if (id != null) valueTag.putInt(id.toString(), get().getInt(item));
        }
        tag.put("value", valueTag);

        return tag;
    }

    @Override
    public Object2IntMap<Item> load(NbtCompound tag) {
        get().clear();

        NbtCompound valueTag = tag.getCompound("value").get();
        for (String key : valueTag.getKeys()) {
            Item item = Registries.ITEM.getEntry(Identifier.of(key)).get().value();
            if (item != null) get().put(item, valueTag.getInt(key).get());
        }

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, Object2IntMap<Item>, Item2IntMapSetting> {
        public Builder() {
            super(new Object2IntArrayMap<>(0));
        }

        @Override
        public Item2IntMapSetting build() {
            return new Item2IntMapSetting(name, description, defaultValue, onChanged, onModuleActivated, visible);
        }
    }

    public static Object2IntMap<Item> createItemMap() {
        Object2IntMap<Item> map = new Object2IntArrayMap<>(Registries.ITEM.getIds().size());

        Registries.ITEM.forEach(item -> map.put(item, 0));

        return map;
    }
}
