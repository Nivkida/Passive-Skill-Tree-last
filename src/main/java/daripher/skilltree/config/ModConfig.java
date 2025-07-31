package daripher.skilltree.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

public class ModConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Common {
        private static final Gson GSON = new Gson();
        private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

        // JSON-строка, которая парсится в Map<String, String>
        private final ForgeConfigSpec.ConfigValue<String> rawClassToTree;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Mapping from player class ID to skill tree ResourceLocation as JSON string")
                    .push("class-tree-mapping");

            rawClassToTree = builder
                    .comment("Example: {\"assassin\": \"skilltree:assassin_tree\", \"mage\": \"skilltree:mage_tree\"}")
                    .define("classToTree", "{\"assasin\": \"skilltree:assasin_tree\", \"wizard\": \"skilltree:wizard_tree\", \"tank\": \"skilltree:tank_tree\", \"samurai\": \"skilltree:samurai_tree\", \"knight\": \"skilltree:knight_tree\", \"berserk\": \"skilltree:berserk_tree\", \"archer\": \"skilltree:archer_tree\"}");

            builder.pop();
        }

        public Map<String, String> getClassToTree() {
            try {
                return GSON.fromJson(rawClassToTree.get(), MAP_TYPE);
            } catch (Exception e) {
                return Collections.emptyMap();
            }
        }
    }
}