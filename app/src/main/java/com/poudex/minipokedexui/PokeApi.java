package com.poudex.minipokedexui;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PokeApi {
    @GET("pokemon/{nameOrId}")
    Call<PokemonResponse> getPokemon(@Path("nameOrId") String nameOrId);
}
