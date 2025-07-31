package daripher.skilltree.skill.bonus.multiplier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTLivingMultipliers;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.SkillBonus;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class AttributeValueMultiplier implements LivingMultiplier {
  private Attribute attribute;
  private float divisor;

  public AttributeValueMultiplier(Attribute attribute) {
    this(attribute, 1f);
  }

  public AttributeValueMultiplier(Attribute attribute, float divisor) {
    this.attribute = attribute;
    this.divisor = divisor;
  }

  @Override
  public float getValue(LivingEntity entity) {
    return (int) (entity.getAttributeValue(attribute) / divisor);
  }

  @Override
  public MutableComponent getTooltip(MutableComponent bonusTooltip, SkillBonus.Target target) {
    Component attributeDescription = Component.translatable(attribute.getDescriptionId());
    String multiplierDescription = getDescriptionId(target);
    String divisorDescription = TooltipHelper.formatNumber(divisor);
    return Component.translatable(
        multiplierDescription, bonusTooltip, divisorDescription, attributeDescription);
  }

  @Override
  public void addEditorWidgets(SkillTreeEditor editor, Consumer<LivingMultiplier> consumer) {
    editor.addLabel(0, 0, "Attribute", ChatFormatting.GREEN);
    editor.increaseHeight(19);
    editor
        .addSelectionMenu(0, 0, 200, attribute)
        .setResponder(attribute -> selectAttribute(consumer, attribute));
    editor.increaseHeight(19);
    editor.addLabel(0, 0, "Divisor", ChatFormatting.GREEN);
    editor.increaseHeight(19);
    editor
        .addNumericTextField(0, 0, 50, 14, divisor)
        .setNumericFilter(value -> value > 0)
        .setNumericResponder(value -> selectDivisor(consumer, value));
    editor.increaseHeight(19);
  }

  private void selectDivisor(Consumer<LivingMultiplier> consumer, Double value) {
    setDivisor(value.floatValue());
    consumer.accept(this);
  }

  private void selectAttribute(Consumer<LivingMultiplier> consumer, Attribute attribute) {
    setAttribute(attribute);
    consumer.accept(this);
  }

  @Override
  public LivingMultiplier.Serializer getSerializer() {
    return PSTLivingMultipliers.ATTRIBUTE_VALUE.get();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AttributeValueMultiplier that = (AttributeValueMultiplier) o;
    return Float.compare(divisor, that.divisor) == 0 && Objects.equals(attribute, that.attribute);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attribute, divisor);
  }

  public void setAttribute(Attribute attribute) {
    this.attribute = attribute;
  }

  public void setDivisor(float divisor) {
    this.divisor = divisor;
  }

  public static class Serializer implements LivingMultiplier.Serializer {
    @Override
    public LivingMultiplier deserialize(JsonObject json) throws JsonParseException {
      Attribute attribute = SerializationHelper.deserializeAttribute(json);
      float divisor = !json.has("divisor") ? 1f : json.get("divisor").getAsFloat();
      return new AttributeValueMultiplier(attribute, divisor);
    }

    @Override
    public void serialize(JsonObject json, LivingMultiplier multiplier) {
      if (!(multiplier instanceof AttributeValueMultiplier aMultiplier)) {
        throw new IllegalArgumentException();
      }
      SerializationHelper.serializeAttribute(json, aMultiplier.attribute);
      json.addProperty("divisor", aMultiplier.divisor);
    }

    @Override
    public LivingMultiplier deserialize(CompoundTag tag) {
      Attribute attribute = SerializationHelper.deserializeAttribute(tag);
      float divisor = !tag.contains("divisor") ? 1f : tag.getFloat("divisor");
      return new AttributeValueMultiplier(attribute, divisor);
    }

    @Override
    public CompoundTag serialize(LivingMultiplier multiplier) {
      if (!(multiplier instanceof AttributeValueMultiplier aMultiplier)) {
        throw new IllegalArgumentException();
      }
      CompoundTag tag = new CompoundTag();
      SerializationHelper.serializeAttribute(tag, aMultiplier.attribute);
      tag.putFloat("divisor", aMultiplier.divisor);
      return tag;
    }

    @Override
    public LivingMultiplier deserialize(FriendlyByteBuf buf) {
      Attribute attribute = NetworkHelper.readAttribute(buf);
      float divisor = buf.readFloat();
      return new AttributeValueMultiplier(attribute, divisor);
    }

    @Override
    public void serialize(FriendlyByteBuf buf, LivingMultiplier multiplier) {
      if (!(multiplier instanceof AttributeValueMultiplier aMultiplier)) {
        throw new IllegalArgumentException();
      }
      NetworkHelper.writeAttribute(buf, aMultiplier.attribute);
      buf.writeFloat(aMultiplier.divisor);
    }

    @Override
    public LivingMultiplier createDefaultInstance() {
      return new AttributeValueMultiplier(Attributes.ARMOR, 1f);
    }
  }
}
