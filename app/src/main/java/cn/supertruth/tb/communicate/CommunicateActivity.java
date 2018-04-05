package cn.supertruth.tb.communicate;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.supertruth.tb.R;
import cn.supertruth.tb.communicate.view.MsgItem;
import cn.supertruth.tb.help.GData;
import cn.supertruth.tb.utils.ConvertUtils;
import cn.supertruth.tblib.TBErrorCode;
import cn.supertruth.tblib.ble.TBBLEBase;
import cn.supertruth.tblib.ble.action.TBBLEBaseAction;
import cn.supertruth.tblib.ble.action.TBBLEWriteCharacterAction;

public class CommunicateActivity extends AppCompatActivity {

    private GData gData = GData.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicate);

        initView();
    }

    private TextView tvStatus;
    private ListView lvMsg;
    private EditText etInput;
    private Button btnSend;
    private void initView(){
        tvStatus = (TextView)findViewById(R.id.tvStatus);
        lvMsg = (ListView)findViewById(R.id.lvMsg);
        etInput = (EditText)findViewById(R.id.etInput);
        btnSend = (Button)findViewById(R.id.btnSend);

        btnSend.setOnClickListener(onClickListener);
        lvMsg.setAdapter(baseAdapter);

        if(gData.tbbleBase.isConnected()){
            tvStatus.setText("connected");
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!gData.tbbleBase.isConnected()){
                return;
            }
            byte[] sendData = ConvertUtils.hexString2Bytes(etInput.getText().toString());
            if((sendData == null) || (sendData.length == 0)){
                return;
            }
            MsgBean msgBean = new MsgBean();
            msgBean.isSelf = true;
            msgBean.msg = ConvertUtils.bytes2HexString(sendData, " ");
            msgs.add(msgBean);
            baseAdapter.notifyDataSetChanged();

            TBBLEWriteCharacterAction writeCharacterAction = new TBBLEWriteCharacterAction(
                    gData.workingChararcter, sendData, new TBBLEBaseAction.TBBLEBaseOption(),
                    writeCB);

            gData.tbbleBase.addAction(writeCharacterAction);
        }
    };

    private TBBLEBaseAction.TBBLEBaseActionCB writeCB = new TBBLEWriteCharacterAction.TBBLEWriteCharacterActionCB() {
        @Override
        public void onSendPerData(byte[] data) {

        }

        @Override
        public void onStart(TBBLEBaseAction action) {

        }

        @Override
        public void onSuccess(TBBLEBaseAction action) {
            System.out.println("send onSuccess");
        }

        @Override
        public void onFail(TBBLEBaseAction action, TBErrorCode errorCode) {

        }

        @Override
        public void onCancel(TBBLEBaseAction action) {

        }
    };

    private List<MsgBean> msgs = new ArrayList<>();
    private BaseAdapter baseAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return msgs.size();
        }

        @Override
        public MsgBean getItem(int position) {
            return msgs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MsgItem view;
            if(convertView == null){
                view = new MsgItem(CommunicateActivity.this);
            } else {
                view = (MsgItem) convertView;
            }
            view.setData(getItem(position));
            return view;
        }
    };

    private TBBLEBase.TBBLEBaseCB tbbleBaseCB = new TBBLEBase.TBBLEBaseCB() {
        @Override
        public void onConnect(TBBLEBase ble, TBErrorCode errorCode) {

        }

        @Override
        public void onRetryConnect(TBBLEBase ble, int remainTimes) {

        }

        @Override
        public void onDisConnect(TBBLEBase ble, TBErrorCode errorCode) {
            tvStatus.setText("disconnected");
        }

        @Override
        public void onManualDisConnect(TBBLEBase ble, TBErrorCode errorCode) {

        }

        @Override
        public void onCharactChanged(TBBLEBase ble, BluetoothGattCharacteristic characteristic, byte[] datas) {
            MsgBean msgBean = new MsgBean();
            msgBean.isSelf = false;
            msgBean.msg = ConvertUtils.bytes2HexString(datas, " ");
            msgs.add(msgBean);
            baseAdapter.notifyDataSetChanged();
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        gData.tbbleBase.setCB(tbbleBaseCB);
    }
}
