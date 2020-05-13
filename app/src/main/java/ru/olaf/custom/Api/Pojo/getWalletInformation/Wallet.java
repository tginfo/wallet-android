
package ru.olaf.custom.Api.Pojo.getWalletInformation;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Wallet {

    @SerializedName("account_state")
    private String mAccountState;
    @SerializedName("balance")
    private long mBalance;
    @SerializedName("last_transaction_id")
    private LastTransactionId mLastTransactionId;
    @SerializedName("seqno")
    private Object mSeqno;
    @SerializedName("wallet")
    private Boolean mWallet;
    @SerializedName("wallet_type")
    private Object mWalletType;

    public String getAccountState() {
        return mAccountState;
    }

    public void setAccountState(String accountState) {
        mAccountState = accountState;
    }

    public long getBalance() {
        return mBalance;
    }

    public void setBalance(long balance) {
        mBalance = balance;
    }

    public LastTransactionId getLastTransactionId() {
        return mLastTransactionId;
    }

    public void setLastTransactionId(LastTransactionId lastTransactionId) {
        mLastTransactionId = lastTransactionId;
    }

    public Object getSeqno() {
        return mSeqno;
    }

    public void setSeqno(Object seqno) {
        mSeqno = seqno;
    }

    public Boolean getWallet() {
        return mWallet;
    }

    public void setWallet(Boolean wallet) {
        mWallet = wallet;
    }

    public Object getWalletType() {
        return mWalletType;
    }

    public void setWalletType(Object walletType) {
        mWalletType = walletType;
    }

}
