package cn.supertruth.tblib.ble.action;

import android.bluetooth.BluetoothGattCharacteristic;

import cn.supertruth.tblib.TBErrorCode;

/**
 * Created by Truth on 2017/11/15.
 */

public class TBBLEEnNotifyAction extends TBBLEBaseAction{
    public BluetoothGattCharacteristic characteristic;
    public boolean enable;
    public TBBLEEnNotifyAction(BluetoothGattCharacteristic characteristic, boolean enable, TBBLEBaseOption option, TBBLEBaseActionCB cb) {
        super(TBBaseActionType.TBBLEACTION_ENCHARACTERNOTIFY, option, cb);
        this.characteristic = characteristic;
        this.enable = enable;
    }

    // 读取数据回调
    public interface TBBLEReadCharacterActionCB extends TBBLEBaseAction.TBBLEBaseActionCB{
        void onReadData(TBBLEBaseAction action, byte[] data);
    }
}
