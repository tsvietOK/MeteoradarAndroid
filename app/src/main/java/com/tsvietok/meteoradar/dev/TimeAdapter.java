package com.tsvietok.meteoradar.dev;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimeAdapter extends CustomRecyclerView.Adapter<TimeAdapter.ViewHolder> {
    private final View.OnClickListener mOnClickListener;
    private LayoutInflater mInflater;
    private List<String> mTimes;

    TimeAdapter(Context context, View.OnClickListener onClickListener) {
        mTimes = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
        mOnClickListener = onClickListener;
    }

    @NonNull
    @Override
    public TimeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.horizontal_picker_item, parent, false);
        view.setOnClickListener(mOnClickListener);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeAdapter.ViewHolder holder, int position) {
        holder.textView.setText(mTimes.get(position));
    }

    @Override
    public int getItemCount() {
        return mTimes.size();
    }

    void refreshData(String[] times) {
        mTimes = Arrays.asList(times);

        notifyDataSetChanged();
    }

    class ViewHolder extends CustomRecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.horizontal_picker_item_text);
        }
    }
}
