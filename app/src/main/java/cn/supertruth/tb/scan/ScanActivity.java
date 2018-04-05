package cn.supertruth.tb.scan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.qmuiteam.qmui.widget.QMUITopBar;

import java.util.ArrayList;
import java.util.List;

import cn.supertruth.tb.R;
import cn.supertruth.tb.connect.ConnectActivity;
import cn.supertruth.tb.help.GData;
import cn.supertruth.tb.scan.item.DeviceItem;
import cn.supertruth.tblib.TBBLEDevice;
import cn.supertruth.tblib.TBBLEManager;
import cn.supertruth.tblib.TBErrorCode;

public class ScanActivity extends AppCompatActivity {

    private TBBLEManager tbbleManager;

    private final int REFRESHLISTVIEW = 5;
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == REFRESHLISTVIEW){
                listAdapter.notifyDataSetChanged();
                topbar.setTitle(String.valueOf(devices.size()));
                return;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        tbbleManager = TBBLEManager.getInstance();

        initView();

        startScanDevices();
    }

    private QMUITopBar topbar;
    private ListView lvDevices;
    private SwipeRefreshLayout sw;
    private void initView(){
        topbar = (QMUITopBar)findViewById(R.id.topbar);
        lvDevices = (ListView)findViewById(R.id.lvDevices);
        sw = (SwipeRefreshLayout) findViewById(R.id.sw);

        sw.setOnRefreshListener(refreshListener);
        topbar.setTitle("0");

        lvDevices.setAdapter(listAdapter);

        lvDevices.setOnItemClickListener(itemClickListener);
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            tbbleManager.stopScan();

            GData.getInstance().workDevice = devices.get(position);
            Intent intent = new Intent(ScanActivity.this, ConnectActivity.class);
            startActivity(intent);
        }
    };

    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            tbbleManager.stopScan();
            devices.clear();
            listAdapter.notifyDataSetChanged();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sw.setRefreshing(false);
                    startScanDevices();
                }
            }, 1000);

        }
    };

    private void startScanDevices(){
        tbbleManager.startScan(TBBLEManager.TBBLEScanMode.TBBLE_FAST, null, scanCB, 10000, 1000, -1);
    }

    private TBBLEManager.TBBLEScanCB scanCB = new TBBLEManager.TBBLEScanCB() {
        @Override
        public void onScan(TBBLEDevice device) {
            System.out.println("onScan->"+device.device.getName());
            for (TBBLEDevice tmpDevice : devices){  // find and refresh device info
                if(tmpDevice.device.getAddress().toUpperCase().equals(device.device.getAddress().toUpperCase())){
                    tmpDevice.refreshInfo(device);
                    handler.removeMessages(REFRESHLISTVIEW);
                    handler.sendEmptyMessage(REFRESHLISTVIEW);
                    return;
                }
            }

            devices.add(device);  // add new device
            handler.removeMessages(REFRESHLISTVIEW);
            handler.sendEmptyMessage(REFRESHLISTVIEW);
        }

        @Override
        public void onScanFail(TBErrorCode errorCode) {

        }

        @Override
        public void onScanOver() {

        }
    };

    private List<TBBLEDevice> devices = new ArrayList<>();
    private BaseAdapter listAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public TBBLEDevice getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DeviceItem view;
            if(convertView == null){
                view = new DeviceItem(ScanActivity.this);
            }else{
                view = (DeviceItem)convertView;
            }

            view.setData(getItem(position));

            return view;
        }
    };
}
