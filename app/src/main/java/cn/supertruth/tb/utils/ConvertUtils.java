package cn.supertruth.tb.utils;

import java.util.Arrays;

/***************************************************************************************************
 *                                  Copyright (C), Nexgo Inc.                                      *
 *                                    http://www.nexgo.cn                                          *
 ***************************************************************************************************
 * usage           : 
 * Version         : 1
 * Author          : Truth
 * Date            : 2017/12/8
 * Modify          : create file
 **************************************************************************************************/
public class ConvertUtils {

    private ConvertUtils(){

    }

    private static final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * byteArr -> hexString
     * <p>exp：</p>
     * bytes2HexString(new byte[] { 0, (byte) 0xa8 }, ", ") returns 00, A8
     *
     * @param bytes source data
     * @return upcase result string
     */
    public static String bytes2HexString(final byte[] bytes, final String split) {
        if ((bytes == null) || (bytes.length <= 0)) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexDigits[(bytes[i] >> 4) & 0x0f]);
            sb.append(hexDigits[bytes[i] & 0x0f]);
            if((i != (bytes.length - 1)) && (split != null)){
                sb.append(split);
            }
        }
        return sb.toString();
    }

    /**
     * byteArr -> hexString
     * <p>exp：</p>
     * bytes2HexString(new byte[] { 0, (byte) 0xa8 }) returns 00A8
     * @param bytes
     * @return
     */
    public static String bytes2HexString(final byte[] bytes) {
        return bytes2HexString(bytes, null);
    }

    /**
     * hexString -> byteArr
     * <p>exp：</p>
     * hexString2Bytes("00A8") returns { 0, (byte) 0xA8 }
     *
     * @param hexString source data
     * @return result byte array
     */
    public static byte[] hexString2Bytes(String hexString) {
        String tmp = formatHexString(hexString);
        if(tmp == null){
            return null;
        }
        int len = tmp.length();

        char[] hexBytes = tmp.toCharArray();
        byte[] ret = new byte[len >> 1];
        for (int i = 0; i < len; i += 2) {
            ret[i >> 1] = (byte) (hex2Dec(hexBytes[i]) << 4 | hex2Dec(hexBytes[i + 1]));
        }
        return ret;
    }

    /**
     * long -> hex bytes
     * <p>exp：</p>
     * long2BytesHex(1025, 3) returns {00, 04, 01}
     *
     * @param value
     * @param len return buffer len
     * @return
     */
    public static byte[] long2BytesHex(long value, int len){
        byte[] ret = new byte[len];
        Arrays.fill(ret, (byte) 0);

        long tmp = value;

        for (int i = 0; i < ret.length; i++) {
            ret[ret.length - 1 - i] = (byte) (tmp & 0xFF);
            tmp >>= 8;
            if(tmp <= 0){
                break;
            }
        }

        return ret;
    }

    /**
     * hex bytes -> long
     * <p>exp：</p>
     * bytesHex2Long({ 0x11, 0xA8, 0x23 }, 1, 2) returns 43043
     *
     * @param bytes
     * @param offset
     * @param len
     * @return
     */
    public static long bytesHex2Long(byte[] bytes, int offset, int len){
        long ret = 0;
        if((bytes == null) || (bytes.length <= 0)){
            return ret;
        }

        if((offset + len) > bytes.length){
            return ret;
        }

        for (int i = 0; i < len; i++) {
            ret <<= 8;
            ret |= (bytes[offset+i] & 0xFF);
        }

        return ret;
    }

    /**
     * long -> BCD bytes
     * <p>exp：</p>
     * long2BytesBCD(1223, 3) returns { 0x00, 0x12, 0x23 }
     *
     * @param value
     * @param len return buffer len
     * @return
     */
    public static byte[] long2BytesBCD(long value, int len){
        byte[] ret = new byte[len];
        Arrays.fill(ret, (byte) 0);

        long tmp = value;

        for (int i = 0; i < ret.length; i++) {
            ret[ret.length - 1 - i] = int2BCD((int) (tmp % 100));
            tmp /= 100;
            if(tmp <= 0){
                break;
            }
        }

        return ret;
    }

    /**
     * BCD bytes -> long
     * <p>exp：</p>
     * bytesBCD2Long({ 0x11, 0x12, 0x23 }, 1, 2) returns 1223
     *
     * @param bytes
     * @param offset
     * @param len
     * @return
     */
    public static long bytesBCD2Long(byte[] bytes, int offset, int len){
        long ret = 0;
        if((bytes == null) || (bytes.length <= 0)){
            return ret;
        }

        if((offset + len) > bytes.length){
            return ret;
        }

        for (int i = 0; i < len; i++) {
            ret *= 100;
            ret += bcd2Int(bytes[offset+i]);
        }

        return ret;
    }

    /**
     * delete character in hex string what's not hex format
     * <p>exp：</p>
     * formatHexString("0x00, 0xA8") returns "00A8"
     *
     * @param hexString
     * @return hex string
     */
    public static String formatHexString(final String hexString){
        if((hexString == null) || (hexString.length() <= 0)){
            return null;
        }
        String ret = hexString.toUpperCase();

        // check source if format is current, so when the format is correct, it's can save much time
        char[] retChars = ret.toCharArray();
        boolean formatCorrect = true;
        for (char c : retChars) {
            if(((c >= '0') && (c <= '9')) || ((c >= 'A') && (c <= 'F'))){
                continue;
            }
            formatCorrect = false;
            break;
        }
        if(formatCorrect){
            if(ret.length() % 2 != 0){
                return "0"+ret;
            }
            return ret;
        }

        ret = ret.replaceAll("0X", "");  // delete "0x"
        retChars = ret.toCharArray();

        StringBuilder sb = new StringBuilder();
        for (char c : retChars) {
            if(((c >= '0') && (c <= '9')) || ((c >= 'A') && (c <= 'F'))){
                sb.append(c);
                continue;
            }
        }
        if(sb.length() % 2 != 0){
            sb.insert(0, '0');
        }
        return sb.toString();
    }

    /**
     * delete character in hex string what's not hex format
     * <p>exp：</p>
     * formatNumString("12:5 8 ,78") returns "125878"
     * @param str
     * @return
     */
    public static String formatNumString(final String str){
        if((str == null) || (str.length() <= 0)){
            return null;
        }

        // check source if format is current, so when the format is correct, it's can save much time
        char[] retChars = str.toCharArray();
        boolean formatCorrect = true;
        for (char c : retChars) {
            if((c >= '0') && (c <= '9')){
                continue;
            }
            formatCorrect = false;
            break;
        }
        if(formatCorrect){
            return str;
        }

        StringBuilder sb = new StringBuilder();
        for (char c : retChars) {
            if((c >= '0') && (c <= '9')){
                sb.append(c);
                continue;
            }
        }
        return sb.toString();
    }

    /**
     * hexChar -> int
     *
     * @param hexChar hex
     * @return 0..15
     */
    private static int hex2Dec(final char hexChar) {
        if (hexChar >= '0' && hexChar <= '9') {
            return hexChar - '0';
        } else if (hexChar >= 'A' && hexChar <= 'F') {
            return hexChar - 'A' + 10;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * BCD byte -> int
     *
     * @param b bcd data
     * @return 0..99
     */
    private static int bcd2Int(final byte b) {
        int tmpb = (b & 0xFF);

        return tmpb/16*10+tmpb%16;
    }
    /**
     * BCD int -> BCD
     *
     * @param value bcd data
     * @return 0..99
     */
    private static byte int2BCD(final int value) {
        if(value > 99){
            return 0;
        }
        return (byte)(value/10*16+value%10);
    }

}
