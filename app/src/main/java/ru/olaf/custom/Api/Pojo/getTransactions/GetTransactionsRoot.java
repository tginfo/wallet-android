
package ru.olaf.custom.Api.Pojo.getTransactions;

import java.util.List;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class GetTransactionsRoot {

    @SerializedName("ok")
    private Boolean mOk;
    @SerializedName("result")
    private List<Transaction> mTransaction;

    public Boolean getOk() {
        return mOk;
    }

    public void setOk(Boolean ok) {
        mOk = ok;
    }

    public List<Transaction> getTransactions() {
        return mTransaction;
    }

    public void setResult(List<Transaction> transaction) {
        mTransaction = transaction;
    }

}
