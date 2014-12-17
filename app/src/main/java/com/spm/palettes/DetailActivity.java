package com.spm.palettes;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;


public class DetailActivity extends Activity {

    private static final String LOG_TAG = "Palettes.DetailActivity";

    private final WeakReference<DetailActivity> weakDetailActivity;

    private ProgressDialog progDiag;

    public DetailActivity() {
        this.weakDetailActivity = new WeakReference<>(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        ActionBar actionBar = this.getActionBar();

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Configuration config = this.getResources().getConfiguration();

        if(config != null){

            LinearLayout detailRoot = (LinearLayout) this.findViewById(R.id.detailRoot);

            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                detailRoot.setOrientation(LinearLayout.HORIZONTAL);
            } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                detailRoot.setOrientation(LinearLayout.VERTICAL);
            }

        }

        ImageView imgView = (ImageView) this.findViewById(R.id.imgView);

        Uri imageUri = Uri.parse(this.getIntent().getStringExtra(PaletteExtras.IMAGE_URI));

        imgView.setImageURI(imageUri);

        new LoadTask().execute(imageUri);

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(progDiag != null && progDiag.isShowing()){
            progDiag.dismiss();
        }

    }

    public void zoomImage(View view) {

        ImageView imgView = (ImageView) this.findViewById(R.id.imgView);

        if (ImageView.ScaleType.CENTER_CROP.equals(imgView.getScaleType())) {
            imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

    }

    private void lockOrientation(){

        Configuration config = this.getResources().getConfiguration();

        if(config != null){

            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

        }
    }

    private void unlockOrientation(){
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.save_palette) {
            savePalette();
        }

        return super.onOptionsItemSelected(item);
    }

    private void savePalette() {

        String imageName = this.getIntent().getStringExtra(PaletteExtras.IMAGE_NAME);

        new SaveTask().execute(imageName);

    }

    private class LoadTask extends AsyncTask<Uri, Palette, Palette> {

        private DetailActivity detailActivity;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            detailActivity = weakDetailActivity.get();

            if(detailActivity != null) {

                lockOrientation();

                progDiag = ProgressDialog.show(detailActivity, "Palettes", "Loading palette...", true, true);
            }
            else{
                cancel(true);
            }

        }

        @Override
        protected Palette doInBackground(Uri... params) {

            Palette palette = null;

            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),params[0]);

                palette = Palette.generate(bitmap);

            } catch(Exception ex){

                Log.e(LOG_TAG,"Load palette failed",ex);

            }

            return palette;

        }

        @Override
        protected void onPostExecute(Palette palette) {

            if(detailActivity != null){

                if(palette != null) {

                    detailActivity.findViewById(R.id.cell1).setBackgroundColor(palette.getVibrantColor(0));
                    detailActivity.findViewById(R.id.cell2).setBackgroundColor(palette.getLightVibrantColor(0));
                    detailActivity.findViewById(R.id.cell3).setBackgroundColor(palette.getDarkVibrantColor(0));
                    detailActivity.findViewById(R.id.cell4).setBackgroundColor(palette.getMutedColor(0));
                    detailActivity.findViewById(R.id.cell5).setBackgroundColor(palette.getLightMutedColor(0));
                    detailActivity.findViewById(R.id.cell6).setBackgroundColor(palette.getDarkMutedColor(0));
                } else {

                    Toast failed = Toast.makeText(detailActivity, "Palette load failed ", Toast.LENGTH_LONG);

                    failed.show();
                }

                if(progDiag != null && progDiag.isShowing()){
                    progDiag.dismiss();
                }

                unlockOrientation();

            }

        }
    }

    private class SaveTask extends AsyncTask<String, Integer, Boolean> {

        private DetailActivity detailActivity;

        private String fileName;
        private Uri contentUri;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            detailActivity = weakDetailActivity.get();

            if(detailActivity != null) {

                lockOrientation();

                progDiag = ProgressDialog.show(detailActivity, "Palettes", "Saving palette...", true, true);

            } else {
                cancel(true);
            }

        }

        @Override
        protected Boolean doInBackground(String... params) {

            View detailRoot = findViewById(R.id.detailRoot);

            detailRoot.setDrawingCacheEnabled(true);

            Bitmap bitMap = detailRoot.getDrawingCache();

            String imageName = params[0];

            fileName = "/palette_" + imageName + ".png";

            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + fileName;

            try {

                File imageFile = new File(filePath);

                if (imageFile.exists()) {
                    if(imageFile.delete()){
                        Log.i(LOG_TAG,"File " + filePath + "overwritten");
                    }
                }

                FileOutputStream fos = new FileOutputStream(imageFile);
                bitMap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                fos.flush();
                fos.close();

                contentUri = Uri.fromFile(imageFile);

                return true;

            } catch (IOException ioe) {

                Log.e(LOG_TAG,"Palette save failed");

                return false;

            }

        }

        @Override
        protected void onPostExecute(Boolean result) {

            if(detailActivity != null) {

                if(result){

                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

                    mediaScanIntent.setData(contentUri);

                    detailActivity.sendBroadcast(mediaScanIntent);

                    Toast confirm = Toast.makeText(detailActivity, "Palette Saved: " + fileName, Toast.LENGTH_LONG);
                    confirm.show();

                } else {

                    Toast failed = Toast.makeText(detailActivity, "Palette save failed ", Toast.LENGTH_LONG);
                    failed.show();

                }

                if(progDiag != null && progDiag.isShowing()){
                    progDiag.dismiss();
                }

                unlockOrientation();

            }

        }
    }
}
