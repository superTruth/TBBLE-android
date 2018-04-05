package cn.supertruth.tb.connect.item;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
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
public class CharacterItem extends LinearLayout {
    public CharacterItem(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CharacterItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CharacterItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private TextView tvUuid;
    private TextView tvPerhaps;
    private Switch btnNotify;
    private void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.characteritem, this);

        tvUuid = (TextView) findViewById(R.id.tvUuid);
        tvPerhaps = (TextView) findViewById(R.id.tvPerhaps);
        btnNotify = (Switch) findViewById(R.id.btnNotify);

        btnNotify.setOnClickListener(onClickListener);
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(onCheckListener != null){
                onCheckListener.onCheckChange(CharacterItem.this, groundID, childID, btnNotify.isChecked());
            }
        }
    };

    public void setData(String uuid, String perhaps){
        tvUuid.setText(uuid);
        tvPerhaps.setText(perhaps);
    }

    private int groundID;
    private int childID;
    public void setID(int groundID, int childID){
        this.groundID = groundID;
        this.childID = childID;
    }

    public int getGrounpID(){
        return this.groundID;
    }

    public int getChildID(){
        return this.childID;
    }

    public void checkEn(boolean visable){
        if(visable){
            btnNotify.setVisibility(View.VISIBLE);
        } else {
            btnNotify.setVisibility(View.GONE);
        }
    }

    private OnCheckListener onCheckListener;
    public void setOnCheckListener(OnCheckListener onCheckListener){
        this.onCheckListener = onCheckListener;
    }


    public interface OnCheckListener{
        void onCheckChange(CharacterItem view, int grounpId, int childID, boolean enable);
    }
}
