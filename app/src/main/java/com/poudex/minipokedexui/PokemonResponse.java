package com.poudex.minipokedexui;

import com.google.gson.annotations.SerializedName;

public class PokemonResponse {
    public int id;
    public String name;
    public int weight;
    public Sprites sprites;
    public Type[] types;
    public Ability[] abilities;

    public static class Sprites {
        @SerializedName("front_default") public String front_default;
        @SerializedName("back_default")  public String back_default;
        @SerializedName("front_shiny")   public String front_shiny;
        @SerializedName("back_shiny")    public String back_shiny;
        public Other other;

        public static class Other {
            @SerializedName("official-artwork")
            public OfficialArtwork official_artwork;

            public static class OfficialArtwork {
                @SerializedName("front_default") public String front_default;
                @SerializedName("front_shiny")   public String front_shiny;
            }
        }
    }

    public static class Type {
        public TypeInfo type;
        public static class TypeInfo { public String name; }
    }

    public static class Ability {
        public AbilityInfo ability;
        public static class AbilityInfo { public String name; }
    }
}
