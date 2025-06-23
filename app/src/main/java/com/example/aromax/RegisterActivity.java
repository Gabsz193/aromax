// IA Generated
// Local do arquivo: app/src/main/java/com/example/aromax/RegisterActivity.java
package com.example.aromax;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Configura o clique no botão "Cadastrar"
        findViewById(R.id.buttonRegister).setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Configura o clique no link "Entrar"
        TextView textViewLoginLink = findViewById(R.id.textViewLoginLink);
        textViewLoginLink.setText(Html.fromHtml(getString(R.string.register_already_have_account), Html.FROM_HTML_MODE_COMPACT));
        textViewLoginLink.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }
}