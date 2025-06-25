package com.example.aromax;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.aromax.bluetooth.BluetoothService;
import com.example.aromax.data.HistoricoRepository;
import com.example.aromax.data.models.CommandRegistro;
import com.example.aromax.services.TimerService;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "Aromax";
    private EditText editTextMinutes;
    private EditText editTextSeconds;
    private TextView textViewTotalTimeLabel;

    private TextView textViewTimerDisplay;

    private BluetoothService bluetoothService;

    private BroadcastReceiver timerUpdateReceiver;
    private final String DEVICE_NAME = "ESP32-BT-Slave";

    // O botão de conectar foi removido da lógica da Activity
    private Button buttonStart;

    // Variáveis de instância para guardar os valores numéricos
    private int currentMinutes = 0;
    private int currentSeconds = 0;

    private HistoricoRepository historicoRepository;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        historicoRepository = new HistoricoRepository(this);

        editTextMinutes = findViewById(R.id.editTextMinutes);
        editTextSeconds = findViewById(R.id.editTextSeconds);
        textViewTotalTimeLabel = findViewById(R.id.textViewTotalTimeLabel);
        textViewTimerDisplay = findViewById(R.id.textViewTimerDisplay);

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
            if(currentMinutes == currentSeconds && currentSeconds == 0) {
                Toast.makeText(this, "O tempo não pode ser zero.", Toast.LENGTH_SHORT).show();
                return;
            }

            if(currentSeconds > 10) {
                Toast.makeText(this, "O intervalor não pode ser maior que 10 segundos.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Se já estiver conectado, apenas envia a mensagem.
            if (bluetoothService.isConnected()) {
                String message = String.format("%d-%d", currentMinutes, currentSeconds);
                sendMessageAndShowToast(message);
                historicoRepository.saveRegistro(new CommandRegistro(
                        currentMinutes,
                        currentSeconds
                ));
                setupReceiver();
                handleStartTimerClick();
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
                        historicoRepository.saveRegistro(new CommandRegistro(
                            currentMinutes,
                            currentSeconds
                        ));
                        setupReceiver();
                        handleStartTimerClick();
                    }

                    @Override
                    public void onError(String message) {
                        // Callback de erro: notifica o usuário que a conexão falhou.
                        Toast.makeText(MainActivity.this, "Não foi possível conectar. Verifique se o dispositivo está ligado e pareado nas configurações de Bluetooth.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        // 1. Encontre o novo botão no layout
        Button buttonViewHistory = findViewById(R.id.buttonViewHistory);

        // 2. Configure o OnClickListener para o botão de histórico
        buttonViewHistory.setOnClickListener(v -> {
            // Crie um Intent para abrir a HistoricoActivity
            Intent intent = new Intent(MainActivity.this, HistoricoActivity.class);
            startActivity(intent);
        });

        // Lembre-se de ter as permissões de Bluetooth no seu AndroidManifest.xml

    }

    private void handleStartTimerClick() {
        // Passo 1: Verificar se a permissão é necessária (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Passo 2: Verificar se a permissão JÁ FOI CONCEDIDA
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Permissão já concedida, pode iniciar o serviço.
                launchTimerService();
            } else {
                // Permissão ainda não concedida, pedir ao usuário.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // Para versões mais antigas do Android, a permissão não é necessária.
            launchTimerService();
        }
    }

    private void launchTimerService() {
        String minutesStr = String.valueOf(currentMinutes);
        String secondsStr = String.valueOf(currentSeconds);
        Log.d(TAG, "launchTimerService: Here");

        if (TextUtils.isEmpty(minutesStr) && TextUtils.isEmpty(secondsStr)) {
            Toast.makeText(this, "Por favor, insira um tempo.", Toast.LENGTH_SHORT).show();
            return;
        }

        int minutes = TextUtils.isEmpty(minutesStr) ? 0 : Integer.parseInt(minutesStr);
        int seconds = TextUtils.isEmpty(secondsStr) ? 0 : Integer.parseInt(secondsStr);

        if (minutes == 0 && seconds == 0) {
            Toast.makeText(this, "O tempo não pode ser zero.", Toast.LENGTH_SHORT).show();
            return;
        }

        long durationInMillis = (minutes * 60L) * 1000L;

        // Inicia o serviço em primeiro plano
        Intent serviceIntent = new Intent(this, TimerService.class);
        serviceIntent.putExtra(TimerService.EXTRA_DURATION_MS, durationInMillis);
        ContextCompat.startForegroundService(this, serviceIntent);

        // Atualiza a UI
        IntentFilter filter = new IntentFilter();
        filter.addAction(TimerService.ACTION_TIMER_TICK);
        filter.addAction(TimerService.ACTION_TIMER_FINISH);
        registerReceiver(timerUpdateReceiver, filter, Context.RECEIVER_EXPORTED);
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
    private void setupReceiver() {
        timerUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null) {
                    if (intent.getAction().equals(TimerService.ACTION_TIMER_TICK)) {
                        String timeRemaining = intent.getStringExtra(TimerService.EXTRA_TIME_REMAINING);
                        textViewTimerDisplay.setText(timeRemaining);
                        setTimerRunningUI(true); // Garante que a UI está no estado "rodando"
                    } else if (intent.getAction().equals(TimerService.ACTION_TIMER_FINISH)) {
                        textViewTimerDisplay.setText(R.string.main_timer_default); // Ou "00:00"
                        setTimerRunningUI(false); // Reseta a UI para o estado inicial
                    }
                }
            }
        };
    }

    private void setTimerRunningUI(boolean isRunning) {
        editTextMinutes.setEnabled(!isRunning);
        editTextSeconds.setEnabled(!isRunning);
        buttonStart.setEnabled(!isRunning);
        buttonStart.setAlpha(isRunning ? 0.5f : 1.0f); // Feedback visual
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(TimerService.ACTION_TIMER_TICK);
        filter.addAction(TimerService.ACTION_TIMER_FINISH);
        registerReceiver(timerUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registra o receiver para receber atualizações do timer
        IntentFilter filter = new IntentFilter();
        filter.addAction(TimerService.ACTION_TIMER_TICK);
        filter.addAction(TimerService.ACTION_TIMER_FINISH);
        registerReceiver(timerUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(timerUpdateReceiver != null) {
            // É CRUCIAL desregistrar o receiver para evitar memory leaks
            try {
                unregisterReceiver(timerUpdateReceiver);
            } catch (Exception e) {
                Log.e("Erro de unregisterReceiver", "onPause: Erro");
            }
        }
    }
}