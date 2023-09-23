package com.example.cinema.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinema.databinding.ItemTimeBinding;
import com.example.cinema.model.SlotTime;

import java.util.List;

public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.TimeViewHolder> {

    private final List<SlotTime> mListTimes;
    private final IManagerTimeListener iManagerTimeListener;
    private boolean onBind;

    public interface IManagerTimeListener {
        void clickItemTime(SlotTime time);
    }

    public TimeAdapter(List<SlotTime> mListTimes, IManagerTimeListener iManagerTimeListener) {
        this.mListTimes = mListTimes;
        this.iManagerTimeListener = iManagerTimeListener;
    }

    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTimeBinding itemTimeBinding = ItemTimeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TimeViewHolder(itemTimeBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
        SlotTime time = mListTimes.get(position);
        if (time == null) {
            return;
        }
        holder.mItemTimeBinding.tvTitle.setText(time.getTitle());
        onBind = true;
        holder.mItemTimeBinding.chbSelected.setChecked(time.isSelected());
        onBind = false;
        holder.mItemTimeBinding.chbSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!onBind) {
                iManagerTimeListener.clickItemTime(time);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mListTimes != null) {
            return mListTimes.size();
        }
        return 0;
    }

    public static class TimeViewHolder extends RecyclerView.ViewHolder {

        private final ItemTimeBinding mItemTimeBinding;

        public TimeViewHolder(@NonNull ItemTimeBinding itemTimeBinding) {
            super(itemTimeBinding.getRoot());
            this.mItemTimeBinding = itemTimeBinding;
        }
    }
}
