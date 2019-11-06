package cm.deone.jetestefirebase.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.DataFormatException;

import cm.deone.jetestefirebase.R;
import cm.deone.jetestefirebase.RegisterActivity;
import cm.deone.jetestefirebase.model.Chat;
import cm.deone.jetestefirebase.model.User;

import static java.lang.String.format;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyHolder>{


    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGTH = 1;

    Context context;
    List<Chat> chatList;
    String mImageUrl;

    FirebaseUser user;

    public ChatAdapter(Context context, List<Chat> chatList, String mImageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.mImageUrl = mImageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGTH){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_droite, parent, false);
            return  new MyHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_gauche, parent, false);
            return  new MyHolder(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));

        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.mMessageChat.setText(message);
        holder.mTimeChat.setText(dateTime);
        try{
            Picasso.get().load(mImageUrl).into(holder.mProfile);
        }catch (Exception e){

        }

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Voulez vous suppprimer ce message?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        if (position==chatList.size()-1){
            if (chatList.get(position).isSeen()){
                holder.mIsSeen.setText("Seen");
            }else{
                holder.mIsSeen.setText("Delivered");
            }

        }else {
            holder.mIsSeen.setVisibility(View.GONE);
        }
    }

    private void deleteMessage(int position) {

        final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgTimeStaamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStaamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    if (ds.child("sender").getValue().equals(myUid)){
                        ds.getRef().removeValue(); //cette fonction peut etre utiliser toutes seule

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted...");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "Message deleted...",
                                Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, "You can delete only your messages...",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(user.getUid())){
            return MSG_TYPE_RIGTH;
        }else {
            return MSG_TYPE_LEFT;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView mProfile;
        TextView mMessageChat, mTimeChat, mIsSeen;
        LinearLayout messageLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            mProfile = itemView.findViewById(R.id.img_profile);
            mMessageChat = itemView.findViewById(R.id.tv_left_message);
            mTimeChat = itemView.findViewById(R.id.tv_receiver_time);
            mIsSeen = itemView.findViewById(R.id.tv_receiver_voir);
            messageLayout = itemView.findViewById(R.id.messageLayout);

        }
    }
}
