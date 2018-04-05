package cn.supertruth.tb.communicate.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.supertruth.tb.R;
import cn.supertruth.tb.communicate.MsgBean;

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
public class MsgItem extends FrameLayout {
    public MsgItem(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MsgItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MsgItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private TextView tvMsgLeft;
    private TextView tvMsgRight;
    private void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.msgitem, this);

        tvMsgLeft = (TextView) findViewById(R.id.tvMsgLeft);
        tvMsgRight = (TextView) findViewById(R.id.tvMsgRight);
    }

    public void setData(MsgBean msgBean){
        tvMsgLeft.setVisibility(View.INVISIBLE);
        tvMsgRight.setVisibility(View.INVISIBLE);
        if(msgBean.isSelf){
            tvMsgRight.setVisibility(View.VISIBLE);
            tvMsgRight.setText(msgBean.msg);
        }else{
            tvMsgLeft.setVisibility(View.VISIBLE);
            tvMsgLeft.setText(msgBean.msg);
        }
    }

}
