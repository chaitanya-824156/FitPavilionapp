package com.example.fitpavillion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitpavillion.R;
import com.example.fitpavillion.models.FoodConsumption;
import com.example.fitpavillion.utils.Callback;

import java.util.List;

public class FoodDairyAdapter extends RecyclerView.Adapter<FoodDairyAdapter.FoodDairyViewHolder> {
    private List<FoodConsumption> dataList;
    private Callback<FoodConsumption> clickListener;
    private Context context;

    public FoodDairyAdapter(List<FoodConsumption> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }

    @NonNull
    @Override
    public FoodDairyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_dairy_list_item, parent, false);
        return new FoodDairyViewHolder(view);
    }

    public void setClickListener(Callback<FoodConsumption> clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull FoodDairyViewHolder h, int position) {
        FoodConsumption consumption = dataList.get(position);
        h.i_name.setText(consumption.getName());
        h.i_quantity.setText("Qty : " + String.valueOf(consumption.getQuantity()));
        h.i_date.setText(consumption.getDateString());
        h.i_totalCal.setText("Calories\n" + String.valueOf(consumption.getCalorie()));
    }


    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }


    public class FoodDairyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView i_name, i_quantity, i_date, i_totalCal;

        public FoodDairyViewHolder(@NonNull View v) {
            super(v);
            i_name = v.findViewById(R.id.item_food_dairy_name);
            i_quantity = v.findViewById(R.id.item_food_dairy_quantity);
            i_date = v.findViewById(R.id.item_food_dairy_date);
            i_totalCal = v.findViewById(R.id.item_food_dairy_total_cal);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.result(dataList.get(getAdapterPosition()));
        }
    }
}
