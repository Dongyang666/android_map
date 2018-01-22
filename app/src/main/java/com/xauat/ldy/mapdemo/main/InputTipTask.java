package com.xauat.ldy.mapdemo.main;

/**
 * Created by liu dongyang on 2017/6/24.
 */

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.xauat.ldy.mapdemo.R;

import java.util.ArrayList;
import java.util.List;
/*
该类用来实现搜索数据提示功能
 */
public class InputTipTask implements Inputtips.InputtipsListener {

    private static InputTipTask mInstance;
    private Inputtips mInputTips;
    private Context mContext;
    private AutoCompleteTextView et;

    private InputTipTask(Context context) {
        this.mContext = context;
    }

    //单例模式
    public static InputTipTask getInstance(Context context) {
        if (mInstance == null) {
            synchronized (InputTipTask.class) {
                if (mInstance == null) {
                    mInstance = new InputTipTask(context);
                }
            }
        }
        return mInstance;
    }

    public InputTipTask setAdapter(AutoCompleteTextView et) {
        this.et = et;
        return this;
    }


    public void searchTips(String keyWord, String city) {
        //第二个参数默认代表全国，也可以为城市区号
        InputtipsQuery inputquery = new InputtipsQuery(keyWord,"");
        mInputTips = new Inputtips(mContext, inputquery);
        mInputTips.setInputtipsListener(this);
        mInputTips.requestInputtipsAsyn();
    }

    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {

        if (rCode == 1000) {
           // Toast.makeText(mContext, "121",Toast.LENGTH_SHORT).show();
            List<String> listString = new ArrayList<String>();
            for (int i = 0; i < tipList.size(); i++) {
                listString.add( tipList.get(i).getName());
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext, R.layout.main_listview_item,listString);
            et.setAdapter(arrayAdapter);
            arrayAdapter.notifyDataSetChanged();
        }
    }
}