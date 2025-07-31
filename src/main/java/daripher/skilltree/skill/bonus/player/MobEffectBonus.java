package daripher.skilltree.skill.bonus.player;

import com.google.gson.*;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.client.widget.editor.SkillTreeEditor;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.init.PSTSkillBonuses;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.EventListenerBonus;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.event.AttackEventListener;
import daripher.skilltree.skill.bonus.event.SkillEventListener;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class MobEffectBonus implements EventListenerBonus<MobEffectBonus> {
  private MobEffectInstance effect;
  private SkillEventListener eventListener;
  private float chance;

  public MobEffectBonus(float chance, MobEffectInstance effect, SkillEventListener eventListener) {
    this.chance = chance;
    this.effect = effect;
    this.eventListener = eventListener;
  }

  public MobEffectBonus(float chance, MobEffectInstance effect) {
    this(chance, effect, new AttackEventListener());
  }

  @Override
  public void applyEffect(LivingEntity target) {
    if (target.getRandom().nextFloat() < chance) {
      target.addEffect(new MobEffectInstance(effect));
    }
  }

  @Override
  public SkillBonus.Serializer getSerializer() {
    return PSTSkillBonuses.MOB_EFFECT.get();
  }

  @Override
  public MobEffectBonus copy() {
    return new MobEffectBonus(chance, effect, eventListener);
  }

  @Override
  public MobEffectBonus multiply(double multiplier) {
    chance *= (float) multiplier;
    return this;
  }

  @Override
  public boolean canMerge(SkillBonus<?> other) {
    if (!(other instanceof MobEffectBonus otherBonus)) return false;
    if (!Objects.equals(otherBonus.effect, this.effect)) return false;
    return Objects.equals(otherBonus.eventListener, this.eventListener);
  }

  @Override
  public SkillBonus<EventListenerBonus<MobEffectBonus>> merge(SkillBonus<?> other) {
    if (!(other instanceof MobEffectBonus otherBonus)) {
      throw new IllegalArgumentException();
    }
    return new MobEffectBonus(otherBonus.chance + this.chance, effect, eventListener);
  }

  @Override
  public MutableComponent getTooltip() {
    Component effectDescription = TooltipHelper.getEffectInstanceTooltip(effect);
    int duration = effect.getDuration();
    String targetDescription = eventListener.getTarget().name().toLowerCase();
    String bonusDescription = getDescriptionId() + "." + targetDescription;
    if (chance < 1) {
      bonusDescription += ".chance";
    }
    MutableComponent tooltip;
    if (duration > 0) {
      Component durationDescription =
          Component.translatable(
              getDescriptionId() + ".duration", StringUtil.formatTickDuration(duration));
      tooltip = Component.translatable(bonusDescription, effectDescription, durationDescription);
    } else {
      tooltip = Component.translatable(bonusDescription, effectDescription, "");
    }
    if (chance < 1) {
      tooltip =
          TooltipHelper.getSkillBonusTooltip(
              tooltip, chance, AttributeModifier.Operation.MULTIPLY_BASE);
    }
    tooltip = eventListener.getTooltip(tooltip);
    return tooltip.withStyle(TooltipHelper.getSkillBonusStyle(isPositive()));
  }

  @Override
  public void gatherInfo(Consumer<MutableComponent> consumer) {
    TooltipHelper.consumeTranslated(effect.getDescriptionId() + ".info", consumer);
  }

  @Override
  public boolean isPositive() {
    return chance > 0
        ^ eventListener.getTarget() == Target.PLAYER
        ^ effect.getEffect().getCategory() != MobEffectCategory.HARMFUL;
  }

  @Override
  public SkillEventListener getEventListener() {
    return eventListener;
  }

  @Override
  public void addEditorWidgets(
      SkillTreeEditor editor, int row, Consumer<EventListenerBonus<MobEffectBonus>> consumer) {
    editor.addLabel(0, 0, "Effect", ChatFormatting.GOLD);
    editor.addLabel(150, 0, "Chance", ChatFormatting.GOLD);
    editor.increaseHeight(19);
    editor
        .addSelectionMenu(0, 0, 145, effect.getEffect())
        .setResponder(effect -> selectEffect(consumer, effect));
    editor
        .addNumericTextField(150, 0, 50, 14, chance)
        .setNumericResponder(value -> selectChance(consumer, value));
    editor.increaseHeight(19);
    editor.addLabel(0, 0, "Duration", ChatFormatting.GOLD);
    editor.addLabel(55, 0, "Amplifier", ChatFormatting.GOLD);
    editor.increaseHeight(19);
    editor
        .addNumericTextField(0, 0, 50, 14, effect.getDuration())
        .setNumericFilter(value -> value >= -1)
        .setNumericResponder(value -> selectDuration(consumer, value));
    editor
        .addNumericTextField(55, 0, 50, 14, effect.getAmplifier())
        .setNumericFilter(value -> value >= 0)
        .setNumericResponder(value -> selectAmplifier(consumer, value));
    editor.increaseHeight(19);
    editor.addLabel(0, 0, "Event", ChatFormatting.GOLD);
    editor.increaseHeight(19);
    editor
        .addSelectionMenu(0, 0, 200, eventListener)
        .setResponder(eventListener -> selectEventListener(editor, consumer, eventListener))
        .setMenuInitFunc(() -> addEventListenerWidgets(editor, consumer));
    editor.increaseHeight(19);
  }

  private void addEventListenerWidgets(
      SkillTreeEditor editor, Consumer<EventListenerBonus<MobEffectBonus>> consumer) {
    eventListener.addEditorWidgets(
        editor,
        eventListener -> {
          setEventListener(eventListener);
          consumer.accept(this.copy());
        });
  }

  private void selectEventListener(
      SkillTreeEditor editor,
      Consumer<EventListenerBonus<MobEffectBonus>> consumer,
      SkillEventListener eventListener) {
    setEventListener(eventListener);
    consumer.accept(this.copy());
    editor.rebuildWidgets();
  }

  private void selectAmplifier(
      Consumer<EventListenerBonus<MobEffectBonus>> consumer, Double value) {
    setAmplifier(value.intValue());
    consumer.accept(this.copy());
  }

  private void selectDuration(Consumer<EventListenerBonus<MobEffectBonus>> consumer, Double value) {
    setDuration(value.intValue());
    consumer.accept(this.copy());
  }

  private void selectChance(Consumer<EventListenerBonus<MobEffectBonus>> consumer, Double value) {
    setChance(value.floatValue());
    consumer.accept(this.copy());
  }

  private void selectEffect(
      Consumer<EventListenerBonus<MobEffectBonus>> consumer, MobEffect effect) {
    setEffect(effect);
    consumer.accept(this);
  }

  public void setChance(float chance) {
    this.chance = chance;
  }

  public void setEffect(MobEffect effect) {
    this.effect =
        new MobEffectInstance(effect, this.effect.getDuration(), this.effect.getAmplifier());
  }

  public void setDuration(int duration) {
    this.effect =
        new MobEffectInstance(this.effect.getEffect(), duration, this.effect.getAmplifier());
  }

  public void setAmplifier(int amplifier) {
    this.effect =
        new MobEffectInstance(this.effect.getEffect(), this.effect.getDuration(), amplifier);
  }

  public void setEventListener(SkillEventListener eventListener) {
    this.eventListener = eventListener;
  }

  public static class Serializer implements SkillBonus.Serializer {
    @Override
    public MobEffectBonus deserialize(JsonObject json) throws JsonParseException {
      float chance = SerializationHelper.getElement(json, "chance").getAsFloat();
      MobEffectInstance effect = SerializationHelper.deserializeEffectInstance(json);
      MobEffectBonus bonus = new MobEffectBonus(chance, effect);
      bonus.eventListener = SerializationHelper.deserializeEventListener(json);
      return bonus;
    }

    @Override
    public void serialize(JsonObject json, SkillBonus<?> bonus) {
      if (!(bonus instanceof MobEffectBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      json.addProperty("chance", aBonus.chance);
      SerializationHelper.serializeEffectInstance(json, aBonus.effect);
      SerializationHelper.serializeEventListener(json, aBonus.eventListener);
    }

    @Override
    public MobEffectBonus deserialize(CompoundTag tag) {
      float chance = tag.getFloat("chance");
      MobEffectInstance effect = SerializationHelper.deserializeEffectInstance(tag);
      MobEffectBonus bonus = new MobEffectBonus(chance, effect);
      bonus.eventListener = SerializationHelper.deserializeEventListener(tag);
      return bonus;
    }

    @Override
    public CompoundTag serialize(SkillBonus<?> bonus) {
      if (!(bonus instanceof MobEffectBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      CompoundTag tag = new CompoundTag();
      tag.putFloat("chance", aBonus.chance);
      SerializationHelper.serializeEffectInstance(tag, aBonus.effect);
      SerializationHelper.serializeEventListener(tag, aBonus.eventListener);
      return tag;
    }

    @Override
    public MobEffectBonus deserialize(FriendlyByteBuf buf) {
      float amount = buf.readFloat();
      MobEffectInstance effect = NetworkHelper.readEffectInstance(buf);
      MobEffectBonus bonus = new MobEffectBonus(amount, effect);
      bonus.eventListener = NetworkHelper.readEventListener(buf);
      return bonus;
    }

    @Override
    public void serialize(FriendlyByteBuf buf, SkillBonus<?> bonus) {
      if (!(bonus instanceof MobEffectBonus aBonus)) {
        throw new IllegalArgumentException();
      }
      buf.writeFloat(aBonus.chance);
      NetworkHelper.writeEffectInstance(buf, aBonus.effect);
      NetworkHelper.writeEventListener(buf, aBonus.eventListener);
    }

    @Override
    public SkillBonus<?> createDefaultInstance() {
      return new MobEffectBonus(0.05f, new MobEffectInstance(MobEffects.POISON, 100));
    }
  }
}
