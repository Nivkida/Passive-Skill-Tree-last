package daripher.skilltree.skill;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public class PassiveSkillTree {
  private final List<ResourceLocation> skillIds = new ArrayList<>();
  private final ResourceLocation id;
  private @Nullable Map<String, Integer> skillLimitations;

  public PassiveSkillTree(ResourceLocation id) {
    this.id = id;
  }

  public ResourceLocation getId() {
    return id;
  }

  public List<ResourceLocation> getSkillIds() {
    return skillIds;
  }

  public Map<String, Integer> getSkillLimitations() {
    if (skillLimitations == null) return skillLimitations = new LinkedHashMap<>();
    return skillLimitations;
  }
}
