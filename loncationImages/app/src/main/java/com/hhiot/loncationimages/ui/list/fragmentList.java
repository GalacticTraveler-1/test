package com.hhiot.loncationimages.ui.list;

import android.Manifest;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.hhiot.loncationimages.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragmentList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragmentList extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int RESULT_OK = 1;
    private static final Object MODE_PRIVATE = ;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public fragmentList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragmentList.
     */
    // TODO: Rename and change types and number of parameters
    public static fragmentList newInstance(String param1, String param2) {
        fragmentList fragment = new fragmentList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }


    public static final int CHOOSE_PHOTO = 2;
    ImageView picture;
    String imagePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_list);

        Button btnChoosePhoto = (Button) btnChoosePhoto.findViewById();
        picture = (ImageView) picture.findViewById();
        btnChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissino();
            }
        });
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissino();
            }
        });
    }

    private void setContentView(int fragment_list) {
    }

    private void requestPermissino() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else {
            openAlbum();
        }

    }

    private void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO); //????????????
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    Toast.makeText(this,"?????????????????????", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CHOOSE_PHOTO:
                if(resultCode == RESULT_OK){
                    //???????????????????????????
                    if(Build.VERSION.SDK_INT>=19){
                        //4.4?????????????????????????????????????????????
                        handleImageOnKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitKat(Intent data){
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            //?????????document?????????Uri????????????document id??????
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];  //????????????????????????id
                String selection = MediaStore.Images.Media._ID+"="+id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public downloads"),Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            //?????????file?????????Uri?????????????????????????????????
            imagePath = getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            //?????????file?????????Uri?????????????????????????????????
            imagePath = uri.getPath();
        }
        displayImage(imagePath); //??????????????????????????????
    }

    //??????????????????Uri???????????????
    private String getImagePath(Uri uri,String selection){
        String path = null;
        //??????Uri???selection??????????????????????????????
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor!= null){
            if(cursor.moveToFirst()){
                path = cursor.getString(((Cursor) cursor).getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private ContentProvider getContentResolver() {
    }

    //????????????
    private void displayImage(String imagePath){
        if(imagePath!=null && !imagePath.equals("")){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
            //??????????????????????????????????????????????????????app????????????
            SharedPreferences sp = getSharedPreferences("sp_img",MODE_PRIVATE);  //??????xml?????????????????????name:?????????xml?????????
            SharedPreferences.Editor editor = sp.edit(); //??????edit()
            editor.putString("imgPath",imagePath);
            editor.apply();
        }else {
            Toast.makeText(this,"??????????????????",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //????????????app??????????????????
        SharedPreferences sp = getSharedPreferences("sp_img", MODE_PRIVATE);
        //????????????????????????????????????????????????????????????
        String beforeImagePath = sp.getString("imgPath", null);
        displayImage(beforeImagePath);
    }

    private SharedPreferences getSharedPreferences(String sp_img, Object modePrivate) {
    }

}