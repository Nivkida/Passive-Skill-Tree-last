package daripher.skilltree.client.widget.editor.menu;

import daripher.skilltree.client.data.SkillTexturesData;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.skill.PassiveSkill;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

public class SkillTexturesEditor extends EditorMenu {
  public SkillTexturesEditor(SkillTreeEditor editor, EditorMenu previousMenu) {
    super(editor, previousMenu);
  }

  @Override
  public void init() {
    editor.addButton(0, 0, 90, 14, "Back").setPressFunc(b -> editor.selectMenu(previousMenu));
    editor.increaseHeight(29);
    PassiveSkill selectedSkill = editor.getFirstSelectedSkill();
    if (selectedSkill == null) return;
    if (editor.canEdit(PassiveSkill::getFrameTexture)) {
      editor.addLabel(0, 0, "Frame Texture", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor
          .addSelectionMenu(0, 0, 200, SkillTexturesData.BORDERS)
          .setValue(selectedSkill.getFrameTexture())
          .setElementNameGetter(TooltipHelper::getTextureName)
          .setResponder(this::setFrameTextures);
      editor.increaseHeight(19);
    }
    if (editor.canEdit(PassiveSkill::getTooltipFrameTexture)) {
      editor.addLabel(0, 0, "Tooltip Frame", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor
          .addSelectionMenu(0, 0, 200, SkillTexturesData.TOOLTIP_BACKGROUNDS)
          .setValue(selectedSkill.getTooltipFrameTexture())
          .setResponder(this::setTooltipFrameTextures)
          .setElementNameGetter(TooltipHelper::getTextureName);
      editor.increaseHeight(19);
    }
    if (editor.canEdit(PassiveSkill::getIconTexture)) {
      editor.addLabel(0, 0, "Icon Texture", ChatFormatting.GOLD);
      editor.increaseHeight(19);
      editor
          .addSelectionMenu(0, 0, 200, SkillTexturesData.ICONS)
          .setValue(selectedSkill.getIconTexture())
          .setElementNameGetter(TooltipHelper::getTextureName)
          .setResponder(this::setIconTextures);
      editor.increaseHeight(19);
    }
  }

  private void setFrameTextures(ResourceLocation value) {
    editor.getSelectedSkills().forEach(s -> s.setBackgroundTexture(value));
    editor.saveSelectedSkills();
  }

  private void setTooltipFrameTextures(ResourceLocation value) {
    editor.getSelectedSkills().forEach(s -> s.setBorderTexture(value));
    editor.saveSelectedSkills();
  }

  private void setIconTextures(ResourceLocation value) {
    editor.getSelectedSkills().forEach(s -> s.setIconTexture(value));
    editor.saveSelectedSkills();
  }
}
