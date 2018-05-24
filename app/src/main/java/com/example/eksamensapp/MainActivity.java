package com.example.eksamensapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_REQUEST = 1;
    private static final int RESULT_LOAD_IMAGE = 0;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    Button btnLoad, btnSave, btnShare, btnApply;

    ImageView imageView, imageFilter;

    String currentImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ask for storage permission
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
            else
            {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }

            imageView = findViewById(R.id.imageView);
            imageFilter = findViewById(R.id.imageFilter);

            btnShare = (Button) findViewById(R.id.btnShare);
            btnApply = (Button) findViewById(R.id.btnApply);
            btnLoad = (Button) findViewById(R.id.btnLoad);
            btnSave = (Button) findViewById(R.id.btnSave);

            btnShare.setEnabled(false);
            btnApply.setEnabled(false);
            btnSave.setEnabled(false);

            btnLoad.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent (Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, RESULT_LOAD_IMAGE);
                }
            });

            // functionality for the apply button
            btnApply.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    imageFilter.setImageResource(R.drawable.nostalgi);
                   // imageFilter.setImageResource(R.drawable.darkeffect);
                   // imageFilter.setImageResource(R.drawable.suneffect);
                    btnSave.setEnabled(true);
                }
            });

            btnSave.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    View content = findViewById(R.id.relLay);
                    Bitmap bitmap = getScreenShot(content);
                    currentImage = "image" + System.currentTimeMillis() + ".png";
                    store(bitmap, currentImage);
                    btnShare.setEnabled(true);
                }
            });

            btnShare.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    shareImage(currentImage);
                }
            });
        }

        Button btnCam = (Button)findViewById(R.id.btnCam);
        imageView = (ImageView)findViewById(R.id.imageView);

        btnCam.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view){
                //Intent intent = new  Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //startActivityForResult(intent,0);
                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // Method for getting the filtered image
    private static Bitmap getScreenShot(View view)
    {
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    // Method for saving the image in internal storage
    private void store(Bitmap bm, String fileName)
    {
        String dirPath = Environment.getExternalStoragePublicDirectory("f").getAbsolutePath() + "/FILTEREDIMAGES";
        File dir = new File(dirPath);
        if(!dir.exists())
        {
            dir.mkdirs();
        }
        File file = new File(dirPath, fileName);

        try
        {
            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Method for sharing the image
    private void shareImage(String fileName)
    {
        String dirPath = Environment.getExternalStoragePublicDirectory("3").getAbsolutePath() + "/FILTEREDIMAGES";
        Uri uri = Uri.fromFile(new File(dirPath, fileName));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(intent.EXTRA_SUBJECT, "");
        intent.putExtra(intent.EXTRA_TEXT, "");
        intent.putExtra(intent.EXTRA_STREAM, uri);

        try
        {
            startActivity(Intent.createChooser(intent, "Share via"));
        }
        catch (Exception e)
        {
            Toast.makeText(this, "No sharing app found!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data)
        {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            btnApply.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    {
                        // Do nothing
                    }
                }
                else
                {
                    Toast.makeText(this,"No Permission Granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    protected void OnActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        imageView.setImageBitmap(bitmap);
    }
}
