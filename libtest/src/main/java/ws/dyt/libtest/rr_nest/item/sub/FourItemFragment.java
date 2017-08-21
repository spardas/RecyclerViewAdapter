package ws.dyt.recyclerviewadapter.rr_nest.item.sub;

import android.support.annotation.DrawableRes;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ws.dyt.recyclerviewadapter.R;
import ws.dyt.recyclerviewadapter.rr_nest.item.BaseItemFrament;
import ws.dyt.view.viewholder.BaseViewHolder;

/**
 * Created by yangxiaowei on 17/1/12.
 */

public class FourItemFragment extends BaseItemFrament {

    @Override
    protected void setupView() {
        super.setupView();
        mFab.setVisibility(View.VISIBLE);

        GridLayoutManager lm = new GridLayoutManager(getContext(), 3);
        mRecyclerview.setLayoutManager(lm);

        adapter.addAll(this.generateData());
    }

    @Override
    protected int setItemLayoutId() {
        return R.layout.item_grid;
    }

    @Override
    protected void onConvert(BaseViewHolder holder, int position) {
        DataHolder dh = (DataHolder) adapter.getItem(position);
        holder.setText(R.id.tv_util_name, dh.desc).setImageResource(R.id.iv_utils_icon, dh.src);
    }


    private List<DataHolder> generateData() {
        List<DataHolder> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            list.add(new DataHolder("特特  --  > " +i, R.drawable.xy));
        }
        return list;
    }

    private static class DataHolder{
        public String desc;
        public @DrawableRes int src;

        public DataHolder(String desc, int src) {
            this.desc = desc;
            this.src = src;
        }
    }
}
