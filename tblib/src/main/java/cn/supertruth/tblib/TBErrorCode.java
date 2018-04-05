package cn.supertruth.tblib;

/**
 * Created by Truth on 2017/11/13.
 */

public class TBErrorCode {

    public static final int TBSUCCESS_CODE = 0;
    public static final int TBBLECLOSE_CODE = 1;
    public static final int TBTIMEOUT_CODE = 2;
    public static final int TBDISCONNECT_CODE = 3;
    public static final int TBNOPERMISSION_CODE = 4;
    public static final int TBOTHER_CODE = -1;
    public static final TBErrorCode TBSUCCESS = new TBErrorCode(TBSUCCESS_CODE, "");
    public static final TBErrorCode TBOTHER = new TBErrorCode(TBOTHER_CODE, "");

    public static final TBErrorCode TBBLECLOSE = new TBErrorCode(TBBLECLOSE_CODE, "BLE close");
    public static final TBErrorCode TBTIMEOUT = new TBErrorCode(TBTIMEOUT_CODE, "Time out");
    public static final TBErrorCode TBDISCONNECT = new TBErrorCode(TBDISCONNECT_CODE, "Disconnect");
    public static final TBErrorCode TBNOPERMISSION = new TBErrorCode(TBNOPERMISSION_CODE, "No permission");

    public int code;
    public String msg;

    public TBErrorCode(int code, String msg){
        this.code = code;
        this.msg = msg;
    }
}
