package com.example.fitpavillion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitpavillion.R;
import com.example.fitpavillion.models.User;
import com.example.fitpavillion.utils.Callback;
import com.example.fitpavillion.utils.SharedPref;

import java.util.List;

public class TrainersAdapter extends RecyclerView.Adapter<TrainersAdapter.TrainersViewHolder> {
    private List<User> dataList;
    private Callback<User> clickListener;
    private Context context;

    public TrainersAdapter(List<User> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }

    @NonNull
    @Override
    public TrainersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trainers_list, parent, false);
        return new TrainersViewHolder(view);
    }

    public void setClickListener(Callback<User> clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull TrainersViewHolder h, int position) {
        User user = dataList.get(position);
        h.i_name.setText(user.getName());
        h.i_area.setText(user.getArea());
        h.i_city.setText(user.getCity());
        h.i_pincode.setText(String.valueOf(user.getPincode()));
    }


    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }


    public class TrainersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView i_name, i_city, i_area, i_pincode;

        public TrainersViewHolder(@NonNull View v) {
            super(v);
            i_name = v.findViewById(R.id.item_trainers_name);
            i_city = v.findViewById(R.id.item_trainers_city);
            i_area = v.findViewById(R.id.item_trainers_area);
            i_pincode = v.findViewById(R.id.item_trainers_pincode);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.result(dataList.get(getAdapterPosition()));
        }
    }
}
