/*
 * This is the source code of Wallet for Android v. 1.0.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright Nikolai Kudashov, 2019-2020.
 */

package org.telegram.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.tginfo.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Wallet.WalletActivity;
import org.telegram.ui.Wallet.WalletCreateActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class LaunchActivity extends Activity implements ActionBarLayout.ActionBarLayoutDelegate {

    private boolean finished;
    private static ArrayList<BaseFragment> mainFragmentsStack = new ArrayList<>();
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

    private ActionMode visibleActionMode;

    private ActionBarLayout actionBarLayout;
    protected DrawerLayoutContainer drawerLayoutContainer;
    private AlertDialog visibleDialog;


    private AlertDialog localeDialog;
    private boolean loadingLocaleDialog;
    private HashMap<String, String> systemLocaleStrings;
    private HashMap<String, String> englishLocaleStrings;

    private boolean tabletFullSize;

    private static final int PLAY_SERVICES_REQUEST_CHECK_SETTINGS = 140;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ApplicationLoader.postInitApplication();
        AndroidUtilities.checkDisplaySize(this, getResources().getConfiguration());


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTheme(R.style.Theme_TMessages);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                setTaskDescription(new ActivityManager.TaskDescription(null, null, Theme.getColor(Theme.key_actionBarDefault) | 0xff000000));
            } catch (Exception ignore) {

            }
            try {
                getWindow().setNavigationBarColor(0xff000000);
            } catch (Exception ignore) {

            }
        }

        getWindow().setBackgroundDrawableResource(R.drawable.transparent);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 24) {
            AndroidUtilities.isInMultiwindow = isInMultiWindowMode();
        }

        Theme.createCommonResources(this);
        AndroidUtilities.fillStatusBarHeight(this);
        actionBarLayout = new ActionBarLayout(this);

        drawerLayoutContainer = new DrawerLayoutContainer(this);
        drawerLayoutContainer.setBehindKeyboardColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        setContentView(drawerLayoutContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        drawerLayoutContainer.addView(actionBarLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        drawerLayoutContainer.setParentActionBarLayout(actionBarLayout);
        actionBarLayout.setDrawerLayoutContainer(drawerLayoutContainer);
        actionBarLayout.init(mainFragmentsStack);
        actionBarLayout.setDelegate(this);

        if (actionBarLayout.fragmentsStack.isEmpty()) {
            actionBarLayout.addFragmentToStack(getCurrentWalletFragment());
        }
        drawerLayoutContainer.setAllowOpenDrawer(false, false);

        handleIntent(getIntent(), false, savedInstanceState != null, false);

        try {
            String os1 = Build.DISPLAY;
            String os2 = Build.USER;
            if (os1 != null) {
                os1 = os1.toLowerCase();
            } else {
                os1 = "";
            }
            if (os2 != null) {
                os2 = os1.toLowerCase();
            } else {
                os2 = "";
            }
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("OS name " + os1 + " " + os2);
            }
            if (os1.contains("flyme") || os2.contains("flyme")) {
                AndroidUtilities.incorrectDisplaySizeFix = true;
                final View view = getWindow().getDecorView().getRootView();
                view.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener = () -> {
                    int height = view.getMeasuredHeight();
                    FileLog.d("height = " + height + " displayHeight = " + AndroidUtilities.displaySize.y);
                    if (Build.VERSION.SDK_INT >= 21) {
                        height -= AndroidUtilities.statusBarHeight;
                    }
                    if (height > AndroidUtilities.dp(100) && height < AndroidUtilities.displaySize.y && height + AndroidUtilities.dp(100) > AndroidUtilities.displaySize.y) {
                        AndroidUtilities.displaySize.y = height;
                        if (BuildVars.LOGS_ENABLED) {
                            FileLog.d("fix display size y to " + AndroidUtilities.displaySize.y);
                        }
                    }
                });
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private BaseFragment getCurrentWalletFragment() {
        BaseFragment fragment;
        UserConfig userConfig = UserConfig.getInstance(UserConfig.selectedAccount);
        /*if (!TextUtils.isEmpty(userConfig.tonEncryptedData) && TextUtils.isEmpty(userConfig.tonAccountAddress) && userConfig.tonWalletVersion == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(LocaleController.getString("Wallet", R.string.Wallet));
            builder.setMessage(LocaleController.getString("WalletSwitchedToMainnet", R.string.WalletSwitchedToMainnet));
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
            builder.show();
            userConfig.clearTonConfig();
            userConfig.saveConfig(true);
        }*/
        if (TextUtils.isEmpty(userConfig.tonEncryptedData)) {
            fragment = new WalletCreateActivity(WalletCreateActivity.TYPE_CREATE);
        } else if (!userConfig.tonCreationFinished) {
            WalletCreateActivity activity = new WalletCreateActivity(WalletCreateActivity.TYPE_KEY_GENERATED);
            activity.setResumeCreation();
            fragment = activity;
        } else {
            fragment = new WalletActivity();
        }
        return fragment;
    }

    public int getMainFragmentsCount() {
        return mainFragmentsStack.size();
    }

    private void handleIntent(Intent intent, boolean isNew, boolean restore, boolean fromPassword) {
        int flags = intent.getFlags();
        String transferWalletUrl = null;

        if ((flags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0 && intent != null && intent.getAction() != null && !restore && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String scheme = data.getScheme();
                if (scheme != null) {
                    if ("ton".equals(scheme)) {
                        String url = data.toString();
                        if (url.startsWith("ton:transfer") || url.startsWith("ton://transfer")) {
                            transferWalletUrl = url.replace("ton:transfer", "ton://transfer");
                        }
                    }
                }
            }
        }

        if (!isNew) {
            if (actionBarLayout.fragmentsStack.isEmpty()) {
                actionBarLayout.addFragmentToStack(getCurrentWalletFragment());
            }
        }

        if (transferWalletUrl != null) {
            UserConfig userConfig = UserConfig.getInstance(UserConfig.selectedAccount);
            if (TextUtils.isEmpty(userConfig.tonEncryptedData)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(LocaleController.getString("Wallet", R.string.Wallet));
                builder.setMessage(LocaleController.getString("WalletTonLinkNoWalletText", R.string.WalletTonLinkNoWalletText));
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                builder.setPositiveButton(LocaleController.getString("WalletTonLinkNoWalletCreateWallet", R.string.WalletTonLinkNoWalletCreateWallet), (dialog, which) -> presentFragment(new WalletCreateActivity(WalletCreateActivity.TYPE_CREATE)));
                builder.show();
            } else if (!actionBarLayout.fragmentsStack.isEmpty()) {
                if (actionBarLayout.fragmentsStack.size() > 1) {
                    ArrayList<BaseFragment> stack = new ArrayList<>(actionBarLayout.fragmentsStack);
                    for (int a = 1, N = stack.size(); a < N; a++) {
                        actionBarLayout.removeFragmentFromStack(stack.get(a));
                    }
                    isNew = false;
                }
                BaseFragment fragment = actionBarLayout.fragmentsStack.get(0);
                if (fragment instanceof WalletActivity) {
                    WalletActivity walletActivity = (WalletActivity) fragment;
                    walletActivity.openTransfer(transferWalletUrl, null);
                }
            }
        }

        if (!isNew) {
            actionBarLayout.showLastFragment();
        }

        intent.setAction(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent, true, false, false);
    }

    @Override
    public boolean onPreIme() {
        return false;
    }

    private void onFinish() {
        if (finished) {
            return;
        }
        finished = true;
    }

    public void presentFragment(BaseFragment fragment) {
        actionBarLayout.presentFragment(fragment);
    }

    public boolean presentFragment(final BaseFragment fragment, final boolean removeLast, boolean forceWithoutAnimation) {
        return actionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, true, false);
    }

    public ActionBarLayout getActionBarLayout() {
        return actionBarLayout;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (actionBarLayout.fragmentsStack.size() != 0) {
            BaseFragment fragment = actionBarLayout.fragmentsStack.get(actionBarLayout.fragmentsStack.size() - 1);
            fragment.onActivityResultFragment(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults == null) {
            grantResults = new int[0];
        }
        if (permissions == null) {
            permissions = new String[0];
        }
        if (actionBarLayout.fragmentsStack.size() != 0) {
            BaseFragment fragment = actionBarLayout.fragmentsStack.get(actionBarLayout.fragmentsStack.size() - 1);
            fragment.onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
        }
    }

    private void showPermissionErrorAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
        builder.setMessage(message);
        builder.setNegativeButton(LocaleController.getString("PermissionOpenSettings", R.string.PermissionOpenSettings), (dialog, which) -> {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ApplicationLoader.mainInterfacePaused = true;
        actionBarLayout.onPause();
        AndroidUtilities.unregisterUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        try {
            if (visibleDialog != null) {
                visibleDialog.dismiss();
                visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        try {
            if (onGlobalLayoutListener != null) {
                final View view = getWindow().getDecorView().getRootView();
                view.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        super.onDestroy();
        onFinish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationLoader.mainInterfacePaused = false;

        actionBarLayout.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        AndroidUtilities.checkDisplaySize(this, newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        AndroidUtilities.isInMultiwindow = isInMultiWindowMode;
    }

    private String getStringForLanguageAlert(HashMap<String, String> map, String key, int intKey) {
        String value = map.get(key);
        if (value == null) {
            return LocaleController.getString(key, intKey);
        }
        return value;
    }

    public void hideVisibleActionMode() {
        if (visibleActionMode == null) {
            return;
        }
        visibleActionMode.finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayoutContainer.isDrawerOpened()) {
            drawerLayoutContainer.closeDrawer(false);
        } else {
            actionBarLayout.onBackPressed();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        actionBarLayout.onLowMemory();
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        visibleActionMode = mode;
        try {
            Menu menu = mode.getMenu();
            if (menu != null) {
                actionBarLayout.extendActionMode(menu);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        if (Build.VERSION.SDK_INT >= 23 && mode.getType() == ActionMode.TYPE_FLOATING) {
            return;
        }
        actionBarLayout.onActionModeStarted(mode);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        if (visibleActionMode == mode) {
            visibleActionMode = null;
        }
        if (Build.VERSION.SDK_INT >= 23 && mode.getType() == ActionMode.TYPE_FLOATING) {
            return;
        }
        actionBarLayout.onActionModeFinished(mode);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (actionBarLayout.fragmentsStack.size() == 1) {
                if (!drawerLayoutContainer.isDrawerOpened()) {
                    if (getCurrentFocus() != null) {
                        AndroidUtilities.hideKeyboard(getCurrentFocus());
                    }
                    drawerLayoutContainer.openDrawer(false);
                } else {
                    drawerLayoutContainer.closeDrawer(false);
                }
            } else {
                actionBarLayout.onKeyUp(keyCode, event);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean needPresentFragment(BaseFragment fragment, boolean removeLast, boolean forceWithoutAnimation, ActionBarLayout layout) {
        return true;
    }

    @Override
    public boolean needAddFragmentToStack(BaseFragment fragment, ActionBarLayout layout) {
        return true;
    }

    @Override
    public boolean needCloseLastFragment(ActionBarLayout layout) {
        if (layout.fragmentsStack.size() <= 1) {
            onFinish();
            finish();
            return false;
        }
        return true;
    }

    public void rebuildAllFragments(boolean last) {
        actionBarLayout.rebuildAllFragmentViews(last, last);
    }

    @Override
    public void onRebuildAllFragments(ActionBarLayout layout, boolean last) {

    }
}
