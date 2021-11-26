package com.example.fitpavillion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitpavillion.R;
import com.example.fitpavillion.models.FoodItem;
import com.example.fitpavillion.utils.Callback;

import java.util.List;

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.WorkOutViewHolder> {
    private List<FoodItem> dataList;
    private Callback<FoodItem> clickListener;
    private Context context;

    public FoodItemAdapter(List<FoodItem> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }

    @NonNull
    @Override
    public WorkOutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_list_item, parent, false);
        return new WorkOutViewHolder(view);
    }

    public void setClickListener(Callback<FoodItem> clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull FoodItemAdapter.WorkOutViewHolder h, int position) {
        FoodItem plan = dataList.get(position);
        h.i_name.setText(plan.getName());
        h.i_protein.setText("Protein: " + String.valueOf(plan.getProtien()));
        h.i_fat.setText("Fat: " + String.valueOf(plan.getFat()));
        h.i_total.setText("Total Calorie\n" + String.valueOf(plan.getCalorie()));
    }


    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }


    public class WorkOutViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView i_name, i_protein, i_fat, i_total;

        public WorkOutViewHolder(@NonNull View v) {
            super(v);
            i_name = v.findViewById(R.id.item_food_name);
            i_protein = v.findViewById(R.id.item_food_protien);
            i_fat = v.findViewById(R.id.item_food_fat);
            i_total = v.findViewById(R.id.item_food_total_cal);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.result(dataList.get(getAdapterPosition()));
        }
    }
}
