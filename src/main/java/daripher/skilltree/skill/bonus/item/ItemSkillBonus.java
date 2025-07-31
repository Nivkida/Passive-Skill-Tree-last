package daripher.skilltree.skill.bonus.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTItemBonuses;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.player.DamageBonus;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class ItemSkillBonus implements ItemBonus<ItemSkillBonus> {
  private SkillBonus<?> bonus;

  public ItemSkillBonus(SkillBonus<?> bonus) {
    this.bonus = bonus;
  }

  @Override
  public boolean canMerge(ItemBonus<?> other) {
    if (!(other instanceof ItemSkillBonus otherBonus)) return false;
    return otherBonus.bonus.canMerge(this.bonus);
  }

  @Override
  public ItemSkillBonus merge(ItemBonus<?> other) {
    if (!(other instanceof ItemSkillBonus otherBonus)) {
      throw new IllegalArgumentException();
    }
    return new ItemSkillBonus(otherBonus.bonus.merge(this.bonus));
  }

  @Override
  public ItemSkillBonus copy() {
    return new ItemSkillBonus(bonus.copy());
  }

  @Override
  public ItemSkillBonus multiply(double multiplier) {
    bonus.multiply(multiplier);
    return this;
  }

  @Override
  public ItemBonus.Serializer getSerializer() {
    return PSTItemBonuses.SKILL_BONUS.get();
  }

  @Override
  public MutableComponent getTooltip() {
    return bonus.getTooltip();
  }

  @Override
  public boolean isPositive() {
    return bonus.isPositive();
  }

  @Override
  public void addEditorWidgets(SkillTreeEditor editor, int index, Consumer<ItemBonus<?>> consumer) {
    editor.addLabel(0, 0, "Bonus Type", ChatFormatting.GREEN);
    editor.increaseHeight(19);
    editor
        .addSelectionMenu(0, 0, 200, bonus)
        .setResponder(skillBonus -> selectSkillBonus(editor, consumer, skillBonus))
        .setMenuInitFunc(() -> addSkillBonusWidgets(editor, index, consumer));
    editor.increaseHeight(19);
  }

  private void addSkillBonusWidgets(
      SkillTreeEditor editor, int index, Consumer<ItemBonus<?>> consumer) {
    bonus.addEditorWidgets(
        editor,
        index,
        skillBonus -> {
          setBonus(skillBonus);
          consumer.accept(this);
        });
  }

  private void selectSkillBonus(
      SkillTreeEditor editor, Consumer<ItemBonus<?>> consumer, SkillBonus<?> skillBonus) {
    setBonus(skillBonus);
    consumer.accept(this);
    editor.rebuildWidgets();
  }

  public void setBonus(SkillBonus<?> bonus) {
    this.bonus = bonus;
  }

  public SkillBonus<?> getBonus() {
    return bonus;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    ItemSkillBonus that = (ItemSkillBonus) obj;
    return Objects.equals(this.bonus, that.bonus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bonus);
  }

  public static class Serializer implements ItemBonus.Serializer {
    @Override
    public ItemBonus<?> deserialize(JsonObject json) throws JsonParseException {
      return new ItemSkillBonus(SerializationHelper.deserializeSkillBonus(json));
    }

    @Override
    public void serialize(JsonObject json, ItemBonus<?> bonus) {
      if (!(bonus instanceof ItemSkillBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      SerializationHelper.serializeSkillBonus(json, aBonus.bonus);
    }

    @Override
    public ItemBonus<?> deserialize(CompoundTag tag) {
      return new ItemSkillBonus(SerializationHelper.deserializeSkillBonus(tag));
    }

    @Override
    public CompoundTag serialize(ItemBonus<?> bonus) {
      if (!(bonus instanceof ItemSkillBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      CompoundTag tag = new CompoundTag();
      SerializationHelper.serializeSkillBonus(tag, aBonus.bonus);
      return tag;
    }

    @Override
    public ItemBonus<?> deserialize(FriendlyByteBuf buf) {
      return new ItemSkillBonus(NetworkHelper.readSkillBonus(buf));
    }

    @Override
    public void serialize(FriendlyByteBuf buf, ItemBonus<?> bonus) {
      if (!(bonus instanceof ItemSkillBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      NetworkHelper.writeSkillBonus(buf, aBonus.bonus);
    }

    @Override
    public ItemBonus<?> createDefaultInstance() {
      return new ItemSkillBonus(new DamageBonus(0.1f, AttributeModifier.Operation.MULTIPLY_BASE));
    }
  }
}
