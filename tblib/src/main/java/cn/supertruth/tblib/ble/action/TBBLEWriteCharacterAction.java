package cn.supertruth.tblib.ble.action;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.Arrays;

/**
 * Created by Truth on 2017/11/15.
 */

public class TBBLEWriteCharacterAction extends TBBLEBaseAction {
    public BluetoothGattCharacteristic characteristic;
    public byte[] sendData;
    public WriteType writeType = WriteType.DEFAULT;
    public WriteType realWriteType = WriteType.DEFAULT;
    public TBBLEWriteCharacterAction(BluetoothGattCharacteristic characteristic, byte[] sendData, TBBLEBaseOption option, TBBLEBaseActionCB cb) {
        super(TBBaseActionType.TBBLEACTION_WRITECHACT, option, cb);
        this.characteristic = characteristic;
        this.sendData = sendData;
    }

    // 获取单条数据
    private int sendIndex = 0;
    public byte[] getPerData(){
        if((sendData == null) || (sendIndex >= sendData.length)){  // 发送完毕
            status = TBBaseActionStatus.TBBLEACTION_DONE;
            return null;
        }

        if((sendIndex + option.perLen) > sendData.length){   // 剩余数据不足一整个桢
            byte[] sendDataTmp = Arrays.copyOfRange(sendData, sendIndex, sendData.length);
            sendIndex = sendData.length;
            return sendDataTmp;
        }

        byte[] sendDataTmp = Arrays.copyOfRange(sendData, sendIndex, sendIndex + option.perLen);
        sendIndex += option.perLen;

        return sendDataTmp;
    }

    // 复位操作
    public void resetAction(){
        status = TBBaseActionStatus.TBBLEACTION_WAITTING;
        sendIndex = 0;
    }

    // 写方式
    public enum WriteType{
        DEFAULT,  // 默认方式，以有相应优先，
        WRITE_NO_RESPONSE,    // 无相应方式写
    }

    // 写数据回调
    public interface TBBLEWriteCharacterActionCB extends TBBLEBaseAction.TBBLEBaseActionCB{
        void onSendPerData(byte[] data);
    }
}
