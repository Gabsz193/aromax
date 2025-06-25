package com.example.aromax;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aromax.bluetooth.BluetoothService;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private EditText editTextMinutes;
    private EditText editTextSeconds;
    private TextView textViewTotalTimeLabel;

    private BluetoothService bluetoothService;
    private final String DEVICE_NAME = "ESP32-BT-Slave";

    // O botão de conectar foi removido da lógica da Activity
    private Button buttonStart;

    // Variáveis de instância para guardar os valores numéricos
    private int currentMinutes = 0;
    private int currentSeconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextMinutes = findViewById(R.id.editTextMinutes);
        editTextSeconds = findViewById(R.id.editTextSeconds);
        textViewTotalTimeLabel = findViewById(R.id.textViewTotalTimeLabel);

        // Define valores iniciais para os EditTexts e atualiza o label
        // Se você quiser carregar de algum lugar (como SharedPreferences da resposta anterior),
        // faria isso aqui antes de definir o texto e chamar updateTotalTimeLabel.
        // Por agora, vamos manter os valores iniciais como 0.
        editTextMinutes.setText(String.valueOf(currentMinutes));
        editTextSeconds.setText(String.valueOf(currentSeconds));
        updateTotalTimeLabelAndUpdateVariables(); // Chama o método atualizado

        editTextMinutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotalTimeLabelAndUpdateVariables();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editTextSeconds.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotalTimeLabelAndUpdateVariables();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Instancia o serviço
        bluetoothService = new BluetoothService(this);

        buttonStart = findViewById(R.id.buttonStart);

        // **LÓGICA PRINCIPAL ATUALIZADA**
        buttonStart.setOnClickListener(v -> {
            // Se já estiver conectado, apenas envia a mensagem.
            if (bluetoothService.isConnected()) {
                String message = String.format("%d-%d", currentMinutes, currentSeconds);
                sendMessageAndShowToast(message);
            } else {
                // Se não estiver conectado, tenta se conectar ao dispositivo já pareado.
                Toast.makeText(this, "Iniciando conexão com " + DEVICE_NAME + "...", Toast.LENGTH_SHORT).show();

                bluetoothService.connect(DEVICE_NAME, new BluetoothService.ConnectionListener() {
                    @Override
                    public void onConnected() {
                        // Callback de sucesso: agora que conectou, envia a mensagem.
                        Toast.makeText(MainActivity.this, "Conectado!", Toast.LENGTH_SHORT).show();
                        String message = String.format("%d-%d", currentMinutes, currentSeconds);
                        sendMessageAndShowToast(message);
                    }

                    @Override
                    public void onError(String message) {
                        // Callback de erro: notifica o usuário que a conexão falhou.
                        Toast.makeText(MainActivity.this, "Não foi possível conectar. Verifique se o dispositivo está ligado e pareado nas configurações de Bluetooth.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        // Lembre-se de ter as permissões de Bluetooth no seu AndroidManifest.xml

    }

    // Método que atualiza o texto de textViewTotalTimeLabel E as variáveis de instância
    private void updateTotalTimeLabelAndUpdateVariables() {
        String minutesStr = editTextMinutes.getText().toString();
        String secondsStr = editTextSeconds.getText().toString();

        // Atualiza as variáveis de instância
        currentMinutes = minutesStr.isEmpty() ? 0 : Integer.parseInt(minutesStr);
        currentSeconds = secondsStr.isEmpty() ? 0 : Integer.parseInt(secondsStr);

        // Formata e define o texto do label
        // String formattedText = currentMinutes + " min, intervalo de " + currentSeconds + " s";
        // Melhor usar getString para internacionalização, se possível:
        String formattedText = getString(R.string.total_time_format, currentMinutes, currentSeconds);
        textViewTotalTimeLabel.setText(formattedText);

        // Agora você pode usar currentMinutes e currentSeconds em outros métodos desta Activity
        // Exemplo:
        // if (currentMinutes > 10) {
        //     // Fazer algo
        // }
    }

    /**
     * Função auxiliar para gerar e enviar a mensagem, e mostrar o Toast de confirmação.
     */
    private void sendMessageAndShowToast(String message) {
        bluetoothService.sendMessage(message);
        Toast.makeText(this, "Mensagem enviada: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Garante que a conexão seja fechada ao sair do app
        if (bluetoothService != null) {
            bluetoothService.disconnect();
        }
    }

    /*
    Se você tiver um string resource como este em res/values/strings.xml:
    <string name="total_time_format">%1$d min, intervalo de %2$d s</string>
    */


    //    @Override
    //    public void onBackPressed() {
    //        super.onBackPressed();
    //        // Se você precisar passar currentMinutes e currentSeconds de volta para LoginActivity:
    //        // Intent intent = new Intent(MainActivity.this, LoginActivity.class);
    //        // intent.putExtra("USER_MINUTES", currentMinutes);
    //        // intent.putExtra("USER_SECONDS", currentSeconds);
    //        // startActivity(intent);
    //        // finish();
    //    }
}