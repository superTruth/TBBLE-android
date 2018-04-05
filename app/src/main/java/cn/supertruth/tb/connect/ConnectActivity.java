package cn.supertruth.tb.connect;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.QMUITopBar;

import java.util.List;

import cn.supertruth.tb.R;
import cn.supertruth.tb.communicate.CommunicateActivity;
import cn.supertruth.tb.connect.item.CharacterItem;
import cn.supertruth.tb.connect.item.ServiceItem;
import cn.supertruth.tb.help.GData;
import cn.supertruth.tblib.TBBLEDevice;
import cn.supertruth.tblib.TBBLEManager;
import cn.supertruth.tblib.TBErrorCode;
import cn.supertruth.tblib.ble.TBBLEBase;
import cn.supertruth.tblib.ble.action.TBBLEBaseAction;
import cn.supertruth.tblib.ble.action.TBBLEEnNotifyAction;

public class ConnectActivity extends AppCompatActivity {
    private List<BluetoothGattService> services;
    private GData gData = GData.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        gData.tbbleBase = new TBBLEBase(TBBLEManager.getInstance(), this);

        initData();
        initView();

        startConnect();
    }

    private TBBLEDevice device;
    private void initData(){
        device = GData.getInstance().workDevice;
    }

    private QMUITopBar topbar;
    private ExpandableListView exlv;
    private void initView(){
        topbar = (QMUITopBar) findViewById(R.id.topbar);
        exlv = (ExpandableListView) findViewById(R.id.exlv);
        if(device.device.getName() == null){
            topbar.setTitle("No name");
        }else{
            topbar.setTitle(device.device.getName());
        }

        exlv.setOnChildClickListener(childClickListener);

    }

    private ExpandableListAdapter expandableListAdapter = new ExpandableListAdapter() {
        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public int getGroupCount() {
            if(services == null){
                return 0;
            }
            return services.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return services.get(groupPosition).getCharacteristics().size();
        }

        @Override
        public BluetoothGattService getGroup(int groupPosition) {
            return services.get(groupPosition);
        }

        @Override
        public BluetoothGattCharacteristic getChild(int groupPosition, int childPosition) {
            return services.get(groupPosition).getCharacteristics().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ServiceItem view;
            if(convertView == null){
                view = new ServiceItem(ConnectActivity.this);
            } else {
                view = (ServiceItem) convertView;
            }

            view.setData(getGroup(groupPosition).getUuid().toString());

            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            CharacterItem view;
            if(convertView == null){
                view = new CharacterItem(ConnectActivity.this);
            } else {
                view = (CharacterItem) convertView;
            }

            BluetoothGattCharacteristic characteristic = getChild(groupPosition, childPosition);

            if(characteristic == null){
                return null;
            }

            view.setData(characteristic.getUuid().toString(), properties2Str(characteristic.getProperties()));

            view.checkEn(canNotify(characteristic.getProperties()));
            view.setID(groupPosition, childPosition);
            view.setOnCheckListener(onCheckListener);

            return view;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void onGroupExpanded(int groupPosition) {

        }

        @Override
        public void onGroupCollapsed(int groupPosition) {

        }

        @Override
        public long getCombinedChildId(long groupId, long childId) {
            return 0;
        }

        @Override
        public long getCombinedGroupId(long groupId) {
            return 0;
        }
    };

    private ProgressDialog pd;
    private void startConnect(){
        pd = ProgressDialog.show(this, "info", "connectting", true, false);
        gData.tbbleBase.connect(device, 10000, 3, connectCB);
    }
    private TBBLEBase.TBBLEBaseCB connectCB = new TBBLEBase.TBBLEBaseCB() {
        @Override
        public void onConnect(TBBLEBase ble, TBErrorCode errorCode) {
            if(errorCode.code != TBErrorCode.TBSUCCESS_CODE){
                Toast.makeText(getApplicationContext(), "connect fail", Toast.LENGTH_SHORT).show();
                pd.dismiss();
                finish();
                return;
            }
            System.out.println("onConnect->"+errorCode.code);
            pd.dismiss();

            services = ble.getAllServices();

            exlv.setAdapter(expandableListAdapter);
//            expandableListAdapter.
        }

        @Override
        public void onRetryConnect(TBBLEBase ble, int remainTimes) {
            System.out.println("onRetryConnect->"+remainTimes);
        }

        @Override
        public void onDisConnect(TBBLEBase ble, TBErrorCode errorCode) {
            System.out.println("onDisConnect->"+errorCode.msg);
        }

        @Override
        public void onManualDisConnect(TBBLEBase ble, TBErrorCode errorCode) {
            System.out.println("onManualDisConnect->"+errorCode.msg);
        }

        @Override
        public void onCharactChanged(TBBLEBase ble, BluetoothGattCharacteristic characteristic, byte[] datas) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(gData.tbbleBase.isConnected()){
            gData.tbbleBase.disConnect();
        }
    }

    private String properties2Str(int properties){
        StringBuilder sb = new StringBuilder();
        sb.append("("+properties+")");


        if((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0){
            sb.append("notify,");
        }

        if((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0){
            sb.append("indicate,");
        }

        if((properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0){
            sb.append("read,");
        }

        if((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0){
            sb.append("write without response,");
        }

        if((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0){
            sb.append("write,");
        }

        return sb.toString();
    }

    private boolean canNotify(int properties){
        if((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0){
            return true;
        }

        if((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0){
            return true;
        }
        return false;
    }

    private ExpandableListView.OnChildClickListener childClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            System.out.println("onChildClick->"+groupPosition+","+childPosition);
            gData.workingChararcter = gData.tbbleBase.getAllServices().get(groupPosition).getCharacteristics().get(childPosition);
            Intent intent = new Intent(ConnectActivity.this, CommunicateActivity.class);
            startActivity(intent);
            return false;
        }
    };

    private CharacterItem.OnCheckListener onCheckListener = new CharacterItem.OnCheckListener() {
        @Override
        public void onCheckChange(CharacterItem view, int grounpId, int childID, boolean enable) {
            System.out.println("OnCheckListener->"+grounpId+","+childID+","+enable);

            BluetoothGattCharacteristic chararcter = gData.tbbleBase.getAllServices().get(grounpId).getCharacteristics().get(childID);
            TBBLEEnNotifyAction notifyAction = new TBBLEEnNotifyAction(chararcter, enable, new TBBLEBaseAction.TBBLEBaseOption(),
                    new TBBLEBaseAction.TBBLEBaseActionCB(){

                        @Override
                        public void onStart(TBBLEBaseAction action) {

                        }

                        @Override
                        public void onSuccess(TBBLEBaseAction action) {
                            System.out.println("enable success");
                        }

                        @Override
                        public void onFail(TBBLEBaseAction action, TBErrorCode errorCode) {
                            System.out.println("enable fail");
                        }

                        @Override
                        public void onCancel(TBBLEBaseAction action) {

                        }
                    });
            gData.tbbleBase.addAction(notifyAction);

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        gData.tbbleBase.setCB(connectCB);
    }
}
