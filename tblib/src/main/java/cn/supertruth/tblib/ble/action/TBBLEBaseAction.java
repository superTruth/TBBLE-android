package cn.supertruth.tblib.ble.action;

import cn.supertruth.tblib.TBErrorCode;

/**
 * Created by Truth on 2017/11/15.
 */

public class TBBLEBaseAction {

    public TBBaseActionType actionType;  // 类型
    public TBBaseActionStatus status = TBBaseActionStatus.TBBLEACTION_WAITTING;    // 状态
    public TBBLEBaseOption option;       // 参数
    public TBBLEBaseActionCB cb;         // 回调

    public TBBLEBaseAction(TBBaseActionType actionType, TBBLEBaseOption option, TBBLEBaseActionCB cb){
        this.actionType = actionType;
        this.option = option;
        this.cb = cb;
    }

    // 回调
    public interface TBBLEBaseActionCB{
        void onStart(TBBLEBaseAction action);
        void onSuccess(TBBLEBaseAction action);
        void onFail(TBBLEBaseAction action, TBErrorCode errorCode);
        void onCancel(TBBLEBaseAction action);
    }

    // 任务参数
    public static class TBBLEBaseOption{
        public int timeout = 20;         // 超时时间(单位ms)，默认20
        public int perLen = 20;          // 每条数据的长度默认20
        public int waitTime = 20;        // 无响应写入方式，等待时间(单位ms)，默认20
    }

    // 动作类型
    public enum TBBaseActionType{
        TBBLEACTION_WRITECHACT,        // 写特征值
        TBBLEACTION_READCHACT,         // 读特征值
        TBBLEACTION_READRSSI,          // 读Rssi
        TBBLEACTION_ENCHARACTERNOTIFY  // 使能特征值监听
    }

    // 任务状态
    public enum TBBaseActionStatus{
        TBBLEACTION_WAITTING,    // 等待运行
        TBBLEACTION_RUNNING,         // 正在运行
        TBBLEACTION_DONE,            // 完成
        TBBLEACTION_FAIL,            // 失败
        TBBLEACTION_CANCEL,          // 被取消
    }
}
