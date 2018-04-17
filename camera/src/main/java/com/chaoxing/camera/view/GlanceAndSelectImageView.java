package com.chaoxing.camera.view;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chaoxing.camera.util.ToastUtils;
import com.cjt2325.camera.R;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangxu on 2017/9/23.
 */

public class GlanceAndSelectImageView extends FrameLayout {

    private Context mContext;
    private RecyclerView rvList;
    private List<Uri> images = new ArrayList<>();
    private List<Uri> selectItems = new ArrayList<>();
    private MyAdapter adapter;
    private int mMaxCount;

    public GlanceAndSelectImageView(@NonNull Context context) {
        this(context, null);
    }

    public GlanceAndSelectImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GlanceAndSelectImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.layout_glance_select, null);
        rvList = ((RecyclerView) rootView.findViewById(R.id.rvList));
        rvList.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        adapter = new MyAdapter();
        rvList.setAdapter(adapter);
        addView(rootView);
    }

    public void addImage(Uri url) {
        if (url == null || TextUtils.isEmpty(url.toString())) {
            return;
        }
        images.add(0, url);
        adapter.notifyDataSetChanged();
        rvList.scrollToPosition(0);
    }

    public List<Uri> getAllImage() {
        return selectItems;
    }


    private class MyAdapter extends RecyclerView.Adapter<MyHolder> {

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.lauyout_image_item, null);
            MyHolder holder = new MyHolder(rootView);
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyHolder holder, int position) {
            final Uri imgUrl = images.get(position);
            Glide.with(mContext).load(imgUrl).into(holder.ivItem);
            if (selectItems.contains(imgUrl)) {
                holder.tvTag.setBackgroundResource(R.drawable.icon_image_select);
                int i = selectItems.indexOf(imgUrl);
                if (i >= 0 && i < images.size()) {
                    holder.tvTag.setText((i + 1) + "");
                }
            } else {
                holder.tvTag.setText("");
                holder.tvTag.setBackgroundResource(R.drawable.icon_image_unselect);
            }
            holder.rlTvContainer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selectItems.contains(imgUrl)) {
                        selectItems.remove(imgUrl);
                    } else {
                        if (selectItems.size() >= mMaxCount) {
                            ToastUtils.showShortText(mContext, "最多选择" + mMaxCount + "张图片!");
                            return;
                        }
                        selectItems.add(imgUrl);
                    }
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return images.size();
        }
    }

    public void setMaxCount(int maxCount) {
        mMaxCount = maxCount;
        if (mMaxCount == 0) {
            mMaxCount = 9;
        }
    }

    private class MyHolder extends RecyclerView.ViewHolder {
        ImageView ivItem;
        TextView tvTag;
        RelativeLayout rlTvContainer;

        public MyHolder(View itemView) {
            super(itemView);
            ivItem = (ImageView) itemView.findViewById(R.id.ivItem);
            tvTag = ((TextView) itemView.findViewById(R.id.tvTag));
            rlTvContainer = (RelativeLayout) itemView.findViewById(R.id.rlTvContainer);
        }
    }
}
