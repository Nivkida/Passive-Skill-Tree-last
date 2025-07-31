package daripher.skilltree.client.init;

import com.mojang.blaze3d.platform.InputConstants;
import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.client.screen.SkillTreeScreen;
import daripher.skilltree.network.ModNetwork;
import daripher.skilltree.network.RequestSkillTreeOpenPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = SkillTreeMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PSTKeybinds {
  public static final KeyMapping SKILL_TREE_KEY = new KeyMapping(
          "key.display_skill_tree",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_O,
          "key.categories." + SkillTreeMod.MOD_ID
  );

  @SubscribeEvent
  public static void registerKeybinds(RegisterKeyMappingsEvent event) {
    event.register(SKILL_TREE_KEY);
  }

  @Mod.EventBusSubscriber(modid = SkillTreeMod.MOD_ID, value = Dist.CLIENT)
  public static class KeyEvents {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
      Minecraft mc = Minecraft.getInstance();
      if (event.getAction() != GLFW.GLFW_PRESS) return;
      if (mc.screen != null || mc.player == null) return;
      if (!SKILL_TREE_KEY.isDown()) return;

      // запрос на сервер
      ModNetwork.sendToServer(new RequestSkillTreeOpenPacket());
    }
  }
}
