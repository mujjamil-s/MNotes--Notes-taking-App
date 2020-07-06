package com.example.textsaver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class FilesData extends AppCompatActivity {

    private TextView dataText;
    private String rootPath;
    private ArrayList<String> dataList;

    private HashMap<String,File> dataMap;

    private String currentFolderName;
    private TextView folderTitle;

    // Recycler View initialisation and set-up
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private SearchView filesSearchView;
    private FloatingActionButton newFileCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_data);

        Toolbar fileToolBar = findViewById(R.id.files_ActionBar);
        setSupportActionBar(fileToolBar);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Bundle extras = intent.getExtras();
        currentFolderName = extras.getString("folder_name");

        folderTitle = findViewById(R.id.title_name_folder);
        folderTitle.setText(currentFolderName);
        dataText = findViewById(R.id.dataText);
        dataList = new ArrayList<>();

        dataMap = new HashMap<>();

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecyclerViewAdapter(this.dataList,this.dataMap);
        recyclerView.setAdapter(mAdapter);
        final String currentPath = FilesData.this.getExternalFilesDir(null).getAbsolutePath() + "/Save Text/" + currentFolderName;
        newFileCreate = findViewById(R.id.new_file_create_fab);

        newFileCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder newFileCreate = new AlertDialog.Builder(FilesData.this);
                final EditText inputFileText = new EditText(FilesData.this);
                newFileCreate.getContext().setTheme(R.style.Theme_AppCompat_Dialog_Alert);
                inputFileText.setHint("Type Here");
                inputFileText.setTextColor(Color.WHITE);

                newFileCreate.setTitle("Create New File");
                newFileCreate.setView(inputFileText);

                newFileCreate.setPositiveButton("New File", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(inputFileText.length() != 0){
                            WriteFile(inputFileText.getText().toString(),currentPath);
                        }
                        else {
                            Toast.makeText(FilesData.this, "File Cannot be Empty", Toast.LENGTH_LONG).show();
                        }
                        //CreateFolder(inputFile.getText().toString(), folder);
                        mAdapter.notifyDataSetChanged();
                    }
                });

                newFileCreate.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = newFileCreate.create();
                dialog.show();
                Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                positive.setTextColor(Color.WHITE);
                negative.setTextColor(Color.WHITE);
            }
        });

        filesSearchView = findViewById(R.id.filesSearch);
        filesSearchView.setIconifiedByDefault(true);

        filesSearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filesSearchView.setIconified(false);
                filesSearchView.requestFocusFromTouch();
                SearchData();
            }
        });
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                HandleSendTexts(intent);

            }
        } else {

            Log.d("Current Path", "onCreate: " + currentPath);
            ReadFile(currentPath);
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();

        }
    }

    private void HandleSendTexts(Intent intent) {
        final String sharedtext = intent.getStringExtra(Intent.EXTRA_TEXT);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setText("Choose a Folder");
        ll.addView(title,0);
        Button b1 = new Button(this);
        b1.setBackgroundColor(Color.YELLOW);
        ll.addView(b1, 1);

        AlertDialog.Builder builder = new AlertDialog.Builder(FilesData.this);
        //builder.setTitle("Choose a Folder");
        builder.setCustomTitle(ll);
        rootPath = FilesData.this.getExternalFilesDir(null).getAbsolutePath() + "/" + "Save Text";
        final File dir = new File(rootPath);
        final File[] files = dir.listFiles();
        final String[] folderPath = dir.list();

        builder.setItems(folderPath, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                WriteFile(sharedtext, files[which].getAbsolutePath());
                    //  dataText.setText(ReadFile());

            }


        });
        AlertDialog dialog = builder.create();
        dialog.show();

        //
        //  if(sharedtext!= null){
        //      File file = new File(FilesData.this.getFilesDir(),)
        //  }
    }

   private void WriteFile(String fileInput, String currentFolderPath) {

       if (fileInput != null) {
           File file = new File(currentFolderPath);
           File[] files = file.listFiles();

           List<String> fileNames = new ArrayList<>();

           for (File f : files) {
               String name = f.getName();

               fileNames.add(name);
           }

           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
               fileNames.sort(new Comparator<String>() {
                   @Override
                   public int compare(String o1, String o2) {
                       int compareReturn = 0;
                       if (Integer.valueOf(o1) > Integer.valueOf(o2)) {
                           compareReturn = 1;
                       } else if (Integer.valueOf(o1) == Integer.valueOf(o2)) {
                           compareReturn = 0;
                       } else if (Integer.valueOf(o1) < Integer.valueOf(o2)) {
                           compareReturn = -1;
                       }
                       return compareReturn;
                   }
               });
           }
           String lastName = fileNames.get(fileNames.size() - 1);


           int count = Integer.valueOf(lastName);

           try {
               File txtFile = new File(file, String.valueOf(count + 1));
               FileWriter writer = new FileWriter(txtFile);
               writer.append(fileInput);
               writer.flush();
               writer.close();
               Toast.makeText(FilesData.this, "Saved the file", Toast.LENGTH_LONG).show();

           } catch (Exception e) {
               Toast.makeText(FilesData.this, "Does not Save the file", Toast.LENGTH_LONG).show();
           }

           ReadFile(file.getAbsolutePath());

       }
   }
    //Reading File Data
    private void ReadFile(String path) {
        File fileEvents = new File(path);
        File[] files = fileEvents.listFiles();

        if (files != null) {
            for (File f : files) {
                StringBuilder text = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    String line;
                    while ((line = br.readLine()) != null) {
                        text.append(line);
                        text.append('\n');
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String result = text.toString();
                if (!dataMap.containsKey(result) && !dataList.contains(result))
                {       dataMap.put(result, f);
                        dataList.add(result);
                }
                mAdapter.notifyDataSetChanged();

            }
        }
        SetDataOnView(dataMap.keySet());
    }

    public void SetDataOnView(Set<String> fileData ) {
        mAdapter.notifyDataSetChanged();
        Log.d("Set Data", "SetDataOnView: " + fileData.toString() + fileData.size() + "data list " + dataList.size() + dataList.toString());
    }

    private void SearchData() {
        filesSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }

        });


    }
}