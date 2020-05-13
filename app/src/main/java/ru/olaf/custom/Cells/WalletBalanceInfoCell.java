/*
 * This is the source code of Wallet for Android v. 1.0.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright Nikolai Kudashov, 2019-2020.
 */

package ru.olaf.custom.Cells;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.TonController;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.TypefaceSpan;
import org.tginfo.telegram.messenger.R;

@SuppressWarnings("FieldCanBeLocal")
public class WalletBalanceInfoCell extends FrameLayout {

    private SimpleTextView valueTextView;
    private TextView yourBalanceTextView;
    private Typeface defaultTypeFace;
    private RLottieDrawable gemDrawable;

    public WalletBalanceInfoCell(Context context) {
        super(context);

        valueTextView = new SimpleTextView(context);
        valueTextView.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));
        valueTextView.setTextSize(41);
        valueTextView.setDrawablePadding(AndroidUtilities.dp(7));
        valueTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        valueTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        addView(valueTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 35, 0, 0));

        gemDrawable = new RLottieDrawable(R.raw.wallet_gem, "" + R.raw.wallet_gem, AndroidUtilities.dp(42), AndroidUtilities.dp(42), false);
        gemDrawable.setAutoRepeat(1);
        gemDrawable.setAllowDecodeSingleFrame(true);
        gemDrawable.addParentView(valueTextView);
        valueTextView.setRightDrawable(gemDrawable);
        gemDrawable.start();

        yourBalanceTextView = new TextView(context);
        yourBalanceTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        yourBalanceTextView.setTextColor(Theme.getColor(Theme.key_wallet_whiteText));
        yourBalanceTextView.setGravity(Gravity.CENTER_HORIZONTAL);

        yourBalanceTextView.setFocusable(true);
        yourBalanceTextView.setClickable(true);
        yourBalanceTextView.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_wallet_blackBackgroundSelector), 7));
        yourBalanceTextView.setPadding(AndroidUtilities.dp(5), AndroidUtilities.dp(2), AndroidUtilities.dp(5), AndroidUtilities.dp(2));
        defaultTypeFace = yourBalanceTextView.getTypeface();
        addView(yourBalanceTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, AndroidUtilities.dp(10), 90, AndroidUtilities.dp(10), 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(236 + 6), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        gemDrawable.stop();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        gemDrawable.start();
    }

    public void setBalance(long balance, String walletAddress) {
        if (balance >= 0) {
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(TonController.formatCurrency(balance));
            int index = TextUtils.indexOf(stringBuilder, '.');
            if (index >= 0) {
                stringBuilder.setSpan(new TypefaceSpan(defaultTypeFace, AndroidUtilities.dp(27)), index + 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            valueTextView.setText(stringBuilder);
            valueTextView.setTranslationX(0);
            yourBalanceTextView.setVisibility(VISIBLE);

            yourBalanceTextView.setText(String.format("%s\n%s", LocaleController.getString("WalletTransactionsInfoWallet", R.string.WalletTransactionsInfoWallet), walletAddress));
            yourBalanceTextView.setOnClickListener(view -> {
                Context context = view.getContext();
                if (context == null)
                    return;

                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboardManager != null) {
                    Toast.makeText(context, LocaleController.getString("WalletTransactionAddressCopied", R.string.WalletTransactionAddressCopied), Toast.LENGTH_SHORT).show();
                    ClipData clipData = ClipData.newPlainText("GramWalletAddress", walletAddress);
                    clipboardManager.setPrimaryClip(clipData);
                }
            });
        } else {
            valueTextView.setText("");
            valueTextView.setTranslationX(-AndroidUtilities.dp(4));
            yourBalanceTextView.setVisibility(GONE);
        }
    }
}