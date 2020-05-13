
package ru.olaf.custom.Api.Pojo.getTransactions;

import java.util.List;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Transaction {

    @SerializedName("data")
    private String mData;
    @SerializedName("fee")
    private long mFee;
    @SerializedName("in_msg")
    private InMsg mInMsg;
    @SerializedName("other_fee")
    private long mOtherFee;
    @SerializedName("out_msgs")
    private List<InMsg> mOutMsgs;
    @SerializedName("storage_fee")
    private long mStorageFee;
    @SerializedName("transaction_id")
    private TransactionId mTransactionId;
    @SerializedName("@type")
    private String mType;
    @SerializedName("utime")
    private Long mUtime;

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        mData = data;
    }

    public long getFee() {
        return mFee;
    }

    public void setFee(long fee) {
        mFee = fee;
    }

    public InMsg getInMsg() {
        return mInMsg;
    }

    public void setInMsg(InMsg inMsg) {
        mInMsg = inMsg;
    }

    public long getOtherFee() {
        return mOtherFee;
    }

    public void setOtherFee(long otherFee) {
        mOtherFee = otherFee;
    }

    public List<InMsg> getOutMsgs() {
        return mOutMsgs;
    }

    public void setOutMsgs(List<InMsg> outMsgs) {
        mOutMsgs = outMsgs;
    }

    public long getStorageFee() {
        return mStorageFee;
    }

    public void setStorageFee(long storageFee) {
        mStorageFee = storageFee;
    }

    public TransactionId getTransactionId() {
        return mTransactionId;
    }

    public void setTransactionId(TransactionId transactionId) {
        mTransactionId = transactionId;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public Long getUtime() {
        return mUtime;
    }

    public void setUtime(Long utime) {
        mUtime = utime;
    }

}
