/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package hybanos.addon.modules.hihi;

import hybanos.addon.HAHAddon;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;

import java.util.Random;
import java.util.Set;
import java.util.UUID;


// Edited by Cookie
// now with cool chat coords where you met a player!

import static meteordevelopment.meteorclient.utils.player.ChatUtils.formatCoords;

public class NotifierP extends Module {
    private final SettingGroup sgTotemPops = settings.createGroup("Totem Pops");
    private final SettingGroup sgVisualRange = settings.createGroup("Visual Range");

    // Totem Pops

    private final Setting<Boolean> totemPops = sgTotemPops.add(new BoolSetting.Builder()
        .name("totem-pops")
        .description("Notifies you when a player pops a totem.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> totemsIgnoreOwn = sgTotemPops.add(new BoolSetting.Builder()
        .name("ignore-own")
        .description("Ignores your own totem pops.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> totemsIgnoreFriends = sgTotemPops.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Ignores friends totem pops.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> totemsIgnoreOthers = sgTotemPops.add(new BoolSetting.Builder()
        .name("ignore-others")
        .description("Ignores other players totem pops.")
        .defaultValue(false)
        .build()
    );

    // Visual Range

    private final Setting<Boolean> visualRange = sgVisualRange.add(new BoolSetting.Builder()
        .name("visual-range")
        .description("Notifies you when an entity enters your render distance.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Event> event = sgVisualRange.add(new EnumSetting.Builder<Event>()
        .name("event")
        .description("When to log the entities.")
        .defaultValue(Event.Both)
        .build()
    );

    private final Setting<Set<EntityType<?>>> entities = sgVisualRange.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Which entities to nofity about.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<Boolean> visualRangeIgnoreFriends = sgVisualRange.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Ignores friends.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> visualRangeIgnoreFakes = sgVisualRange.add(new BoolSetting.Builder()
        .name("ignore-fake-players")
        .description("Ignores fake players.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> getCoords = sgVisualRange.add(new BoolSetting.Builder()
    .name("player-logger")
    .description("Show the coords where the player spawned in your visual range.")
    .defaultValue(false)
    .build()
    );

    private final Setting<Double> spawnDistance = sgVisualRange.add(new DoubleSetting.Builder()
        .name("spawn distance (km)")
        .description("The max distance from spawn where the coords get printed in chat. (km)")
        .defaultValue(50)
        .sliderMin(0)
        .sliderMax(3000)
        .visible(() -> getCoords.get())
        .build()
);

    private final Object2IntMap<UUID> totemPopMap = new Object2IntOpenHashMap<>();
    private final Object2IntMap<UUID> chatIdMap = new Object2IntOpenHashMap<>();

    private final Random random = new Random();

    public NotifierP() {
        super(HAHAddon.SKID, "Notifier+", "Notifies you of different events.");
    }

    // Visual Range

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        double playerX = mc.player.getPos().getX();
        double playerZ = mc.player.getPos().getZ();
        double dist = Math.sqrt(playerX*playerX + playerZ*playerZ);

        if (event.entity.getUuid().equals(mc.player.getUuid()) || !entities.get().contains(event.entity.getType()) || !visualRange.get() || this.event.get() == Event.Despawn) return;
        if (event.entity instanceof PlayerEntity) {
            if ((!visualRangeIgnoreFriends.get() || !Friends.get().isFriend(((PlayerEntity) event.entity))) && (!visualRangeIgnoreFakes.get() || !(event.entity instanceof FakePlayerEntity))) {
                if (getCoords.get() && spawnDistance.get() * 1000 > dist) {
                    MutableText text = Text.literal(event.entity.getEntityName()).formatted(Formatting.WHITE);
                    text.append((Text.literal(" has spawned at ").formatted(Formatting.GRAY)));
                    text.append(formatCoords(event.entity.getPos()));
                    text.append((Text.literal(".")).formatted(Formatting.GRAY));
                    info(text);
                } else {
                    ChatUtils.sendMsg(event.entity.getId() + 100, Formatting.GRAY, "(highlight)%s(default) has entered your visual range!", event.entity.getEntityName());
                }
            }
        } else {
            if (getCoords.get() && spawnDistance.get() * 1000 > dist) {
                MutableText text = Text.literal(event.entity.getEntityName()).formatted(Formatting.WHITE);
                text.append((Text.literal(" has spawned at ").formatted(Formatting.GRAY)));
                text.append(formatCoords(event.entity.getPos()));
                text.append((Text.literal(".")).formatted(Formatting.GRAY));
                info(text);
            } else {
                ChatUtils.sendMsg(event.entity.getId() + 100, Formatting.GRAY, "(highlight)%s(default) has entered your visual range!", event.entity.getType());
            }
        }
    }

    @EventHandler
    private void onEntityRemoved(EntityRemovedEvent event) {
        double playerX = mc.player.getPos().getX();
        double playerZ = mc.player.getPos().getZ();
        double dist = Math.sqrt(playerX*playerX + playerZ*playerZ);

        if (event.entity.getUuid().equals(mc.player.getUuid()) || !entities.get().contains(event.entity.getType()) || !visualRange.get() || this.event.get() == Event.Spawn) return;
        if (event.entity instanceof PlayerEntity) {
            if ((!visualRangeIgnoreFriends.get() || !Friends.get().isFriend(((PlayerEntity) event.entity))) && (!visualRangeIgnoreFakes.get() || !(event.entity instanceof FakePlayerEntity))) {
                if (getCoords.get() && spawnDistance.get() * 1000 > dist) {
                    MutableText text = Text.literal(event.entity.getEntityName()).formatted(Formatting.WHITE);
                    text.append((Text.literal(" has despawned at ").formatted(Formatting.GRAY)));
                    text.append(formatCoords(event.entity.getPos()));
                    text.append((Text.literal(".")).formatted(Formatting.GRAY));
                    info(text);
                } else {
                    ChatUtils.sendMsg(event.entity.getId() + 100, Formatting.GRAY, "(highlight)%s(default) has left your visual range!", event.entity.getEntityName());
                }
            }
        } else {
            if (getCoords.get() && spawnDistance.get() * 1000 > dist) {
                MutableText text = Text.literal(event.entity.getEntityName()).formatted(Formatting.WHITE);
                text.append((Text.literal(" has despawned at ").formatted(Formatting.GRAY)));
                text.append(formatCoords(event.entity.getPos()));
                text.append((Text.literal(".")).formatted(Formatting.GRAY));
                info(text);
            } else {
                ChatUtils.sendMsg(event.entity.getId() + 100, Formatting.GRAY, "(highlight)%s(default) has left your visual range!", event.entity.getType());
            }
        }
    }

    // Totem Pops

    @Override
    public void onActivate() {
        totemPopMap.clear();
        chatIdMap.clear();
    }


    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        totemPopMap.clear();
        chatIdMap.clear();
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (!totemPops.get()) return;
        if (!(event.packet instanceof EntityStatusS2CPacket p)) return;

        if (p.getStatus() != 35) return;

        Entity entity = p.getEntity(mc.world);

        if (!(entity instanceof PlayerEntity)) return;

        if ((entity.equals(mc.player) && totemsIgnoreOwn.get())
            || (Friends.get().isFriend(((PlayerEntity) entity)) && totemsIgnoreOthers.get())
            || (!Friends.get().isFriend(((PlayerEntity) entity)) && totemsIgnoreFriends.get())
        ) return;

        synchronized (totemPopMap) {
            int pops = totemPopMap.getOrDefault(entity.getUuid(), 0);
            totemPopMap.put(entity.getUuid(), ++pops);

            ChatUtils.sendMsg(getChatId(entity), Formatting.GRAY, "(highlight)%s (default)popped (highlight)%d (default)%s.", entity.getEntityName(), pops, pops == 1 ? "totem" : "totems");
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!totemPops.get()) return;
        synchronized (totemPopMap) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (!totemPopMap.containsKey(player.getUuid())) continue;

                if (player.deathTime > 0 || player.getHealth() <= 0) {
                    int pops = totemPopMap.removeInt(player.getUuid());

                    ChatUtils.sendMsg(getChatId(player), Formatting.GRAY, "(highlight)%s (default)died after popping (highlight)%d (default)%s.", player.getEntityName(), pops, pops == 1 ? "totem" : "totems");
                    chatIdMap.removeInt(player.getUuid());
                }
            }
        }
    }

    private int getChatId(Entity entity) {
        return chatIdMap.computeIfAbsent(entity.getUuid(), value -> random.nextInt());
    }

    public enum Event {
        Spawn,
        Despawn,
        Both
    }
}
