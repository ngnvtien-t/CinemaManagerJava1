package com.example.cinema.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinema.R;
import com.example.cinema.databinding.ItemSeatBinding;
import com.example.cinema.model.SeatLocal;

import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {

    private final List<SeatLocal> mListSeats;
    private final IManagerSeatListener iManagerSeatListener;

    public interface IManagerSeatListener {
        void clickItemSeat(SeatLocal seat);
    }

    public SeatAdapter(List<SeatLocal> mListSeats, IManagerSeatListener iManagerSeatListener) {
        this.mListSeats = mListSeats;
        this.iManagerSeatListener = iManagerSeatListener;
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSeatBinding itemSeatBinding = ItemSeatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SeatViewHolder(itemSeatBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        SeatLocal seat = mListSeats.get(position);
        if (seat == null) {
            return;
        }
        if (seat.isSelected()) {
            holder.mItemSeatBinding.layoutItem.setBackgroundResource(R.drawable.bg_seat_not_avaiable_corner_5);
        } else {
            if (seat.isChecked()) {
                holder.mItemSeatBinding.layoutItem.setBackgroundResource(R.drawable.bg_seat_selected_corner_5);
            } else {
                holder.mItemSeatBinding.layoutItem.setBackgroundResource(R.drawable.bg_seat_avaiable_corner_5);
            }
        }
        holder.mItemSeatBinding.tvTitle.setText(seat.getTitle());
        holder.mItemSeatBinding.layoutItem.setOnClickListener(view -> iManagerSeatListener.clickItemSeat(seat));
    }

    @Override
    public int getItemCount() {
        if (mListSeats != null) {
            return mListSeats.size();
        }
        return 0;
    }

    public static class SeatViewHolder extends RecyclerView.ViewHolder {

        private final ItemSeatBinding mItemSeatBinding;

        public SeatViewHolder(@NonNull ItemSeatBinding itemSeatBinding) {
            super(itemSeatBinding.getRoot());
            this.mItemSeatBinding = itemSeatBinding;
        }
    }
}
