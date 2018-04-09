package cn.supertruth.tblib.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.supertruth.tblib.TBBLEDevice;
import cn.supertruth.tblib.TBBLEManager;
import cn.supertruth.tblib.TBErrorCode;
import cn.supertruth.tblib.ble.action.TBBLEBaseAction;
import cn.supertruth.tblib.ble.action.TBBLEEnNotifyAction;
import cn.supertruth.tblib.ble.action.TBBLEReadCharacterAction;
import cn.supertruth.tblib.ble.action.TBBLEReadRssiAction;
import cn.supertruth.tblib.ble.action.TBBLEWriteCharacterAction;

/**
 * Created by Truth on 2017/11/13.
 */

public class TBBLEBase {
    private Handler handler = new Handler(Looper.getMainLooper());

    private TBBLEManager manager;
    private Context context;
    public TBBLEBase(TBBLEManager manager, Context context){
        this.manager = manager;
        this.context = context.getApplicationContext();
    }

    private TBBLEBaseStatus status = TBBLEBaseStatus.TBBLEBASE_DISCONNECT;
    // 连接设备
    private TBBLEDevice device;
    private int timeout;
    private int retryTimes;
    private TBBLEBaseCB cb;
    public TBErrorCode connect(TBBLEDevice device, int timeout, int retryTimes, TBBLEBaseCB cb){
        TBErrorCode errorCode = new TBErrorCode(TBErrorCode.TBOTHER_CODE, "");
        if(status != TBBLEBaseStatus.TBBLEBASE_DISCONNECT){
            errorCode.msg = "didn't disconnect";
            return errorCode;
        }
        if(manager.getStatues() != TBBLEManager.TBBLEStatus.TBBLE_OPENED){
            return TBErrorCode.TBBLECLOSE;
        }

        this.device = device;
        this.timeout = timeout;
        this.retryTimes = retryTimes;
        this.cb = cb;

        status = TBBLEBaseStatus.TBBLEBASE_CONNECTING;

        connectLoop();

        return TBErrorCode.TBSUCCESS;
    }

    public TBErrorCode connect(String mac, int timeout, int retryTimes, TBBLEBaseCB cb){
        BluetoothDevice device = manager.mBluetoothAdapter.getRemoteDevice(mac);
        TBBLEDevice tbbleDevice = new TBBLEDevice(device, null, 0);

        return connect(tbbleDevice, timeout, retryTimes, cb);
    }

    public void setCB(TBBLEBaseCB cb){
        this.cb = cb;
    }

    // 开始连接循环
    private BluetoothGatt bluetoothGatt;
    private void connectLoop(){
        if(status != TBBLEBaseStatus.TBBLEBASE_CONNECTING){
            return;
        }
        isDisConnectWithoutCB = false;

        if(retryTimes <= 0){  // 重试次数用完了
            status = TBBLEBaseStatus.TBBLEBASE_DISCONNECT;
            cb.onConnect(this, TBErrorCode.TBOTHER);
            return;
        }
        retryTimes --;

        cb.onRetryConnect(TBBLEBase.this, retryTimes);

        handler.postDelayed(connectTimeoutRunnable, timeout);
        bluetoothGatt = device.device.connectGatt(context, false, connectGatt);
    }

    private BluetoothGattCallback connectGatt = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if(gatt.getDevice() != device.device){  // 回调的结果不是本设备产生的,直接不理会，关闭掉
                gatt.close();
                return;
            }

            handler.removeCallbacks(connectTimeoutRunnable);  // 先把超时定时器去掉

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (newState == BluetoothProfile.STATE_DISCONNECTED) { // 如果是断开
                        if(isDisConnectWithoutCB){  // 无回调断开
                            isDisConnectWithoutCB = false;
                            disConnectWithoutCB.onCB();
                            return;
                        }

                        if(TBBLEBase.this.status == TBBLEBaseStatus.TBBLEBASE_CONNECTING){  // 正在连接时断开了
                            connectLoop();
                            return;
                        }
                        if(TBBLEBase.this.status == TBBLEBaseStatus.TBBLEBASE_DISCONNECTING) {  // 主动断开
                            TBBLEBase.this.status = TBBLEBaseStatus.TBBLEBASE_DISCONNECT;
                            handler.removeCallbacks(disConnectTimeoutRunnable);
                            cb.onManualDisConnect(TBBLEBase.this, TBErrorCode.TBSUCCESS);
                            return;
                        }

                        // 意外断开的情况
                        TBBLEBase.this.status = TBBLEBaseStatus.TBBLEBASE_DISCONNECT;
                        cb.onDisConnect(TBBLEBase.this, TBErrorCode.TBOTHER);

                        return;
                    }

                    if(TBBLEBase.this.status != TBBLEBaseStatus.TBBLEBASE_CONNECTING){  // 并不是连接时产生的回调
                        gatt.close();
                        return;
                    }

                    if (status != BluetoothGatt.GATT_SUCCESS) { // 连接失败,重连
                        connectLoop();
                        return;
                    }

                    handler.postDelayed(connectTimeoutRunnable, timeout);
                    gatt.discoverServices();

                }
            });
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            super.onServicesDiscovered(gatt, status);
            if(gatt.getDevice() != device.device){  // 回调的结果不是本设备产生的,直接不理会，关闭掉
                gatt.close();
                return;
            }
            if(TBBLEBase.this.status != TBBLEBaseStatus.TBBLEBASE_CONNECTING) {   // 非处于正在连接状态
                gatt.close();
                return;
            }

            handler.removeCallbacks(connectTimeoutRunnable);  // 先把超时定时器去掉

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (status != BluetoothGatt.GATT_SUCCESS) { // 获取服务失败,重连
                        handler.postDelayed(disconnectWithoutCBTimeoutRunnable, timeout);
                        disConnectWithoutCB(new VoidCB() {
                            @Override
                            public void onCB() {
                                handler.removeCallbacks(disconnectWithoutCBTimeoutRunnable);  // 先把超时定时器去掉
                                connectLoop();
                            }
                        });
                        return;
                    }
                    handler.postDelayed(waitStableRunnable, 500);  // 等待500ms的稳定期
                }
            });

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, final int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if(workAction.actionType != TBBLEBaseAction.TBBaseActionType.TBBLEACTION_ENCHARACTERNOTIFY){  // 正在工作的任务类型不对
                return;
            }
            if(!realConnectStatues()){
                return;
            }
            handler.removeCallbacks(actionTimeoutRunnable);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        workAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_FAIL;
                        workAction.cb.onFail(workAction, TBErrorCode.TBOTHER);
                        handler.post(actionLoopRunnable);
                        return;
                    }

                    workAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_DONE;
                    workAction.cb.onSuccess(workAction);
                    handler.post(actionLoopRunnable);
                }
            });

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, final int rssi, final int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if(workAction.actionType != TBBLEBaseAction.TBBaseActionType.TBBLEACTION_ENCHARACTERNOTIFY){  // 正在工作的任务类型不对
                return;
            }
            handler.removeCallbacks(actionTimeoutRunnable);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        workAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_FAIL;
                        workAction.cb.onFail(workAction, TBErrorCode.TBOTHER);
                        handler.post(actionLoopRunnable);
                        return;
                    }
                    ((TBBLEReadRssiAction.TBBLEReadRssiActionCB)workAction.cb).onReadRssi(workAction, rssi);
                    handler.post(actionLoopRunnable);
                }
            });
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, final int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if(workAction.actionType != TBBLEBaseAction.TBBaseActionType.TBBLEACTION_WRITECHACT){  // 正在工作的任务类型不对
                return;
            }
            handler.removeCallbacks(actionTimeoutRunnable);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        workAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_FAIL;
                        workAction.cb.onFail(workAction, TBErrorCode.TBOTHER);
                        handler.post(actionLoopRunnable);
                        return;
                    }
                    handler.post(actionLoopRunnable);
                }
            });
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(cb != null){
                        cb.onCharactChanged(TBBLEBase.this, characteristic, characteristic.getValue());
                    }
                }
            });
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, final int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if(workAction.actionType != TBBLEBaseAction.TBBaseActionType.TBBLEACTION_READCHACT){  // 正在工作的任务类型不对
                return;
            }
            handler.removeCallbacks(actionTimeoutRunnable);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        workAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_FAIL;
                        workAction.cb.onFail(workAction, TBErrorCode.TBOTHER);
                        handler.post(actionLoopRunnable);
                        return;
                    }
                    workAction.cb.onSuccess(workAction);
                    handler.post(actionLoopRunnable);
                }
            });
        }
    };

    // 等待设备稳定，防止假连接
    private Runnable waitStableRunnable = new Runnable() {
        @Override
        public void run() {
            if(status != TBBLEBaseStatus.TBBLEBASE_CONNECTING){
                return;
            }

            if(!realConnectStatues()){  // 在此期间断开了
                connectLoop();
                return;
            }
            status = TBBLEBaseStatus.TBBLEBASE_FREE;
            cb.onConnect(TBBLEBase.this, TBErrorCode.TBSUCCESS);  // 回调连接成功
        }
    };

    // 无回调断开超时事件
    private Runnable disconnectWithoutCBTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO: 2017/11/15 断开还是有可能会再次回调
            bluetoothGatt.close();
            isDisConnectWithoutCB = false;

            disConnectWithoutCB.onCB();
        }
    };

    // 连接超时事件
    private Runnable connectTimeoutRunnable = new Runnable() {
        @Override
        public void run() {

            if(status != TBBLEBaseStatus.TBBLEBASE_CONNECTING){
                return;
            }

            if(!realConnectStatues()){   // 还未连接成功
                bluetoothGatt.close();
                connectLoop();
                return;
            }

            disConnectWithoutCB(new VoidCB() {  // 已经连接成功的情况，需要向断开一下，然后再继续
                @Override
                public void onCB() {
                    connectLoop();
                }
            });
        }
    };

    // 主动断开超时事件
    private Runnable disConnectTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if(status != TBBLEBaseStatus.TBBLEBASE_DISCONNECTING){
                return;
            }
            status = TBBLEBaseStatus.TBBLEBASE_DISCONNECT;
            if(!realConnectStatues()){
                cb.onManualDisConnect(TBBLEBase.this, TBErrorCode.TBSUCCESS);
                return;
            }
            bluetoothGatt.close();
            cb.onManualDisConnect(TBBLEBase.this, TBErrorCode.TBTIMEOUT);
        }
    };

    // 断开设备
    public TBErrorCode disConnect(){

        status = TBBLEBaseStatus.TBBLEBASE_DISCONNECTING;  // 设置为正在断开状态

        if(!realConnectStatues()){   // 此时根本就没有连接上
            bluetoothGatt.close();
            status = TBBLEBaseStatus.TBBLEBASE_DISCONNECT;
            cb.onManualDisConnect(TBBLEBase.this, TBErrorCode.TBSUCCESS);
            return TBErrorCode.TBSUCCESS;
        }

        handler.postDelayed(disConnectTimeoutRunnable, 2000); // 2秒的断开超时
        bluetoothGatt.disconnect();

        return TBErrorCode.TBSUCCESS;
    }

    // 无反馈回调，内部使用，过程被中断时，用来内部断开
    private VoidCB disConnectWithoutCB;
    private boolean isDisConnectWithoutCB = false;
    public TBErrorCode disConnectWithoutCB(VoidCB cb){
        this.disConnectWithoutCB = cb;
        isDisConnectWithoutCB = true;

        if (!realConnectStatues()) {  // 本身就还未连接时
            disConnectWithoutCB.onCB();
            return TBErrorCode.TBSUCCESS;
        }

        bluetoothGatt.disconnect();

        return TBErrorCode.TBSUCCESS;
    }

    // 查看连接状态
    public boolean isConnected(){
        if(status == TBBLEBaseStatus.TBBLEBASE_FREE){
            return true;
        }

        return false;
    }

    // 获取所有服务
    public List<BluetoothGattService> getAllServices(){
        if(!isConnected()){
            return null;
        }
        return bluetoothGatt.getServices();
    }

    // 通过UUID, 获取指定的服务
    public BluetoothGattService getServiceByUuid(String uuid){
        if(!isConnected()){
            return null;
        }
        return bluetoothGatt.getService(UUID.fromString(uuid));
    }

    // 获取指定特征值
    public BluetoothGattCharacteristic getCharacterByUuid(String servicesUUID, String characterUUID){
        BluetoothGattService service = getServiceByUuid(servicesUUID);
        if(service == null){
            return null;
        }

        return service.getCharacteristic(UUID.fromString(characterUUID));
    }

    // 添加任务
    private List<TBBLEBaseAction> actions = new ArrayList<>();
    public TBErrorCode addAction(TBBLEBaseAction action){
        if(!isConnected()){
            return TBErrorCode.TBDISCONNECT;
        }

        actions.add(action);

        startDoAction();

        return TBErrorCode.TBSUCCESS;
    }

    // 启动任务队列
    private boolean working = false;
    private void startDoAction(){
        if(working){  // 正在工作
            return;
        }

        doActionLoop();
    }

    // 循环执行任务
    private TBBLEBaseAction workAction;
    private void doActionLoop(){
        if(!isConnected()){  // 断开了连接
            return;
        }
        working = true;

        removeDoneActions();  // 把已经运行完的任务删除掉

        if(actions.size() == 0){  // 任务运行完毕
            status = TBBLEBaseStatus.TBBLEBASE_FREE;
            working = false;
            return;
        }

        workAction = actions.get(0); // 提取出需要执行的任务

        // **************************写特征值任务***************************
        if(workAction.actionType == TBBLEBaseAction.TBBaseActionType.TBBLEACTION_WRITECHACT){
            TBBLEWriteCharacterAction writeCharactAction = (TBBLEWriteCharacterAction)workAction;
            byte[] needSendData = writeCharactAction.getPerData();
            if(needSendData == null){  // 已经发送完毕的
                writeCharactAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_DONE;
                writeCharactAction.cb.onSuccess(writeCharactAction);
                handler.post(actionLoopRunnable);
                return;
            }
            workAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_RUNNING;
            handler.postDelayed(actionTimeoutRunnable, writeCharactAction.option.timeout);

            ((TBBLEWriteCharacterAction.TBBLEWriteCharacterActionCB)writeCharactAction.cb).onSendPerData(needSendData);
            if(writeCharactAction.writeType == TBBLEWriteCharacterAction.WriteType.DEFAULT){ // 默认写入方式
                if((writeCharactAction.characteristic.getProperties() &
                        BluetoothGattCharacteristic.PROPERTY_WRITE) != 0){  // 有反馈写入
                    writeCharactAction.characteristic
                            .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    writeCharactAction.realWriteType = TBBLEWriteCharacterAction.WriteType.DEFAULT;
                    writeCharactAction.characteristic.setValue(needSendData);
                    bluetoothGatt.writeCharacteristic(writeCharactAction.characteristic);
                    return;
                }
                if((writeCharactAction.characteristic.getProperties() &  // 无反馈写入
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0){
                    writeCharactAction.characteristic
                            .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    writeCharactAction.realWriteType = TBBLEWriteCharacterAction.WriteType.WRITE_NO_RESPONSE;
                    writeCharactAction.characteristic.setValue(needSendData);
                    bluetoothGatt.writeCharacteristic(writeCharactAction.characteristic);
                    return;
                }
                handler.removeCallbacks(actionTimeoutRunnable);
                writeCharactAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_FAIL;
                writeCharactAction.cb.onFail(writeCharactAction, TBErrorCode.TBNOPERMISSION);
                handler.post(actionLoopRunnable);
                return;
            }

            if((writeCharactAction.characteristic.getProperties() &  // 无反馈写入
                    BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0){
                writeCharactAction.realWriteType = TBBLEWriteCharacterAction.WriteType.WRITE_NO_RESPONSE;
                writeCharactAction.characteristic
                        .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                writeCharactAction.characteristic.setValue(needSendData);
                bluetoothGatt.writeCharacteristic(writeCharactAction.characteristic);
                return;
            }
            handler.removeCallbacks(actionTimeoutRunnable);
            writeCharactAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_FAIL;
            writeCharactAction.cb.onFail(writeCharactAction, TBErrorCode.TBNOPERMISSION);
            handler.post(actionLoopRunnable);
            return;
        }

        // **************************读特征值任务***************************
        if(workAction.actionType == TBBLEBaseAction.TBBaseActionType.TBBLEACTION_READCHACT){
            TBBLEReadCharacterAction readCharacterAction = (TBBLEReadCharacterAction)workAction;
            if((readCharacterAction.characteristic.getProperties() &  // 判断权限
                    BluetoothGattCharacteristic.PROPERTY_READ) == 0){
                workAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_FAIL;
                workAction.cb.onFail(workAction, TBErrorCode.TBNOPERMISSION);
                handler.post(actionLoopRunnable);
                return;
            }
            workAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_RUNNING;
            handler.postDelayed(actionTimeoutRunnable, workAction.option.timeout);
            bluetoothGatt.readCharacteristic(readCharacterAction.characteristic);
            return;
        }

        // **************************读信号强度任务***************************
        if(workAction.actionType == TBBLEBaseAction.TBBaseActionType.TBBLEACTION_READRSSI){
            workAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_RUNNING;
            handler.postDelayed(actionTimeoutRunnable, workAction.option.timeout);
            workAction.cb.onStart(workAction); // 回调任务开始执行
            bluetoothGatt.readRemoteRssi();
            return;
        }

        // **************************使能特征值监听任务***************************
        if(workAction.actionType == TBBLEBaseAction.TBBaseActionType.TBBLEACTION_ENCHARACTERNOTIFY){
            TBBLEEnNotifyAction ennotifyAction = (TBBLEEnNotifyAction)workAction;
            BluetoothGattDescriptor descriptor = ennotifyAction.characteristic
                    .getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            if(descriptor == null){
                ennotifyAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_FAIL;
                ennotifyAction.cb.onFail(ennotifyAction, TBErrorCode.TBNOPERMISSION);
                handler.post(actionLoopRunnable);
                return;
            }

            if(((ennotifyAction.characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) &&  // 判断权限
                    ((ennotifyAction.characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0)){
                ennotifyAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_FAIL;
                ennotifyAction.cb.onFail(ennotifyAction, TBErrorCode.TBNOPERMISSION);
                handler.post(actionLoopRunnable);
            }
            bluetoothGatt.setCharacteristicNotification(ennotifyAction.characteristic, ennotifyAction.enable);
            if((ennotifyAction.characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0){
                if(ennotifyAction.enable){
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                }else{
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
            }else{
                if(ennotifyAction.enable) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                }else{
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
            }
            workAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_RUNNING;
            handler.postDelayed(actionTimeoutRunnable, workAction.option.timeout);
            bluetoothGatt.writeDescriptor(descriptor);
            return;
        }

        // **************************无效任务***************************
        workAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_FAIL;
        workAction.cb.onFail(workAction, TBErrorCode.TBOTHER);
        handler.post(actionLoopRunnable);
        return;
    }

    // 队列循环执行loop事件，防止递归
    private Runnable actionLoopRunnable = new Runnable() {
        @Override
        public void run() {
            doActionLoop();
        }
    };

    // 任务执行超时事件
    private Runnable actionTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            workAction.status = TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_FAIL;
            workAction.cb.onFail(workAction, TBErrorCode.TBTIMEOUT);
            handler.post(actionLoopRunnable);
        }
    };

    // 清除已完成或错误队列
    private void removeDoneActions(){
        for (int i=0; i < actions.size(); i++) {
            TBBLEBaseAction action = actions.get(i);
            if((action.status == TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_DONE) ||
                    (action.status == TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_CANCEL) ||
                    (action.status == TBBLEBaseAction.TBBaseActionStatus.TBBLEACTION_FAIL)){
                actions.remove(i);
                i--;
                continue;
            }else{
                break;  // 只删除前面的任务，后面有的，暂时保留，增加性能
            }
        }
    }

    public interface TBBLEBaseCB{
        void onConnect(TBBLEBase ble, TBErrorCode errorCode);
        void onRetryConnect(TBBLEBase ble, int remainTimes);
        void onDisConnect(TBBLEBase ble, TBErrorCode errorCode);
        void onManualDisConnect(TBBLEBase ble, TBErrorCode errorCode);
        void onCharactChanged(TBBLEBase ble, BluetoothGattCharacteristic characteristic, byte[] datas);
    }

    public interface VoidCB{
        void onCB();
    }
    // 状态
    private enum TBBLEBaseStatus{
        TBBLEBASE_DISCONNECT,  // 未连接
        TBBLEBASE_CONNECTING,  // 正在连接
        TBBLEBASE_FREE,         // 空闲状态
        TBBLEBASE_DISCONNECTING, // 正在断开
    }

    // 真正有没有在连接
    private boolean realConnectStatues(){
        if (manager.mBluetoothManager.getConnectionState(device.device, BluetoothProfile.GATT) != BluetoothProfile.STATE_CONNECTED) {  // 本身就还未连接时
            return false;
        }
        return true;
    }
}
