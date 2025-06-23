// Local do arquivo: app/src/main/java/com/example/aromax/MainActivity.java
package com.example.aromax;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Define o layout da activity como o activity_main.xml
        setContentView(R.layout.activity_main);

        // Nenhuma funcionalidade é implementada aqui, apenas a exibição do layout.
        // A lógica de controle do tempo, dos botões e do timer seria
        // adicionada aqui posteriormente.
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}