package com.example.fitpavillion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitpavillion.R;
import com.example.fitpavillion.models.Conversation;
import com.example.fitpavillion.utils.Callback;
import com.example.fitpavillion.utils.SharedPref;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {
    private List<Conversation> dataList;
    private Callback<Conversation> clickListener;
    private Context context;
    private String profileType;
    private SharedPref sharedPref;

    public ConversationAdapter(List<Conversation> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
        if (sharedPref == null) sharedPref = SharedPref.getInstance(context);
        if (sharedPref != null) profileType = sharedPref.getProfileType();

    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_list, parent, false);
        return new ConversationViewHolder(view);
    }

    public void setClickListener(Callback<Conversation> clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder h, int position) {
        Conversation conversation = dataList.get(position);
        h.i_name.setText(profileType.equals("USER") ? conversation.getTRAINER_NAME() : conversation.getUSER_NAME());
        h.i_date.setText(conversation.getUpdatedDateString());
    }


    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }


    public class ConversationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView i_name, i_date;

        public ConversationViewHolder(@NonNull View v) {
            super(v);
            i_name = v.findViewById(R.id.item_conv_name);
            i_date = v.findViewById(R.id.item_conv_date);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.result(dataList.get(getAdapterPosition()));
        }
    }
}
