package ws.dyt.recyclerviewadapter.main;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ws.dyt.recyclerviewadapter.R;
import ws.dyt.recyclerviewadapter.bilili_1.Bilili_1_ListFragment;
import ws.dyt.recyclerviewadapter.bilili_1.Bilili_2_ListFragment;
import ws.dyt.recyclerviewadapter.pinned.TestPinnedItemFragment;
import ws.dyt.recyclerviewadapter.swipe.TestSwipeItemFragment;
import ws.dyt.recyclerviewadapter.wandoujia.WandoujianListFragment;
import ws.dyt.adapter.adapter.SuperAdapter;
import ws.dyt.adapter.viewholder.BaseViewHolder;

/**
 */
public class MainFragment extends Fragment {


    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(){
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recyclerview, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.init();
    }

    private void init() {
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerview);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(llm);
        final SuperAdapter<FragmentEntity> adapter = getAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new SuperAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                toFragment(adapter.getItem(position).clazz);
            }
        });

    }

    private SuperAdapter<FragmentEntity> getAdapter() {
        return new SuperAdapter<FragmentEntity>(getContext(), generate(), R.layout.item_main) {
            @Override
            public void convert(BaseViewHolder holder, int position) {
                holder.setText(R.id.tv_text, getItem(position).showName);
            }

        };
    }

    private List<FragmentEntity> generate() {
        List<FragmentEntity> data = new ArrayList<>();
        data.add(new FragmentEntity("模仿豌豆荚列表", WandoujianListFragment.class));
        data.add(new FragmentEntity("模Bilili, Grid——1", Bilili_1_ListFragment.class));
        data.add(new FragmentEntity("模Bilili, Grid——2", Bilili_2_ListFragment.class));

        data.add(new FragmentEntity("滑动菜单测试", TestSwipeItemFragment.class));
        data.add(new FragmentEntity("Pinned测试", TestPinnedItemFragment.class));


        return data;
    }

    private void toFragment(Class clazz) {
        Intent intent = new Intent(getActivity(), SingleFragmentActivity.class);
        intent.putExtra(SingleFragmentActivity.CLASS_FRAGMENT, clazz.getName());
        startActivity(intent);
    }
}
