package ru.olaf.custom.Api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.olaf.custom.Api.Pojo.getTransactions.GetTransactionsRoot;
import ru.olaf.custom.Api.Pojo.getWalletInformation.GetWalletInformationRoot;

public interface ToncenterMethods {

    @GET("getTransactions")
    Call<GetTransactionsRoot> getTransactions(@Query("address") String address);

    @GET("getWalletInformation")
    Call<GetWalletInformationRoot> getWalletInformation(@Query("address") String address);
}