package com.example.aromax.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.aromax.data.models.CommandRegistro;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HistoricoRepository {

    private static final String PREFS_NAME = "historico_prefs";
    private static final String REGISTROS_KEY = "registros_list";
    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();

    public HistoricoRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<CommandRegistro> getRegistros() {
        String json = sharedPreferences.getString(REGISTROS_KEY, null);
        if (json == null) {
            return new ArrayList<>(); // Retorna lista vazia se não houver nada
        }
        Type type = new TypeToken<ArrayList<CommandRegistro>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveRegistro(CommandRegistro registro) {
        List<CommandRegistro> registros = getRegistros();
        registros.add(0, registro); // Adiciona no início da lista

        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(registros);
        editor.putString(REGISTROS_KEY, json);
        editor.apply();
    }

    public void clearRegistros() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(REGISTROS_KEY);
        editor.apply();
    }
}
