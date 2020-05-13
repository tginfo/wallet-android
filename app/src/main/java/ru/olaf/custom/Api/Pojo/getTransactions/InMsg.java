
package ru.olaf.custom.Api.Pojo.getTransactions;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class InMsg {

    @SerializedName("body_hash")
    private String mBodyHash;
    @SerializedName("created_lt")
    private long mCreatedLt;
    @SerializedName("destination")
    private String mDestination;
    @SerializedName("fwd_fee")
    private long mFwdFee;
    @SerializedName("ihr_fee")
    private long mIhrFee;
    @SerializedName("message")
    private String mMessage;
    @SerializedName("msg_data")
    private MsgData mMsgData;
    @SerializedName("source")
    private String mSource;
    @SerializedName("@type")
    private String mType;
    @SerializedName("value")
    private long mValue;

    public String getBodyHash() {
        return mBodyHash;
    }

    public void setBodyHash(String bodyHash) {
        mBodyHash = bodyHash;
    }

    public long getCreatedLt() {
        return mCreatedLt;
    }

    public void setCreatedLt(long createdLt) {
        mCreatedLt = createdLt;
    }

    public String getDestination() {
        return mDestination;
    }

    public void setDestination(String destination) {
        mDestination = destination;
    }

    public long getFwdFee() {
        return mFwdFee;
    }

    public void setFwdFee(long fwdFee) {
        mFwdFee = fwdFee;
    }

    public long getIhrFee() {
        return mIhrFee;
    }

    public void setIhrFee(long ihrFee) {
        mIhrFee = ihrFee;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public MsgData getMsgData() {
        return mMsgData;
    }

    public void setMsgData(MsgData msgData) {
        mMsgData = msgData;
    }

    public String getSource() {
        return mSource;
    }

    public void setSource(String source) {
        mSource = source;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public long getValue() {
        return mValue;
    }

    public void setValue(long value) {
        mValue = value;
    }

}
