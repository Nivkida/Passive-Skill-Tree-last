package daripher.skilltree.client.widget.editor.menu.bonuses;

import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.client.widget.editor.menu.EditorMenu;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.skill.PassiveSkill;
import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.List;
import net.minecraft.network.chat.Component;

public class SkillBonusesEditor extends EditorMenu {
  public SkillBonusesEditor(SkillTreeEditor editor, EditorMenu previousMenu) {
    super(editor, previousMenu);
  }

  @Override
  public void init() {
    editor.addButton(0, 0, 90, 14, "Back").setPressFunc(b -> editor.selectMenu(previousMenu));
    editor.increaseHeight(29);
    if (!canEditBonuses(editor)) return;
    SkillBonus<?> defaultBonus = PSTSkillBonuses.ATTRIBUTE.get().createDefaultInstance();
    editor
        .addSelectionMenu(110, -29, 90, defaultBonus)
        .setResponder(skillBonus -> addSkillBonuses(editor, skillBonus))
        .setMessage(Component.literal("Add"));
    PassiveSkill selectedSkill = editor.getFirstSelectedSkill();
    if (selectedSkill == null) return;
    List<SkillBonus<?>> bonuses = selectedSkill.getBonuses();
    for (int i = 0; i < bonuses.size(); i++) {
      final int bonusIndex = i;
      SkillBonus<?> bonus = bonuses.get(i);
      String message = bonus.getTooltip().getString();
      message = TooltipHelper.getTrimmedString(message, 190);
      editor
          .addButton(0, 0, 200, 14, message)
          .setPressFunc(b -> editor.selectMenu(new SkillBonusEditor(editor, this, bonusIndex)));
      editor.increaseHeight(19);
    }
  }

  private void addSkillBonuses(SkillTreeEditor editor, SkillBonus<?> skillBonus) {
    editor.getSelectedSkills().forEach(s -> s.getBonuses().add(skillBonus.copy()));
    editor.saveSelectedSkills();
    editor.selectMenu(editor.getSelectedMenu().previousMenu);
  }

  private boolean canEditBonuses(SkillTreeEditor editor) {
    PassiveSkill selectedSkill = editor.getFirstSelectedSkill();
    if (selectedSkill == null) return false;
    for (PassiveSkill otherSkill : editor.getSelectedSkills()) {
      if (otherSkill == selectedSkill) continue;
      List<SkillBonus<?>> bonuses = otherSkill.getBonuses();
      List<SkillBonus<?>> otherBonuses = selectedSkill.getBonuses();
      if (bonuses.size() != otherBonuses.size()) return false;
      for (int i = 0; i < bonuses.size(); i++) {
        if (!bonuses.get(i).sameBonus(otherBonuses.get(i))) return false;
      }
    }
    return true;
  }
}
