package daripher.skilltree.network;

import daripher.skilltree.client.screen.SkillTreeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenSkillTreePacket {
    private final ResourceLocation treeId;

    public OpenSkillTreePacket(ResourceLocation treeId) {
        this.treeId = treeId;
    }

    public OpenSkillTreePacket(FriendlyByteBuf buf) {
        this.treeId = buf.readResourceLocation();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(treeId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(treeId));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient(ResourceLocation treeId) {
        Minecraft.getInstance().setScreen(new SkillTreeScreen(treeId));
    }
}


