package cn.supertruth.tb;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import cn.supertruth.tb.scan.ScanActivity;
import cn.supertruth.tblib.TBBLEManager;

public class MainActivity extends AppCompatActivity {

    private TBBLEManager tbbleManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermision();
        }else{
            requestEnableBLE();
        }
    }

    private static final int PERMISSION_REQUEST = 1;
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermision(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_SMS}, PERMISSION_REQUEST);
            }else{
                requestEnableBLE();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        requestEnableBLE();
    }

    // 请求开启蓝牙
    private final int REQUESTBLE_OPEN = 5;
    private void requestEnableBLE(){
        tbbleManager = TBBLEManager.getInstance();
        tbbleManager.init(getApplication());
        if(tbbleManager.getStatues() != TBBLEManager.TBBLEStatus.TBBLE_OPENED){
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUESTBLE_OPEN);
            return;
        }
        startProcess();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            startProcess();
            return;
        }
    }

    private void startProcess(){
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);

        finish();
    }

}
