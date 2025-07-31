package daripher.skilltree.network;

import com.mojang.logging.LogUtils;
import daripher.skilltree.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.Map;
import java.util.function.Supplier;

public class RequestSkillTreeOpenPacket {
    public RequestSkillTreeOpenPacket() {}
    public RequestSkillTreeOpenPacket(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}

    private static final Logger LOGGER = LogUtils.getLogger();

    // Укажи здесь ID дерева навыков по умолчанию (например: "skilltree:default_tree")
    private static final ResourceLocation DEFAULT_TREE = new ResourceLocation("skilltree", "default_tree");

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return;

        ctx.get().enqueueWork(() -> {
            CompoundTag data = player.getPersistentData();
            String selectedClass = data.getString("selected_class");

            Map<String, String> mapping = ModConfig.COMMON.getClassToTree();
            String treeStr;

            // Если класс не выбран, то древо не открывается
            if (selectedClass == null || selectedClass.isEmpty()) {
                player.displayClientMessage(Component.literal("Выберите класс перед открытием дерева навыков!")
                        .withStyle(ChatFormatting.RED), false);
                ctx.get().setPacketHandled(true);
                return;
            }

            // Иначе ищем дерево по классу
            treeStr = mapping.get(selectedClass);
            if (treeStr == null || treeStr.isEmpty()) {
                player.displayClientMessage(Component.literal("Для класса \"" + selectedClass + "\" не найдено дерево навыков.")
                        .withStyle(ChatFormatting.RED), false);
                return;
            }

            try {
                ResourceLocation treeId = new ResourceLocation(treeStr);
                ModNetwork.sendToClient(player, new OpenSkillTreePacket(treeId));
            } catch (Exception e) {
                player.displayClientMessage(Component.literal("Некорректный идентификатор дерева навыков: " + treeStr)
                        .withStyle(ChatFormatting.DARK_RED), false);
                LOGGER.warn("Invalid skill tree ID: {}", treeStr, e);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}