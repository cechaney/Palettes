package com.spm.palettes;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import java.util.ArrayList;
import java.util.List;


public class DetailActivity extends Activity {

    private final WeakReference<DetailActivity> weakDetailActivity;

    public DetailActivity() {
        this.weakDetailActivity = new WeakReference<>(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        LinearLayout detailRoot = (LinearLayout) this.findViewById(R.id.detailRoot);

        ActionBar actionBar = this.getActionBar();

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Configuration config = this.getResources().getConfiguration();

        if(config != null){
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                detailRoot.setOrientation(LinearLayout.HORIZONTAL);
            } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                detailRoot.setOrientation(LinearLayout.VERTICAL);
            }
        }

        new LoadTask().execute();

    }

    public void zoomImage(View view) {

        ImageView imgView = (ImageView) this.findViewById(R.id.imgView);

        if (ImageView.ScaleType.CENTER_CROP.equals(imgView.getScaleType())) {
            imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

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

        new SaveTask().execute();

    }

    private class LoadTask extends AsyncTask<String, Boolean, Boolean> {

        private ProgressDialog progDiag;
        private DetailActivity detailActivity;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            detailActivity = weakDetailActivity.get();

            progDiag = ProgressDialog.show(detailActivity, "Palettes", "Loading palette...", true, true);


        }

        @Override
        protected Boolean doInBackground(String... params) {

            ImageView imgView = (ImageView) detailActivity.findViewById(R.id.imgView);

            List<ImageView> cells = new ArrayList<>();

            try {

                cells.add((ImageView) detailActivity.findViewById(R.id.cell1));
                cells.add((ImageView) detailActivity.findViewById(R.id.cell2));
                cells.add((ImageView) detailActivity.findViewById(R.id.cell3));
                cells.add((ImageView) detailActivity.findViewById(R.id.cell4));
                cells.add((ImageView) detailActivity.findViewById(R.id.cell5));
                cells.add((ImageView) detailActivity.findViewById(R.id.cell6));

                Uri imageUri = Uri.parse(detailActivity.getIntent().getStringExtra(EXTRAS.IMAGE_URI));

                imgView.setImageURI(imageUri);

                Bitmap bitmap = ((BitmapDrawable) imgView.getDrawable()).getBitmap();

                Palette palette = Palette.generate(bitmap);

                cells.get(0).setBackgroundColor(palette.getVibrantColor(0));
                cells.get(1).setBackgroundColor(palette.getLightVibrantColor(0));
                cells.get(2).setBackgroundColor(palette.getDarkVibrantColor(0));
                cells.get(3).setBackgroundColor(palette.getMutedColor(0));
                cells.get(4).setBackgroundColor(palette.getLightMutedColor(0));
                cells.get(5).setBackgroundColor(palette.getDarkMutedColor(0));

            } catch(Exception ex){

                Log.e(detailActivity.getPackageName(),"Load palette failed",ex);

                return false;

            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if(!result){

                Toast failed = Toast.makeText(detailActivity, "Palette load failed ", Toast.LENGTH_LONG);
                failed.show();
            }

            progDiag.dismiss();

        }
    }

    private class SaveTask extends AsyncTask<String, Boolean, Boolean> {

        private ProgressDialog progDiag;
        private DetailActivity detailActivity;

        String imageName;
        String fileName;
        String filePath;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            detailActivity = weakDetailActivity.get();

            progDiag = ProgressDialog.show(detailActivity, "Palettes", "Saving palette...", true, true);


        }

        @Override
        protected Boolean doInBackground(String... params) {

            View detailRoot = findViewById(R.id.detailRoot);

            detailRoot.setDrawingCacheEnabled(true);

            Bitmap bitMap = detailRoot.getDrawingCache();

            imageName = detailActivity.getIntent().getStringExtra(EXTRAS.IMAGE_NAME);
            fileName = "/palette_" + imageName + ".png";
            filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + fileName;

            try {

                File imageFile = new File(filePath);

                if (imageFile.exists()) {
                    if(imageFile.delete()){
                        Log.i(detailActivity.getPackageName(),"File " + filePath + "overwritten");
                    }
                }

                FileOutputStream fos = new FileOutputStream(imageFile);
                bitMap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                fos.flush();
                fos.close();

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

                Uri contentUri = Uri.fromFile(imageFile);
                mediaScanIntent.setData(contentUri);

                detailActivity.sendBroadcast(mediaScanIntent);


            } catch (IOException ioe) {

                Log.e(detailActivity.getPackageName(),"Palette save failed");

                return false;

            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if(result){

                Toast confirm = Toast.makeText(detailActivity, "Palette Saved: " + fileName, Toast.LENGTH_LONG);
                confirm.show();

            } else {

                Toast failed = Toast.makeText(detailActivity, "Palette save failed ", Toast.LENGTH_LONG);
                failed.show();

            }

            progDiag.dismiss();

        }
    }
}
