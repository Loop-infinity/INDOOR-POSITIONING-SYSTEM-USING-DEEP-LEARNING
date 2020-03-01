package dspanah.sensor_based_har;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.widget.ImageView;
import android.view.View;
import android.animation.AnimatorSet;


public class MainActivity extends AppCompatActivity implements SensorEventListener, TextToSpeech.OnInitListener {

    private static final int N_SAMPLES = 100;
    private static List<Float> x;
    private static List<Float> y;
    private static List<Float> z;

  /*  private static List<Float> x1;
    private static List<Float> y1;
    private static List<Float> z1; */

  //image
    ImageView blueDot;
    ImageView imageView;
    ObjectAnimator  objectAnimatorX;
    ObjectAnimator  objectAnimatorY;
    ObjectAnimator animRot;

    private TextView runningTextView;
    private TextView standingTextView;
    private TextView walkingTextView;
    private TextToSpeech textToSpeech;
    private float[] results;
    private HARClassifier classifier;
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private float cAzimuth = 0f;


    double Xcpos=90;
    double Xnpos=90;
    double Ycpos=120;
    double Ynpos=120;

    float density = 1;
  //  int xsize=0;
 //   int x1size=0;

    private String[] labels = { "Running", "Standind", "Walking"};


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        x = new ArrayList<>();
        y = new ArrayList<>();
        z = new ArrayList<>();

     /*   x1 = new ArrayList<>();
        y1 = new ArrayList<>();
        z1 = new ArrayList<>(); */


        runningTextView = (TextView) findViewById(R.id.running_prob);
        standingTextView = (TextView) findViewById(R.id.standing_prob);
        walkingTextView = (TextView) findViewById(R.id.walking_prob);
        blueDot = (ImageView) findViewById(R.id.blue_dot);
        imageView = (ImageView) findViewById(R.id.imageView);
   //     objectAnimator = ObjectAnimator.ofFloat(blueDot,"x",cpos,50);

        classifier = new HARClassifier(getApplicationContext());

        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setLanguage(Locale.US);

        density = getResources().getDisplayMetrics().density;


       imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                int x = (int)event.getX();
                int y = (int)event.getY();

                System.out.println("X :"+x);
                System.out.println("Y :"+y);

                //adding initial location
              /*  RelativeLayout rl = (RelativeLayout) findViewById(R.id.blue_dot_row);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams();
                params.leftMargin = 50;
                params.topMargin = 60;
                rl.addView(blueDot,params);
                 */
                objectAnimatorX = ObjectAnimator.ofFloat(blueDot,"x",x,x);
               // objectAnimatorX.setDuration(2500);
                objectAnimatorY = ObjectAnimator.ofFloat(blueDot,"y",y,y);
              //  objectAnimatorY.setDuration(2500);*/

                AnimatorSet animSetXY = new AnimatorSet();
                animSetXY.playTogether(objectAnimatorX, objectAnimatorY);
                animSetXY.start();

                Xnpos = x/density;
                Ynpos = y/density;
                return true;
            }
        });


    }

    @Override
    public void onInit(int status) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (results == null || results.length == 0) {
                    return;
                }
                float max = -1;
                int idx = -1;
                for (int i = 0; i < results.length; i++) {
                    if (results[i] > max) {
                        idx = i;
                        max = results[i];
                    }
                }

                textToSpeech.speak(labels[idx], TextToSpeech.QUEUE_ADD, null, Integer.toString(new Random().nextInt()));
            }
        }, 2000, 5000);
    }

    protected void onPause() {
        getSensorManager().unregisterListener(this);
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.97f;
        activityPrediction();
        synchronized (this){

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ){   // && x.size() < N_SAMPLES
                x.add(event.values[0]); //System.out.println("Yo"+event.values[0]);
                y.add(event.values[1]);
                z.add(event.values[2]);

                mGravity[0] = alpha*mGravity[0]+(1-alpha)*event.values[0];
                mGravity[1] = alpha*mGravity[1]+(1-alpha)*event.values[1];
                mGravity[2] = alpha*mGravity[2]+(1-alpha)*event.values[2];
                //  xsize++;
            }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD ){   //&& x1.size() < N_SAMPLES
            mGeomagnetic[0] = alpha*mGeomagnetic[0]+(1-alpha)*event.values[0];  //System.out.println("Yoff"+event.values[0]);
            mGeomagnetic[1] = alpha*mGeomagnetic[1]+(1-alpha)*event.values[1];
            mGeomagnetic[2] = alpha*mGeomagnetic[2]+(1-alpha)*event.values[2];
           // x1size++;
            }
        }

        float R[] = new float[9];
        float I[] = new float[9];
        boolean success = SensorManager.getRotationMatrix(R,I,mGravity,mGeomagnetic);

        if(success)
        {
            float orientation[] = new float[3];
            SensorManager.getOrientation(R,orientation);
            azimuth = (float) Math.toDegrees(orientation[0]);
            azimuth = (azimuth+360) % 360;
          //  System.out.println("AZIMUTH:  "+azimuth);

            animRot = ObjectAnimator.ofFloat(blueDot,"rotation",cAzimuth,azimuth);
            animRot.setDuration(5);
            animRot.start();
      //      Animation animRot = new RotateAnimation(-cAzimuth,-azimuth,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            cAzimuth=azimuth;
           /* animRot.setDuration(500);
            animRot.setRepeatCount(0);
            animRot.setFillAfter(true);

            blueDot.startAnimation(animRot); */
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void activityPrediction() {
        if (x.size() == N_SAMPLES && y.size() == N_SAMPLES && z.size() == N_SAMPLES ) {
            List<Float> data = new ArrayList<>();
            data.addAll(x);
            data.addAll(y);
            data.addAll(z);

        /*    data.addAll(x1);
            data.addAll(y1);
            data.addAll(z1); */

            results = classifier.predictProbabilities(toFloatArray(data));

                runningTextView.setText(Float.toString(round(results[0], 2)));
                standingTextView.setText(Float.toString(round(results[1], 2)));
                walkingTextView.setText(Float.toString(round(results[2], 2))); /* */

            if(results[2] > results[1])
            {
                Xcpos = Xnpos;
                Xnpos = Xnpos +  (25*Math.cos(Math.toRadians(azimuth-90)));// System.out.println("Azimuth"+(azimuth-90));
            //    System.out.println("Val"+50*Math.cos(Math.toRadians(azimuth-90)));

             //   System.out.println("Cpos "+cpos+"Npos"+npos);

                Ycpos = Ynpos;
                Ynpos = Ynpos +  (25*Math.sin(Math.toRadians(azimuth-90)));

                objectAnimatorX = ObjectAnimator.ofFloat(blueDot,"x",density* (float)Xcpos, density* (float)Xnpos);
                objectAnimatorX.setDuration(2500);
                objectAnimatorY = ObjectAnimator.ofFloat(blueDot,"y",density* (float)Ycpos,density* (float)Ynpos);
                objectAnimatorY.setDuration(2500);

                /*
                objectAnimatorX = ObjectAnimator.ofFloat(blueDot,"x",0,720);
                objectAnimatorX.setDuration(2500);
                objectAnimatorY = ObjectAnimator.ofFloat(blueDot,"y",0,0);
                objectAnimatorY.setDuration(2500);*/

                AnimatorSet animSetXY = new AnimatorSet();
                animSetXY.playTogether(objectAnimatorX, objectAnimatorY);
                animSetXY.start();

             //   cpos=cpos+50;
             //   npos=npos+50;
                System.out.println("getTop "+imageView.getTop());
                System.out.println("getX "+imageView.getX());
                System.out.println("getY "+imageView.getY());
                System.out.println("Width "+imageView.getWidth());
                System.out.println("height "+imageView.getHeight());

            }



    // objectAnimator = ObjectAnimator.ofFloat(blueDot,"y",400);



            x.clear();
            y.clear();
            z.clear();
          //  xsize=0;

        /*    x1.clear();
            y1.clear();
            z1.clear();
            x1size=0; */
        }
    }

    private float[] toFloatArray(List<Float> list) {
        int i = 0;
        float[] array = new float[list.size()];

        for (Float f : list) {
            array[i++] = (f != null ? f : Float.NaN);
        }
        return array;
    }

    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    private SensorManager getSensorManager() {
        return (SensorManager) getSystemService(SENSOR_SERVICE);
    }

}
