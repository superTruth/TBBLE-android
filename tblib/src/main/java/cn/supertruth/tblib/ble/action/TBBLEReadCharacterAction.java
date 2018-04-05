package cn.supertruth.tblib.ble.action;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by Truth on 2017/11/15.
 */

public class TBBLEReadCharacterAction extends TBBLEBaseAction {
    public BluetoothGattCharacteristic characteristic;
    public TBBLEReadCharacterAction(BluetoothGattCharacteristic characteristic, TBBLEBaseOption option, TBBLEBaseActionCB cb) {
        super(TBBaseActionType.TBBLEACTION_READCHACT, option, cb);
        this.characteristic = characteristic;
    }
}
