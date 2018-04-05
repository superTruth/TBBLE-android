package cn.supertruth.tb.scan.item;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.supertruth.tb.R;
import cn.supertruth.tblib.TBBLEDevice;

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
public class DeviceItem extends FrameLayout{
    public DeviceItem(@NonNull Context context) {
        super(context);
        init(context);
    }

    public DeviceItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DeviceItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private TextView tvName;
    private TextView tvRssi;
    private TextView tvMac;
    private void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.devicesitem, this);

        tvName = (TextView) findViewById(R.id.tvName);
        tvRssi = (TextView) findViewById(R.id.tvRssi);
        tvMac = (TextView) findViewById(R.id.tvMac);
    }

    public void setData(TBBLEDevice device){
        if(device.device.getName() == null){
            tvName.setText("No name");
        }else{
            tvName.setText(device.device.getName());
        }

        tvRssi.setText(String.valueOf(device.rssi));
        tvMac.setText(device.device.getAddress().toUpperCase());
    }
}
