package com.example.aromax.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Classe modularizada para gerenciar a conexão Bluetooth SPP (Serial).
 */
public class BluetoothService {

    private static final String TAG = "BluetoothService";
    // UUID padrão para Serial Port Profile (SPP)
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Context context;

    // Interface para notificar a Activity sobre o status da conexão.
    public interface ConnectionListener {
        void onConnected();
        void onError(String message);
    }

    public BluetoothService(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Tenta se conectar a um dispositivo em uma nova thread.
     * @param deviceName O nome do dispositivo (ex: "ESP32-BT-Slave").
     * @param listener   O listener para notificar sobre o sucesso ou falha.
     */
    @SuppressLint("MissingPermission") // As permissões são verificadas na Activity
    public void connect(String deviceName, ConnectionListener listener) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            listener.onError("Bluetooth não está ativado.");
            return;
        }

        new Thread(() -> {
            BluetoothDevice targetDevice = findDevice(deviceName);
            if (targetDevice == null) {
                handler.post(() -> listener.onError("Dispositivo '" + deviceName + "' não encontrado."));
                return;
            }

            try {
                // Tenta fechar qualquer conexão antiga antes de criar uma nova.
                disconnect();

                bluetoothSocket = targetDevice.createRfcommSocketToServiceRecord(SPP_UUID);
                bluetoothSocket.connect(); // Chamada bloqueante
                outputStream = bluetoothSocket.getOutputStream();
                handler.post(listener::onConnected);
                Log.i(TAG, "Conectado com sucesso a " + deviceName);
            } catch (IOException e) {
                Log.e(TAG, "Erro na conexão: ", e);
                disconnect(); // Garante que tudo seja limpo em caso de falha.
                handler.post(() -> listener.onError("Falha ao conectar: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Envia uma mensagem de texto para o dispositivo conectado.
     * @param message A mensagem a ser enviada.
     */
    public void sendMessage(String message) {
        if (outputStream == null) {
            Log.e(TAG, "Não é possível enviar mensagem, outputStream é nulo.");
            return;
        }
        new Thread(() -> {
            try {
                outputStream.write(message.getBytes());
                Log.i(TAG, "Mensagem enviada: " + message);
            } catch (IOException e) {
                Log.e(TAG, "Erro ao enviar mensagem: ", e);
            }
        }).start();
    }

    /**
     * Verifica se existe uma conexão ativa.
     * @return true se conectado, false caso contrário.
     */
    public boolean isConnected() {
        return bluetoothSocket != null && bluetoothSocket.isConnected() && outputStream != null;
    }

    /**
     * Encerra a conexão e libera os recursos.
     */
    public void disconnect() {
        try {
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Erro ao desconectar: ", e);
        }
    }

    @SuppressLint("MissingPermission")
    private BluetoothDevice findDevice(String deviceName) {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (deviceName.equals(device.getName())) {
                return device;
            }
        }
        return null;
    }
}
