package daripher.skilltree.skill.bonus.condition.damage;

import daripher.skilltree.init.PSTRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;

import java.util.Objects;

public interface DamageCondition {
  boolean met(DamageSource source);

  default String getDescriptionId() {
    ResourceLocation id = PSTRegistries.DAMAGE_CONDITIONS.get().getKey(getSerializer());
    Objects.requireNonNull(id);
    return "damage_condition.%s.%s".formatted(id.getNamespace(), id.getPath());
  }

  default MutableComponent getTooltip() {
    return Component.translatable(getDescriptionId());
  }

  default MutableComponent getTooltip(String type) {
    return Component.translatable(getDescriptionId() + "." + type);
  }

  Serializer getSerializer();

  interface Serializer extends daripher.skilltree.data.serializers.Serializer<DamageCondition> {
    DamageCondition createDefaultInstance();
  }
}
