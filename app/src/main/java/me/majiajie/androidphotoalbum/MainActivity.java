package me.majiajie.androidphotoalbum;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.Locale;

import me.majiajie.photoalbum.PhotoAlbumActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_album);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PhotoAlbumActivity.REQUEST_CODE){
            PhotoAlbumActivity.ResultData resultData = PhotoAlbumActivity.getResult(data);
            Toast.makeText(this,String.format(Locale.CHINA,"选择了%d张图片",resultData.getPhotos().size()),Toast.LENGTH_LONG).show();
        }
    }

    public void openPhotoAlbum(View view) {
        PhotoAlbumActivity.startActivityForResult(this,new PhotoAlbumActivity.RequestData());
    }

    public void openPhotoAlbumDarkTheme(View view) {
        PhotoAlbumActivity.RequestData requestData = new PhotoAlbumActivity.RequestData();
        requestData.setTheme(R.style.PhotoAlbumDarkTheme);
        requestData.setShowFullImageBtn(false);
        requestData.setFilterImageMimeType(new String[]{"image/gif"});
        PhotoAlbumActivity.startActivityForResult(this,requestData);
    }

    public void openPhotoWithFragment(View view) {
        PhotoAlbumActivity.RequestData requestData = new PhotoAlbumActivity.RequestData();
        requestData.setFilterImageMimeType(new String[]{"image/gif"});
        requestData.setFragmentClassName(SelectPhotoCompleteFragment.class.getName());
        PhotoAlbumActivity.startActivityForResult(this,requestData);
    }
}
