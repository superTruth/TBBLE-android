package cn.supertruth.tblib;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Truth on 2017/11/13.
 */

public class TBBLEDevice {
    public BluetoothDevice device;
    public byte[] advertisementData;
    public int rssi;
    public int currentRssi;
    public TBBLEDevice(BluetoothDevice device, byte[] advertisementData, int rssi){
        refreshInfo(device, advertisementData, rssi);
    }

    public void refreshInfo(TBBLEDevice device){
        refreshInfo(device.device, device.advertisementData, device.currentRssi);
    }

    public void refreshInfo(BluetoothDevice device, byte[] advertisementData, int rssi){
        this.device = device;
        this.advertisementData = advertisementData;
        this.currentRssi = rssi;
        if(rssi == 0){
            this.rssi = currentRssi;
        } else {
            this.rssi = (this.rssi + currentRssi)/2;
        }
    }

    public static boolean isEque(TBBLEDevice device1, TBBLEDevice device2){
        return device1.device.getAddress().toUpperCase().equals(device2.device.getAddress().toUpperCase());
    }
}
