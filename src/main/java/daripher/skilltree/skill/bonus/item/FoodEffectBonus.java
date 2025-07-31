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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.*;

public final class FoodEffectBonus implements ItemBonus<FoodEffectBonus> {
  private MobEffectInstance effect;

  public FoodEffectBonus(MobEffectInstance effect) {
    this.effect = effect;
  }

  @Override
  public boolean canMerge(ItemBonus<?> other) {
    if (!(other instanceof FoodEffectBonus otherBonus)) return false;
    return otherBonus.effect.getEffect() == effect.getEffect();
  }

  @Override
  public FoodEffectBonus merge(ItemBonus<?> other) {
    if (!(other instanceof FoodEffectBonus otherBonus)) {
      throw new IllegalArgumentException();
    }
    int amplifier = effect.getAmplifier() + otherBonus.effect.getAmplifier() + 1;
    return new FoodEffectBonus(
        new MobEffectInstance(effect.getEffect(), effect.getDuration(), amplifier));
  }

  @Override
  public FoodEffectBonus copy() {
    return new FoodEffectBonus(new MobEffectInstance(effect));
  }

  @Override
  public FoodEffectBonus multiply(double multiplier) {
    int amplifier = (int) (effect.getAmplifier() * multiplier);
    return new FoodEffectBonus(
        new MobEffectInstance(effect.getEffect(), effect.getDuration(), amplifier));
  }

  @Override
  public ItemBonus.Serializer getSerializer() {
    return PSTItemBonuses.FOOD_EFFECT.get();
  }

  @Override
  public MutableComponent getTooltip() {
    Component effectDescription = TooltipHelper.getEffectInstanceTooltip(effect);
    Component durationDescription = MobEffectUtil.formatDuration(effect, 1f);
    return Component.translatable(getDescriptionId(), effectDescription, durationDescription);
  }

  @Override
  public boolean isPositive() {
    return effect.getEffect().getCategory() != MobEffectCategory.HARMFUL;
  }

  @Override
  public void addEditorWidgets(SkillTreeEditor editor, int index, Consumer<ItemBonus<?>> consumer) {
    editor.addLabel(0, 0, "Effect", ChatFormatting.GREEN);
    editor.increaseHeight(19);
    editor
        .addSelectionMenu(0, 0, 200, effect.getEffect())
        .setResponder(effect -> selectEffect(consumer, effect));
    editor.increaseHeight(19);
    editor.addLabel(0, 0, "Duration", ChatFormatting.GREEN);
    editor.addLabel(110, 0, "Amplifier", ChatFormatting.GREEN);
    editor.increaseHeight(19);
    editor
        .addNumericTextField(0, 0, 90, 14, getEffectInstance().getDuration())
        .setNumericResponder(value -> selectDuration(consumer, value));
    editor
        .addNumericTextField(110, 0, 90, 14, getEffectInstance().getAmplifier())
        .setNumericResponder(value -> selectAmplifier(consumer, value));
    editor.increaseHeight(19);
  }

  private void selectAmplifier(Consumer<ItemBonus<?>> consumer, Double value) {
    setAmplifier(value.intValue());
    consumer.accept(this);
  }

  private void selectDuration(Consumer<ItemBonus<?>> consumer, Double value) {
    setDuration(value.intValue());
    consumer.accept(this);
  }

  private void selectEffect(Consumer<ItemBonus<?>> consumer, MobEffect effect) {
    setEffect(effect);
    consumer.accept(this);
  }

  public void setDuration(int duration) {
    this.effect =
        new MobEffectInstance(
            getEffectInstance().getEffect(), duration, getEffectInstance().getAmplifier());
  }

  public void setAmplifier(int amplifier) {
    this.effect =
        new MobEffectInstance(
            getEffectInstance().getEffect(), getEffectInstance().getDuration(), amplifier);
  }

  public void setEffect(MobEffect effect) {
    this.effect =
        new MobEffectInstance(
            effect, getEffectInstance().getDuration(), getEffectInstance().getAmplifier());
  }

  public MobEffectInstance getEffectInstance() {
    return effect;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    FoodEffectBonus that = (FoodEffectBonus) obj;
    return Objects.equals(this.effect, that.effect);
  }

  @Override
  public int hashCode() {
    return Objects.hash(effect);
  }

  public static class Serializer implements ItemBonus.Serializer {
    @Override
    public ItemBonus<?> deserialize(JsonObject json) throws JsonParseException {
      return new FoodEffectBonus(SerializationHelper.deserializeEffectInstance(json));
    }

    @Override
    public void serialize(JsonObject json, ItemBonus<?> bonus) {
      if (!(bonus instanceof FoodEffectBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      SerializationHelper.serializeEffectInstance(json, aBonus.effect);
    }

    @Override
    public ItemBonus<?> deserialize(CompoundTag tag) {
      return new FoodEffectBonus(SerializationHelper.deserializeEffectInstance(tag));
    }

    @Override
    public CompoundTag serialize(ItemBonus<?> bonus) {
      if (!(bonus instanceof FoodEffectBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      CompoundTag tag = new CompoundTag();
      SerializationHelper.serializeEffectInstance(tag, aBonus.effect);
      return tag;
    }

    @Override
    public ItemBonus<?> deserialize(FriendlyByteBuf buf) {
      return new FoodEffectBonus(NetworkHelper.readEffectInstance(buf));
    }

    @Override
    public void serialize(FriendlyByteBuf buf, ItemBonus<?> bonus) {
      if (!(bonus instanceof FoodEffectBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      NetworkHelper.writeEffectInstance(buf, aBonus.effect);
    }

    @Override
    public ItemBonus<?> createDefaultInstance() {
      return new FoodEffectBonus(new MobEffectInstance(MobEffects.REGENERATION, 600));
    }
  }
}
