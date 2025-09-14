package com.poudex.minipokedexui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int MAX_POKEMON_ID = 1025; // rango práctico actual

    private EditText etQuery;
    private Button btnSearch, btnClear;
    private ImageButton btnRandom;
    private CheckBox cbShiny;
    private ToggleButton tgSpriteArtwork;
    private RadioGroup rgSide;
    private RadioButton rbFront, rbBack;
    private Switch swDetails;
    private ImageView ivPokemon;
    private TextView tvBasic, tvDetails;

    private PokeApi pokeApi;
    private PokemonResponse currentPokemon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initApi();
        setupListeners();
    }

    private void initViews() {
        etQuery = findViewById(R.id.etQuery);
        btnSearch = findViewById(R.id.btnSearch);
        btnClear = findViewById(R.id.btnClear);
        btnRandom = findViewById(R.id.btnRandom);
        cbShiny = findViewById(R.id.cbShiny);
        tgSpriteArtwork = findViewById(R.id.tgSpriteArtwork);
        rgSide = findViewById(R.id.rgSide);
        rbFront = findViewById(R.id.rbFront);
        rbBack = findViewById(R.id.rbBack);
        swDetails = findViewById(R.id.swDetails);
        ivPokemon = findViewById(R.id.ivPokemon);
        tvBasic = findViewById(R.id.tvBasic);
        tvDetails = findViewById(R.id.tvDetails);
    }

    private void initApi() {
        pokeApi = ApiClient.getPokeApi();
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> {
            String query = etQuery.getText().toString().trim().toLowerCase();
            if (!query.isEmpty()) {
                searchPokemon(query);
            } else {
                Toast.makeText(this, "Ingresa un nombre o ID", Toast.LENGTH_SHORT).show();
            }
        });

        btnRandom.setOnClickListener(v -> {
            int randomId = new Random().nextInt(MAX_POKEMON_ID) + 1;
            searchPokemon(String.valueOf(randomId));
        });

        btnClear.setOnClickListener(v -> clearAll());

        // Actualizar imagen al cambiar opciones
        cbShiny.setOnCheckedChangeListener((buttonView, isChecked) -> updatePokemonImage());
        tgSpriteArtwork.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updatePokemonImage();
            // Artwork solo tiene frente → deshabilitamos radios
            rbFront.setEnabled(!isChecked);
            rbBack.setEnabled(!isChecked);
            if (isChecked) rbFront.setChecked(true);
        });
        rgSide.setOnCheckedChangeListener((group, checkedId) -> updatePokemonImage());

        swDetails.setOnCheckedChangeListener((buttonView, isChecked) ->
                tvDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE));
    }

    private void searchPokemon(String nameOrId) {
        btnSearch.setEnabled(false);
        btnRandom.setEnabled(false);

        Call<PokemonResponse> call = pokeApi.getPokemon(nameOrId);
        call.enqueue(new Callback<PokemonResponse>() {
            @Override
            public void onResponse(Call<PokemonResponse> call, Response<PokemonResponse> response) {
                btnSearch.setEnabled(true);
                btnRandom.setEnabled(true);

                if (!response.isSuccessful()) {
                    if (response.code() == 404) {
                        Toast.makeText(MainActivity.this, "No existe ese Pokémon 🙃", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                if (response.body() != null) {
                    currentPokemon = response.body();
                    displayPokemon();
                } else {
                    Toast.makeText(MainActivity.this, "Respuesta vacía", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PokemonResponse> call, Throwable t) {
                btnSearch.setEnabled(true);
                btnRandom.setEnabled(true);
                Toast.makeText(MainActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPokemon() {
        if (currentPokemon == null) return;

        // Info básica
        String basicInfo = String.format("%s (#%d)",
                capitalize(currentPokemon.name), currentPokemon.id);
        tvBasic.setText(basicInfo);

        // Detalles
        StringBuilder details = new StringBuilder();
        details.append("Peso: ").append(currentPokemon.weight / 10.0).append(" kg\n");

        if (currentPokemon.types != null && currentPokemon.types.length > 0) {
            details.append("Tipo(s): ");
            for (int i = 0; i < currentPokemon.types.length; i++) {
                details.append(capitalize(currentPokemon.types[i].type.name));
                if (i < currentPokemon.types.length - 1) details.append(", ");
            }
            details.append("\n");
        }

        if (currentPokemon.abilities != null && currentPokemon.abilities.length > 0) {
            details.append("Habilidades: ");
            for (int i = 0; i < currentPokemon.abilities.length; i++) {
                details.append(capitalize(currentPokemon.abilities[i].ability.name));
                if (i < currentPokemon.abilities.length - 1) details.append(", ");
            }
        }

        tvDetails.setText(details.toString());
        updatePokemonImage();
    }

    private void updatePokemonImage() {
        if (currentPokemon == null || currentPokemon.sprites == null) return;

        String imageUrl = null;
        boolean isShiny = cbShiny.isChecked();
        boolean isArtwork = tgSpriteArtwork.isChecked();
        boolean isFront = rbFront.isChecked();

        if (isArtwork) {
            if (currentPokemon.sprites.other != null &&
                    currentPokemon.sprites.other.official_artwork != null) {
                imageUrl = isShiny
                        ? currentPokemon.sprites.other.official_artwork.front_shiny
                        : currentPokemon.sprites.other.official_artwork.front_default;
            }
        } else {
            if (isFront) {
                imageUrl = isShiny ? currentPokemon.sprites.front_shiny : currentPokemon.sprites.front_default;
            } else {
                imageUrl = isShiny ? currentPokemon.sprites.back_shiny : currentPokemon.sprites.back_default;
            }
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(ivPokemon);
        } else {
            Toast.makeText(this, "Imagen no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearAll() {
        etQuery.setText("");
        tvBasic.setText(getString(R.string.tv_basic));
        tvDetails.setText(getString(R.string.tv_details_placeholder));
        tvDetails.setVisibility(View.GONE);
        ivPokemon.setImageResource(R.drawable.ic_launcher_foreground);

        cbShiny.setChecked(false);
        tgSpriteArtwork.setChecked(false);
        rbFront.setChecked(true);
        swDetails.setChecked(false);
        rbFront.setEnabled(true);
        rbBack.setEnabled(true);

        currentPokemon = null;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
