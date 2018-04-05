package cn.supertruth.tblib.ble.action;

/**
 * Created by Truth on 2017/11/15.
 */

public class TBBLEReadRssiAction extends TBBLEBaseAction{
    public TBBLEReadRssiAction(TBBLEBaseOption option, TBBLEBaseActionCB cb) {
        super(TBBaseActionType.TBBLEACTION_READRSSI, option, cb);
    }

    public interface TBBLEReadRssiActionCB extends TBBLEBaseAction.TBBLEBaseActionCB{
        void onReadRssi(TBBLEBaseAction action, int rssi);
    }
}
