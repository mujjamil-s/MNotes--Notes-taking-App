package com.example.textsaver;

import androidx.cardview.widget.CardView;

public class Folder {

    public String getFolderName() {
        return folderName;
    }

    private String folderName;

    public CardView getFolderView() {
        return folderView;
    }

    //private  int image;
    private  CardView folderView;

    public Folder(String folderName, CardView folderView) {
        this.folderName = folderName;
        this.folderView = folderView;
    }
}
