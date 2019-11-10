package cm.deone.jetestefirebase.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cm.deone.jetestefirebase.R;
import cm.deone.jetestefirebase.model.Post;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder>{

    Context context;
    List<Post> postList;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_post, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String uid = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();
        String uName = postList.get(position).getuName();
        String uDp = postList.get(position).getuDp();
        String pId = postList.get(position).getpId();
        String pTitle = postList.get(position).getpTitle();
        String pDescription = postList.get(position).getpDescr();
        String pImage = postList.get(position).getpImage();
        String PTimeStamp = postList.get(position).getpTime();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(PTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.mUserName.setText(uName);
        holder.mPostTime.setText(pTime);
        holder.mPostTitle.setText(pTitle);
        holder.mPostDescription.setText(pDescription);

        // dp user
        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(holder.mUserAvatar);
        }catch (Exception e){

        }
        // post image
        if (pImage.equals("noImage")){
            holder.mShowPostImage.setVisibility(View.GONE);
        }else {
            try{
                Picasso.get().load(pImage).into(holder.mShowPostImage);
            }catch (Exception e){

            }
        }
        // Buttons
        holder.mPostLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Likes...",
                        Toast.LENGTH_SHORT).show();
            }
        });
        holder.mPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Comment...",
                        Toast.LENGTH_SHORT).show();
            }
        });
        holder.mPostShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Share...",
                        Toast.LENGTH_SHORT).show();
            }
        });
        holder.mPostMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "More...",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView mUserAvatar, mShowPostImage;
        TextView mUserName, mPostTime, mPostTitle, mPostDescription, mPostLike;
        ImageButton mPostMore;
        Button mPostLikes, mPostComment, mPostShare;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            mUserAvatar = itemView.findViewById(R.id.img_user_avatar);
            mShowPostImage = itemView.findViewById(R.id.img_show_post_image);

            mUserName = itemView.findViewById(R.id.tv_post_user_name);
            mPostTime = itemView.findViewById(R.id.tv_post_time);
            mPostTitle = itemView.findViewById(R.id.tv_post_title);
            mPostDescription = itemView.findViewById(R.id.tv_post_description);
            mPostLike = itemView.findViewById(R.id.tv_post_like);

            mPostLikes = itemView.findViewById(R.id.bt_post_like);
            mPostComment = itemView.findViewById(R.id.bt_post_comment);
            mPostShare = itemView.findViewById(R.id.bt_post_share);

            mPostMore = itemView.findViewById(R.id.img_post_more);

        }
    }
}
