package com.example.likeyesterday;

import android.app.Application;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.likeyesterday.LoginSignup.UserObject;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class AddFriendsFragment extends Fragment {

    private RecyclerView userListRecyclerView;
    private RecyclerView.Adapter userListAdapter;
    private RecyclerView.LayoutManager userListLayoutManager;

    ArrayList<UserObject> userList,contactList;

    FirebaseFirestore db= FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root=(ViewGroup) inflater.inflate(R.layout.fragment_add_friends, container, false);
        // Inflate the layout for this fragment
        userListRecyclerView= root.findViewById(R.id.userList);

        contactList=new ArrayList<>();
        userList=new ArrayList<>();

        initializeRecyclerView();
        getContactList();
        return root;
    }

    private void getContactList() {
        String ISOPrefix = getCountryISO();

        Cursor phones = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while(phones.moveToNext()){
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(")", "");

            if(!String.valueOf(phone.charAt(0)).equals("+"))
                phone = ISOPrefix + phone;

            UserObject mContact = new UserObject(name, phone);
            contactList.add(mContact);
            getUserDetails(mContact);
        }
    }

    private void initializeRecyclerView() {
        userListRecyclerView.setNestedScrollingEnabled(false);
        userListRecyclerView.setHasFixedSize(false);
//        userListLayoutManager = new LinearLayoutManager(getContext(), LinearLayout.VERTICAL, false);
//        userListRecyclerView.setLayoutManager(userListLayoutManager);
        userListAdapter=new UserListAdapter(userList);
        userListRecyclerView.setAdapter(userListAdapter);
    }

    private void getUserDetails(UserObject mContact){
        CollectionReference userCollectionReference = db.collection("Users");
        userCollectionReference
                .whereEqualTo("Phone Number",mContact.getPhone())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){

                            String phoneNumber=documentSnapshot.get("Phone Number").toString();
                            String name= documentSnapshot.get("Full Name").toString();

                            UserObject user=new UserObject(name,phoneNumber);
                            userList.add(user);
                            userListAdapter.notifyDataSetChanged();
                            return;
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),"Error!",Toast.LENGTH_SHORT).show();
                        Log.d("Add friend",e.toString());
                    }
                });
    }

    private String getCountryISO(){
        String iso=null;
        TelephonyManager telephonyManager= (TelephonyManager) getContext().getSystemService(getContext().TELEPHONY_SERVICE);
        if(telephonyManager.getNetworkCountryIso()!=null){
            if (telephonyManager.getNetworkCountryIso().equals(""));{
                iso=telephonyManager.getNetworkCountryIso();
            }
        }
        return CountryToPhonePrefix.getPhone(iso);
    }
}