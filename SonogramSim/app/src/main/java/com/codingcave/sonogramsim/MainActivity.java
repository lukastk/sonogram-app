package com.codingcave.sonogramsim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {

    SonogramDraw drawing;
    SonogramSimulator sim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sim = new SonogramSimulator();
        sim.load_file(getResources().openRawResource(R.raw.sonodata));

        ViewGroup myLayout = (ViewGroup) findViewById(R.id.content_main);
        drawing = new SonogramDraw(this);
        myLayout.addView(drawing);

        drawing.setTag(R.raw.sonodata, sim);

        final Handler handler=new Handler();
        handler.post(new Runnable(){
            @Override
            public void run() {
                drawing.invalidate();
                handler.postDelayed(this,100);
            }
        });
    }

    private class SonogramDraw extends View {

        public SonogramDraw(Context context) {
            super(context);
        }

        protected void onDraw(Canvas canvas) {
            Paint p = new Paint();
            p.setColor(Color.BLACK);

            SonogramSimulator sim = (SonogramSimulator) this.getTag(R.raw.sonodata);
            sim.extract_slice();

            byte[][] slice_data = sim.get_slice_data();
            int zoom = 7;

            for (int y = 0; y < slice_data.length; y++) {
                for (int x = 0; x < slice_data[0].length; x++) {
                    p.setARGB(255, slice_data[y][x]*10, slice_data[y][x]*10, slice_data[y][x]*10);
                    canvas.drawRect(x*zoom, y*zoom, x*zoom + zoom, y*zoom + zoom, p);
                }
            }
        }
    }
}
