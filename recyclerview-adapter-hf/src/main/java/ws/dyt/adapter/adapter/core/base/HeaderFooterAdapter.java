package ws.dyt.adapter.adapter.core.base;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ws.dyt.adapter.adapter.Log.L;
import ws.dyt.adapter.viewholder.BaseViewHolder;

/**
 * Created by yangxiaowei on 16/6/8.
 *
 * 带有头部、尾部的 {@link RecyclerView} 适配器，item结构如下
 * {item_sys_header - item_header - item_data - item_footer - item_sys_footer}
 * 1. 系统尾部 sys_footer_item 目前只支持设置一个view
 */
abstract
public class HeaderFooterAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> implements ISysHeader, IUserHeader, ISysFooter, IUserFooter, IFullSpanItemView, IGC{
    protected Context mContext;
    protected LayoutInflater mInflater;
    protected RecyclerView recyclerView;
    protected List<View> mHeaderViews = new ArrayList<>();
    protected List<View> mFooterViews = new ArrayList<>();
    //逻辑上设计为系统头部也可以是多个 ，但是实现上系统头部实现为仅有一个
    private List<View> mSysHeaderViews = new ArrayList<>();
    private View mSysFooterView;
    //真实的数据部分
    protected List<T> realData;

    public HeaderFooterAdapter(Context context, List<T> realData) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.realData = null == realData ? new ArrayList<T>() : realData;
    }

    public HeaderFooterAdapter(Context context, List<List<T>> sectionData, int unused) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        if (null == sectionData) {
            this.realData = new ArrayList<T>();
        }else {
            if (sectionData.isEmpty()) {
                return;
            }
            for (List<T> e:sectionData) {
                if (null == e || e.isEmpty()) {
                    continue;
                }
                this.realData.addAll(e);
            }
        }
    }

    /**
     * 针对数据项
     * api 中永远保留
     * @return
     */
    final
    @Deprecated
    protected boolean isEmpty() {
        return null == this.realData || this.realData.isEmpty();
    }

    final
    public T getItem(int position) {
        return isEmpty() ? null : this.realData.get(position);
    }

    /**
     * 所有数据项
     * @return
     */
    @Override
    final
    public int getItemCount() {
        return
                this.getSysHeaderViewCount() +
                this.getHeaderViewCount() +
                this.getDataSectionItemCount() +
                this.getFooterViewCount() +
                this.getSysFooterViewCount();
    }

    /**
     * 只针对数据区域项
     *
     * @return
     */
    public int getDataSectionItemCount() {
        return isEmpty() ? 0 : this.realData.size();
    }

    @Override
    final
    public int getSysHeaderViewCount(){
        return this.mSysHeaderViews.size();
    }

    @Override
    final
    public boolean isEmptyOfSysHeaders() {
        return 0 == getSysHeaderViewCount();
    }

    @Override
    final
    public int getHeaderViewCount() {
        return this.mHeaderViews.size();
    }

    @Override
    final
    public boolean isEmptyOfHeaders() {
        return 0 == getHeaderViewCount();
    }

    @Override
    final
    public int getFooterViewCount() {
        return this.mFooterViews.size();
    }

    @Override
    final
    public boolean isEmptyOfFooters() {
        return 0 == getFooterViewCount();
    }

    @Override
    //系统添加footer数量
    final
    public int getSysFooterViewCount() {
        return null == this.mSysFooterView ? 0 : 1;
    }

    @Override
    final
    public boolean isEmptyOfSysFooters() {
        return 0 == getSysFooterViewCount();
    }

    @Deprecated
    public int getAllHeaderViewCount() {
        return this.getAllHeaderViewsCount();
    }

    public int getAllHeaderViewsCount() {
        return this.getSysHeaderViewCount() + this.getHeaderViewCount();
    }

    public List<View> getAllHeaderViews() {

        if (mSysHeaderViews.isEmpty() && mHeaderViews.isEmpty()) {

            return Collections.EMPTY_LIST;
        }

        List<View> views = new ArrayList<>(mSysHeaderViews.size() + mHeaderViews.size());
        if (!mSysHeaderViews.isEmpty()) {

            views.addAll(mSysHeaderViews);
        }
        if (!mHeaderViews.isEmpty()) {

            views.addAll(mHeaderViews);
        }
        return views;
    }

    @Deprecated
    public int getAllFooterViewCount() {
        return this.getAllFooterViewsCount();
    }

    public int getAllFooterViewsCount() {
        return this.getSysFooterViewCount() + this.getFooterViewCount();
    }

    public List<View> getAllFooterViews() {

        if (mFooterViews.isEmpty() && null == mSysFooterView) {

            return Collections.EMPTY_LIST;
        }else {

            if (null == mSysFooterView) {

                return new ArrayList<>(mFooterViews);
            }else {

                List<View> views = new ArrayList<>(1);
                views.add(mSysFooterView);
                return views;
            }
        }

    }

    @Override
    final
    public int getItemViewType(int position) {
        int shc = this.getSysHeaderViewCount();
        int hc = this.getHeaderViewCount();
        int fc = this.getFooterViewCount();
        int dc = this.getDataSectionItemCount();

        int hAll = shc + hc;

        //处理数据项
        if ((position >= hAll) && (position < (hAll + dc))) {
            position = position - hAll;
            return this.mapDataSectionItemViewTypeToItemLayoutId(position);
        }

        //处理系统头部
        if (shc > 0 && position < shc) {
            return this.mSysHeaderViews.get(position).hashCode();
        }

        //处理头部
        if (hc > 0 && position >= shc && position < hAll) {
            position = position - shc;
            return this.mHeaderViews.get(position).hashCode();
        }

        //处理尾部
        if (fc > 0 && position >= (hAll + dc) && position < (hAll + dc + fc)) {
            position = position - (hAll + dc);
            return this.mFooterViews.get(position).hashCode();
        }

        int sfc = this.getSysFooterViewCount();
        //处理系统尾部
        if (sfc > 0 && position >= (getItemCount() - sfc)) {
            return this.mSysFooterView.hashCode();
        }
        return super.getItemViewType(position);
    }

    /**
     * 初始化数据域类型,总是从0开始，已经除去头部
     * @param positionOffsetHeaders
     * @return
     */
    protected int mapDataSectionItemViewTypeToItemLayoutId(int positionOffsetHeaders) {
        return 0;
    }

    private View getSysFooterViewByHashCode(int hashCode) {
        return this.getViewByHashCodeFromList(this.mSysHeaderViews, hashCode);
    }

    private View getHeaderViewByHashCode(int hashCode) {
        return this.getViewByHashCodeFromList(this.mHeaderViews, hashCode);
    }

    private View getFooterViewByHashCode(int hashCode) {
        return this.getViewByHashCodeFromList(this.mFooterViews, hashCode);
    }

    private View getViewByHashCodeFromList(List<View> views, int hashCode) {
        if (null == views) {
            return null;
        }
        for (int i = 0; i < views.size(); i++) {
            View v = views.get(i);
            if (v.hashCode() == hashCode) {
                return v;
            }
        }
        return null;
    }

    private int getViewPositionByHashCodeFromList(List<View> views, int hashCode) {
        if (null == views) {
            return NO_POSITION;
        }
        for (int i = 0; i < views.size(); i++) {
            View v = views.get(i);
            if (v.hashCode() == hashCode) {
                return i;
            }
        }
        return NO_POSITION;
    }

    @Override
    final
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        //处理系统头部
        View sysHeaderView = this.getSysFooterViewByHashCode(viewType);
        if (null != sysHeaderView) {
            return new BaseViewHolder(sysHeaderView);
        }

        //处理头部
        View headerView = this.getHeaderViewByHashCode(viewType);
        if (null != headerView) {
            return new BaseViewHolder(headerView);
        }

        //处理尾部
        View footerView = this.getFooterViewByHashCode(viewType);
        if (null != footerView) {
            return new BaseViewHolder(footerView);
        }

        //处理系统尾部
        View sysFooterView = this.mSysFooterView;
        if (null != sysFooterView && viewType == sysFooterView.hashCode()) {
            return new BaseViewHolder(sysFooterView);
        }

        //事件只针对正常数据项
        final BaseViewHolder holder = this.onCreateHolder(parent, viewType);
        this.initItemListener(holder/*, viewType*/);
        return holder;
    }

    protected void initItemListener(final BaseViewHolder holder/*, final int viewType*/){
        if (null == holder) {
            return;
        }
        holder.eventItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HeaderFooterAdapter.this.onItemClick(holder, v);
            }
        });

        holder.eventItemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return HeaderFooterAdapter.this.onItemLongClick(holder, v);
            }
        });
    }


    protected void onItemClick(final BaseViewHolder holder, View view){
        if (null == this.onItemClickListener) {
            return;
        }
        int hAll = this.getHeaderViewCount() + this.getSysHeaderViewCount();
        this.onItemClickListener.onItemClick(view, holder.getAdapterPosition() - hAll);
    }

    protected boolean onItemLongClick(final BaseViewHolder holder, View view){
        if (null == this.onItemLongClickListener) {
            return false;
        }
        int hAll = this.getHeaderViewCount() + this.getSysHeaderViewCount();
        this.onItemLongClickListener.onItemLongClick(view, holder.getAdapterPosition() - hAll);
        return true;
    }

    /**
     * [for extend] 重新定义data_section域的item生成方式
     * @param parent
     * @param viewType
     * @return
     */
    abstract
    public BaseViewHolder onCreateHolder(ViewGroup parent, int viewType);

    @Override
    final
    public void onBindViewHolder(BaseViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    final
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        int shc = this.getSysHeaderViewCount();
        //item_sys_header
        if (0 != shc && position < shc) {
            return;
        }

        int hc = this.getHeaderViewCount();
        int hAll = shc + hc;
        //item_header
        if (0 != hc && position < hAll) {
            return;
        }

        int fc = this.getFooterViewCount();
        int dc = this.getDataSectionItemCount();
        //item_footer
        if (0 != fc && position >= (hAll + dc) && position < (hAll + dc + fc)) {
            return;
        }

        int sfc = this.getSysFooterViewCount();
        //item_sys_footer
        if (0 != sfc && position >= (this.getItemCount() - sfc)) {
            return;
        }

        //item_data
        this.onBindHolder(holder, position - hAll);
    }

    /**
     * [for extend]
     * @param holder
     * @param position
     */
    @CallSuper
    protected void onBindHolder(BaseViewHolder holder, int position) {
        this.convert(holder, position);
    }

    /**
     * 绑定数据
     *
     * @param holder
     * @param position  数据域索引从0开始，已经除去头部
     */
    abstract
    public void convert(BaseViewHolder holder, int position);

    /**
     * 参数校验
     *
     * @param view
     * @param position
     * @param views
     * @return
     */
    private boolean validateAddViewParams(View view, int position, List<View> views) {
        if (null == view) {
            return false;
        }
        if (null == views) {
            views = new ArrayList<>();
        }
        if (views.contains(view)) {
            L.w("Adapter had contains view");
            return false;
        }

        if (position < 0 || position > views.size()) {
            throw new IndexOutOfBoundsException("header or footer position out of bounds");
        }
        return true;
    }

    @Override
    final
    public int addSysHeaderView(View view) {
        final int position = null == mSysHeaderViews ? 0 : mSysHeaderViews.size();
        this.addSysHeaderView(view, position);
        return position;
    }

    public static final int NO_POSITION = -1;
    @Override
    final
    public void addSysHeaderView(View view, int position) {
        if (!this.validateAddViewParams(view, position, this.mSysHeaderViews)) {
            return;
        }

        this.mSysHeaderViews.add(position, view);

        notifyItemInserted(position);
    }

    /**
     * 清除所有sys_header
     * @param view
     */
    @Override
    final
    public int setSysHeaderView(View view) {
        if (null == view) {
            return NO_POSITION;
        }

        int index = getSysHeaderViewCount();
        if (index > 0) {
            this.mSysHeaderViews.clear();
            notifyItemRangeRemoved(0, index);
        }

        this.mSysHeaderViews.add(view);
        index = 0;
        notifyItemInserted(index);
        return index;
    }

    @Override
    final
    public int removeSysHeaderView(View view) {
        if (null == view || !this.mSysHeaderViews.contains(view)) {
            return NO_POSITION;
        }
        int index = this.mSysHeaderViews.indexOf(view);
        this.mSysHeaderViews.remove(view);
        notifyItemRemoved(index);
        return index;
    }

    /**
     * 判断item是否为系统头部
     * @param position
     * @return
     */
    @Override
    final
    public boolean isSysHeaderItemView(int position) {
        int shc = this.getSysHeaderViewCount();
        return position >= 0 && 0 != shc && position < shc;
    }

    @Override
    final
    public int findSysHeaderViewPosition(View view) {
        if (null == view) {
            return NO_POSITION;
        }
        return getViewPositionByHashCodeFromList(mSysHeaderViews, view.hashCode());
    }

    @Override
    final
    public void clearSysHeaders() {
        final int size = this.getSysHeaderViewCount();
        this.mSysHeaderViews.clear();
        notifyItemRangeRemoved(0, size);
//        notifyItemRangeChanged(0, size);
    }

    @Override
    final
    public int addHeaderView(View view) {
        final int position = null == mHeaderViews ? 0 : mHeaderViews.size();
        this.addHeaderView(view, position);
        return position;
    }

    @Override
    final
    public void addHeaderView(View view, int position) {
        if (!this.validateAddViewParams(view, position, this.mHeaderViews)) {
            return;
        }

        this.mHeaderViews.add(position, view);
        final int shc = getSysHeaderViewCount();
        final int index = shc + position;
        notifyItemInserted(index);
    }

    @Override
    final
    public int removeHeaderView(View view) {
        if (null == view || !this.mHeaderViews.contains(view)) {
            return NO_POSITION;
        }
        final int shc = this.getSysHeaderViewCount();
        final int index = shc + this.mHeaderViews.indexOf(view);
        this.mHeaderViews.remove(view);
        notifyItemRemoved(index);
        return index;
    }

    /**
     * 用来判断item是否为头部
     * @param position
     * @return
     */
    @Override
    final
    public boolean isHeaderItemView(int position) {
        int shc = this.getSysHeaderViewCount();
        int hc = shc + this.getHeaderViewCount();
        return position >= 0 && 0 != hc && position < hc;
    }

    @Override
    final
    public int findHeaderViewPosition(View view) {
        if (null == view) {
            return NO_POSITION;
        }
        return getViewPositionByHashCodeFromList(mHeaderViews, view.hashCode());
    }

    @Override
    final
    public void clearHeaders() {
        final int size = this.getHeaderViewCount();
        this.mHeaderViews.clear();
        notifyItemRangeChanged(getSysHeaderViewCount(), size);
    }

    @Override
    final
    public int addFooterView(View view) {
        final int position = null == mFooterViews ? 0 : mFooterViews.size();
        this.addFooterView(view, position);
        return position;
    }

    @Override
    final
    public void addFooterView(View view, int position) {
        if (!this.validateAddViewParams(view, position, this.mFooterViews)) {
            return;
        }

        this.mFooterViews.add(position, view);
        final int shc = this.getSysHeaderViewCount();
        final int hc = shc + this.getHeaderViewCount();
        final int dc = this.getDataSectionItemCount();
        notifyItemInserted(hc + dc + position);
    }

    @Override
    final
    public int removeFooterView(View view) {
        if (null == view) {
            return NO_POSITION;
        }
        int index = this.mFooterViews.indexOf(view);
        this.removeFooterView(index);
        return index;
    }

    @Override
    final
    public void removeFooterView(int position) {
        if (position < 0 || position >= mFooterViews.size()) {
            return;
        }
        final int shc = this.getSysHeaderViewCount();
        final int hc = this.getHeaderViewCount();
        final int hAll = shc + hc;
        final int dc = this.getDataSectionItemCount();
        final int index = position;
        this.mFooterViews.remove(index);
        notifyItemRemoved(hAll + dc + index);
    }

    /**
     * 用来判断item是否为尾部
     * @param position
     * @return
     */
    @Override
    final
    public boolean isFooterItemView(int position) {
        final int shc = this.getSysHeaderViewCount();
        final int hc = shc + this.getHeaderViewCount();
        final int dc = this.getDataSectionItemCount();
        final int fc = this.getFooterViewCount();
        return position >= 0 && fc != 0 && position >= (hc + dc) && position < (hc + dc + fc);
    }

    @Override
    final
    public int findFooterViewPosition(View view) {
        if (null == view) {
            return NO_POSITION;
        }
        return getViewPositionByHashCodeFromList(mFooterViews, view.hashCode());
    }

    @Override
    final
    public void clearFooters() {
        final int size = this.getFooterViewCount();
        this.mFooterViews.clear();
        final int index = getAllHeaderViewsCount() + getDataSectionItemCount();
        notifyItemRangeRemoved(index, size);
    }

    @Override
    final
    public int setSysFooterView(View view) {
        if (null == view) {
            return NO_POSITION;
        }
        if (this.mSysFooterView == view) {
            L.w("Adapter had contains view");
            return NO_POSITION;
        }
        this.mSysFooterView = view;
        final int index = getItemCount();
        notifyItemChanged(index);
        return index;
    }

    @Override
    final
    public int removeSysFooterView(View view) {
        if (null != this.mSysFooterView && null != view && this.mSysFooterView == view) {
            this.mSysFooterView = null;
            final int index = getItemCount();
            notifyItemRemoved(index);
            return index;
        }
        return NO_POSITION;
    }

    /**
     * 用来判断item是否为系统尾部
     * @param position
     * @return
     */
    @Override
    final
    public boolean isSysFooterView(int position) {
        int sfc = this.getSysFooterViewCount();
        int ic = this.getItemCount();
        return position >= 0 && sfc != 0 && position >= (ic - sfc);
    }

    @Override
    final
    public int findSysFooterViewPosition(View view) {
        if (null == view) {
            return NO_POSITION;
        }
        return getViewPositionByHashCodeFromList(mSysHeaderViews, view.hashCode());
    }

    @Override
    final
    public void clearSysFooters() {
        final int size = this.getSysFooterViewCount();
        this.mSysFooterView = null;
        final int index = getAllHeaderViewsCount() + getDataSectionItemCount() + getFooterViewCount();
        notifyItemRangeRemoved(index, size);
    }

    /**
     * 用来判断item是否为真实数据项，除了头部、尾部、系统尾部等非真实数据项，结构为:
     * item_header - item_data - item_footer - item_sys_footer
     * @param position
     * @return true:将保留LayoutManager的设置  false:该item将会横跨整行(对GridLayoutManager,StaggeredLayoutManager将很有用)
     */
    private boolean isDataItemView(int position) {
        int shc = this.getSysHeaderViewCount();
        int hc = this.getHeaderViewCount();
        int hAll = shc + hc;
        int dc = this.getDataSectionItemCount();
        boolean isHeaderOrFooter = position >= 0 && position >= hAll && position < (hAll + dc);
        if (!isHeaderOrFooter) {
            return isHeaderOrFooter;
        }
        //这里需要取反
        return !this.isFullSpanWithItemView(position - hAll);
    }

    /**
     * 设置数据域item是否横跨
     * @param position
     * @return
     */
    @Override
    public boolean isFullSpanWithItemView(int position) {
        return false;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (this.recyclerView == recyclerView) {
            return;
        }
        this.recyclerView = recyclerView;
        this.layoutManager = recyclerView.getLayoutManager();
        this.adapterGridLayoutManager();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
        this.release();
    }

    @Override
    public void onViewAttachedToWindow(BaseViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        this.adapterStaggeredGridLayoutManager(holder);
    }

    @Override
    public void onViewDetachedFromWindow(BaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(BaseViewHolder holder) {
        super.onViewRecycled(holder);
//        if (holder.itemView instanceof SwipeLayout) {
//            SwipeLayout swipeLayout = (SwipeLayout) holder.itemView;
//            swipeLayout.release();
//        }
    }

    private void adapterGridLayoutManager() {
        final RecyclerView.LayoutManager layoutManager = null == recyclerView ? null : recyclerView.getLayoutManager();
        if (null == layoutManager) {
            return;
        }
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager glm = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup ssl = glm.getSpanSizeLookup();
            glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return !isDataItemView(position) ? glm.getSpanCount() : ssl.getSpanSize(position);
                }
            });
        }
    }

    private void adapterStaggeredGridLayoutManager(BaseViewHolder holder) {
        final RecyclerView.LayoutManager layoutManager = null == recyclerView ? null : recyclerView.getLayoutManager();
        if (null == layoutManager) {
            return;
        }

        if (layoutManager instanceof StaggeredGridLayoutManager) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            int position = holder.getAdapterPosition();
            if (null != lp && lp instanceof StaggeredGridLayoutManager.LayoutParams && !isDataItemView(position)) {
                ((StaggeredGridLayoutManager.LayoutParams) lp).setFullSpan(true);
            }
        }
    }


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            ItemTypeSummary.HEADER_SYS,
            ItemTypeSummary.HEADER_USR,
            ItemTypeSummary.DATA,
            ItemTypeSummary.FOOTER_USR,
            ItemTypeSummary.FOOTER_SYS
    })
    public @interface ItemTypeSummaryWhere{}
    protected interface ItemTypeSummaryPrivate {
        int HEADER_SYS = 0;
        int HEADER_USR = 1;
        int FOOTER_USR = 3;
        int FOOTER_SYS = 4;
    }
    public interface ItemTypeSummary extends ItemTypeSummaryPrivate{
        int DATA       = 2;
    }

    /**
     * 根据item view位置获取对应类型
     * @param position
     * @return
     */
    @ItemTypeSummaryWhere
    public int getItemTypeByPosition(int position) {
        if (isSysHeaderItemView(position)) {
            return ItemTypeSummary.HEADER_SYS;
        }

        if (isHeaderItemView(position)) {
            return ItemTypeSummary.HEADER_USR;
        }

        if (isFooterItemView(position)) {
            return ItemTypeSummary.FOOTER_USR;
        }

        if (isSysFooterView(position)) {
            return ItemTypeSummary.FOOTER_SYS;
        }

        return ItemTypeSummary.DATA;
    }

    //##------------------------->>>

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 点击事件
     */
    public interface OnItemClickListener {
        /**
         * @param itemView
         * @param position
         */
        void onItemClick(View itemView, int position);
    }

    private OnItemLongClickListener onItemLongClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    /**
     * 长按事件
     */
    public interface OnItemLongClickListener {
        /**
         * @param itemView
         * @param position
         */
        void onItemLongClick(View itemView, int position);
    }

    protected RecyclerView.LayoutManager layoutManager;
    protected int firstVisibleItemIndex;
    protected int firstCompletelyVisibleItemIndex;
    protected int lastVisibleItemIndex;
    protected int lastCompletelyVisibleItemIndex;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FindItemType.ALL, FindItemType.FIRST, FindItemType.LAST})
    public @interface FindItemTypeWhere{}
    public interface FindItemType{
        int ALL      = 1 + 0;
        int FIRST    = 1 + ALL;
        int LAST     = 1 + FIRST;
    }
    protected void findFirstAndLastVisibleItemIndex(@FindItemTypeWhere int findItemType) {
        if (null == layoutManager) {
            return ;
        }

        if (layoutManager instanceof GridLayoutManager) {

            final GridLayoutManager lm = (GridLayoutManager) layoutManager;

            if (findItemType != FindItemType.LAST) {
                firstVisibleItemIndex = lm.findFirstVisibleItemPosition();
                firstCompletelyVisibleItemIndex = lm.findFirstCompletelyVisibleItemPosition();
            }

            if (findItemType != FindItemType.FIRST) {
                lastVisibleItemIndex = lm.findLastVisibleItemPosition();
                lastCompletelyVisibleItemIndex = lm.findLastCompletelyVisibleItemPosition();
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {

            final StaggeredGridLayoutManager lm = (StaggeredGridLayoutManager) layoutManager;

            if (findItemType != FindItemType.LAST) {
                firstVisibleItemIndex = lm.findFirstVisibleItemPositions(new int[1])[0];
                firstCompletelyVisibleItemIndex = lm.findFirstCompletelyVisibleItemPositions(new int[1])[0];
            }

            if (findItemType != FindItemType.FIRST) {
                lastVisibleItemIndex = lm.findLastVisibleItemPositions(new int[1])[0];
                lastCompletelyVisibleItemIndex = lm.findLastCompletelyVisibleItemPositions(new int[1])[0];
            }
        } else if (layoutManager instanceof LinearLayoutManager) {

            final LinearLayoutManager lm = (LinearLayoutManager) layoutManager;

            if (findItemType != FindItemType.LAST) {
                firstVisibleItemIndex = lm.findFirstVisibleItemPosition();
                firstCompletelyVisibleItemIndex = lm.findFirstCompletelyVisibleItemPosition();
            }

            if (findItemType != FindItemType.FIRST) {
                lastVisibleItemIndex = lm.findLastVisibleItemPosition();
                lastCompletelyVisibleItemIndex = lm.findLastCompletelyVisibleItemPosition();
            }
        }
    }

    public List<T> getRealAdapterData() {
        return realData;
    }

    @Override
    @CallSuper
    public void release() {
        if (null != mSysHeaderViews) {
            mSysHeaderViews.clear();
        }

        if (null != mHeaderViews) {
            mHeaderViews.clear();
        }

        if (null != realData) {
            realData.clear();
        }

        if (null != mFooterViews) {
            mFooterViews.clear();
        }
    }

    public static void setDebugMode(boolean debug) {
        L.DEBUG = debug;
    }
}
