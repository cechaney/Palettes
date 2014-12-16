package com.spm.palettes;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import java.util.ArrayList;
import java.util.List;


public class DetailActivity extends Activity {

    private static final String LOG_TAG = "Palettes.DetailActivity";

    private final WeakReference<DetailActivity> weakDetailActivity;

    private Uri imageUri;
    private String imageName;

    private ProgressDialog progDiag;

    List<ImageView> cells = new ArrayList<>();

    LinearLayout detailRoot;

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

        imageUri = Uri.parse(this.getIntent().getStringExtra(EXTRAS.IMAGE_URI));

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

        imageName = this.getIntent().getStringExtra(EXTRAS.IMAGE_NAME);

        new SaveTask().execute(imageName);

    }

    private class LoadTask extends AsyncTask<Uri, Palette, Palette> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            progDiag = ProgressDialog.show(weakDetailActivity.get(), "Palettes", "Loading palette...", true, true);

        }

        @Override
        protected Palette doInBackground(Uri... params) {

            Palette palette = null;

            try {

                Uri imageUri = params[0];

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);

                palette = Palette.generate(bitmap);

            } catch(Exception ex){

                Log.e(LOG_TAG,"Load palette failed",ex);

            }

            return palette;

        }

        @Override
        protected void onPostExecute(Palette palette) {

            DetailActivity detailActivity = weakDetailActivity.get();

            if(detailActivity != null){

                ImageView imgView = (ImageView) detailActivity.findViewById(R.id.imgView);

                imgView.setImageURI(imageUri);

                cells = new ArrayList<>();

                cells.add((ImageView) detailActivity.findViewById(R.id.cell1));
                cells.add((ImageView) detailActivity.findViewById(R.id.cell2));
                cells.add((ImageView) detailActivity.findViewById(R.id.cell3));
                cells.add((ImageView) detailActivity.findViewById(R.id.cell4));
                cells.add((ImageView) detailActivity.findViewById(R.id.cell5));
                cells.add((ImageView) detailActivity.findViewById(R.id.cell6));

                cells.get(0).setBackgroundColor(palette.getVibrantColor(0));
                cells.get(1).setBackgroundColor(palette.getLightVibrantColor(0));
                cells.get(2).setBackgroundColor(palette.getDarkVibrantColor(0));
                cells.get(3).setBackgroundColor(palette.getMutedColor(0));
                cells.get(4).setBackgroundColor(palette.getLightMutedColor(0));
                cells.get(5).setBackgroundColor(palette.getDarkMutedColor(0));

            } else {

                Toast failed = Toast.makeText(detailActivity, "Palette load failed ", Toast.LENGTH_LONG);

                failed.show();

            }

            if(progDiag != null && progDiag.isShowing()){
                progDiag.dismiss();
            }

        }
    }

    private class SaveTask extends AsyncTask<String, Boolean, Boolean> {

        View detailRoot;

        String imageName;
        String fileName;
        String filePath;

        Uri contentUri;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            progDiag = ProgressDialog.show(weakDetailActivity.get(), "Palettes", "Saving palette...", true, true);

            detailRoot = findViewById(R.id.detailRoot);

        }

        @Override
        protected Boolean doInBackground(String... params) {

            detailRoot.setDrawingCacheEnabled(true);

            Bitmap bitMap = detailRoot.getDrawingCache();

            imageName = params[0];
            fileName = "/palette_" + imageName + ".png";
            filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + fileName;

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


            } catch (IOException ioe) {

                Log.e(LOG_TAG,"Palette save failed");

                return false;

            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if(result){

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

                mediaScanIntent.setData(contentUri);

                weakDetailActivity.get().sendBroadcast(mediaScanIntent);

                Toast confirm = Toast.makeText(weakDetailActivity.get(), "Palette Saved: " + fileName, Toast.LENGTH_LONG);
                confirm.show();

            } else {

                Toast failed = Toast.makeText(weakDetailActivity.get(), "Palette save failed ", Toast.LENGTH_LONG);
                failed.show();

            }

            if(progDiag != null && progDiag.isShowing()){
                progDiag.dismiss();
            }

        }
    }
}
