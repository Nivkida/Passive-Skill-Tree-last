package daripher.skilltree.skill.bonus.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTItemBonuses;
import daripher.skilltree.network.NetworkHelper;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class ItemDurabilityBonus implements ItemBonus<ItemDurabilityBonus> {
  private float amount;
  private AttributeModifier.Operation operation;

  public ItemDurabilityBonus(float amount, AttributeModifier.Operation operation) {
    this.amount = amount;
    this.operation = operation;
  }

  @Override
  public boolean canMerge(ItemBonus<?> other) {
    if (!(other instanceof ItemDurabilityBonus otherBonus)) return false;
    return operation == otherBonus.operation;
  }

  @Override
  public ItemDurabilityBonus merge(ItemBonus<?> other) {
    if (!(other instanceof ItemDurabilityBonus otherBonus)) {
      throw new IllegalArgumentException();
    }
    return new ItemDurabilityBonus(this.amount + otherBonus.amount, operation);
  }

  @Override
  public ItemDurabilityBonus copy() {
    return new ItemDurabilityBonus(amount, operation);
  }

  @Override
  public ItemDurabilityBonus multiply(double multiplier) {
    return new ItemDurabilityBonus((float) (amount * multiplier), operation);
  }

  @Override
  public ItemBonus.Serializer getSerializer() {
    return PSTItemBonuses.DURABILITY.get();
  }

  @Override
  public MutableComponent getTooltip() {
    return TooltipHelper.getSkillBonusTooltip(getDescriptionId(), amount, operation);
  }

  @Override
  public boolean isPositive() {
    return amount > 0;
  }

  @Override
  public void addEditorWidgets(SkillTreeEditor editor, int index, Consumer<ItemBonus<?>> consumer) {
    editor.addLabel(0, 0, "Amount", ChatFormatting.GREEN);
    editor.addLabel(55, 0, "Operation", ChatFormatting.GREEN);
    editor.increaseHeight(19);
    editor
        .addNumericTextField(0, 0, 50, 14, amount)
        .setNumericResponder(value -> selectAmount(consumer, value));
    editor
        .addOperationSelection(55, 0, 145, operation)
        .setResponder(operation -> selectOperation(consumer, operation));
    editor.increaseHeight(19);
  }

  private void selectOperation(
      Consumer<ItemBonus<?>> consumer, AttributeModifier.Operation operation) {
    setOperation(operation);
    consumer.accept(this);
  }

  private void selectAmount(Consumer<ItemBonus<?>> consumer, Double value) {
    setAmount(value.floatValue());
    consumer.accept(this);
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public void setOperation(AttributeModifier.Operation operation) {
    this.operation = operation;
  }

  public float getAmount() {
    return amount;
  }

  public AttributeModifier.Operation getOperation() {
    return operation;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    ItemDurabilityBonus that = (ItemDurabilityBonus) obj;
    return Float.floatToIntBits(this.amount) == Float.floatToIntBits(that.amount)
        && Objects.equals(this.operation, that.operation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, operation);
  }

  public static class Serializer implements ItemBonus.Serializer {
    @Override
    public ItemBonus<?> deserialize(JsonObject json) throws JsonParseException {
      float amount = SerializationHelper.getElement(json, "amount").getAsFloat();
      AttributeModifier.Operation operation = SerializationHelper.deserializeOperation(json);
      return new ItemDurabilityBonus(amount, operation);
    }

    @Override
    public void serialize(JsonObject json, ItemBonus<?> bonus) {
      if (!(bonus instanceof ItemDurabilityBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      json.addProperty("amount", aBonus.amount);
      SerializationHelper.serializeOperation(json, aBonus.operation);
    }

    @Override
    public ItemBonus<?> deserialize(CompoundTag tag) {
      float amount = tag.getFloat("amount");
      AttributeModifier.Operation operation = SerializationHelper.deserializeOperation(tag);
      return new ItemDurabilityBonus(amount, operation);
    }

    @Override
    public CompoundTag serialize(ItemBonus<?> bonus) {
      if (!(bonus instanceof ItemDurabilityBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      CompoundTag tag = new CompoundTag();
      tag.putFloat("amount", aBonus.amount);
      SerializationHelper.serializeOperation(tag, aBonus.operation);
      return tag;
    }

    @Override
    public ItemBonus<?> deserialize(FriendlyByteBuf buf) {
      return new ItemDurabilityBonus(buf.readFloat(), NetworkHelper.readOperation(buf));
    }

    @Override
    public void serialize(FriendlyByteBuf buf, ItemBonus<?> bonus) {
      if (!(bonus instanceof ItemDurabilityBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      buf.writeFloat(aBonus.amount);
      NetworkHelper.writeOperation(buf, aBonus.operation);
    }

    @Override
    public ItemBonus<?> createDefaultInstance() {
      return new ItemDurabilityBonus(100, AttributeModifier.Operation.ADDITION);
    }
  }
}
