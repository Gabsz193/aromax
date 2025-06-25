package com.example.aromax.bluetooth;

/**
 * Interface de callback para o BluetoothManager notificar a UI sobre eventos.
 */
public interface BluetoothListener {
    void onBluetoothConnected();
    void onBluetoothDisconnected();
    void onDataReceived(String data);
    void onError(String message);
}