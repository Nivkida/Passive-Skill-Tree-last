package daripher.skilltree.skill.bonus.player;

import com.google.gson.*;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.item.ItemHelper;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.condition.item.EquipmentCondition;
import daripher.skilltree.skill.bonus.condition.item.ItemCondition;
import daripher.skilltree.skill.bonus.item.ItemBonus;
import daripher.skilltree.skill.bonus.item.ItemDurabilityBonus;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class CraftedItemBonus implements SkillBonus<CraftedItemBonus> {
  private @Nonnull ItemBonus<?> bonus;
  private @Nonnull ItemCondition itemCondition;

  public CraftedItemBonus(@NotNull ItemCondition itemCondition, @NotNull ItemBonus<?> bonus) {
    this.itemCondition = itemCondition;
    this.bonus = bonus;
  }

  public void itemCrafted(ItemStack stack) {
    if (!itemCondition.met(stack)) return;
    ItemHelper.addItemBonus(stack, bonus.copy());
  }

  @Override
  public SkillBonus.Serializer getSerializer() {
    return PSTSkillBonuses.CRAFTED_ITEM_BONUS.get();
  }

  @Override
  public CraftedItemBonus copy() {
    return new CraftedItemBonus(itemCondition, this.bonus.copy());
  }

  @Override
  public CraftedItemBonus multiply(double multiplier) {
    bonus = bonus.copy().multiply(multiplier);
    return this;
  }

  @Override
  public boolean canMerge(SkillBonus<?> other) {
    if (!(other instanceof CraftedItemBonus otherBonus)) return false;
    if (!Objects.equals(otherBonus.itemCondition, this.itemCondition)) return false;
    return otherBonus.bonus.canMerge(this.bonus);
  }

  @Override
  public SkillBonus<CraftedItemBonus> merge(SkillBonus<?> other) {
    if (!(other instanceof CraftedItemBonus otherBonus)) {
      throw new IllegalArgumentException();
    }
    return new CraftedItemBonus(itemCondition, otherBonus.bonus.merge(this.bonus));
  }

  @Override
  public MutableComponent getTooltip() {
    Component itemDescription = itemCondition.getTooltip("plural.type");
    Component bonusDescription =
        bonus.getTooltip().withStyle(TooltipHelper.getItemBonusStyle(isPositive()));
    return Component.translatable(getDescriptionId(), itemDescription, bonusDescription)
        .withStyle(TooltipHelper.getSkillBonusStyle(isPositive()));
  }

  @Override
  public boolean isPositive() {
    return bonus.isPositive();
  }

  @Override
  public void addEditorWidgets(
      SkillTreeEditor editor, int index, Consumer<CraftedItemBonus> consumer) {
    editor.addLabel(0, 0, "Item Condition", ChatFormatting.GOLD);
    editor.increaseHeight(19);
    editor
        .addSelectionMenu(0, 0, 200, itemCondition)
        .setResponder(condition -> selectItemCondition(editor, consumer, condition))
        .setMenuInitFunc(() -> addItemConditionWidgets(editor, consumer));
    editor.increaseHeight(19);
    editor.addLabel(0, 0, "Item Bonus", ChatFormatting.GOLD);
    editor.increaseHeight(19);
    editor
        .addSelectionMenu(0, 0, 200, bonus)
        .setResponder(bonus -> selectItemBonus(editor, consumer, bonus))
        .setMenuInitFunc(() -> addItemBonusWidgets(editor, index, consumer));
    editor.increaseHeight(19);
  }

  private void selectItemBonus(
      SkillTreeEditor editor,
      Consumer<CraftedItemBonus> consumer,
      @SuppressWarnings("rawtypes") ItemBonus bonus) {
    setBonus(bonus);
    consumer.accept(this.copy());
    editor.rebuildWidgets();
  }

  private void selectItemCondition(
      SkillTreeEditor editor, Consumer<CraftedItemBonus> consumer, ItemCondition condition) {
    setItemCondition(condition);
    consumer.accept(this.copy());
    editor.rebuildWidgets();
  }

  private void addItemConditionWidgets(
      SkillTreeEditor editor, Consumer<CraftedItemBonus> consumer) {
    itemCondition.addEditorWidgets(
        editor,
        c -> {
          setItemCondition(c);
          consumer.accept(this.copy());
        });
  }

  private void addItemBonusWidgets(
      SkillTreeEditor editor, int index, Consumer<CraftedItemBonus> consumer) {
    bonus.addEditorWidgets(
        editor,
        index,
        b -> {
          setBonus(b);
          consumer.accept(this.copy());
        });
  }

  public void setItemCondition(@Nonnull ItemCondition itemCondition) {
    this.itemCondition = itemCondition;
  }

  public void setBonus(@Nonnull ItemBonus<?> bonus) {
    this.bonus = bonus;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    CraftedItemBonus that = (CraftedItemBonus) obj;
    if (!Objects.equals(this.itemCondition, that.itemCondition)) return false;
    return Objects.equals(this.bonus, that.bonus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemCondition, bonus);
  }

  public static class Serializer implements SkillBonus.Serializer {
    @Override
    public CraftedItemBonus deserialize(JsonObject json) throws JsonParseException {
      ItemCondition condition = SerializationHelper.deserializeItemCondition(json);
      ItemBonus<?> bonus = SerializationHelper.deserializeItemBonus(json);
      return new CraftedItemBonus(condition, bonus);
    }

    @Override
    public void serialize(JsonObject json, SkillBonus<?> bonus) {
      if (!(bonus instanceof CraftedItemBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      SerializationHelper.serializeItemCondition(json, aBonus.itemCondition);
      SerializationHelper.serializeItemBonus(json, aBonus.bonus);
    }

    @Override
    public CraftedItemBonus deserialize(CompoundTag tag) {
      ItemCondition condition = SerializationHelper.deserializeItemCondition(tag);
      ItemBonus<?> bonus = SerializationHelper.deserializeItemBonus(tag);
      return new CraftedItemBonus(condition, bonus);
    }

    @Override
    public CompoundTag serialize(SkillBonus<?> bonus) {
      if (!(bonus instanceof CraftedItemBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      CompoundTag tag = new CompoundTag();
      SerializationHelper.serializeItemCondition(tag, aBonus.itemCondition);
      SerializationHelper.serializeItemBonus(tag, aBonus.bonus);
      return tag;
    }

    @Override
    public CraftedItemBonus deserialize(FriendlyByteBuf buf) {
      ItemCondition condition = NetworkHelper.readItemCondition(buf);
      ItemBonus<?> bonus = NetworkHelper.readItemBonus(buf);
      return new CraftedItemBonus(condition, bonus);
    }

    @Override
    public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
      if (!(bonus instanceof CraftedItemBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      NetworkHelper.writeItemCondition(buf, aBonus.itemCondition);
      NetworkHelper.writeItemBonus(buf, aBonus.bonus);
    }

    @Override
    public SkillBonus<?> createDefaultInstance() {
      return new CraftedItemBonus(
          new EquipmentCondition(EquipmentCondition.Type.ANY),
          new ItemDurabilityBonus(100f, AttributeModifier.Operation.ADDITION));
    }
  }
}
