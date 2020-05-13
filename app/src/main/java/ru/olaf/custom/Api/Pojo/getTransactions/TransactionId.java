
package ru.olaf.custom.Api.Pojo.getTransactions;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class TransactionId {

    @SerializedName("hash")
    private String mHash;
    @SerializedName("lt")
    private long mLt;
    @SerializedName("@type")
    private String mType;

    public String getHash() {
        return mHash;
    }

    public void setHash(String hash) {
        mHash = hash;
    }

    public long getLt() {
        return mLt;
    }

    public void setLt(long lt) {
        mLt = lt;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

}
