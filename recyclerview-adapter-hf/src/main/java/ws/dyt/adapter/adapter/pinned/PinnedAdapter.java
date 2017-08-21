package ws.dyt.adapter.adapter.pinned;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.List;

import ws.dyt.adapter.R;
import ws.dyt.adapter.adapter.ItemWrapper;
import ws.dyt.adapter.adapter.swipe.SwipeAdapter;
import ws.dyt.adapter.adapter.swipe.SwipeLayout;
import ws.dyt.adapter.viewholder.BaseViewHolder;

/**
 * Created by yangxiaowei on 16/8/8.
 *
 * 粘性item适配器,继承自{@link SwipeAdapter},所以支持{@link SwipeAdapter}所支持的所有功能,
 * 值得注意的一点是该适配器对数据有要求(因为粘性是根据数据的type决定的),所以数据都需要继承自{@link ItemWrapper}(其实是一个数据分组的概念)
 */
abstract
public class PinnedAdapter<T extends ItemWrapper> extends SwipeAdapter<T> implements IPinnedItemViewType, IConvert {
    public PinnedAdapter(Context context, List<T> datas) {
        super(context, datas);
    }

    public PinnedAdapter(Context context, List<T> datas, @LayoutRes int itemLayoutResId) {
        super(context, datas, itemLayoutResId);
    }

    private interface PinnedItemRange {
        int FIRST    = 1;
        int MIDDLE   = 2;
        //有footer时的处理
        int FOOTER   = 3;
    }


    @Override
    final
    public BaseViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        BaseViewHolder holder = super.onCreateHolder(parent, viewType);

        View headerView = mInflater.inflate(getPinnedItemViewLayout(), parent, false);
        PinnedLayout pinnedLayout = new PinnedLayout(mContext);
        pinnedLayout.setUpView(parent, holder.itemView, headerView);

        //说明客户端设置了支持滑动菜单
        if (holder.itemView instanceof SwipeLayout) {
            View itemView = ((SwipeLayout) holder.itemView).getItemView();
            itemView.setClickable(true);

            return new BaseViewHolder(pinnedLayout, itemView);
        }else {
            return new BaseViewHolder(pinnedLayout);
        }
    }

    @Override
    final
    protected void onBindHolder(BaseViewHolder holder, int position) {
        PinnedLayout pinnedLayout = (PinnedLayout) holder.itemView;
        //每个组的第一条需要设置header可见
        if (0 == position || getItem(position).type != getItem(position - 1).type) {
            pinnedLayout.pinnedView.setVisibility(View.VISIBLE);
            holder.itemView.setTag(R.string.pinned_item_status, PinnedItemRange.FIRST);
        } else {
            pinnedLayout.pinnedView.setVisibility(View.GONE);
            holder.itemView.setTag(R.string.pinned_item_status, PinnedItemRange.MIDDLE);
        }

        final int type = getItem(position).type;
        holder.itemView.setTag(R.string.pinned_item_data_type, type);
        holder.itemView.setTag(R.string.item_index_in_datasection, position);
        super.onBindHolder(holder, position);

        this.convertPinnedHolder(holder, position, type);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        try {
            //父容器是FrameLayout,否则让粘性功能失效
            FrameLayout parent = (FrameLayout) recyclerView.getParent();
            if (null == this.pinnedViewHolder) {
                this.initPinnedView(parent, recyclerView);
            }
            recyclerView.addOnScrollListener(scrollListener);
        } catch (Exception e) {
            Log.e("DEBUG", "RecyclerView parent must be FrameLayout");
            Toast.makeText(mContext, "RecyclerView parent must be FrameLayout", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        recyclerView.removeOnScrollListener(scrollListener);
    }

    private BaseViewHolder pinnedViewHolder;
    private View topPinnedView;

    private void initPinnedView(ViewGroup parent, RecyclerView recyclerView) {
        this.topPinnedView = mInflater.inflate(getPinnedItemViewLayout(), recyclerView, false);
        this.pinnedViewHolder = new BaseViewHolder(topPinnedView);
        parent.addView(this.topPinnedView);
        //初始让不可见
        this.topPinnedView.setVisibility(View.GONE);
    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if ( firstVisibleItemIndex == firstCompletelyVisibleItemIndex && 0 == firstVisibleItemIndex) {
                topPinnedView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            final int pinnedViewWidth = topPinnedView.getMeasuredWidth();
            final int pinnedViewHeight = topPinnedView.getMeasuredHeight();

            //获取Adapter中粘性头部位置上方的控件
            View headerView = recyclerView.findChildViewUnder(pinnedViewWidth / 2, 5);

            Object obj = null == headerView ? null : headerView.getTag(R.string.pinned_item_data_type);
            if (null != obj && obj instanceof Integer) {
                int position = (int) headerView.getTag(R.string.item_index_in_datasection);
                convertPinnedHolder(pinnedViewHolder, position, (int) obj);
            }

            //获取Adapter中粘性头部位置下方的控件
            headerView = recyclerView.findChildViewUnder(pinnedViewWidth / 2, pinnedViewHeight + 1);

            if (null == headerView) {
                return;
            }

            obj = headerView.getTag(R.string.pinned_item_status);
            if (null == obj || !(obj instanceof Integer)) {
                obj = PinnedItemRange.FOOTER;
            }

            int transViewStatus = (int) obj;

            int translationY = headerView.getTop() - pinnedViewHeight;
            if (transViewStatus == PinnedItemRange.FIRST || transViewStatus == PinnedItemRange.FOOTER) {
                if (headerView.getTop() > 0) {
                    topPinnedView.setTranslationY(translationY);
                } else {
                    topPinnedView.setTranslationY(0);
                }
            } else if (transViewStatus == PinnedItemRange.MIDDLE) {
                topPinnedView.setTranslationY(0);
            }


            findFirstAndLastVisibleItemIndex(FindItemType.FIRST);
            if (getItemTypeByPosition(firstVisibleItemIndex) != ItemTypeSummary.DATA) {
                //添加了header后，需要对header处进行处理，只要header出现时需要隐藏
                topPinnedView.setVisibility(View.GONE);
            }else {
                //在RecyclerView上贴了粘性头部后会遮挡效果，下面处理一下
                if (firstVisibleItemIndex == firstCompletelyVisibleItemIndex && 0 == firstVisibleItemIndex) {
                    if (View.GONE != topPinnedView.getVisibility()) {
                        topPinnedView.setVisibility(View.GONE);
                    }
                }else {
                    if (View.VISIBLE != topPinnedView.getVisibility()) {
                        topPinnedView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    };

}