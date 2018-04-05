package cn.supertruth.tblib;

import android.annotation.TargetApi;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Truth on 2017/11/9.
 */

public class TBBLEManager {

    private TBBLEManager(){

    }
    private static TBBLEManager INSTANCE;
    public static TBBLEManager getInstance(){
        if(INSTANCE == null){
            INSTANCE = new TBBLEManager();
        }
        return INSTANCE;
    }

    // 错误码
    public static final int ERRORCODE_SCANALREADYSTARTED = 2; // 扫描已经启动了
    public static final int ERRORCODE_REGISTRATIONFAILED = 3; // 程序未注册
    public static final int ERRORCODE_FEATUREUNSUPPORTED = 4; // 设备不支持ble
    public static final int ERRORCODE_INTERNALERROR = 5; // 内部错误
    public static final int ERRORCODE_BLESCLOSE = 6; // 蓝牙被关闭

    // 蓝牙状态
    public static final String TBBLE_STATUSCHANGED = "TBBLE_STATUSCHANGED";  // 蓝牙状态变化
    public static final String TBBLE_STATUSCHANGED_VALUE = "TBBLE_STATUSCHANGED_VALUE";

    private Context context;
    public BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;
    private IScan scaner;
    public void init(Application context){
        this.context = context;
        this.mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (android.os.Build.VERSION.SDK_INT < 21) {
            scaner = new Scan_4_3(this.mBluetoothAdapter);
        } else {
            scaner = new Scan_5_0(this.mBluetoothAdapter);
        }

        this.context.registerReceiver(mBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    // 程序关闭时，释放，以免内存泄漏
    public void release(){
        this.context.unregisterReceiver(mBroadcastReceiver);
    }

    // 获取状态信息
    public TBBLEStatus getStatues(){
        if(mBluetoothAdapter.isEnabled()){
            return TBBLEStatus.TBBLE_OPENED;
        }
        return TBBLEStatus.TBBLE_CLOSEED;
    }

    // 强行开关蓝牙(不推荐)
    public void insistEnable(boolean enable){
        if (enable) {
            mBluetoothAdapter.enable();
        } else {
            mBluetoothAdapter.disable();
        }
    }

    // 开始扫描
    private boolean scaning = false;
    private TBBLEScanMode scanMode;
    private UUID[] uuids;
    private TBBLEScanCB scanCB;
    private int workTime;
    private int sleepTime;
    private int workTimes;
    public void startScan(TBBLEScanMode scanMode, UUID[] uuids, TBBLEScanCB scanCB, int workTime, int sleepTime, int workTimes){
        if(scanCB == null){
            return;
        }
        if(getStatues() != TBBLEStatus.TBBLE_OPENED){
            scanCB.onScanFail(new TBErrorCode(ERRORCODE_BLESCLOSE, "ble closed"));
            return;
        }
        if(scaning){  // 如果已经启动扫描了
            scanCB.onScanFail(new TBErrorCode(ERRORCODE_SCANALREADYSTARTED, "already start scan"));
            return;
        }

        this.scanMode = scanMode;
        this.uuids = uuids;
        this.scanCB = scanCB;
        this.workTime = workTime;
        this.sleepTime = sleepTime;
        this.workTimes = workTimes;

        scaning = true;

        scanHandler.sendEmptyMessage(STARTSCANEVENT);
    }

    // 停止扫描
    public boolean stopScan(){
        scanHandler.removeMessages(STARTSCANEVENT);
        scanHandler.removeMessages(STOPSCANEVENT);
        scaner.stopScan();
        scaning = false;
        return true;
    }

    // 扫描接口
    private interface IScan{
        boolean startScan(TBBLEScanMode scanmode, UUID[] serviceUuids, TBBLEScanCB scanCB);
        boolean stopScan();
    }

    private static class Scan_4_3 implements IScan{
        private BluetoothAdapter mBluetoothAdapter;
        private TBBLEScanCB scanCB;
        private Handler handler = new Handler(Looper.getMainLooper());
        public Scan_4_3(BluetoothAdapter mBluetoothAdapter){
            this.mBluetoothAdapter = mBluetoothAdapter;
        }

        private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        TBBLEDevice tbbleDevice = new TBBLEDevice(device, scanRecord, rssi);
                        scanCB.onScan(tbbleDevice);
                    }
                });
            }
        };

        @Override
        public boolean startScan(TBBLEScanMode scanmode, UUID[] serviceUuids, TBBLEScanCB scanCB) {

            this.scanCB = scanCB;

            mBluetoothAdapter.startLeScan(leScanCallback);

            return true;
        }

        @Override
        public boolean stopScan() {
            mBluetoothAdapter.stopLeScan(leScanCallback);
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class Scan_5_0 implements IScan{
        private TBBLEScanCB scanCB;
        private BluetoothAdapter mBluetoothAdapter;
        private BluetoothLeScanner scaner;
        private Handler handler = new Handler(Looper.getMainLooper());
        public Scan_5_0(BluetoothAdapter mBluetoothAdapter){
            this.mBluetoothAdapter = mBluetoothAdapter;
        }
        private ScanCallback mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(final int callbackType, final ScanResult result) {
                super.onScanResult(callbackType, result);
                if (result == null) {
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        TBBLEDevice tbbleDevice = new TBBLEDevice(result.getDevice(), result.getScanRecord().getBytes(), result.getRssi());
                        scanCB.onScan(tbbleDevice);
                    }
                });
            }

            @Override
            public void onScanFailed(final int errorCode) {
                super.onScanFailed(errorCode);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        switch (errorCode) {
                            case SCAN_FAILED_ALREADY_STARTED:
                                scanCB.onScanFail(new TBErrorCode(ERRORCODE_SCANALREADYSTARTED, "already start scan"));
                                break;
                            case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                                scanCB.onScanFail(new TBErrorCode(ERRORCODE_REGISTRATIONFAILED, "unregist"));
                                break;

                            case SCAN_FAILED_FEATURE_UNSUPPORTED:
                                scanCB.onScanFail(new TBErrorCode(ERRORCODE_FEATUREUNSUPPORTED, "unsupported"));
                                break;

                            case ERRORCODE_INTERNALERROR:
                                scanCB.onScanFail(new TBErrorCode(ERRORCODE_FEATUREUNSUPPORTED, "internal error"));
                                break;

                            default:
                                scanCB.onScanFail(TBErrorCode.TBOTHER);
                                break;
                        }
                    }
                });
            }
        };
        @Override
        public boolean startScan(TBBLEScanMode scanmode, UUID[] serviceUuids, TBBLEScanCB scanCB) {
            if(!mBluetoothAdapter.isEnabled()){
                return false;
            }
            scaner = mBluetoothAdapter.getBluetoothLeScanner();
            if(scaner == null){
                return false;
            }

            this.scanCB = scanCB;
            List<ScanFilter> filters = new ArrayList<ScanFilter>();
            if (serviceUuids != null) {
                for (UUID uuid2 : serviceUuids) {
                    filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(uuid2)).build());
                }
            }

            int model;
            switch (scanmode){
                case TBBLE_LOWPOWER:
                    model = ScanSettings.SCAN_MODE_LOW_POWER;
                    break;
                case TBBLE_BLANCE:
                    model = ScanSettings.SCAN_MODE_BALANCED;
                    break;
                default:
                    model = ScanSettings.SCAN_MODE_LOW_LATENCY;
                    break;
            }

            scaner.startScan(filters, new ScanSettings.Builder().setScanMode(model).build(), mScanCallback);

            return true;
        }

        @Override
        public boolean stopScan() {
            if(scaner != null){
                scaner.stopScan(mScanCallback);
            }
            return true;
        }
    }

    // 扫描回调
    public interface TBBLEScanCB{
        void onScan(TBBLEDevice device);
        void onScanFail(TBErrorCode errorCode);
        void onScanOver();
    }

    // 蓝牙状态
    public enum TBBLEStatus{
        TBBLE_OPENED,   // 已开启
        TBBLE_CLOSEED,  // 已关闭
        TBBLE_UNKNOW
    }

    // 扫描模式
    public enum TBBLEScanMode{
        TBBLE_LOWPOWER,   // 低功耗
        TBBLE_FAST,   // 快速(耗电)
        TBBLE_BLANCE  // 均衡
    }

    // 注册广播
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)){  // 如果是蓝牙开关变化

                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON){  // 开启蓝牙
                    Intent intent1 = new Intent(TBBLE_STATUSCHANGED);
                    intent1.putExtra(TBBLE_STATUSCHANGED_VALUE, true);
                    context.sendBroadcast(intent1);
                    return;
                }

                // 关闭蓝牙
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF){
                    if(scaning){
                        stopScan();
                        if(scanCB != null){
                            scanCB.onScanFail(new TBErrorCode(ERRORCODE_BLESCLOSE, "ble closed"));
                        }
                    }
                    Intent intent1 = new Intent(TBBLE_STATUSCHANGED);
                    intent1.putExtra(TBBLE_STATUSCHANGED_VALUE, false);
                    context.sendBroadcast(intent1);
                    return;
                }
            }
        }
    };

    // 扫描定时交替
    private static final int STARTSCANEVENT = 2;
    private static final int STOPSCANEVENT = 3;
    private Handler scanHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(!scaning){  // 已经取消了扫描
                return;
            }

            if(msg.what == STARTSCANEVENT){
                scaner.startScan(scanMode, uuids, scanCB);
                scanHandler.sendEmptyMessageDelayed(STOPSCANEVENT, workTime);
                return;
            }
            if(msg.what == STOPSCANEVENT){
                scaner.stopScan();
                if(workTimes > 0){
                    workTimes --;
                }
                if(workTimes == 0){
                    scaning = false;
                    scanCB.onScanOver();
                    return;
                }
                scanHandler.sendEmptyMessageDelayed(STARTSCANEVENT, sleepTime);
                return;
            }
        }
    };

}
