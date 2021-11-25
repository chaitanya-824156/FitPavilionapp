package com.example.fitpavillion.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.fitpavillion.R;
import com.example.fitpavillion.models.WorkOutPlan;
import com.example.fitpavillion.utils.Callback;

import java.util.List;

public class WorkOutAdapter extends RecyclerView.Adapter<WorkOutAdapter.WorkOutViewHolder> {
    private List<WorkOutPlan> dataList;
    private Callback<WorkOutPlan> clickListener;
    private Context context;

    public WorkOutAdapter(List<WorkOutPlan> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }

    @NonNull
    @Override
    public WorkOutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workout_plan_list_item, parent, false);
        return new WorkOutViewHolder(view);
    }

    public void setClickListener(Callback<WorkOutPlan> clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull WorkOutAdapter.WorkOutViewHolder h, int position) {
        WorkOutPlan plan = dataList.get(position);
        h.getI_name().setText(plan.getName());
        h.getI_reps_count().setText(plan.getReps() + "*" + plan.getCount());
        h.getI_dura().setText(String.valueOf(plan.getDurationInMins()) + " mins");
//        Glide.with(context).load(plan.getImageUrl()).into(h.getI_img()).onLoadFailed(context.getResources().getDrawable(R.drawable.app_icon));
        Glide.with(context).asBitmap().load(plan.getImageUrl()).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                h.getI_img().setImageBitmap(resource);
            }
        });
    }


    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }


    public class WorkOutViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView i_name, i_dura, i_reps_count;
        private ImageView i_img;

        public WorkOutViewHolder(@NonNull View v) {
            super(v);
            i_name = v.findViewById(R.id.item_workout_name);
            i_dura = v.findViewById(R.id.item_workout_reps_duration);
            i_reps_count = v.findViewById(R.id.item_workout_reps_count);
            i_img = v.findViewById(R.id.item_workout_image);
            v.setOnClickListener(this);
        }

        public TextView getI_name() {
            return i_name;
        }

        public TextView getI_dura() {
            return i_dura;
        }

        public TextView getI_reps_count() {
            return i_reps_count;
        }

        public ImageView getI_img() {
            return i_img;
        }

        @Override
        public void onClick(View v) {
            clickListener.result(dataList.get(getAdapterPosition()));
        }
    }
}
