package com.kalsym.network;

/**
 *
 * @author zeeshan
 */
public class RequestResult {

    public int requestResultCode;
    public String responseString;
    public String[] broadcastResultCode;
    public String[] broadcastResponseString;

    /**
     *
     * @param resultCode
     * @param responseStr
     */
    public RequestResult(int resultCode, String responseStr) {
        this.requestResultCode = resultCode;
        this.responseString = responseStr;
    }

    public RequestResult() {
    }
}
