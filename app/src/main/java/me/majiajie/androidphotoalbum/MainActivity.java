package me.majiajie.androidphotoalbum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import me.majiajie.photoalbum.AlbumActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_album);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == AlbumActivity.REQUEST_CODE){
            AlbumActivity.ResultData resultData = AlbumActivity.getResult(data);
            Toast.makeText(this,String.format(Locale.CHINA,"选择了%d张图片",resultData.getPhotos().size()),Toast.LENGTH_LONG).show();
        }
    }

    public void openPhotoAlbum(View view) {
        AlbumActivity.startActivityForResult(this,new AlbumActivity.RequestData());
    }

    public void openPhotoAlbumDarkTheme(View view) {
        AlbumActivity.RequestData requestData = new AlbumActivity.RequestData();
        requestData.setTheme(R.style.PhotoAlbumDarkTheme);
        requestData.setShowFullImageBtn(false);
        requestData.setFilterImageMimeType(new String[]{"image/gif"});
        AlbumActivity.startActivityForResult(this,requestData);
    }

    public void openPhotoWithFragment(View view) {
        AlbumActivity.RequestData requestData = new AlbumActivity.RequestData();
        requestData.setFilterImageMimeType(new String[]{"image/gif"});
        requestData.setFragmentClassName(SelectPhotoCompleteFragment.class.getName());
        AlbumActivity.startActivityForResult(this,requestData);
    }

    public void openWithPhoto(View view) {
        AlbumActivity.RequestData requestData = new AlbumActivity.RequestData();
        requestData.setFilterImageMimeType(new String[]{"image/gif"});
        requestData.setSinglePhoto(true);
        AlbumActivity.startActivityForResult(this,requestData);
    }

    public void openWithVideo(View view) {
        AlbumActivity.RequestData requestData = new AlbumActivity.RequestData();
        requestData.setMaxPhotoNumber(0);
        requestData.setSingleVideo(true);
        AlbumActivity.startActivityForResult(this,requestData);
    }

    public void openWithPhotoAndVideo(View view) {
        AlbumActivity.RequestData requestData = new AlbumActivity.RequestData();
        requestData.setFilterImageMimeType(new String[]{"image/gif"});
        requestData.setMaxNumber(6);
        AlbumActivity.startActivityForResult(this,requestData);
    }
}
