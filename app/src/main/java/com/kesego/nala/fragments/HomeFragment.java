package com.kesego.nala.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kesego.nala.Adapter.PostAdapter;
import com.kesego.nala.MainActivity;
import com.kesego.nala.Model.Post;
import com.kesego.nala.Model.User;
import com.kesego.nala.R;
import com.kesego.nala.RegisterActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private ImageView logout;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private String userid;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerview);
        logout = view.findViewById(R.id.logout);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(),postList);
        recyclerView.setAdapter(postAdapter);
        readPosts();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), RegisterActivity.class);
                startActivity(intent);

            }
        });

        return  view;

    }
    public void  readPosts(){
        userid =  FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    Post post = snapshot1.getValue(Post.class);
                    if(post.getPublisher().equals(userid)){
                        postList.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}