package com.example.textsaver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import org.w3c.dom.Text;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    public List folderList;
    public EditText inputFolderName;
    public int Write_External_Storage = 100;
    public int Read_External_Storage = 101;
    FolderAdapter adapter;
    Toolbar mainMenuToolbar;
    boolean isFilesInitialised = true;
    private GridView folderGridView;
    private Button newFolderButton;
    private CardView folder;
    private SearchView folderSearch;
    private ArrayList<String> folderNames;
    private FloatingActionButton floatingButtonNewFolder;
    private File[] files;
    private File dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainMenuToolbar = findViewById(R.id.main_ActionBar);
        setSupportActionBar(mainMenuToolbar);

        newFolderButton = findViewById(R.id.new_Folder_Button);
        CheckPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Write_External_Storage);
        CheckPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Read_External_Storage);
        inputFolderName = findViewById(R.id.input_folder_name);
        folder = findViewById(R.id.folderObject);
        folderNames = new ArrayList<>();

        folderSearch = (SearchView) findViewById(R.id.folderSearch);
        folderSearch.setIconifiedByDefault(true);

        folderSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                folderSearch.setIconified(false);
                folderSearch.requestFocusFromTouch();
                Log.d("Folder Search", "onClick: Folder Search clicked");
                SearchFolder();
            }
        });
        floatingButtonNewFolder = findViewById(R.id.new_folder_float);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFilesInitialised) {
            final String rootPath = MainActivity.this.getExternalFilesDir(null).getAbsolutePath() + "/" + "Save Text";
            Log.d("Path", "onResume: path is " + MainActivity.this.getExternalFilesDir(null));
            dir = new File(rootPath);
            files = dir.listFiles();


            folderList = new ArrayList<Folder>();
            if (files != null) {
                final int filesFound = files.length;
                for (int i = 0; i < filesFound; i++) {
                    folderList.add(new Folder(files[i].getName(), folder));
                    folderNames.add(files[i].getName());

                }
            }

            adapter = new FolderAdapter(this, (ArrayList<Folder>) folderList);
            folderGridView = findViewById(R.id.gvFolderList);
            folderGridView.setAdapter(adapter);
            folderGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(MainActivity.this, FilesData.class);
                    Log.d("intent Extra", String.valueOf(position));
                    intent.putExtra("folder_name", files[position].getName());
                    Log.d("intent Extra", files[position].getName());
                    startActivity(intent);
                }
            });
            folderGridView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
            folderGridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                public int nr = 0;

                @Override
                public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
                    if (checked) {
                        nr++;
                        adapter.newSelection(position, checked);
                    } else {
                        nr--;
                        adapter.removeSelection(position);
                    }
                    mode.setTitle(nr + " selected");


                }

                @Override
                public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                    nr = 0;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        getWindow().setStatusBarColor(getColor(R.color.contextualDark));
                    }
                    MenuInflater inflater = getMenuInflater();
                    inflater.inflate(R.menu.contextual_menu_main, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {

                    switch (item.getItemId()) {

                        case R.id.delete:
                            nr = 0;
                            for (int pos : adapter.getCurrentCheckedPos()) {
                                files[pos].delete();
                                if (files[pos].isDirectory() && files[pos].listFiles() != null) {
                                    boolean fileDeleted = true;
                                    for (File child : files[pos].listFiles()) {
                                        fileDeleted = child.delete();
                                    }
                                    if (fileDeleted)
                                        files[pos].delete();
                                }
                                folderList.remove(pos);
                            }
                            adapter.notifyDataSetChanged();
                            adapter.clearSelection();
                            mode.finish();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                getWindow().setStatusBarColor(getColor(R.color.colorPrimaryDark));
                            }

                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(android.view.ActionMode mode) {
                    adapter.clearSelection();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        getWindow().setStatusBarColor(getColor(R.color.colorPrimaryDark));
                    }
                }
            });

            folderGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    folderGridView.setItemChecked(position, !adapter.isItemChecked(position));

                    return false;
                }
            });


            newFolderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String folderName = inputFolderName.getText().toString();
                    CreateFolder(folderName, folder);
                    folderGridView.setAdapter(adapter);
                }
            });

            floatingButtonNewFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder newFolderCreate = new AlertDialog.Builder(MainActivity.this);
                    final EditText inputFolderName = new EditText(MainActivity.this);
                    newFolderCreate.getContext().setTheme(R.style.Theme_AppCompat_Dialog_Alert);
                    inputFolderName.setHint("Type Here");
                    inputFolderName.setTextColor(Color.WHITE);

                    newFolderCreate.setTitle("Create New Folder");
                    newFolderCreate.setView(inputFolderName);

                    newFolderCreate.setPositiveButton("New Folder", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CreateFolder(inputFolderName.getText().toString(), folder);
                            adapter.notifyDataSetChanged();
                        }
                    });

                    newFolderCreate.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog dialog = newFolderCreate.create();
                    dialog.show();
                    inputFolderName.requestFocus();
                    Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    Button negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    positive.setTextColor(Color.WHITE);
                    negative.setTextColor(Color.WHITE);
                }
            });
            isFilesInitialised = true;
        }

    }

    public void CreateFolder(String folderName, CardView folder) {
        String myFolder = MainActivity.this.getExternalFilesDir(null).getAbsolutePath() + "/" + "Save Text" + "/" + folderName;
        File newFile = new File(myFolder);
        if (!newFile.exists()) {
            boolean isFolderCreated = newFile.mkdirs();
            if (isFolderCreated)
                folderList.add(new Folder(folderName, folder));
            files = dir.listFiles();
        } else {
            Toast.makeText(this, myFolder + " this cant be created", Toast.LENGTH_LONG).show();
        }
    }

    public void CheckPermissions(String p1, int indexCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, p1) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{p1}, indexCode);
        } else {
            Toast.makeText(MainActivity.this, "Permission Already Granted", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void SearchFolder() {
        folderSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (folderNames.contains(query)) {
                    adapter.getFilter().filter(query);
                    Log.d("Filtering Folder", "onQueryTextSubmit: reached to SearchResult");

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);

                return false;
            }
        });
    }

    public class FolderAdapter extends BaseAdapter implements Filterable {
        ArrayList<Folder> folderList;
        ArrayList<Folder> filteredData;
        Context context;
        private HashMap<Integer, Boolean> mSelection = new HashMap<>();

        public FolderAdapter(Context context, ArrayList<Folder> folderList) {
            super();
            this.folderList = folderList;
            this.context = context;
            filteredData = folderList;
        }

        public void newSelection(int pos, boolean value) {
            mSelection.put(pos, value);
            notifyDataSetChanged();

        }


        public boolean isItemChecked(int pos) {
            Boolean result = mSelection.get(pos);
            return result == null ? false : result;
        }

        public Set<Integer> getCurrentCheckedPos() {
            return mSelection.keySet();

        }


        public boolean removeSelection(int pos) {
            mSelection.remove(pos);
            notifyDataSetChanged();

            return true;
        }

        public void clearSelection() {
            mSelection.clear();
            notifyDataSetChanged();

        }


        @Override
        public int getCount() {
            return filteredData.size();
        }

        @Override
        public Object getItem(int position) {
            return filteredData.get((position));
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View folderView = inflater.inflate(R.layout.list_folder, null);
            TextView folderName = folderView.findViewById(R.id.folderName);
            Folder folder = (Folder) filteredData.get(position);
            folderName.setText(folder.getFolderName());
            folderName.setTextColor(Color.BLACK);

            if (mSelection.get(position) != null) {
                folderView.setBackgroundColor(getResources().getColor(R.color.colorSecondaryDark));
            }
            return folderView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();

                    //if there's nothing to filterOn
                    if (constraint == null || constraint.length() == 0) {
                        results.values = folderList;
                        results.count = folderList.size();
                    } else {
                        ArrayList<Folder> filterResultData = new ArrayList<>();

                        for (Folder data : folderList) {
                            if (data.getFolderName().toLowerCase().contains(constraint.toString().toLowerCase())) {
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
                    filteredData = (ArrayList<Folder>) results.values;
                    notifyDataSetChanged();

                }
            };
        }
    }


}
