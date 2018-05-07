# android-photo-album [![GitHub release](https://img.shields.io/github/release/tyzlmjj/android-photo-album.svg)](https://github.com/tyzlmjj/android-photo-album/releases)

Android 本地相册选择

[Demo.apk](https://github.com/tyzlmjj/android-photo-album/releases/download/0.2.2/Demo.apk)

## 添加gradle依赖

```
implementation 'com.android.support:appcompat-v7:+'
implementation 'com.android.support:recyclerview-v7:+'
implementation 'me.majiajie:photo-album:0.2.2'
```

## 使用

- **直接调用相册**

启动相册Activity.第二个参数可以配置一些相关的设置
```java
PhotoAlbumActivity.startActivityForResult(this,new PhotoAlbumActivity.RequestData());
```

在`onActivityResult`中接收扫码结果
```
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK && requestCode == PhotoAlbumActivity.REQUEST_CODE){
        // 获取返回的数据
        PhotoAlbumActivity.ResultData resultData = PhotoAlbumActivity.getResult(data);
    }
}
```

- **在相册选择页面处理结果**

假如你不想返回启动相册的Activity/Fragment中去处理图片选择的结果,那你可以创建一个无UI的Fragment去处理。

创建一个继承[BaseCompleteFragment](https://github.com/tyzlmjj/android-photo-album/blob/master/photo-album/src/main/java/me/majiajie/photoalbum/BaseCompleteFragment.java)的Fragment，注意这个类必须是公开的

例如：
```
public class SelectPhotoCompleteFragment extends BaseCompleteFragment {

    @Override
    protected void onResultData(PhotoAlbumActivity.ResultData resultData) {
       // 在这里处理选择的图片
    }

}
```

启动相册Activity时需要设置这个Fragment的全名。
```
PhotoAlbumActivity.RequestData requestData = new PhotoAlbumActivity.RequestData();
requestData.setFragmentClassName(SelectPhotoCompleteFragment.class.getName());
PhotoAlbumActivity.startActivityForResult(this,requestData);
```



