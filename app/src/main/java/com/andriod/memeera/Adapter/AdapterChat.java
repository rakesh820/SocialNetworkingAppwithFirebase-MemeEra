package com.andriod.memeera.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andriod.memeera.Model.Chat;
import com.andriod.memeera.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {
    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;
    Context context;
    List<Chat> chatList;
    String imageUrl;
    FirebaseUser firebaseUser;

    public AdapterChat(Context context, List<Chat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if(i==MSG_TYPE_LEFT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup,false);
            return new MyHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup,false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String message=chatList.get(position).getMessage();
        String timeStamp=chatList.get(position).getTimeStamp();

        //Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        //calendar.setTimeInMillis(Long.parseLong(timeStamp));
        //String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        holder.messageTv.setText(message);
        //holder.timeTv.setText(dateTime);
        Picasso.get().load(imageUrl).into(holder.ProfileIv);

        if(position==chatList.size()-1){
            if(chatList.get(position).isSeen()){
                holder.isSeenTv.setText("Seen");
            }
            else {
                holder.isSeenTv.setText("Delivered");
            }
        }else {
            holder.isSeenTv.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
    @Override
    public int getItemViewType(int position){
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView ProfileIv;
        TextView messageTv,isSeenTv,timeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            ProfileIv=itemView.findViewById(R.id.profileIv);
            messageTv=itemView.findViewById(R.id.messageTv);
            isSeenTv=itemView.findViewById(R.id.isSeenTv);
            timeTv=itemView.findViewById(R.id.timeTv);
        }
    }
}
