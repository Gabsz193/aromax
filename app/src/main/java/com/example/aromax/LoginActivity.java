package com.example.aromax; // Altere para o seu pacote

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configura o clique no botão "Entrar"
        findViewById(R.id.buttonLogin).setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Configura o clique em "Cadastre-se"
        TextView textViewSignUp = findViewById(R.id.textViewSignUp);
        textViewSignUp.setText(Html.fromHtml(getString(R.string.login_no_account_prompt), Html.FROM_HTML_MODE_COMPACT));
        textViewSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }


}