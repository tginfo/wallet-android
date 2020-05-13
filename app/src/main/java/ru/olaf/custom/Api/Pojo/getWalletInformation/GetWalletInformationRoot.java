
package ru.olaf.custom.Api.Pojo.getWalletInformation;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class GetWalletInformationRoot {

    @SerializedName("ok")
    private Boolean mOk;
    @SerializedName("result")
    private Wallet mWallet;

    public Boolean getOk() {
        return mOk;
    }

    public void setOk(Boolean ok) {
        mOk = ok;
    }

    public Wallet getResult() {
        return mWallet;
    }

    public void setResult(Wallet wallet) {
        mWallet = wallet;
    }

}
