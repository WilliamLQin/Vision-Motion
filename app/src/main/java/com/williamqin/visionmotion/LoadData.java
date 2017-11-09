package com.williamqin.visionmotion;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by williamqin on 2017-11-08.
 */

public class LoadData extends AppCompatActivity {

    private ListView mListView;

    private ArrayList<MetaData> mMetaDataList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loaddata);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        mListView = (ListView) findViewById(R.id.main_list_view);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mUser == null)
                    return;

                final ArrayList<DataEntry> loadedData = new ArrayList<>();

                String path = "Data/" + mUser.getUid() + "/" + mMetaDataList.get(position).getDate();

                final DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference().child(path);
                dataRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        DataEntry dataEntry = dataSnapshot.getValue(DataEntry.class);
                        loadedData.add(dataEntry);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Intent intent = new Intent(getApplicationContext(), Graphs.class);
                        Bundle b = new Bundle();
                        b.putParcelableArrayList("data", (loadedData));
                        intent.putExtras(b);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onStart() {
        super.onStart();

        mUser = mAuth.getCurrentUser();
        updateUI(mUser);
    }

    private void updateUI(FirebaseUser user) {

        mMetaDataList.clear();

        final DatabaseReference metaDataRef = FirebaseDatabase.getInstance().getReference().child("MetaData").child(user.getUid());
        metaDataRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                MetaData metaData = dataSnapshot.getValue(MetaData.class);
                mMetaDataList.add(metaData);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        metaDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void updateList() {

        String[] displayNames = new String[mMetaDataList.size()];

        for (int i = 0; i < mMetaDataList.size(); i++) {
            MetaData metaData = mMetaDataList.get(i);
            String displayName = metaData.getName();
            displayName += " " + metaData.getDate();
            displayNames[i] = displayName;
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, displayNames);
        mListView.setAdapter(adapter);
    }
}
