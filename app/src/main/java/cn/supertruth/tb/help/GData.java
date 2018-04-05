package cn.supertruth.tb.help;

import android.bluetooth.BluetoothGattCharacteristic;

import cn.supertruth.tblib.TBBLEDevice;
import cn.supertruth.tblib.ble.TBBLEBase;

/***************************************************************************************************
 *                                  Copyright (C), Nexgo Inc.                                      *
 *                                    http://www.nexgo.cn                                          *
 ***************************************************************************************************
 * usage           : 
 * Version         : 1
 * Author          : Truth
 * Date            : 2017/12/26
 * Modify          : create file
 **************************************************************************************************/
public class GData {
    private GData(){}

    private static GData INSTANCE;
    public static GData getInstance(){
        if(INSTANCE == null){
            INSTANCE = new GData();
        }
        return INSTANCE;
    }

    public TBBLEDevice workDevice;
    public TBBLEBase tbbleBase;
    public BluetoothGattCharacteristic workingChararcter;
}
