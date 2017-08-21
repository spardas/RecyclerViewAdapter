package ws.dyt.recyclerviewadapter.swipe;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.widget.Space;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import butterknife.internal.Utils;
import ws.dyt.recyclerviewadapter.base.DevFragment;
import ws.dyt.recyclerviewadapter.R;
import ws.dyt.recyclerviewadapter.utils.FileUtils;
import ws.dyt.recyclerviewadapter.utils.UnitUtils;
import ws.dyt.view.adapter.SuperAdapter;
import ws.dyt.view.adapter.core.base.HeaderFooterAdapter;
import ws.dyt.view.adapter.swipe.MenuItem;
import ws.dyt.view.adapter.swipe.OnItemMenuClickListener;
import ws.dyt.view.adapter.swipe.SwipeDragHelperDelegate;
import ws.dyt.view.adapter.swipe.SwipeLayout;
import ws.dyt.view.viewholder.BaseViewHolder;

/**
 */
public class TestSwipeItemFragment extends DevFragment {


    public TestSwipeItemFragment() {
        // Required empty public constructor
    }

    public static TestSwipeItemFragment newInstance(){
        return new TestSwipeItemFragment();
    }


    private SuperAdapter<News> adapter;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.init();
    }

    private void init() {
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerview);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.addItemDecoration(new Divider(getContext()));

        recyclerView.setLayoutManager(llm);
        adapter = getAdapter();

        final int h = UnitUtils.dip2Px(getContext(), 10);
        Space space = new Space(getContext());
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h);
        space.setLayoutParams(lp);
        adapter.addHeaderView(space);

        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new SuperAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Toast.makeText(getContext(), "item: " + position, Toast.LENGTH_SHORT).show();
            }
        });

        adapter.setOnItemLongClickListener(new HeaderFooterAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {

            }
        });

        adapter.setOnItemMenuClickListener(new OnItemMenuClickListener() {
            @Override
            public void onMenuClick(SwipeLayout swipeItemView, View itemView, View menuView, int position, int menuId) {
                if (menuId == 01) {
//                    swipeItemView.closeMenuItem();
                    adapter.remove(position);
                    SwipeDragHelperDelegate.release();
                    Log.d("DEBUG", "--menu: 删除 -> position: " + position + " , menuId: " + menuId);
                    Toast.makeText(getContext(), "删除", Toast.LENGTH_SHORT).show();
                } else if (menuId == 02) {
                    swipeItemView.closeMenuItem();
                    Log.d("DEBUG", "--menu: 关注 -> position: " + position + " , menuId: " + menuId);
                    Toast.makeText(getContext(), "加关注", Toast.LENGTH_SHORT).show();

                }else if (menuId == 03) {
                    adapter.remove(position);
                }
            }
        });
    }

    private SuperAdapter<News> getAdapter() {
        return new SuperAdapter<News>(getContext(), generate()) {
            @Override
            public int getItemViewLayout(int position) {
//                return position == 0 ? R.layout.item_swipe_wrapper_for_menu : R.layout.item_swipe;
                return R.layout.item_swipe;
            }

//            @Override
//            public List<MenuItem> onCreateMultiMenuItem(@LayoutRes int viewType) {
//                List<MenuItem> mm = new ArrayList<>();
//                if (viewType == R.layout.item_swipe_wrapper_for_menu) {
//                    mm.add(new MenuItem(R.layout.menu_item_test_0, MenuItem.EdgeTrack.RIGHT, 03));
//                }else {
//                    mm.add(new MenuItem(R.layout.menu_item_test_delete, MenuItem.EdgeTrack.RIGHT, 01));
//                    mm.add(new MenuItem(R.layout.menu_item_test_mark, MenuItem.EdgeTrack.RIGHT, 02));
//                }
////                mm.add(new MenuItem(R.layout.menu_item_test_delete, MenuItem.EdgeTrack.LEFT, 03));
//                return mm;
//            }

            @Override
            public MenuItem onCreateSingleMenuItem(@LayoutRes int viewType) {
                return new MenuItem(R.layout.menu_item_test_delete, MenuItem.EdgeTrack.RIGHT, 01);
            }

            @Override
            public void convert(BaseViewHolder holder, final int position) {
                final News news = getItem(position);
                holder.setText(R.id.tv_title, news.title)
                .setText(R.id.tv_from, news.from)
                .setText(R.id.tv_time, news.time);
                 View v = holder.getView(R.id.tv_menu_mark);
                if (null != v) {
                    ((TextView) v).setText("加关注");
                }

                holder.setOnClickListener(R.id.btn_to, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(), "点击测试："+position, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public boolean isCloseOtherItemsWhenThisWillOpen() {
                return true;
            }
        };
    }

    private List<News> generate() {
        String json = FileUtils.readRawFile(getResources(), R.raw.news);
        List<News> data = new Gson().fromJson(json, new TypeToken<ArrayList<News>>(){}.getType());
        return data;
    }

    @Override
    protected void onFloatActionButtonClicked() {
        adapter.remove(0);
    }


    @Override
    public void onDestroy() {
        if (null != adapter) {
            adapter.release();
        }
        super.onDestroy();
    }

    private static class Divider extends RecyclerView.ItemDecoration {
//        Drawable divider = null;
        int dividerH = 0;

        Rect rect;
        Paint paint;
        public Divider(Context context) {
            context = context.getApplicationContext();
            dividerH = UnitUtils.dip2Px(context, 10);

            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.argb(200, 200, 200, 200));
            rect = new Rect();

        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            int cc = parent.getChildCount();
            for (int i = 0; i < cc; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + dividerH;

                rect.set(left + child.getPaddingLeft(), top, right - child.getPaddingRight(), bottom);
                c.drawRect(rect, paint);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.top = dividerH;
        }
    }
}
