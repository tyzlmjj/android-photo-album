package me.majiajie.androidphotoalbum;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import me.majiajie.photoalbum.PhotoAlbumActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_album);
    }

    public void openPhotoAlbum(View view) {
        PhotoAlbumActivity.startActivityForResult(this,new PhotoAlbumActivity.RequestData());
    }

    public void openPhotoAlbumDarkTheme(View view) {
        PhotoAlbumActivity.RequestData requestData = new PhotoAlbumActivity.RequestData();
        requestData.setTheme(R.style.PhotoAlbumDarkTheme);
        requestData.setShowFullImageBtn(false);
        requestData.setFilterImageMimeType(new String[]{"image/png"});
        PhotoAlbumActivity.startActivityForResult(this,requestData);
    }
}
