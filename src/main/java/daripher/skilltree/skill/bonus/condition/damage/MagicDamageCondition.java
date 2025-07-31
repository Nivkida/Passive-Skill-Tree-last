package daripher.skilltree.skill.bonus.condition.damage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import daripher.skilltree.init.PSTDamageConditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

public record MagicDamageCondition() implements DamageCondition {
  @Override
  public boolean met(DamageSource source) {
    return source.is(DamageTypes.MAGIC);
  }

  @Override
  public DamageCondition.Serializer getSerializer() {
    return PSTDamageConditions.MAGIC.get();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    return o != null && getClass() == o.getClass();
  }

  @Override
  public int hashCode() {
    return getSerializer().hashCode();
  }

  public static class Serializer implements DamageCondition.Serializer {
    @Override
    public DamageCondition deserialize(JsonObject json) throws JsonParseException {
      return new MagicDamageCondition();
    }

    @Override
    public void serialize(JsonObject json, DamageCondition condition) {
      if (!(condition instanceof MagicDamageCondition)) {
        throw new IllegalArgumentException();
      }
    }

    @Override
    public DamageCondition deserialize(CompoundTag tag) {
      return new MagicDamageCondition();
    }

    @Override
    public CompoundTag serialize(DamageCondition condition) {
      if (!(condition instanceof MagicDamageCondition)) {
        throw new IllegalArgumentException();
      }
      return new CompoundTag();
    }

    @Override
    public DamageCondition deserialize(FriendlyByteBuf buf) {
      return new MagicDamageCondition();
    }

    @Override
    public void serialize(FriendlyByteBuf buf, DamageCondition condition) {
      if (!(condition instanceof MagicDamageCondition)) {
        throw new IllegalArgumentException();
      }
    }

    @Override
    public DamageCondition createDefaultInstance() {
      return new MagicDamageCondition();
    }
  }
}
