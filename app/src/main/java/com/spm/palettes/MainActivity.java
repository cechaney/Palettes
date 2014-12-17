package com.spm.palettes;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.File;


public class MainActivity extends Activity {

    private static final int PICK_IMAGE = 1;
    private static final int TAKE_IMAGE = 2;

    private Uri capImageUri;
    private String capImageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout mainRoot = (LinearLayout) findViewById(R.id.mainRoot);

        Configuration config = this.getResources().getConfiguration();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mainRoot.setOrientation(LinearLayout.HORIZONTAL);
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
            mainRoot.setOrientation(LinearLayout.VERTICAL);
        }

        ImageButton cameraButton = (ImageButton) findViewById(R.id.cameraButton);

        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            cameraButton.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_IMAGE) {

            if (resultCode == RESULT_OK) {

                Uri imageUri = data.getData();

                Cursor cursor = getContentResolver().query(imageUri,null,null,null,null);

                if(cursor == null){
                    return;
                }
                else{

                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    cursor.moveToFirst();
                    capImageName = cursor.getString(nameIndex);
                    capImageName = capImageName.replaceFirst("[.][^.]+$", "");

                    Intent detail = new Intent(this, DetailActivity.class);

                    detail.putExtra(PaletteExtras.IMAGE_URI, imageUri.toString());
                    detail.putExtra(PaletteExtras.IMAGE_NAME, capImageName);

                    startActivity(detail);
                }


            }
        }

        if (requestCode == TAKE_IMAGE) {

            if (resultCode == RESULT_OK) {

                if(capImageUri != null) {

                    //Tell the device to scan for the new image
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(capImageUri);
                    this.sendBroadcast(mediaScanIntent);

                    //Show the detail activity
                    Intent detail = new Intent(this, DetailActivity.class);
                    detail.putExtra(PaletteExtras.IMAGE_URI, capImageUri.toString());
                    detail.putExtra(PaletteExtras.IMAGE_NAME, capImageName);
                    startActivity(detail);
                }

            }
        }


    }

    public void selectImage(View view){

        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        intent.setType("image/*");

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    public void takeImage(View view){

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String fileName = "palette_" + System.currentTimeMillis() + ".jpg";

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);

        capImageUri = Uri.fromFile(file);
        capImageName = fileName;

        intent.putExtra(MediaStore.EXTRA_OUTPUT, capImageUri);

        startActivityForResult(intent, TAKE_IMAGE);
    }


}
