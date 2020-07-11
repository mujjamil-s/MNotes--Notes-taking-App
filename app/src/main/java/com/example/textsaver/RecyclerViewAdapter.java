package com.example.textsaver;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> implements Filterable {

    private ArrayList<String> mDataset;
    private ArrayList<String> filteredData;

    private HashMap<String, File> dataMap;



    public RecyclerViewAdapter( ArrayList<String> myDataset, HashMap<String,File> myDataMap) {
        mDataset = myDataset;
        filteredData = myDataset;
        this.dataMap = myDataMap;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
     View fileView =  LayoutInflater.from(parent.getContext()).inflate(R.layout.files_list,parent, false);
     TextView dataText =  fileView.findViewById(R.id.dataText);
      Button fileMenuButton = fileView.findViewById(R.id.fCMenuButton);

     MyViewHolder viewHolder = new MyViewHolder(fileView,dataText,fileMenuButton);
     return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.dataText.setText(filteredData.get(position));
        holder.fCMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()){
                            case R.id.cardDelete:
                                File fileToBeDeleted = dataMap.get(filteredData.get(position));
                                boolean result =   fileToBeDeleted.delete();
                                Log.d("deleteResult", "onMenuItemClick:  " + result);
                                filteredData.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, 1);
                        }
                        return true;
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                //if there's nothing to filterOn
                if(constraint == null || constraint.length() ==0){
                    results.values = new ArrayList<>( dataMap.keySet());
                    results.count = dataMap.size();
                }
                else {
                    ArrayList<String> filterResultData = new ArrayList<>();

                    for (String data: dataMap.keySet()){
                        if(data.toLowerCase().contains(constraint.toString().toLowerCase())){
                            filterResultData.add(data);
                        }
                    }
                    results.values = filterResultData;
                    results.count = filterResultData.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
               filteredData =(ArrayList<String>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public CardView fileCard;
        public TextView dataText;
        private Button fCMenuButton;
        public MyViewHolder (@NonNull View itemView, TextView dataText, Button fCMenuButton) {
            super(itemView);
            this.fileCard = (CardView) itemView;
            this.dataText = dataText;
            this.fCMenuButton = fCMenuButton;

        }
    }


}
