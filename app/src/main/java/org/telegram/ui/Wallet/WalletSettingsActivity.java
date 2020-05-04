/*
 * This is the source code of Wallet for Android v. 1.0.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright Nikolai Kudashov, 2019-2020.
 */

package org.telegram.ui.Wallet;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.TonController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.PollEditTextCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BiometricPromtHelper;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.tginfo.telegram.messenger.BuildConfig;
import org.tginfo.telegram.messenger.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WalletSettingsActivity extends BaseFragment {

    public static final int SEND_ACTIVITY_RESULT_CODE = 34;

    public static final int TYPE_SETTINGS = 0;
    public static final int TYPE_SERVER = 1;

    private RecyclerListView listView;
    private Adapter adapter;
    private BaseFragment parentFragment;
    private BiometricPromtHelper biometricPromtHelper;

    private Paint paint = new Paint();

    private String[] blockchainName;
    private String[] blockchainUrl;
    private String[] blockchainJson;
    private String[] blockchainConfigFromUrl;
    private int[] configType;
    private int networkType;

    private int currentType;

    private int typeHeaderRow;
    private int urlTypeRow;
    private int jsonTypeRow;
    private int typeSectionRow;
    private int fieldHeaderRow;
    private int fieldRow;
    private int fieldSectionRow;
    private int blockchainNameHeaderRow;
    private int blockchainNameRow;
    private int blockchainMainRow;
    private int blockchainTestRow;
    private int blockchainNameSectionRow;
    private int headerRow;
    private int exportRow;
    private int serverSettingsRow;
    private int changePasscodeRow;
    private int walletSectionRow;
    private int deleteRow;
    private int deleteSectionRow;
    private int appVersionRow;
    private int sendLogsRow;
    private int clearLogsRow;
    private int rowCount;


    private int whereGramsSectionRow;
    private int whereGramsHeaderRow;
    private int whereGramEnChatRow;
    private int whereGramRuChatRow;
    private int whereGramBotRow;

    private int feedbackSectionRow;
    private int feedbackHeaderRow;
    private int feedbackEnChatRow;
    private int feedbackRuChatRow;

    private static final int done_button = 1;

    public static class TypeCell extends FrameLayout {

        private TextView textView;
        private ImageView checkImage;
        private boolean needDivider;

        public TypeCell(Context context) {
            super(context);

            setWillNotDraw(false);

            textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 23 + 48 : 21, 0, LocaleController.isRTL ? 21 : 23, 0));

            checkImage = new ImageView(context);
            checkImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_addedIcon), PorterDuff.Mode.MULTIPLY));
            checkImage.setImageResource(R.drawable.sticker_added);
            addView(checkImage, LayoutHelper.createFrame(19, 14, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL, 21, 0, 21, 0));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(50) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY));
        }

        public void setValue(String name, boolean checked, boolean divider) {
            textView.setText(name);
            checkImage.setVisibility(checked ? VISIBLE : INVISIBLE);
            needDivider = divider;
        }

        public void setTypeChecked(boolean value) {
            checkImage.setVisibility(value ? VISIBLE : INVISIBLE);
        }

        public void setNeedDivider(boolean value) {
            needDivider = value;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (needDivider) {
                canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(20), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
            }
        }
    }

    public WalletSettingsActivity(int type, BaseFragment parent) {
        super();

        parentFragment = parent;
        currentType = type;

        if (currentType == TYPE_SERVER) {
            UserConfig userConfig = getUserConfig();
            blockchainName = new String[]{userConfig.getWalletBlockchainName(UserConfig.NETWORK_TYPE_TEST), userConfig.getWalletBlockchainName(UserConfig.NETWORK_TYPE_MAIN)};
            blockchainJson = new String[]{userConfig.getWalletConfig(UserConfig.NETWORK_TYPE_TEST), userConfig.getWalletConfig(UserConfig.NETWORK_TYPE_MAIN)};
            blockchainConfigFromUrl = new String[]{userConfig.getWalletConfigFromUrl(UserConfig.NETWORK_TYPE_TEST), userConfig.getWalletConfigFromUrl(UserConfig.NETWORK_TYPE_MAIN)};
            blockchainUrl = new String[]{userConfig.getWalletConfigUrl(UserConfig.NETWORK_TYPE_TEST), userConfig.getWalletConfigUrl(UserConfig.NETWORK_TYPE_MAIN)};
            configType = new int[]{userConfig.getWalletConfigType(UserConfig.NETWORK_TYPE_TEST), userConfig.getWalletConfigType(UserConfig.NETWORK_TYPE_MAIN)};
            networkType = userConfig.getCurrentNetworkType();
        }
        updateRows();
    }

    private void updateRows() {
        rowCount = 0;

        typeHeaderRow = -1;
        fieldHeaderRow = -1;
        urlTypeRow = -1;
        jsonTypeRow = -1;
        typeSectionRow = -1;
        fieldRow = -1;
        fieldSectionRow = -1;
        headerRow = -1;
        exportRow = -1;
        changePasscodeRow = -1;
        walletSectionRow = -1;
        deleteRow = -1;
        deleteSectionRow = -1;
        appVersionRow = -1;
        serverSettingsRow = -1;
        blockchainNameHeaderRow = -1;
        blockchainNameRow = -1;
        blockchainMainRow = -1;
        blockchainTestRow = -1;
        blockchainNameSectionRow = -1;
        sendLogsRow = -1;
        clearLogsRow = -1;

        whereGramsHeaderRow = -1;
        whereGramsSectionRow = -1;
        whereGramEnChatRow = -1;
        whereGramRuChatRow = -1;
        whereGramBotRow = -1;

        feedbackHeaderRow = -1;
        feedbackSectionRow = -1;
        feedbackEnChatRow = -1;
        feedbackRuChatRow = -1;

        if (currentType == TYPE_SETTINGS) {
            headerRow = rowCount++;


            if (BuildVars.DEBUG_VERSION) {
                clearLogsRow = rowCount++;
                sendLogsRow = rowCount++;
            }
            exportRow = rowCount++;
            if (BuildVars.TON_WALLET_STANDALONE) {
                serverSettingsRow = rowCount++;
            }

            whereGramsSectionRow = rowCount++;
            whereGramsHeaderRow = rowCount++;
            whereGramRuChatRow = rowCount++;
            whereGramEnChatRow = rowCount++;
            whereGramBotRow = rowCount++;

            feedbackSectionRow = rowCount++;
            feedbackHeaderRow = rowCount++;
            feedbackRuChatRow = rowCount++;
            feedbackEnChatRow = rowCount++;

            if (getUserConfig().tonPasscodeType != -1) {
                changePasscodeRow = rowCount++;
            }
            walletSectionRow = rowCount++;
            deleteRow = rowCount++;
            deleteSectionRow = rowCount++;
            appVersionRow = rowCount++;
        } else if (currentType == TYPE_SERVER) {
            typeHeaderRow = rowCount++;
            urlTypeRow = rowCount++;
            jsonTypeRow = rowCount++;
            typeSectionRow = rowCount++;
            fieldHeaderRow = rowCount++;
            fieldRow = rowCount++;
            fieldSectionRow = rowCount++;
            blockchainNameHeaderRow = rowCount++;
            //blockchainMainRow = rowCount++;
            //blockchainTestRow = rowCount++;
            if (networkType == UserConfig.NETWORK_TYPE_TEST) {
                blockchainNameRow = rowCount++;
            }
            blockchainNameSectionRow = rowCount++;
        }
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        AndroidUtilities.removeAdjustResize(getParentActivity(), classGuid);
    }

    @Override
    protected ActionBar createActionBar(Context context) {
        ActionBar actionBar = new ActionBar(context);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_wallet_blackBackground));
        actionBar.setTitleColor(Theme.getColor(Theme.key_wallet_whiteText));
        actionBar.setItemsColor(Theme.getColor(Theme.key_wallet_whiteText), false);
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_wallet_blackBackgroundSelector), false);
        if (currentType == TYPE_SETTINGS) {
            actionBar.setTitle(LocaleController.getString("WalletSettings", R.string.WalletSettings));
        } else if (currentType == TYPE_SERVER) {
            actionBar.setTitle(LocaleController.getString("WalletServerSettings", R.string.WalletServerSettings));
            ActionBarMenuItem doneItem = actionBar.createMenu().addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));
            doneItem.setContentDescription(LocaleController.getString("Done", R.string.Done));
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == done_button) {
                    saveConfig(true, true);
                }
            }
        });
        return actionBar;
    }

    private void saveConfig(boolean alert, boolean verify) {
        if (getParentActivity() == null) {
            return;
        }
        UserConfig userConfig = getUserConfig();
        boolean needApply = false;
        boolean blockchainNameChanged = networkType != userConfig.getCurrentNetworkType() || !TextUtils.equals(userConfig.getWalletBlockchainName(networkType), blockchainName[networkType]);
        if (configType[networkType] != userConfig.getWalletConfigType(networkType) || blockchainNameChanged) {
            needApply = true;
        } else if (configType[networkType] == TonController.CONFIG_TYPE_URL) {
            needApply = !TextUtils.equals(userConfig.getWalletConfigUrl(networkType), blockchainUrl[networkType]);
        } else if (configType[networkType] == TonController.CONFIG_TYPE_JSON) {
            needApply = !TextUtils.equals(userConfig.getWalletConfig(networkType), blockchainJson[networkType]);
        }
        if (needApply) {
            if (alert) {
                if (networkType != userConfig.getCurrentNetworkType()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("WalletSendWarningTitle", R.string.WalletSendWarningTitle));
                    builder.setMessage(networkType == UserConfig.NETWORK_TYPE_TEST ? LocaleController.getString("WalletTestNetworkSwitch", R.string.WalletTestNetworkSwitch) : LocaleController.getString("WalletMainNetworkSwitch", R.string.WalletMainNetworkSwitch));
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    builder.setPositiveButton(LocaleController.getString("WalletContinue", R.string.WalletContinue), (dialog, which) -> saveConfig(false, true));
                    AlertDialog dialog = builder.create();
                    showDialog(dialog);
                    TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
                    }
                    return;
                } else if (!TextUtils.equals(getUserConfig().getWalletBlockchainName(networkType), blockchainName[networkType])) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("Wallet", R.string.Wallet));
                    builder.setMessage(LocaleController.getString("WalletBlockchainNameWarning", R.string.WalletBlockchainNameWarning));
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    builder.setPositiveButton(LocaleController.getString("WalletContinue", R.string.WalletContinue), (dialog, which) -> saveConfig(false, true));
                    AlertDialog dialog = builder.create();
                    showDialog(dialog);
                    TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
                    }
                    return;
                }
            } else if (configType[networkType] == TonController.CONFIG_TYPE_JSON) {
                if (TextUtils.isEmpty(blockchainJson[networkType])) {
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(blockchainJson[networkType]);
                } catch (Throwable e) {
                    FileLog.e(e);
                    AlertsCreator.showSimpleAlert(this, LocaleController.getString("WalletError", R.string.WalletError), LocaleController.getString("WalletBlockchainConfigInvalid", R.string.WalletBlockchainConfigInvalid));
                    return;
                }
            } else if (verify && configType[networkType] == TonController.CONFIG_TYPE_URL) {
                AlertDialog progressDialog = new AlertDialog(getParentActivity(), 3);
                progressDialog.setCanCacnel(false);
                progressDialog.show();
                WalletConfigLoader.loadConfig(blockchainUrl[networkType], result -> {
                    progressDialog.dismiss();
                    if (TextUtils.isEmpty(result)) {
                        AlertsCreator.showSimpleAlert(this, LocaleController.getString("WalletError", R.string.WalletError), LocaleController.getString("WalletBlockchainConfigLoadError", R.string.WalletBlockchainConfigLoadError));
                        return;
                    }
                    blockchainConfigFromUrl[networkType] = result;
                    saveConfig(false, false);
                });
                return;
            }
        }
        if (needApply) {
            String oldWalletBlockchainName = userConfig.getWalletBlockchainName();
            String oldWalletConfig = userConfig.getWalletConfig();
            String oldWalletConfigUrl = userConfig.getWalletConfigUrl();
            int oldWalletConfigType = userConfig.getWalletConfigType();
            String oldWalletConfigFromUrl = blockchainConfigFromUrl[networkType];
            int oldNetworkType = userConfig.getCurrentNetworkType();

            userConfig.setWalletBlockchainName(networkType, blockchainName[networkType]);
            userConfig.setWalletConfig(networkType, blockchainJson[networkType]);
            userConfig.setWalletConfigUrl(networkType, blockchainUrl[networkType]);
            userConfig.setWalletConfigType(networkType, configType[networkType]);
            userConfig.setWalletConfigFromUrl(networkType, blockchainConfigFromUrl[networkType]);
            userConfig.setCurrentNetworkType(networkType);

            if (!getTonController().onTonConfigUpdated()) {
                userConfig.setWalletBlockchainName(networkType, oldWalletBlockchainName);
                userConfig.setWalletConfig(networkType, oldWalletConfig);
                userConfig.setWalletConfigUrl(networkType, oldWalletConfigUrl);
                userConfig.setWalletConfigType(networkType, oldWalletConfigType);
                userConfig.setWalletConfigFromUrl(networkType, oldWalletConfigFromUrl);
                userConfig.setCurrentNetworkType(oldNetworkType);
                AlertsCreator.showSimpleAlert(this, LocaleController.getString("WalletError", R.string.WalletError), LocaleController.getString("WalletBlockchainConfigInvalid", R.string.WalletBlockchainConfigInvalid));
                return;
            }

            userConfig.saveConfig(false);
        }
        if (blockchainNameChanged) {
            doLogout();
            if (parentFragment != null) {
                parentFragment.removeSelfFromStack();
            }
            presentFragment(new WalletCreateActivity(WalletCreateActivity.TYPE_CREATE), true);
        } else {
            finishFragment();
        }
    }

    @Override
    public View createView(Context context) {
        biometricPromtHelper = new BiometricPromtHelper(this);

        fragmentView = listView = new RecyclerListView(context) {
            @Override
            public void onDraw(Canvas c) {
                ViewHolder holder;
                if (deleteSectionRow != -1) {
                    holder = findViewHolderForAdapterPosition(deleteSectionRow);
                } else if (blockchainNameSectionRow != -1) {
                    holder = findViewHolderForAdapterPosition(blockchainNameSectionRow);
                } else if (appVersionRow != -1) {
                    holder = findViewHolderForAdapterPosition(appVersionRow);
                } else {
                    holder = null;
                }
                int bottom;
                int height = getMeasuredHeight();
                if (holder != null) {
                    bottom = (int) (holder.itemView.getY() + holder.itemView.getMeasuredHeight());
                    if (holder.itemView.getBottom() >= height) {
                        bottom = height;
                    }
                } else {
                    bottom = height;
                }

                paint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                c.drawRect(0, 0, getMeasuredWidth(), bottom, paint);
                if (bottom != height) {
                    paint.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
                    c.drawRect(0, bottom, getMeasuredWidth(), height, paint);
                }
            }
        };
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(adapter = new Adapter(context));
        listView.setGlowColor(Theme.getColor(Theme.key_wallet_blackBackground));
        listView.setItemAnimator(new DefaultItemAnimator() {
            protected void onMoveAnimationUpdate(RecyclerView.ViewHolder holder) {
                listView.invalidate();
            }
        });
        DefaultItemAnimator itemAnimator = (DefaultItemAnimator) listView.getItemAnimator();
        itemAnimator.setDelayAnimations(false);
        listView.setOnItemClickListener((view, position) -> {
            if (position == exportRow) {
                switch (getTonController().getKeyProtectionType()) {
                    case TonController.KEY_PROTECTION_TYPE_LOCKSCREEN: {
                        if (Build.VERSION.SDK_INT >= 23) {
                            KeyguardManager keyguardManager = (KeyguardManager) ApplicationLoader.applicationContext.getSystemService(Context.KEYGUARD_SERVICE);
                            Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(LocaleController.getString("Wallet", R.string.Wallet), LocaleController.getString("WalletExportConfirmCredentials", R.string.WalletExportConfirmCredentials));
                            getParentActivity().startActivityForResult(intent, SEND_ACTIVITY_RESULT_CODE);
                        }
                        break;
                    }
                    case TonController.KEY_PROTECTION_TYPE_BIOMETRIC: {
                        biometricPromtHelper.promtWithCipher(getTonController().getCipherForDecrypt(), LocaleController.getString("WalletExportConfirmCredentials", R.string.WalletExportConfirmCredentials), this::doExport);
                        break;
                    }
                    case TonController.KEY_PROTECTION_TYPE_NONE: {
                        presentFragment(new WalletPasscodeActivity(WalletPasscodeActivity.TYPE_PASSCODE_EXPORT));
                        break;
                    }
                }
            } else if (position == clearLogsRow) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle("Clear Logs");
                builder.setMessage("Are you sure you want to clear logs?");
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                builder.setPositiveButton("Clear", (dialog, which) -> FileLog.cleanupLogs());
                showDialog(builder.create());
            } else if (position == sendLogsRow) {
                sendLogs();
            } else if (position == whereGramEnChatRow) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/tginfochaten"));
                if (browserIntent.resolveActivity(context.getPackageManager()) != null)
                    context.startActivity(browserIntent);
            } else if (position == whereGramRuChatRow) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/infotonchat"));
                if (browserIntent.resolveActivity(context.getPackageManager()) != null)
                    context.startActivity(browserIntent);
            } else if (position == whereGramBotRow) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/test_ton_bot"));
                if (browserIntent.resolveActivity(context.getPackageManager()) != null)
                    context.startActivity(browserIntent);
            } else if (position == feedbackRuChatRow) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/infotonchat"));
                if (browserIntent.resolveActivity(context.getPackageManager()) != null)
                    context.startActivity(browserIntent);
            } else if (position == feedbackEnChatRow) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/tginfochaten"));
                if (browserIntent.resolveActivity(context.getPackageManager()) != null)
                    context.startActivity(browserIntent);
            } else if (position == deleteRow) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("WalletDeleteTitle", R.string.WalletDeleteTitle));
                builder.setMessage(LocaleController.getString("WalletDeleteInfo", R.string.WalletDeleteInfo));
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                builder.setPositiveButton(LocaleController.getString("Delete", R.string.Delete), (dialog, which) -> {
                    doLogout();
                    if (parentFragment != null) {
                        parentFragment.removeSelfFromStack();
                    }
                    if (BuildVars.TON_WALLET_STANDALONE) {
                        presentFragment(new WalletCreateActivity(WalletCreateActivity.TYPE_CREATE), true);
                    } else {
                        finishFragment();
                    }
                });
                AlertDialog dialog = builder.create();
                showDialog(dialog);
                TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (button != null) {
                    button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
                }
            } else if (position == changePasscodeRow) {
                presentFragment(new WalletPasscodeActivity(WalletPasscodeActivity.TYPE_PASSCODE_CHANGE));
            } else if (position == urlTypeRow || position == jsonTypeRow) {
                configType[networkType] = position == urlTypeRow ? TonController.CONFIG_TYPE_URL : TonController.CONFIG_TYPE_JSON;
                adapter.notifyItemChanged(fieldRow);
            } else if (position == blockchainMainRow || position == blockchainTestRow) {
                int currentType = networkType;
                networkType = position == blockchainMainRow ? UserConfig.NETWORK_TYPE_MAIN : UserConfig.NETWORK_TYPE_TEST;
                if (currentType != networkType) {
                    int prevBlockchainNameRow = blockchainNameRow;
                    updateRows();
                    adapter.notifyItemChanged(fieldRow);
                    if (networkType == UserConfig.NETWORK_TYPE_TEST) {
                        adapter.notifyItemInserted(blockchainNameRow);
                    } else {
                        adapter.notifyItemRemoved(prevBlockchainNameRow);
                    }
                }
            } else if (position == serverSettingsRow) {
                presentFragment(new WalletSettingsActivity(TYPE_SERVER, WalletSettingsActivity.this));
            }
            if (view instanceof TypeCell) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = listView.getChildAt(a);
                    if (child instanceof TypeCell) {
                        TypeCell cell = (TypeCell) child;
                        RecyclerListView.ViewHolder holder = listView.findContainingViewHolder(child);
                        if (holder != null) {
                            position = holder.getAdapterPosition();
                            if (position == urlTypeRow) {
                                cell.setTypeChecked(configType[networkType] == TonController.CONFIG_TYPE_URL);
                            } else if (position == jsonTypeRow) {
                                cell.setTypeChecked(configType[networkType] == TonController.CONFIG_TYPE_JSON);
                            } else if (position == blockchainMainRow) {
                                cell.setTypeChecked(networkType == UserConfig.NETWORK_TYPE_MAIN);
                            } else if (position == blockchainTestRow) {
                                cell.setTypeChecked(networkType == UserConfig.NETWORK_TYPE_TEST);
                                cell.setNeedDivider(networkType == UserConfig.NETWORK_TYPE_TEST);
                            }
                        }
                    }
                }
            }
        });

        return fragmentView;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (biometricPromtHelper != null) {
            biometricPromtHelper.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
    }

    @Override
    public void removeSelfFromStack() {
        super.removeSelfFromStack();
        if (parentFragment != null) {
            parentFragment.removeSelfFromStack();
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEND_ACTIVITY_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                doExport(null);
            }
        }
    }

    private void sendLogs() {
        if (getParentActivity() == null) {
            return;
        }
        AlertDialog progressDialog = new AlertDialog(getParentActivity(), 3);
        progressDialog.setCanCacnel(false);
        progressDialog.show();
        Utilities.globalQueue.postRunnable(() -> {
            try {
                File sdCard = ApplicationLoader.applicationContext.getExternalFilesDir(null);
                File dir = new File(sdCard.getAbsolutePath() + "/logs");

                File zipFile = new File(dir, "wallet_logs.zip");
                if (zipFile.exists()) {
                    zipFile.delete();
                }

                File[] files = dir.listFiles();

                boolean[] finished = new boolean[1];

                BufferedInputStream origin = null;
                ZipOutputStream out = null;
                try {
                    FileOutputStream dest = new FileOutputStream(zipFile);
                    out = new ZipOutputStream(new BufferedOutputStream(dest));
                    byte[] data = new byte[1024 * 64];

                    for (int i = 0; i < files.length; i++) {
                        FileInputStream fi = new FileInputStream(files[i]);
                        origin = new BufferedInputStream(fi, data.length);

                        ZipEntry entry = new ZipEntry(files[i].getName());
                        out.putNextEntry(entry);
                        int count;
                        while ((count = origin.read(data, 0, data.length)) != -1) {
                            out.write(data, 0, count);
                        }
                        if (origin != null) {
                            origin.close();
                            origin = null;
                        }
                    }
                    finished[0] = true;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (origin != null) {
                        origin.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }

                AndroidUtilities.runOnUIThread(() -> {
                    try {
                        progressDialog.dismiss();
                    } catch (Exception ignore) {

                    }
                    if (finished[0]) {
                        Uri uri;
                        if (Build.VERSION.SDK_INT >= 24) {
                            uri = FileProvider.getUriForFile(getParentActivity(), BuildConfig.APPLICATION_ID + ".provider", zipFile);
                        } else {
                            uri = Uri.fromFile(zipFile);
                        }

                        Intent i = new Intent(Intent.ACTION_SEND);
                        if (Build.VERSION.SDK_INT >= 24) {
                            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL, "");
                        i.putExtra(Intent.EXTRA_SUBJECT, "Logs from " + LocaleController.getInstance().formatterStats.format(System.currentTimeMillis()));
                        i.putExtra(Intent.EXTRA_STREAM, uri);
                        if (getParentActivity() != null) {
                            getParentActivity().startActivityForResult(Intent.createChooser(i, "Select email application."), 500);
                        }
                    } else {
                        Toast.makeText(getParentActivity(), LocaleController.getString("ErrorOccurred", R.string.ErrorOccurred), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void doLogout() {
        getTonController().cleanup();
        UserConfig userConfig = getUserConfig();
        userConfig.clearTonConfig();
        userConfig.saveConfig(false);
    }

    private void doExport(Cipher cipher) {
        if (getParentActivity() == null) {
            return;
        }
        AlertDialog progressDialog = new AlertDialog(getParentActivity(), 3);
        progressDialog.setCanCacnel(false);
        progressDialog.show();
        getTonController().getSecretWords(null, cipher, (words) -> {
            progressDialog.dismiss();
            WalletCreateActivity fragment = new WalletCreateActivity(WalletCreateActivity.TYPE_24_WORDS);
            fragment.setSecretWords(words);
            presentFragment(fragment);
        }, (text, error) -> {
            progressDialog.dismiss();
            AlertsCreator.showSimpleAlert(this, LocaleController.getString("ErrorOccurred", R.string.ErrorOccurred) + "\n" + (error != null ? error.message : text));
        });
    }

    private class Adapter extends RecyclerListView.SelectionAdapter {

        private Context context;

        public Adapter(Context c) {
            context = c;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0: {
                    view = new HeaderCell(context);
                    break;
                }
                case 1: {
                    view = new TextSettingsCell(context);
                    break;
                }
                case 2: {
                    view = new ShadowSectionCell(context);
                    break;
                }
                case 3: {
                    view = new TextInfoPrivacyCell(context);
                    break;
                }
                case 4: {
                    view = new TypeCell(context);
                    break;
                }
                case 5:
                default: {
                    PollEditTextCell cell = new PollEditTextCell(context, null);
                    EditTextBoldCursor editText = cell.getTextView();
                    editText.setPadding(0, AndroidUtilities.dp(14), AndroidUtilities.dp(37), AndroidUtilities.dp(14));
                    cell.addTextWatcher(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            Integer tag = (Integer) cell.getTag();
                            if (tag == null) {
                                return;
                            }
                            if (tag == fieldRow) {
                                if (configType[networkType] == TonController.CONFIG_TYPE_URL) {
                                    blockchainUrl[networkType] = s.toString();
                                } else {
                                    blockchainJson[networkType] = s.toString();
                                }
                            } else if (tag == blockchainNameRow) {
                                blockchainName[networkType] = s.toString();
                            }
                        }
                    });
                    view = cell;
                    break;
                }
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    HeaderCell cell = (HeaderCell) holder.itemView;
                    if (position == headerRow) {
                        cell.setText(LocaleController.getString("Wallet", R.string.Wallet));
                    } else if (position == whereGramsHeaderRow) {
                        cell.setText(LocaleController.getString("WalletWhereGrams", R.string.WalletWhereGrams));
                    } else if (position == feedbackHeaderRow) {
                        cell.setText(LocaleController.getString("WalletFeedback", R.string.WalletFeedback));
                    } else if (position == blockchainNameHeaderRow) {
                        cell.setText(LocaleController.getString("WalletBlockchainName", R.string.WalletBlockchainName));
                    } else if (position == typeHeaderRow) {
                        cell.setText(LocaleController.getString("WalletConfigType", R.string.WalletConfigType));
                    } else if (position == fieldHeaderRow) {
                        if (configType[networkType] == TonController.CONFIG_TYPE_URL) {
                            cell.setText(LocaleController.getString("WalletConfigTypeUrlHeader", R.string.WalletConfigTypeUrlHeader));
                        } else {
                            cell.setText(LocaleController.getString("WalletConfigTypeJsonHeader", R.string.WalletConfigTypeJsonHeader));
                        }
                    }
                    break;
                }
                case 1: {
                    TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                    if (position == exportRow) {
                        cell.setText(LocaleController.getString("WalletExport", R.string.WalletExport), changePasscodeRow != -1 || serverSettingsRow != -1);
                        cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                        cell.setTag(Theme.key_windowBackgroundWhiteBlackText);
                    } else if (position == whereGramEnChatRow) {
                        cell.setText(LocaleController.getString("WalletWhereGramsEnChat", R.string.WalletWhereGramsEnChat), whereGramBotRow != -1);
                        cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                        cell.setTag(Theme.key_windowBackgroundWhiteBlackText);
                    } else if (position == whereGramRuChatRow) {
                        cell.setText(LocaleController.getString("WalletWhereGramsRuChat", R.string.WalletWhereGramsRuChat), whereGramBotRow != -1);
                        cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                        cell.setTag(Theme.key_windowBackgroundWhiteBlackText);
                    } else if (position == whereGramBotRow) {
                        cell.setText(LocaleController.getString("WalletWhereGramsBot", R.string.WalletWhereGramsBot), false);
                        cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                        cell.setTag(Theme.key_windowBackgroundWhiteBlackText);
                    } else if (position == feedbackEnChatRow) {
                        cell.setText(LocaleController.getString("WalletFeedbackEnChat", R.string.WalletFeedbackEnChat), false);
                        cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                        cell.setTag(Theme.key_windowBackgroundWhiteBlackText);
                    } else if (position == feedbackRuChatRow) {
                        cell.setText(LocaleController.getString("WalletFeedbackRuChat", R.string.WalletFeedbackRuChat), feedbackEnChatRow != -1);
                        cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                        cell.setTag(Theme.key_windowBackgroundWhiteBlackText);
                    } else if (position == changePasscodeRow) {
                        cell.setText(LocaleController.getString("WalletChangePasscode", R.string.WalletChangePasscode), false);
                        cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                        cell.setTag(Theme.key_windowBackgroundWhiteBlackText);
                    } else if (position == deleteRow) {
                        cell.setText(LocaleController.getString("WalletDelete", R.string.WalletDelete), false);
                        cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText2));
                        cell.setTag(Theme.key_windowBackgroundWhiteRedText2);
                    } else if (position == serverSettingsRow) {
                        cell.setText(LocaleController.getString("WalletServerSettings", R.string.WalletServerSettings), changePasscodeRow != -1);
                        cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                        cell.setTag(Theme.key_windowBackgroundWhiteBlackText);
                    } else if (position == clearLogsRow) {
                        cell.setText("Clear Logs", true);
                        cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                        cell.setTag(Theme.key_windowBackgroundWhiteBlackText);
                    } else if (position == sendLogsRow) {
                        cell.setText("Send Logs", true);
                        cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                        cell.setTag(Theme.key_windowBackgroundWhiteBlackText);
                    }
                    break;
                }
                case 2: {
                    if (position == walletSectionRow || position == fieldSectionRow || position == whereGramsSectionRow || position == feedbackSectionRow) {
                        Drawable drawable = Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow);
                        CombinedDrawable combinedDrawable = new CombinedDrawable(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundGray)), drawable);
                        combinedDrawable.setFullsize(true);
                        holder.itemView.setBackgroundDrawable(combinedDrawable);
                    }
                    break;
                }
                case 3: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    int resId = 0;
                    if (position == deleteSectionRow) {
                        cell.setText(LocaleController.getString("WalletDeleteInfo", R.string.WalletDeleteInfo));
                        cell.resetCustomGravity();
                        cell.setOnClickListener(null);
                        resId = R.drawable.greydivider_bottom;
                    } else if (position == appVersionRow) {
                        cell.setText(String.format("%s %s(%s)",
                                LocaleController.getString("AppName", R.string.AppName),
                                BuildConfig.VERSION_NAME,
                                BuildConfig.VERSION_CODE));
                        cell.setCustomGravity(Gravity.CENTER_HORIZONTAL);
                        cell.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                if (clipboardManager != null) {
                                    Toast.makeText(context, LocaleController.getString("WalletDebugInfoCopied", R.string.WalletDebugInfoCopied), Toast.LENGTH_SHORT).show();

                                    int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
                                    int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                                    String resolution = String.format(Locale.US, "%dx%d", screenHeight, screenWidth);

                                    String debugInfo = String.format(Locale.US, "%s %s(%s)(Android %s; SDK %d; %s; %s %s; %s; %s)",
                                            LocaleController.getString("AppName", R.string.AppName),
                                            BuildConfig.VERSION_NAME,
                                            BuildConfig.VERSION_CODE,
                                            Build.VERSION.RELEASE,
                                            Build.VERSION.SDK_INT,
                                            Build.CPU_ABI,
                                            Build.MANUFACTURER,
                                            Build.MODEL,
                                            Locale.getDefault().getLanguage(),
                                            resolution);

                                    ClipData clipData = ClipData.newPlainText("GramDebug", debugInfo);
                                    clipboardManager.setPrimaryClip(clipData);
                                }

                            }
                        });
                        resId = R.drawable.greydivider_bottom;
                    } else if (position == typeSectionRow) {
                        cell.setText(LocaleController.getString("WalletConfigTypeInfo", R.string.WalletConfigTypeInfo));
                        cell.resetCustomGravity();
                        cell.setOnClickListener(null);
                        resId = R.drawable.greydivider;
                    } else if (position == blockchainNameSectionRow) {
                        cell.setText(LocaleController.getString("WalletBlockchainNameInfo", R.string.WalletBlockchainNameInfo));
                        cell.resetCustomGravity();
                        cell.setOnClickListener(null);
                        resId = R.drawable.greydivider_bottom;
                    }
                    Drawable drawable = Theme.getThemedDrawable(context, resId, Theme.key_windowBackgroundGrayShadow);
                    CombinedDrawable combinedDrawable = new CombinedDrawable(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundGray)), drawable);
                    combinedDrawable.setFullsize(true);
                    holder.itemView.setBackgroundDrawable(combinedDrawable);
                    break;
                }
                case 4: {
                    TypeCell cell = (TypeCell) holder.itemView;
                    if (position == urlTypeRow) {
                        cell.setValue(LocaleController.getString("WalletConfigTypeUrl", R.string.WalletConfigTypeUrl), configType[networkType] == TonController.CONFIG_TYPE_URL, true);
                    } else if (position == jsonTypeRow) {
                        cell.setValue(LocaleController.getString("WalletConfigTypeJson", R.string.WalletConfigTypeJson), configType[networkType] == TonController.CONFIG_TYPE_JSON, false);
                    } else if (position == blockchainMainRow) {
                        cell.setValue(LocaleController.getString("WalletMainNetwork", R.string.WalletMainNetwork), networkType == UserConfig.NETWORK_TYPE_MAIN, true);
                    } else if (position == blockchainTestRow) {
                        cell.setValue(LocaleController.getString("WalletTestNetwork", R.string.WalletTestNetwork), networkType == UserConfig.NETWORK_TYPE_TEST, networkType == UserConfig.NETWORK_TYPE_TEST);
                    }
                    break;
                }
                case 5: {
                    PollEditTextCell textCell = (PollEditTextCell) holder.itemView;
                    textCell.setTag(null);
                    if (position == blockchainNameRow) {
                        textCell.setTextAndHint(blockchainName[networkType], LocaleController.getString("WalletBlockchainNameHint", R.string.WalletBlockchainNameHint), false);
                    } else if (position == fieldRow) {
                        if (configType[networkType] == TonController.CONFIG_TYPE_URL) {
                            textCell.setTextAndHint(blockchainUrl[networkType], LocaleController.getString("WalletConfigTypeUrlHint", R.string.WalletConfigTypeUrlHint), false);
                        } else {
                            textCell.setTextAndHint(blockchainJson[networkType], LocaleController.getString("WalletConfigTypeJsonHint", R.string.WalletConfigTypeJsonHint), false);
                        }
                    }
                    textCell.setTag(position);
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == headerRow || position == blockchainNameHeaderRow || position == typeHeaderRow || position == fieldHeaderRow || position == whereGramsHeaderRow || position == feedbackHeaderRow) {
                return 0;
            } else if (position == exportRow || position == changePasscodeRow || position == deleteRow || position == serverSettingsRow || position == sendLogsRow || position == clearLogsRow || position == whereGramEnChatRow | position == whereGramRuChatRow || position == whereGramBotRow || position == feedbackEnChatRow || position == feedbackRuChatRow) {
                return 1;
            } else if (position == walletSectionRow || position == fieldSectionRow || position == whereGramsSectionRow || position == feedbackSectionRow) {
                return 2;
            } else if (position == jsonTypeRow || position == urlTypeRow || position == blockchainMainRow || position == blockchainTestRow) {
                return 4;
            } else if (position == fieldRow || position == blockchainNameRow) {
                return 5;
            } else {
                return 3;
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            if (holder.getItemViewType() == 4) {
                TypeCell cell = (TypeCell) holder.itemView;
                int position = holder.getAdapterPosition();
                if (position == urlTypeRow) {
                    cell.setTypeChecked(configType[networkType] == TonController.CONFIG_TYPE_URL);
                } else if (position == jsonTypeRow) {
                    cell.setTypeChecked(configType[networkType] == TonController.CONFIG_TYPE_JSON);
                } else if (position == blockchainMainRow) {
                    cell.setTypeChecked(networkType == UserConfig.NETWORK_TYPE_MAIN);
                } else if (position == blockchainTestRow) {
                    cell.setTypeChecked(networkType == UserConfig.NETWORK_TYPE_TEST);
                }
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 1 || type == 4;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }
    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        return new ThemeDescription[]{
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(listView, 0, null, null, null, null, Theme.key_windowBackgroundGray),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_wallet_blackBackground),
                new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_wallet_blackBackground),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_wallet_whiteText),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_wallet_whiteText),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_wallet_blackBackgroundSelector),

                new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class, TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),
                new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{ShadowSectionCell.class, TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGray),

                new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

                new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader),

                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteRedText2),

                new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),
                new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4),

                new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),

                new ThemeDescription(listView, 0, new Class[]{TypeCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{TypeCell.class}, new String[]{"checkImage"}, null, null, null, Theme.key_featuredStickers_addedIcon)
        };
    }
}
