package daripher.skilltree.client.widget.editor;

import daripher.skilltree.skill.PassiveSkill;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SkillDragger extends AbstractWidget {
  private final SkillTreeEditor editor;

  public SkillDragger(SkillTreeEditor editor) {
    super(0, 0, 0, 0, Component.empty());
    this.editor = editor;
  }

  @Override
  protected void renderWidget(
      @NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

  @Override
  public boolean mouseDragged(
      double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
    if (mouseButton == 0 && Screen.hasControlDown() && !editor.getSelectedSkills().isEmpty()) {
      dragSelectedSkills((float) dragX / editor.getZoom(), (float) dragY / editor.getZoom());
      return true;
    }
    return true;
  }

  private void dragSelectedSkills(float x, float y) {
    editor.getSelectedSkills().forEach(skill -> dragSkill(x, y, skill));
    editor.updateSkillConnections();
    editor.saveSelectedSkills();
  }

  private void dragSkill(float x, float y, PassiveSkill skill) {
    skill.setPosition(skill.getPositionX() + x, skill.getPositionY() + y);
    editor.getSkillButtons().removeIf(button -> button.skill == skill);
    editor.addSkillButton(skill);
  }

  @Override
  protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {}
}
