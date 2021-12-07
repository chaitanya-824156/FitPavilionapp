package com.example.fitpavillion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitpavillion.R;
import com.example.fitpavillion.models.ChatItem;
import com.example.fitpavillion.models.User;
import com.example.fitpavillion.utils.Callback;
import com.example.fitpavillion.utils.SharedPref;

import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> {
    private List<ChatItem> dataList;
    private Callback<ChatItem> clickListener;
    private Context context;
    private String profileType;
    private SharedPref sharedPref;
    private User user;

    public ChatsAdapter(List<ChatItem> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
        if (sharedPref == null) sharedPref = SharedPref.getInstance(context);
        if (sharedPref != null) {
            if (user == null) user = sharedPref.getUser();
            profileType = sharedPref.getProfileType();
        }

    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_list, parent, false);
        return new ChatsViewHolder(view);
    }

    public void setClickListener(Callback<ChatItem> clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsViewHolder h, int position) {
        ChatItem chatItem = dataList.get(position);
        if (chatItem.getFrom().equals(user.getUid())) {
            h.i_card.setVisibility(View.VISIBLE);
            h.i_card_to.setVisibility(View.GONE);

            h.i_msg.setText(chatItem.getMessage());
            h.i_date.setText(chatItem.getDateString());
        } else {
            h.i_card.setVisibility(View.GONE);
            h.i_card_to.setVisibility(View.VISIBLE);

            h.i_msg_to.setText(chatItem.getMessage());
            h.i_date_to.setText(chatItem.getDateString());
        }
    }


    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }


    public class ChatsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView i_msg, i_date, i_msg_to, i_date_to;
        private CardView i_card, i_card_to;

        public ChatsViewHolder(@NonNull View v) {
            super(v);
            i_msg = v.findViewById(R.id.item_chat_message);
            i_date = v.findViewById(R.id.item_chat_date);
            i_card = v.findViewById(R.id.item_chat_from);

            i_msg_to = v.findViewById(R.id.item_chat_to_message);
            i_date_to = v.findViewById(R.id.item_chat_to_date);
            i_card_to = v.findViewById(R.id.item_chat_to);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.result(dataList.get(getAdapterPosition()));
        }
    }
}
