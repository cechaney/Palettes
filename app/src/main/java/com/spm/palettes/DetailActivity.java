package com.spm.palettes;

import android.app.ActionBar;
import android.app.Activity;
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

    ImageView imgView;
    String imageName;
    LinearLayout palContainer;
    List<ImageView> cells;

    LinearLayout detailRoot;
    OrientationEventListener oel;


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
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
            detailRoot.setOrientation(LinearLayout.VERTICAL);
        }

        cells = new ArrayList<>();

        cells.add((ImageView) this.findViewById(R.id.cell1));
        cells.add((ImageView) this.findViewById(R.id.cell2));
        cells.add((ImageView) this.findViewById(R.id.cell3));
        cells.add((ImageView) this.findViewById(R.id.cell4));
        cells.add((ImageView) this.findViewById(R.id.cell5));
        cells.add((ImageView) this.findViewById(R.id.cell6));
        cells.add((ImageView) this.findViewById(R.id.cell7));
        cells.add((ImageView) this.findViewById(R.id.cell8));

        //Bitmap img = BitmapFactory.decodeResource(this.getResources(),R.drawable.lenna);
        String imageUriString = this.getIntent().getStringExtra(EXTRAS.IMAGE_URI);
        Uri imageUri = Uri.parse(imageUriString);
        imgView.setImageURI(imageUri);
        Bitmap bitmap = ((BitmapDrawable) imgView.getDrawable()).getBitmap();

        imageName = this.getIntent().getStringExtra(EXTRAS.IMAGE_NAME);

        Palette.generateAsync(bitmap, 8,
                new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {

                        List<Palette.Swatch> swatches = palette.getSwatches();

                        for (int i = 0; i < swatches.size(); i++) {

                            if (i <= swatches.size()) {

                                ImageView imgView = cells.get(i);

                                imgView.setBackgroundColor(swatches.get(i).getRgb());

                                android.view.ViewGroup.LayoutParams layoutParams = imgView.getLayoutParams();
                                layoutParams.width = palContainer.getWidth() / 4;
                                layoutParams.height = palContainer.getHeight() / 2;
                                imgView.setLayoutParams(layoutParams);
                            }

                        }
                    }
                });

    }

    public void zoomImage(View view){

        if(ImageView.ScaleType.CENTER_CROP.equals(imgView.getScaleType())){
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

        if(item.getItemId() == R.id.save_palette){
            item.setEnabled(false);
            savePalette();
            item.setEnabled(true);
        }

        return super.onOptionsItemSelected(item);
    }

    private void savePalette(){

        View detailRoot = null;
        Bitmap bitMap = null;
        String filename = null;
        File imageFile = null;
        FileOutputStream fos = null;

        detailRoot = findViewById(R.id.detailRoot);
        detailRoot.setDrawingCacheEnabled(true);
        bitMap = detailRoot.getDrawingCache();

        try{

            filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/palette_" + imageName + ".png";

            imageFile = new File(filename);

            if(imageFile.exists()){
                imageFile.delete();
            }

            fos = new FileOutputStream(imageFile);

            bitMap.compress(Bitmap.CompressFormat.PNG,100,fos);

            fos.flush();
            fos.close();

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

            Uri contentUri = Uri.fromFile(imageFile);
            mediaScanIntent.setData(contentUri);

            this.sendBroadcast(mediaScanIntent);

            Toast confirm = Toast.makeText(this,"Palette Saved: " + imageFile.getName(), Toast.LENGTH_LONG);
            confirm.show();


        } catch(Exception ex){
            ex.printStackTrace();
        }

    }
}
