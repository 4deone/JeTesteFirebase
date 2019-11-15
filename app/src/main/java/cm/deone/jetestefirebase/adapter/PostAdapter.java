package cm.deone.jetestefirebase.adapter;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cm.deone.jetestefirebase.AddPostActivity;
import cm.deone.jetestefirebase.R;
import cm.deone.jetestefirebase.ThereProfileActivity;
import cm.deone.jetestefirebase.model.Post;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder>{

    private Context context;
    private List<Post> postList;

    String myUid;

    private DatabaseReference likesRef;
    private DatabaseReference postsRef;

    boolean mProcessLike = false;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        likesRef = FirebaseDatabase.getInstance().getReference().child("likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_post, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        final String uid = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();
        String uName = postList.get(position).getuName();
        String uDp = postList.get(position).getuDp();
        final String pId = postList.get(position).getpId();
        String pTitle = postList.get(position).getpTitle();
        String pDescription = postList.get(position).getpDescr();
        final String pImage = postList.get(position).getpImage();
        String PTimeStamp = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLikes();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(PTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.mUserName.setText(uName);
        holder.mPostTime.setText(pTime);
        holder.mPostTitle.setText(pTitle);
        holder.mPostDescription.setText(pDescription);
        holder.mPostLike.setText(pLikes + " Likes");
        
        setLikes(holder, pId);

        // dp user
        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(holder.mUserAvatar);
        }catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
        // post image
        if (pImage.equals("noImage")){
            holder.mShowPostImage.setVisibility(View.VISIBLE);
        }else {
            holder.mShowPostImage.setVisibility(View.GONE);
            try{
                Picasso.get().load(pImage).into(holder.mShowPostImage);
            }catch (Exception e){
                Toast.makeText(context, ""+e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Buttons
        holder.mPostLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pLikes = Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike = true;

                final String postIde = postList.get(position).getpId();

                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mProcessLike){
                            if (dataSnapshot.child(postIde).hasChild(myUid)){
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false;
                            }else {
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                likesRef.child(postIde).child(myUid).setValue("Liked");
                                mProcessLike = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.mPostMore, uid, myUid, pId, pImage);
            }
        });
        // Layout
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });
    }

    private void setLikes(final MyHolder holder, final String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postKey).hasChild(myUid)){
                    holder.mPostLikes.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    holder.mPostLikes.setText("Liked");
                }else {
                    holder.mPostLikes.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    holder.mPostLikes.setText("Likes");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showMoreOptions(ImageButton mPostMore, String uid, String myUid, final String pId, final String pImage) {

        PopupMenu popupMenu = new PopupMenu(context, mPostMore, Gravity.END);

        if (uid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id==0){
                    beginDelete(pId, pImage);
                }
                if (id==1){
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        if (pImage.equals("noImage")){
            deleteWithoutImage(pId);
        }else {
            deleteWhithImage(pId, pImage);
        }
    }

    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");
        Query fQuery = FirebaseDatabase.getInstance()
                .getReference("posts").orderByChild("pId").equalTo(pId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }

                Toast.makeText(context, "Deleting successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteWhithImage(final String pId, String pImage) {

        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Query fQuery = FirebaseDatabase.getInstance()
                        .getReference("posts").orderByChild("pId").equalTo(pId);
                fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ds.getRef().removeValue();
                        }

                        Toast.makeText(context, "Deleting successfully", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        LinearLayout linearLayout;

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

            linearLayout = itemView.findViewById(R.id.ly_profile);

        }
    }
}
