
package ru.olaf.custom.Api.Pojo.getWalletInformation;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class LastTransactionId {

    @SerializedName("hash")
    private String mHash;
    @SerializedName("lt")
    private String mLt;
    @SerializedName("@type")
    private String mType;

    public String getHash() {
        return mHash;
    }

    public void setHash(String hash) {
        mHash = hash;
    }

    public String getLt() {
        return mLt;
    }

    public void setLt(String lt) {
        mLt = lt;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

}
