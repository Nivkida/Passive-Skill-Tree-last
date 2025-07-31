package daripher.skilltree.skill.bonus.multiplier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.entity.player.PlayerHelper;
import daripher.skilltree.init.PSTLivingMultipliers;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.condition.item.EquipmentCondition;
import daripher.skilltree.skill.bonus.condition.item.ItemCondition;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class EnchantsAmountMultiplier implements LivingMultiplier {
  private @Nonnull ItemCondition itemCondition;

  public EnchantsAmountMultiplier(@Nonnull ItemCondition itemCondition) {
    this.itemCondition = itemCondition;
  }

  @Override
  public float getValue(LivingEntity entity) {
    return getEnchants(PlayerHelper.getAllEquipment(entity).filter(itemCondition::met));
  }

  private int getEnchants(Stream<ItemStack> items) {
    return items
        .map(EnchantmentHelper::getEnchantments)
        .map(Map::size)
        .reduce(Integer::sum)
        .orElse(0);
  }

  @Override
  public MutableComponent getTooltip(MutableComponent bonusTooltip, SkillBonus.Target target) {
    Component itemDescription = itemCondition.getTooltip("where");
    return Component.translatable(getDescriptionId(target), bonusTooltip, itemDescription);
  }

  @Override
  public void addEditorWidgets(SkillTreeEditor editor, Consumer<LivingMultiplier> consumer) {
    editor.addLabel(0, 0, "Item Condition", ChatFormatting.GREEN);
    editor.increaseHeight(19);
    editor
        .addSelectionMenu(0, 0, 200, itemCondition)
        .setResponder(condition -> selectItemCondition(editor, consumer, condition))
        .setMenuInitFunc(() -> addItemConditionWidgets(editor, consumer));
    editor.increaseHeight(19);
  }

  private void addItemConditionWidgets(
      SkillTreeEditor editor, Consumer<LivingMultiplier> consumer) {
    itemCondition.addEditorWidgets(
        editor,
        condition -> {
          setItemCondition(condition);
          consumer.accept(this);
        });
  }

  private void selectItemCondition(
      SkillTreeEditor editor, Consumer<LivingMultiplier> consumer, ItemCondition condition) {
    setItemCondition(condition);
    consumer.accept(this);
    editor.rebuildWidgets();
  }

  @Override
  public LivingMultiplier.Serializer getSerializer() {
    return PSTLivingMultipliers.ENCHANTS_AMOUNT.get();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EnchantsAmountMultiplier that = (EnchantsAmountMultiplier) o;
    return Objects.equals(itemCondition, that.itemCondition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemCondition);
  }

  public void setItemCondition(@Nonnull ItemCondition itemCondition) {
    this.itemCondition = itemCondition;
  }

  public static class Serializer implements LivingMultiplier.Serializer {
    @Override
    public LivingMultiplier deserialize(JsonObject json) throws JsonParseException {
      ItemCondition itemCondition = SerializationHelper.deserializeItemCondition(json);
      return new EnchantsAmountMultiplier(itemCondition);
    }

    @Override
    public void serialize(JsonObject json, LivingMultiplier multiplier) {
      if (!(multiplier instanceof EnchantsAmountMultiplier aMultiplier)) {
        throw new IllegalArgumentException();
      }
      SerializationHelper.serializeItemCondition(json, aMultiplier.itemCondition);
    }

    @Override
    public LivingMultiplier deserialize(CompoundTag tag) {
      ItemCondition itemCondition = SerializationHelper.deserializeItemCondition(tag);
      return new EnchantsAmountMultiplier(itemCondition);
    }

    @Override
    public CompoundTag serialize(LivingMultiplier multiplier) {
      if (!(multiplier instanceof EnchantsAmountMultiplier aMultiplier)) {
        throw new IllegalArgumentException();
      }
      CompoundTag tag = new CompoundTag();
      SerializationHelper.serializeItemCondition(tag, aMultiplier.itemCondition);
      return tag;
    }

    @Override
    public LivingMultiplier deserialize(FriendlyByteBuf buf) {
      ItemCondition itemCondition = NetworkHelper.readItemCondition(buf);
      return new EnchantsAmountMultiplier(itemCondition);
    }

    @Override
    public void serialize(FriendlyByteBuf buf, LivingMultiplier multiplier) {
      if (!(multiplier instanceof EnchantsAmountMultiplier aMultiplier)) {
        throw new IllegalArgumentException();
      }
      NetworkHelper.writeItemCondition(buf, aMultiplier.itemCondition);
    }

    @Override
    public LivingMultiplier createDefaultInstance() {
      return new EnchantsAmountMultiplier(new EquipmentCondition(EquipmentCondition.Type.ANY));
    }
  }
}
