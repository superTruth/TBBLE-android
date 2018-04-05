package cn.supertruth.tb.connect.item;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.supertruth.tb.R;

/***************************************************************************************************
 *                                  Copyright (C), Nexgo Inc.                                      *
 *                                    http://www.nexgo.cn                                          *
 ***************************************************************************************************
 * usage           : 
 * Version         : 1
 * Author          : Truth
 * Date            : 2017/12/27
 * Modify          : create file
 **************************************************************************************************/
public class ServiceItem extends FrameLayout{


    public ServiceItem(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ServiceItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ServiceItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private TextView tvUuid;
    private void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.serviceitem, this);

        tvUuid = (TextView) findViewById(R.id.tvUuid);
    }

    public void setData(String uuid){
        tvUuid.setText(uuid);
    }
}
