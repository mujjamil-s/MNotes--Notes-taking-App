package com.example.textsaver;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.ActionMenuView;
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
private Context mContext;
    private HashMap<String, File> dataMap;



    public RecyclerViewAdapter( ArrayList<String> myDataset, HashMap<String,File> myDataMap, Context context) {
        mContext = context;
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
      ActionMenuView cardActionMenu = fileView.findViewById(R.id.card_Action_View);

     MyViewHolder viewHolder = new MyViewHolder(fileView,dataText,fileMenuButton,cardActionMenu);
     return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.dataText.setText(filteredData.get(position));
        holder.cardActionView.getMenu().clear();
        ((FilesData)mContext).getMenuInflater().inflate(R.menu.menu, holder.cardActionView.getMenu());
        holder.cardActionView.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
               final File fileToBeDeleted = dataMap.get(filteredData.get(position));
                String filesString = filteredData.get(position);
                switch (item.getItemId()){
                    case R.id.cardDelete:
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                        alertDialog.setTitle("Delete");
                        alertDialog.setMessage("Are You Sure?");
                        alertDialog.getContext().setTheme(R.style.DeleteDialog);
                        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean result =   fileToBeDeleted.delete();
                                Log.d("deleteResult", "onMenuItemClick:  " + result);
                                if(result){
                                filteredData.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, 1);
                                }
                            }
                        });

                        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        final AlertDialog dialog = alertDialog.create();
                        dialog.show();
                        break;
                    case R.id.cardShare:
                        TextView appTag = new  TextView(mContext);
                        appTag.setText("via MNotes (www.Google.com)");
                        Linkify.addLinks(appTag, Linkify.WEB_URLS);
                        Intent shareCard = new Intent(Intent.ACTION_SEND);
                        shareCard.putExtra(Intent.EXTRA_TEXT, filesString + "\n"+appTag.getText() );
                        shareCard.setType("text/plain");

                        Intent sendIntent = Intent.createChooser(shareCard, null);
                        (mContext).startActivity(sendIntent);
                        break;
                    case R.id.cardCopy:
                        ClipboardManager clipboardManager=(ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        try {
                            ClipData clip = ClipData.newPlainText("Copy", filesString);
                            if (clipboardManager != null) {
                                clipboardManager.setPrimaryClip(clip);

                                Toast.makeText(mContext, "Text Copied", Toast.LENGTH_SHORT).show();
                            }
                        }  catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                }
                return false;
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
        public Button fCMenuButton;
        public ActionMenuView cardActionView;
        public MyViewHolder (@NonNull View itemView, TextView dataText, Button fCMenuButton, ActionMenuView cardActionView) {
            super(itemView);
            this.fileCard = (CardView) itemView;
            this.dataText = dataText;
            this.fCMenuButton = fCMenuButton;
            this.cardActionView = cardActionView;

        }
    }


}
