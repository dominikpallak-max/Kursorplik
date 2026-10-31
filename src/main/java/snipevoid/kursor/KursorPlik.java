package snipevoid.kursor;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class KursorPlik {
    public static boolean enabled = false;

    private KursorPlik() {}

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) ->
                dispatcher.register(ClientCommandManager.literal("kursorplik").executes(ctx -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.execute(() -> client.setScreen(new KursorPlikScreen(client.currentScreen)));
                    return 1;
                }))
        );

        // PPM na blok -> nazwa bloku
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (enabled && hand == Hand.MAIN_HAND && world.isClient()) {
                showBlock(world, hit.getBlockPos());
            }
            return ActionResult.PASS;
        });

        // LPM i PPM na encję (hologram / latający item) -> nazwa encji
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (enabled && hand == Hand.MAIN_HAND && world.isClient()) {
                showEntity(entity);
            }
            return ActionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (enabled && hand == Hand.MAIN_HAND && world.isClient()) {
                showEntity(entity);
            }
            return ActionResult.PASS;
        });
    }

    private static void showBlock(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        String id = Registries.BLOCK.getId(block).toString();
        String key = block.getTranslationKey();
        send("§6Blok: §f" + id + " §7| klucz: §f" + key);
    }

    private static void showEntity(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getStack();
            Item item = stack.getItem();
            String id = Registries.ITEM.getId(item).toString();
            String key = item.getTranslationKey();
            send("§6Item: §f" + id + " §7| klucz: §f" + key + " §7x" + stack.getCount());
            return;
        }

        String typeId = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
        String typeKey = entity.getType().getTranslationKey();
        String custom = entity.hasCustomName() && entity.getCustomName() != null
                ? entity.getCustomName().getString()
                : null;

        StringBuilder sb = new StringBuilder("§6Encja: §f").append(typeId)
                .append(" §7| klucz: §f").append(typeKey);
        if (custom != null) sb.append(" §7| nazwa: §f").append(custom);
        if (entity instanceof ArmorStandEntity) sb.append(" §8(hologram)");
        send(sb.toString());
    }

    private static void send(String msg) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(msg), false);
        }
    }
}
