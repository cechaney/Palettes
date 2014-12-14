package com.spm.palettes;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v7.graphics.Palette;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class DetailActivity extends Activity {

    private final Activity detailActivity = this;

    private ImageView imgView;
    private String imageName;
    private LinearLayout palContainer;
    private List<ImageView> cells;

    private LinearLayout detailRoot;
    private OrientationEventListener oel;

    private ProgressDialog progDiag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        imgView = (ImageView) this.findViewById(R.id.imgView);
        palContainer = (LinearLayout) this.findViewById(R.id.palContainer);
        detailRoot = (LinearLayout) this.findViewById(R.id.detailRoot);

        Configuration config = this.getResources().getConfiguration();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            detailRoot.setOrientation(LinearLayout.HORIZONTAL);
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            detailRoot.setOrientation(LinearLayout.VERTICAL);
        }

        cells = new ArrayList<>();

        cells.add((ImageView) this.findViewById(R.id.cell1));
        cells.add((ImageView) this.findViewById(R.id.cell2));
        cells.add((ImageView) this.findViewById(R.id.cell3));
        cells.add((ImageView) this.findViewById(R.id.cell4));
        cells.add((ImageView) this.findViewById(R.id.cell5));
        cells.add((ImageView) this.findViewById(R.id.cell6));

        String imageUriString = this.getIntent().getStringExtra(EXTRAS.IMAGE_URI);
        Uri imageUri = Uri.parse(imageUriString);
        imgView.setImageURI(imageUri);
        Bitmap bitmap = ((BitmapDrawable) imgView.getDrawable()).getBitmap();

        imageName = this.getIntent().getStringExtra(EXTRAS.IMAGE_NAME);

        progDiag = ProgressDialog.show(this, "Palettes", "Loading palette...", true, true);

        Palette.generateAsync(bitmap,
                new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {

                        try{

                            cells.get(0).setBackgroundColor(palette.getVibrantColor(0));
                            cells.get(1).setBackgroundColor(palette.getLightVibrantColor(0));
                            cells.get(2).setBackgroundColor(palette.getDarkVibrantColor(0));
                            cells.get(3).setBackgroundColor(palette.getMutedColor(0));
                            cells.get(4).setBackgroundColor(palette.getLightMutedColor(0));
                            cells.get(5).setBackgroundColor(palette.getDarkMutedColor(0));

                        } catch(Exception ex){
                            detailActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Toast failed = Toast.makeText(detailActivity, "Palette load failed ", Toast.LENGTH_LONG);

                                    failed.show();
                                }
                            });
                        } finally{
                            progDiag.hide();
                        }




                    }
                });

    }

    public void zoomImage(View view) {

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

        final View detailRoot = findViewById(R.id.detailRoot);

        detailRoot.setDrawingCacheEnabled(true);

        final Bitmap bitMap = detailRoot.getDrawingCache();

        progDiag = ProgressDialog.show(this, "Palettes", "Saving palette...", true, true);

        Thread mThread = new Thread() {
            @Override
            public void run() {

                try {

                    final String filename = "/palette_" + imageName + ".png";
                    final String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + filename;

                    File imageFile = new File(filePath);

                    if (imageFile.exists()) {
                        imageFile.delete();
                    }

                    FileOutputStream fos = new FileOutputStream(imageFile);
                    bitMap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                    fos.flush();
                    fos.close();

                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

                    Uri contentUri = Uri.fromFile(imageFile);
                    mediaScanIntent.setData(contentUri);

                    detailActivity.sendBroadcast(mediaScanIntent);

                    detailActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast confirm = Toast.makeText(detailActivity, "Palette Saved: " + filename, Toast.LENGTH_LONG);

                            confirm.show();
                        }
                    });


                } catch (IOException ioe) {

                    detailActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast failed = Toast.makeText(detailActivity, "Palette save failed ", Toast.LENGTH_LONG);

                            failed.show();
                        }
                    });

                } finally{
                    progDiag.dismiss();
                }

            }
        };

        mThread.start();

    }
}
