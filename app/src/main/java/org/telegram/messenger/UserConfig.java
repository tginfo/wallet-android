/*
 * This is the source code of Wallet for Android v. 1.0.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright Nikolai Kudashov, 2019-2020.
 */

package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.ColorInt;


import com.google.android.gms.common.util.ArrayUtils;

import org.tginfo.telegram.messenger.R;

import java.util.Arrays;

public class UserConfig extends BaseController {

    public static int selectedAccount;
    public final static int MAX_ACCOUNT_COUNT = 3;

    private final Object sync = new Object();
    private boolean configLoaded;

    public String tonEncryptedData;
    public String tonPublicKey;
    public String tonAccountAddress;
    public int tonPasscodeType = -1;
    public byte[] tonPasscodeSalt;
    public long tonPasscodeRetryInMs;
    public long tonLastUptimeMillis;
    public int tonBadPasscodeTries;
    public String tonKeyName;
    public boolean tonCreationFinished;
    public int tonWalletVersion;

    private String[] walletConfig = new String[4];
    private String[] walletBlockchainName = new String[4];
    private String[] walletConfigUrl = new String[4];
    private String[] walletConfigFromUrl = new String[4];
    private int[] walletConfigType = new int[4];

    private int currentNetworkType;
    private String currentAccountName;
    @ColorInt
    private int currentAccountColor;


    public static final int NETWORK_TYPE_TEST = 0;
    public static final int NETWORK_TYPE_FREETON = 1;
    public static final int NETWORK_TYPE_TON_COMMUNITY = 2;


    private static volatile UserConfig[] Instance = new UserConfig[UserConfig.MAX_ACCOUNT_COUNT];

    public static UserConfig getInstance(int num) {
        UserConfig localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (UserConfig.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new UserConfig(num);
                }
            }
        }
        return localInstance;
    }

    public UserConfig(int instance) {
        super(instance);
    }


    public void saveConfig(boolean all) {
        synchronized (sync) {
            try {
                SharedPreferences.Editor editor = getPreferences().edit();
                if (tonEncryptedData != null) {
                    editor.putString("tonEncryptedData", tonEncryptedData);
                    editor.putString("tonPublicKey", tonPublicKey);
                    if (tonAccountAddress != null) {
                        editor.putString("tonAccountAddress", tonAccountAddress);
                    }
                    editor.putString("tonKeyName", tonKeyName);
                    editor.putBoolean("tonCreationFinished", tonCreationFinished);
                    editor.putInt("tonWalletVersion", tonWalletVersion);
                    if (tonPasscodeSalt != null) {
                        editor.putInt("tonPasscodeType", tonPasscodeType);
                        editor.putString("tonPasscodeSalt", Base64.encodeToString(tonPasscodeSalt, Base64.DEFAULT));
                        editor.putLong("tonPasscodeRetryInMs", tonPasscodeRetryInMs);
                        editor.putLong("tonLastUptimeMillis", tonLastUptimeMillis);
                        editor.putInt("tonBadPasscodeTries", tonBadPasscodeTries);
                    }
                } else {
                    editor.remove("tonEncryptedData").remove("tonPublicKey").remove("tonKeyName").remove("tonPasscodeType").remove("tonPasscodeSalt").remove("tonPasscodeRetryInMs").remove("tonBadPasscodeTries").remove("tonLastUptimeMillis").remove("tonCreationFinished");
                }
                editor.putString("walletConfig", walletConfig[NETWORK_TYPE_TEST]);
                editor.putString("walletConfigUrl", walletConfigUrl[NETWORK_TYPE_TEST]);
                editor.putInt("walletConfigType", walletConfigType[NETWORK_TYPE_TEST]);
                editor.putString("walletBlockchainName", walletBlockchainName[NETWORK_TYPE_TEST]);
                editor.putString("walletConfigFromUrl", walletConfigFromUrl[NETWORK_TYPE_TEST]);

                editor.putString("walletConfigFreeton", walletConfig[NETWORK_TYPE_FREETON]);
                editor.putString("walletConfigUrlFreeton", walletConfigUrl[NETWORK_TYPE_FREETON]);
                editor.putInt("walletConfigTypeFreeton", walletConfigType[NETWORK_TYPE_FREETON]);
                editor.putString("walletBlockchainNameFreeton", walletBlockchainName[NETWORK_TYPE_FREETON]);
                editor.putString("walletConfigFromUrlFreeton", walletConfigFromUrl[NETWORK_TYPE_FREETON]);


                editor.putString("walletConfigTonCommunity", walletConfig[NETWORK_TYPE_TON_COMMUNITY]);
                editor.putString("walletConfigUrlTonCommunity", walletConfigUrl[NETWORK_TYPE_TON_COMMUNITY]);
                editor.putInt("walletConfigTypeTonCommunity", walletConfigType[NETWORK_TYPE_TON_COMMUNITY]);
                editor.putString("walletBlockchainNameTonCommunity", walletBlockchainName[NETWORK_TYPE_TON_COMMUNITY]);
                editor.putString("walletConfigFromUrlTonCommunity", walletConfigFromUrl[NETWORK_TYPE_TON_COMMUNITY]);


                editor.putInt("walletCurrentNetworkType", currentNetworkType);
                editor.putString("walletCurrentAccountName", currentAccountName);
                editor.putInt("walletCurrentAccountColor", currentAccountColor);

                editor.commit();
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public void loadConfigForce() {
        configLoaded = false;
        loadConfig();
    }

    public void loadConfig() {
        synchronized (sync) {
            if (configLoaded) {
                return;
            }
            SharedPreferences preferences = getPreferences();
            tonEncryptedData = preferences.getString("tonEncryptedData", null);
            tonPublicKey = preferences.getString("tonPublicKey", null);
            tonAccountAddress = preferences.getString("tonAccountAddress", null);
            tonKeyName = preferences.getString("tonKeyName", "walletKey");
            tonCreationFinished = preferences.getBoolean("tonCreationFinished", true);
            tonWalletVersion = preferences.getInt("tonWalletVersion", 0);
            String salt = preferences.getString("tonPasscodeSalt", null);
            if (salt != null) {
                try {
                    tonPasscodeSalt = Base64.decode(salt, Base64.DEFAULT);
                    tonPasscodeType = preferences.getInt("tonPasscodeType", -1);
                    tonPasscodeRetryInMs = preferences.getLong("tonPasscodeRetryInMs", 0);
                    tonLastUptimeMillis = preferences.getLong("tonLastUptimeMillis", 0);
                    tonBadPasscodeTries = preferences.getInt("tonBadPasscodeTries", 0);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            walletConfig[NETWORK_TYPE_TEST] = preferences.getString("walletConfig", "");
            walletConfigUrl[NETWORK_TYPE_TEST] = preferences.getString("walletConfigUrl", "https://test.ton.org/ton-lite-client-test1.config.json");
            walletConfigType[NETWORK_TYPE_TEST] = preferences.getInt("walletConfigType", TonController.CONFIG_TYPE_URL);
            walletBlockchainName[NETWORK_TYPE_TEST] = preferences.getString("walletBlockchainName", "");
            walletConfigFromUrl[NETWORK_TYPE_TEST] = preferences.getString("walletConfigFromUrl", "");


            walletConfig[NETWORK_TYPE_FREETON] = preferences.getString("walletConfigFreeton", "");
            walletConfigUrl[NETWORK_TYPE_FREETON] = preferences.getString("walletConfigUrlFreeton", "https://raw.githubusercontent.com/tonlabs/main.ton.dev/master/configs/ton-lite-client.config.json");
            walletConfigType[NETWORK_TYPE_FREETON] = preferences.getInt("walletConfigTypeFreeton", TonController.CONFIG_TYPE_URL);
            walletBlockchainName[NETWORK_TYPE_FREETON] = preferences.getString("walletBlockchainNameFreeton", "");
            walletConfigFromUrl[NETWORK_TYPE_FREETON] = preferences.getString("walletConfigFromUrlFreeton", "");


            walletConfig[NETWORK_TYPE_TON_COMMUNITY] = preferences.getString("walletConfigTonCommunity", "");
            walletConfigUrl[NETWORK_TYPE_TON_COMMUNITY] = preferences.getString("walletConfigUrlTonCommunity", "https://toncommunity.org/ton-lite-client-test3.config.json");
            walletConfigType[NETWORK_TYPE_TON_COMMUNITY] = preferences.getInt("walletConfigTypeTonCommunity", TonController.CONFIG_TYPE_URL);
            walletBlockchainName[NETWORK_TYPE_TON_COMMUNITY] = preferences.getString("walletBlockchainNameTonCommunity", "testnet3");
            walletConfigFromUrl[NETWORK_TYPE_TON_COMMUNITY] = preferences.getString("walletConfigFromUrlTonCommunity", "");

            currentNetworkType = preferences.getInt("walletCurrentNetworkType", NETWORK_TYPE_FREETON);
            currentAccountName = preferences.getString("walletCurrentAccountName",
                    String.format(LocaleController.getString("WalletDefaultAccountName", R.string.WalletDefaultAccountName), currentAccount));
            currentAccountColor = preferences.getInt("walletCurrentAccountColor", R.color.WalletDefaultAccountColor);


            configLoaded = true;
        }
    }


    public String getWalletConfig() {
        return walletConfig[currentNetworkType];
    }

    public String getWalletConfig(int type) {
        return walletConfig[type];
    }

    public void setWalletConfig(int type, String config) {
        walletConfig[type] = config;
    }

    public String getWalletConfigUrl() {
        return walletConfigUrl[currentNetworkType];
    }

    public String getWalletConfigUrl(int type) {
        return walletConfigUrl[type];
    }

    public void setWalletConfigUrl(int type, String url) {
        walletConfigUrl[type] = url;
    }

    public int getWalletConfigType() {
        return walletConfigType[currentNetworkType];
    }

    public int getWalletConfigType(int type) {
        return walletConfigType[type];
    }

    public void setWalletConfigType(int type, int value) {
        walletConfigType[type] = value;
    }

    public String getWalletBlockchainName() {
        return walletBlockchainName[currentNetworkType];
    }

    public String getWalletBlockchainName(int type) {
        return walletBlockchainName[type];
    }

    public void setWalletBlockchainName(int type, String name) {
        walletBlockchainName[type] = name;
    }

    public String getWalletConfigFromUrl() {
        return walletConfigFromUrl[currentNetworkType];
    }

    public String getWalletConfigFromUrl(int type) {
        return walletConfigFromUrl[type];
    }

    public void setWalletConfigFromUrl(int type, String config) {
        walletConfigFromUrl[type] = config;
    }

    public int getCurrentNetworkType() {
        return currentNetworkType;
    }

    public void setCurrentNetworkType(int type) {
        currentNetworkType = type;
    }

    public void setCurrentAccountName(String accountName) {
        currentAccountName = accountName;
    }

    public String getCurrentAccountName() {
        return currentAccountName;
    }

    public void setCurrentAccountColor(@ColorInt int currentAccountColor) {
        this.currentAccountColor = currentAccountColor;
    }

    @ColorInt
    public int getCurrentAccountColor() {
        return currentAccountColor;
    }

    public boolean isEmpty() {
        return tonEncryptedData == null;
    }


    private SharedPreferences getPreferences() {
        return ApplicationLoader.applicationContext.getSharedPreferences("tonConfig_" + currentAccount, Context.MODE_PRIVATE);
    }

    public void clearTonConfig() {
        tonEncryptedData = null;
        tonKeyName = null;
        tonPublicKey = null;
        tonPasscodeType = -1;
        tonPasscodeSalt = null;
        tonCreationFinished = false;
        tonAccountAddress = null;
        tonWalletVersion = 1;
        tonPasscodeRetryInMs = 0;
        tonLastUptimeMillis = 0;
        tonBadPasscodeTries = 0;
    }

    public void clearConfig() {
        getPreferences().edit().clear().commit();
        clearTonConfig();
        saveConfig(true);
    }
}
