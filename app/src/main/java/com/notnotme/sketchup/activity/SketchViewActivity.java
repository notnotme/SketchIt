package com.notnotme.sketchup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.notnotme.sketchup.R;
import com.notnotme.sketchup.ShareActionProvider;
import com.notnotme.sketchup.dao.Sketch;

import java.io.File;

public final class SketchViewActivity extends BaseActivity {

    private static final String TAG = SketchViewActivity.class.getSimpleName();
    public static final String ARG_SKETCH = TAG + ".arg_sketch";

    private Sketch mSketch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sketch_view);
        setSupportActionBar(findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mSketch = getIntent().getParcelableExtra(ARG_SKETCH);
        ImageView imageView = findViewById(R.id.sketch_image);
        imageView.setImageURI(FileProvider.getUriForFile(this, getPackageName() + ".provider", new File(mSketch.getPath())));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sketch_view, menu);

        ShareCompat.IntentBuilder shareIntentBuilder = ShareCompat.IntentBuilder.from(this).setType("image/png");
        shareIntentBuilder.addStream(FileProvider.getUriForFile(this, getPackageName() + ".provider", new File(mSketch.getPath())));

        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.share));
        shareActionProvider.setShareIntent(shareIntentBuilder.getIntent());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                Intent intent = NavUtils.getParentActivityIntent(this);
                if (intent != null) {
                    intent.putExtra(MainActivity.ARG_SKETCH, mSketch.getPath());
                    NavUtils.navigateUpTo(this, intent);

                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

}
