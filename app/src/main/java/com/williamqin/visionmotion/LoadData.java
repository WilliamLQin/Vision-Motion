package com.williamqin.visionmotion;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
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

                System.out.println("Clicked!");

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

    public class DataAdapter extends ArrayAdapter<String> {
        public DataAdapter(Context context, ArrayList<String> strings) {
            super(context, 0, strings);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            String string = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.textDataEntry);
            textView.setText(string);

            ImageButton delete = (ImageButton) convertView.findViewById(R.id.deleteButton);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    verifyRemoval(getItem(position));

                }
            });

            return convertView;

        }
    }

    private void updateList() {

        ArrayList<String> displayNames = new ArrayList<>();

        for (int i = 0; i < mMetaDataList.size(); i++) {
            MetaData metaData = mMetaDataList.get(i);
            String displayName = metaData.getName();
            displayName += " " + metaData.getDate();
            displayNames.add(displayName);
        }

        DataAdapter adapter = new DataAdapter(this, displayNames);
        mListView.setAdapter(adapter);

    }

    private void verifyRemoval(final String title) {
        DialogInterface.OnClickListener dialogueClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        removeFromFirebase(title);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete '" + title.substring(0, title.length()-20) + "'?").setPositiveButton("Yes", dialogueClickListener).setNegativeButton("No", dialogueClickListener).show();
    }

    private void removeFromFirebase(String title) {
        String date = title.substring(title.length() - 19);
        System.out.println(date);

        if (mUser == null)
            mUser = mAuth.getCurrentUser();

        if (mUser != null) {

            DatabaseReference removedDataRef = FirebaseDatabase.getInstance().getReference().child("Data").child(mUser.getUid()).child(date);
            removedDataRef.removeValue();

            DatabaseReference removedMetaDataRef = FirebaseDatabase.getInstance().getReference().child("MetaData").child(mUser.getUid()).child(date);
            removedMetaDataRef.removeValue();

            updateUI(mUser);

        }

    }
}
