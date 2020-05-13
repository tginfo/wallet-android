package ru.olaf.custom.Wallet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.TonController;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Wallet.WalletActionSheet;
import org.telegram.ui.Wallet.WalletActivity;
import org.telegram.ui.Wallet.WalletDateCell;
import org.telegram.ui.Wallet.WalletSyncCell;
import org.telegram.ui.Wallet.WalletTransaction;
import org.telegram.ui.Wallet.WalletTransactionCell;
import org.tginfo.telegram.messenger.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import retrofit2.Response;
import ru.olaf.custom.Api.Pojo.getTransactions.GetTransactionsRoot;
import ru.olaf.custom.Api.Pojo.getTransactions.Transaction;
import ru.olaf.custom.Api.Pojo.getWalletInformation.GetWalletInformationRoot;
import ru.olaf.custom.Api.Toncenter;
import ru.olaf.custom.Cells.WalletBalanceInfoCell;

public class WalletInfoActivity extends BaseFragment {

    private final static String PENDING_KEY = "pending";

    private Toncenter api;
    private String walletAddress;
    private Loader loader;
    private long balance;
    private BaseFragment parentFragment;
    private WalletActionSheet walletActionSheet;


    private RecyclerListView listView;
    private WalletInfoActivity.Adapter adapter;

    private Paint blackPaint = new Paint();
    private GradientDrawable backgroundDrawable;
    private float[] radii;

    private HashMap<Long, WalletTransaction> transactionsDict = new HashMap<>();
    private HashMap<String, ArrayList<WalletTransaction>> sectionArrays = new HashMap<>();
    private ArrayList<String> sections = new ArrayList<>();


    public WalletInfoActivity(String walletAddress, BaseFragment parentFragment, WalletActionSheet walletActionSheet) {
        super();
        this.walletAddress = walletAddress.replace("\n", "").replace("\r", "");
        this.parentFragment = parentFragment;
        this.walletActionSheet = walletActionSheet;
    }

    @Override
    public View createView(Context context) {

        blackPaint.setColor(Theme.getColor(Theme.key_wallet_blackBackground));
        backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        int r = AndroidUtilities.dp(13);
        backgroundDrawable.setCornerRadii(radii = new float[]{r, r, r, r, 0, 0, 0, 0});
        backgroundDrawable.setColor(Theme.getColor(Theme.key_wallet_whiteBackground));

        FrameLayout frameLayout = new FrameLayout(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                int bottom;
                RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(0);
                if (holder != null) {
                    bottom = holder.itemView.getBottom();
                } else {
                    bottom = 0;
                }
                float rad = AndroidUtilities.dp(13);
                if (bottom < rad) {
                    rad *= bottom / rad;
                }
                radii[0] = radii[1] = radii[2] = radii[3] = rad;
                canvas.drawRect(0, 0, getMeasuredWidth(), bottom + AndroidUtilities.dp(6), blackPaint);
                backgroundDrawable.setBounds(0, bottom - AndroidUtilities.dp(7), getMeasuredWidth(), getMeasuredHeight());
                backgroundDrawable.draw(canvas);
            }
        };
        frameLayout.setWillNotDraw(false);
        fragmentView = frameLayout;

        Drawable pinnedHeaderShadowDrawable = context.getResources().getDrawable(R.drawable.photos_header_shadow);
        pinnedHeaderShadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundGrayShadow), PorterDuff.Mode.MULTIPLY));

        listView = new RecyclerListView(context);
        listView.setSectionsType(2);
        listView.setItemAnimator(null);
        listView.setPinnedHeaderShadowDrawable(pinnedHeaderShadowDrawable);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(adapter = new WalletInfoActivity.Adapter(context));
        listView.setGlowColor(Theme.getColor(Theme.key_wallet_blackBackground));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                fragmentView.invalidate();
            }
        });
        listView.setOnItemClickListener((view, position) -> {
            if (getParentActivity() == null) {
                return;
            }
            if (view instanceof WalletTransactionCell) {
                WalletTransactionCell cell = (WalletTransactionCell) view;
                if (cell.isEmpty()) {
                    return;
                }
                WalletTransaction transaction = cell.getCurrentTransaction();

                if (!transaction.isEmpty)
                    presentFragment(new WalletInfoActivity(((WalletTransactionCell) view).getAddress(), WalletInfoActivity.this, null));
            }
        });


        api = new Toncenter(context);

        if (loader == null || loader.isCancelled()) {
            loader = new Loader(context);
            loader.execute();
        }
        return fragmentView;
    }

    @Override
    protected ActionBar createActionBar(Context context) {
        ActionBar actionBar = new ActionBar(context);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_wallet_blackBackground));
        actionBar.setTitleColor(Theme.getColor(Theme.key_wallet_whiteText));
        actionBar.setItemsColor(Theme.getColor(Theme.key_wallet_whiteText), false);
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_wallet_blackBackgroundSelector), false);
        actionBar.setTitle(LocaleController.getString("WalletTransactionsInfo", R.string.WalletTransactionsInfo));


        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        return actionBar;
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        AndroidUtilities.removeAdjustResize(getParentActivity(), classGuid);

        if (loader != null && !loader.isCancelled())
            loader.cancel(true);


        if (parentFragment instanceof WalletActivity && walletActionSheet != null)
            walletActionSheet.show();

    }

    @Override
    public void removeSelfFromStack() {
        super.removeSelfFromStack();
        if (parentFragment != null) {
            parentFragment.removeSelfFromStack();
        }
    }


    private void fillTransactions(ArrayList<WalletTransaction> arrayList) {
        boolean cleared = sections.isEmpty();
        if (arrayList != null && !arrayList.isEmpty()) {
            WalletTransaction transaction = arrayList.get(arrayList.size() - 1);
            for (int b = 0, N2 = sections.size(); b < N2; b++) {
                if (PENDING_KEY.equals(sections.get(b))) {
                    continue;
                }
                String key = sections.get(b);
                ArrayList<WalletTransaction> existingTransactions = sectionArrays.get(key);
                if (existingTransactions.get(0).rawTransaction.utime < transaction.rawTransaction.utime) {
                    sections.clear();
                    sectionArrays.clear();
                    transactionsDict.clear();
                    getTonController().clearPendingCache();
                    cleared = true;
                } else {
                    Collections.reverse(arrayList);
                }
                break;
            }
        }

        ArrayList<WalletTransaction> pendingTransactions = getTonController().getPendingTransactions();
        if (pendingTransactions.isEmpty()) {
            if (sectionArrays.containsKey(PENDING_KEY)) {
                sectionArrays.remove(PENDING_KEY);
                sections.remove(0);
            }
        } else {
            if (!sectionArrays.containsKey(PENDING_KEY)) {
                sections.add(0, PENDING_KEY);
                sectionArrays.put(PENDING_KEY, pendingTransactions);
            }
        }

        Calendar calendar = Calendar.getInstance();

        for (int a = 0, N = arrayList.size(); a < N; a++) {
            WalletTransaction transaction = arrayList.get(a);
            if (transactionsDict.containsKey(transaction.rawTransaction.transactionId.lt)) {
                continue;
            }

            calendar.setTimeInMillis(transaction.rawTransaction.utime * 1000);
            int dateDay = calendar.get(Calendar.DAY_OF_YEAR);
            int dateYear = calendar.get(Calendar.YEAR);
            int dateMonth = calendar.get(Calendar.MONTH);
            String dateKey = String.format(Locale.US, "%d_%02d_%02d", dateYear, dateMonth, dateDay);
            ArrayList<WalletTransaction> transactions = sectionArrays.get(dateKey);
            if (transactions == null) {
                int addToIndex = sections.size();
                for (int b = 0, N2 = sections.size(); b < N2; b++) {
                    if (PENDING_KEY.equals(sections.get(b))) {
                        continue;
                    }
                    String key = sections.get(b);
                    ArrayList<WalletTransaction> existingTransactions = sectionArrays.get(key);
                    if (existingTransactions.get(0).rawTransaction.utime < transaction.rawTransaction.utime) {
                        addToIndex = b;
                        break;
                    }
                }
                transactions = new ArrayList<>();
                sections.add(addToIndex, dateKey);
                sectionArrays.put(dateKey, transactions);
            }

            if (!cleared) {
                transactions.add(0, transaction);
            } else {
                transactions.add(transaction);
            }
            transactionsDict.put(transaction.rawTransaction.transactionId.lt, transaction);
        }
    }

    private class Adapter extends RecyclerListView.SectionsAdapter {

        private Context context;
        private boolean isLoading = true;

        Adapter(Context c) {
            context = c;
        }

        @Override
        public boolean isEnabled(int section, int row) {
            return section != 0 && row != 0;
        }

        @Override
        public Object getItem(int section, int position) {
            return null;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0: {
                    view = new WalletBalanceInfoCell(context);
                    break;
                }
                case 1: {
                    view = new WalletTransactionCell(context) {
                        @Override
                        protected void updateRowWithTransaction(WalletTransaction transaction) {
                            adapter.notifyDataSetChanged();
                        }
                    };
                    break;
                }
                case 3: {
                    view = new WalletDateCell(context);
                    break;
                }
                case 5: {
                    view = new View(context) {
                        @Override
                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            int n = listView.getChildCount();
                            int itemsCount = adapter.getItemCount();
                            int totalHeight = 0;
                            for (int i = 0; i < n; i++) {
                                View view = listView.getChildAt(i);
                                int pos = listView.getChildAdapterPosition(view);
                                if (pos != 0 && pos != itemsCount - 1) {
                                    totalHeight += listView.getChildAt(i).getMeasuredHeight();
                                }
                            }
                            int paddingHeight = fragmentView.getMeasuredHeight() - totalHeight;
                            if (paddingHeight <= 0) {
                                paddingHeight = 0;
                            }
                            setMeasuredDimension(listView.getMeasuredWidth(), paddingHeight);
                        }
                    };
                    break;
                }
                case 6:
                default: {
                    view = new WalletSyncCell(context) {
                        @Override
                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                            int height = Math.max(AndroidUtilities.dp(280), fragmentView.getMeasuredHeight() - AndroidUtilities.dp(236 + 6));
                            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                        }
                    };
                    break;
                }
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(int section, int position, RecyclerView.ViewHolder holder) {
            switch (holder.getItemViewType()) {
                case 0: {
                    WalletBalanceInfoCell balanceCell = (WalletBalanceInfoCell) holder.itemView;
                    balanceCell.setBalance(balance, walletAddress);
                    break;
                }
                case 1: {
                    WalletTransactionCell transactionCell = (WalletTransactionCell) holder.itemView;
                    section -= 1;
                    String key = sections.get(section);
                    ArrayList<WalletTransaction> arrayList = sectionArrays.get(key);
                    transactionCell.setTransaction(arrayList.get(position - 1), position != arrayList.size());
                    break;
                }
                case 3: {
                    WalletDateCell dateCell = (WalletDateCell) holder.itemView;
                    section -= 1;
                    String key = sections.get(section);
                    if (PENDING_KEY.equals(key)) {
                        dateCell.setText(LocaleController.getString("WalletPendingTransactions", R.string.WalletPendingTransactions));
                    } else {
                        ArrayList<WalletTransaction> arrayList = sectionArrays.get(key);
                        dateCell.setDate(arrayList.get(0).rawTransaction.utime);
                    }
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int section, int position) {
            if (section == 0) {
                return 0;
            } else {
                if (isLoading)
                    return 6;

                section -= 1;
                if (section < sections.size()) {
                    return position == 0 ? 3 : 1;
                } else {
                    return 5;
                }
            }
        }

        @Override
        public int getSectionCount() {
            if (isLoading)
                return 2;

            int count = 1;
            if (!sections.isEmpty()) {
                count += sections.size() + 1;
            }
            return count;
        }

        @Override
        public int getCountForSection(int section) {
            if (section == 0) {
                return 1;
            }
            section -= 1;
            if (section < sections.size()) {
                return sectionArrays.get(sections.get(section)).size() + 1;
            } else {
                return 1;
            }
        }

        @Override
        public View getSectionHeaderView(int section, View view) {
            if (view == null) {
                view = new WalletDateCell(context);
                view.setBackgroundColor(Theme.getColor(Theme.key_wallet_whiteBackground) & 0xe5ffffff);
            }
            WalletDateCell dateCell = (WalletDateCell) view;
            if (section == 0) {
                dateCell.setAlpha(0.0f);
            } else {
                section -= 1;
                if (section < sections.size()) {
                    view.setAlpha(1.0f);
                    String key = sections.get(section);
                    if (PENDING_KEY.equals(key)) {
                        dateCell.setText(LocaleController.getString("WalletPendingTransactions", R.string.WalletPendingTransactions));
                    } else {
                        ArrayList<WalletTransaction> arrayList = sectionArrays.get(key);
                        dateCell.setDate(arrayList.get(0).rawTransaction.utime);
                    }
                }
            }
            return view;
        }

        @Override
        public String getLetter(int position) {
            return null;
        }

        @Override
        public int getPositionForScrollProgress(float progress) {
            return 0;
        }

        public void setLoading(boolean loading) {
            isLoading = loading;
            notifyDataSetChanged();
        }
    }

    private class Loader extends AsyncTask<Void, Void, Void> {

        Context context;

        public Loader(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (adapter != null)
                adapter.setLoading(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Response<GetTransactionsRoot> getTransactionsResponse = api.getMethods().getTransactions(walletAddress).execute();
                Response<GetWalletInformationRoot> getWalletInformationResponse = api.getMethods().getWalletInformation(walletAddress).execute();
                if (getTransactionsResponse.body() == null || !getTransactionsResponse.body().getOk()
                        || getWalletInformationResponse.body() == null || !getWalletInformationResponse.body().getOk()) {
                    AlertsCreator.showSimpleAlert(WalletInfoActivity.this, LocaleController.getString("Wallet", R.string.Wallet), LocaleController.getString("WalletTransactionsInfoError", R.string.WalletTransactionsInfoError));
                    return null;
                }

                ArrayList<WalletTransaction> internalTransactions = new ArrayList<>();
                for (Transaction transaction : getTransactionsResponse.body().getTransactions()) {
                    internalTransactions.add(Toncenter.getInternalTransaction(transaction));
                }

                fillTransactions(internalTransactions);
                balance = getWalletInformationResponse.body().getResult().getBalance();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.setLoading(false);
        }
    }
}