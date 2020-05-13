package ru.olaf.custom.Api;

import android.content.Context;
import android.os.Build;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.telegram.ui.Wallet.WalletTransaction;
import org.tginfo.telegram.messenger.BuildConfig;

import java.util.Locale;

import drinkless.org.ton.TonApi;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.olaf.custom.Api.Pojo.getTransactions.Transaction;

public class Toncenter {


    private static final String USER_AGENT = String.format(Locale.US, "GramWalletAndroidApp/%s-%s", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);


    private OkHttpClient mHttpClient;
    private ToncenterMethods methods;

    public Toncenter(Context context) {
        this.mHttpClient = createHttpClient(context);
        this.methods = createRetrofit();
    }


    private OkHttpClient createHttpClient(Context context) {
        OkHttpClient.Builder mHttpClient = new OkHttpClient.Builder();
        mHttpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header("User-Agent", getUserAgent(context))
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        });

        if (getHttpLoggingInterceptor() != null)
            mHttpClient.addInterceptor(getHttpLoggingInterceptor());
        return mHttpClient.build();
    }

    private ToncenterMethods createRetrofit() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Retrofit mAuthorizationRetrofit = new Retrofit.Builder()
                .baseUrl("https://toncenter.com/api/test/v2/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(mHttpClient)
                .build();

        return mAuthorizationRetrofit.create(ToncenterMethods.class);
    }

    private HttpLoggingInterceptor getHttpLoggingInterceptor() {
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            return interceptor;
        } else {
            return null;
        }
    }

    private String getUserAgent(@Nullable Context context) {
        if (context == null)
            return USER_AGENT;

        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        String resolution = String.format(Locale.US, "%dx%d", screenHeight, screenWidth);

        return String.format(Locale.US, "%s (Android %s; SDK %d; %s; %s %s; %s; %s)",
                USER_AGENT,
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                Build.CPU_ABI,
                Build.MANUFACTURER,
                Build.MODEL,
                Locale.getDefault().getLanguage(),
                resolution);
    }

    public ToncenterMethods getMethods() {
        return methods;
    }

    public static WalletTransaction getInternalTransaction(Transaction externalTransaction) {
        TonApi.RawTransaction internalRawTransaction = new TonApi.RawTransaction();

        internalRawTransaction.utime = externalTransaction.getUtime();
        internalRawTransaction.data = externalTransaction.getData() != null ? externalTransaction.getData().getBytes() : null;

        TonApi.InternalTransactionId internalTransactionId = new TonApi.InternalTransactionId();
        internalTransactionId.hash = externalTransaction.getTransactionId().getHash() != null ? externalTransaction.getTransactionId().getHash().getBytes() : null;
        internalTransactionId.lt = externalTransaction.getTransactionId().getLt();

        internalRawTransaction.transactionId = internalTransactionId;
        internalRawTransaction.fee = externalTransaction.getFee();
        internalRawTransaction.otherFee = externalTransaction.getOtherFee();

        TonApi.RawMessage internalRawMessage = new TonApi.RawMessage();
        internalRawMessage.source = new TonApi.AccountAddress(externalTransaction.getInMsg().getSource());
        internalRawMessage.destination = new TonApi.AccountAddress(externalTransaction.getInMsg().getDestination());
        internalRawMessage.value = externalTransaction.getInMsg().getValue();
        internalRawMessage.fwdFee = externalTransaction.getInMsg().getFwdFee();
        internalRawMessage.ihrFee = externalTransaction.getInMsg().getIhrFee();
        internalRawMessage.createdLt = externalTransaction.getInMsg().getCreatedLt();
        internalRawMessage.bodyHash = externalTransaction.getInMsg().getBodyHash() != null ? externalTransaction.getInMsg().getBodyHash().getBytes() : null;
        internalRawMessage.msgData = new TonApi.MsgDataRaw(
                externalTransaction.getInMsg().getMsgData().getBody() != null ? externalTransaction.getInMsg().getMsgData().getBody().getBytes() : null,
                externalTransaction.getInMsg().getMsgData().getText() != null ? externalTransaction.getInMsg().getMsgData().getText().getBytes() : null);
        internalRawTransaction.inMsg = internalRawMessage;

        TonApi.RawMessage[] internalRawMessages= new TonApi.RawMessage[externalTransaction.getOutMsgs().size()];

        for (int i = 0; i < externalTransaction.getOutMsgs().size(); i++)
        {
            internalRawMessage = new TonApi.RawMessage();
            internalRawMessage.source = new TonApi.AccountAddress(externalTransaction.getOutMsgs().get(i).getSource());
            internalRawMessage.destination = new TonApi.AccountAddress(externalTransaction.getOutMsgs().get(i).getDestination());
            internalRawMessage.value = externalTransaction.getOutMsgs().get(i).getValue();
            internalRawMessage.fwdFee = externalTransaction.getOutMsgs().get(i).getFwdFee();
            internalRawMessage.ihrFee = externalTransaction.getOutMsgs().get(i).getIhrFee();
            internalRawMessage.createdLt = externalTransaction.getOutMsgs().get(i).getCreatedLt();
            internalRawMessage.bodyHash = externalTransaction.getOutMsgs().get(i).getBodyHash() != null ? externalTransaction.getOutMsgs().get(i).getBodyHash().getBytes() : null;
            internalRawMessage.msgData = new TonApi.MsgDataRaw(
                    externalTransaction.getOutMsgs().get(i).getMsgData().getBody() != null ? externalTransaction.getOutMsgs().get(i).getMsgData().getBody().getBytes() : null,
                    externalTransaction.getOutMsgs().get(i).getMsgData().getText() != null ? externalTransaction.getOutMsgs().get(i).getMsgData().getText().getBytes() : null);
            internalRawMessages[i] = internalRawMessage;
        }

        internalRawTransaction.outMsgs = internalRawMessages;

        return new WalletTransaction(internalRawTransaction);
    }
}
