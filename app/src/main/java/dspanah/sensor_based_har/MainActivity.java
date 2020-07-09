package dspanah.sensor_based_har;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
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

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Button;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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

  //image
    ImageView blueDot;
    ObjectAnimator  objectAnimatorX;
    ObjectAnimator  objectAnimatorY;
    ObjectAnimator animRot;

    private TextView runningTextView;
    private TextView standingTextView;
    private TextView walkingTextView;

    private SearchView destinationSearchView;
    private ListView destinationListView;
    ArrayList<String> list;
    java.util.HashMap<String,Integer> searchMap;
    ArrayAdapter<String> adapter;

    private float[] results;
    private HARClassifier classifier;
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private float cAzimuth = 0f;
    private float stepLengthX = 0f;
    private float stepLengthY = 0f;


    double Xcpos=90;
    double Xnpos=90;
    double Ycpos=120;
    double Ynpos=120;

    float density = 1;
    int nSteps=2;
  //  int xsize=0;
 //   int x1size=0;
    ImageView imageView;
    Bitmap myBlankBitmap;
    Bitmap bobBitmap;
    Bitmap destinationBitmap;
    Bitmap mutableBobBitmap;
    Canvas myCanvas;
    Paint myPaint;
    Node mapNodes[];
    Map map;
    int destination=0;


    private String[] labels = { "Running", "Standing", "Walking"};
    private ActivityClassifier activityClassifier;
    private DistanceEstimator distanceEstimator;
    private int activity=0;


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

        destinationSearchView = (SearchView) findViewById(R.id.destinationSearchView);
        destinationListView = (ListView) findViewById(R.id.destinationListView);
        list = new ArrayList<String>();
        searchMap = new HashMap<String,Integer>();
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list);

        blueDot = (ImageView) findViewById(R.id.blue_dot);
        imageView = (ImageView) findViewById(R.id.imageView);

        classifier = new HARClassifier(getApplicationContext());
        activityClassifier = new ActivityClassifier();
        distanceEstimator = new DistanceEstimator();


        //initialising the map
        mapNodes = new Node[36];
        mapNodes[0]  = new Node(87,220,1);
        mapNodes[1]  = new Node(140,220,0,2,3,7);
        mapNodes[2]  = new Node(140,175,1);
        mapNodes[3]  = new Node(140,299,1,4,5);
        mapNodes[4]  = new Node(100,299,3);
        mapNodes[5]  = new Node(140,322,3,6);
        mapNodes[6]  = new Node(185,224,5);
        mapNodes[7]  = new Node(305,220,1,8,9);
        mapNodes[8]  = new Node(305,180,7);
        mapNodes[9]  = new Node(531,220,7,13,14,10);
        mapNodes[10] = new Node(531,177,9,11,12);
        mapNodes[11] = new Node(472,17710,10);
        mapNodes[12] = new Node(571,177,10);
        mapNodes[13] = new Node(665,220,9);
        mapNodes[14] = new Node(531,273,9,17,15,16);
        mapNodes[15] = new Node(490,273,14);
        mapNodes[16] = new Node(571,273,14);
        mapNodes[17] = new Node(531,392,14,20,18,19);
        mapNodes[18] = new Node(485,392,17);
        mapNodes[19] = new Node(580,392,17);
        mapNodes[20] = new Node(531,504,17,21,24);
        mapNodes[21] = new Node(495,504,20,22);
        mapNodes[22] = new Node(495,526,21,23);
        mapNodes[23] = new Node(472,526,22);
        mapNodes[24] = new Node(531,566,20,25,26);
        mapNodes[25] = new Node(479,566,24);
        mapNodes[26] = new Node(531,655,24,27,28);
        mapNodes[27] = new Node(574,655,26);
        mapNodes[28] = new Node(531,701,26,29,30);
        mapNodes[29] = new Node( 567,701,28);
        mapNodes[30] = new Node(531,863,28,31,32,33);
        mapNodes[31] = new Node(390,863,30);
        mapNodes[32] = new Node(570,863,30);
        mapNodes[33] = new Node(531,945,30,34,35);
        mapNodes[34] = new Node(367,945,33);
        mapNodes[35] = new Node(662,945,33);


        map = new Map(mapNodes,36);

        //initialising searchArrayList
        list.add("Academic Section");
        list.add("Accounts Section");
        list.add("Washroom");
        list.add("Office");
        list.add("Principle's Room");
        list.add("Meeting Room");
        list.add("Classroom 1");
        list.add("Classroom 2");
        list.add("Classroom 3");
        list.add("Classroom 4");
        list.add("Cabin 1");
        list.add("Cabin 2");
        list.add("Transport Section");

        searchMap.put("Transport Section",2);
        searchMap.put("Cabin 1",4);
        searchMap.put("Cabin 2",6);
        searchMap.put("Classroom 1",18);
        searchMap.put("Classroom 2",11);
        searchMap.put("Classroom 3",12);
        searchMap.put("Classroom 4",19);
        searchMap.put("Meeting Room",27);
        searchMap.put("Principle's Room",32);
        searchMap.put("Office",32);
        searchMap.put("Washroom",29);
        searchMap.put("Accounts section",25);
        searchMap.put("Academic Section",31);


        destinationListView.setAdapter(adapter);

        //SearchView
        if ( destinationListView.getVisibility() == View.VISIBLE)
        {
            //expandedChildList.set(arg2, true);
            destinationListView.setVisibility(View.GONE);
        }

        destinationSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if ( destinationListView.getVisibility() == View.VISIBLE)
                    destinationListView.setVisibility(View.GONE);

                destination = searchMap.get(query);
                myCanvas.drawBitmap(destinationBitmap,(float)mapNodes[destination].x-(destinationBitmap.getWidth()/2),(float)mapNodes[destination].y-(destinationBitmap.getHeight()),null);

                //

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if ( destinationListView.getVisibility() == View.GONE)
                destinationListView.setVisibility(View.VISIBLE);

                adapter.getFilter().filter(newText);
                return false;
            }
        });

        //ListView
        destinationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView <?> parent, View view, int position, long id) {
                destinationSearchView.setQuery((CharSequence) destinationListView.getItemAtPosition(position),true);
            }
        });

        //density = getResources().getDisplayMetrics().density;
        density=1;

        //bitmap code
        bobBitmap = BitmapFactory.decodeResource
                (getResources(), R.drawable.gec_main_map700);
        destinationBitmap = BitmapFactory.decodeResource
                (getResources(), R.drawable.destination1);
      //  destinationBitmap = Bitmap.createScaledBitmap(destinationBitmap,30,50,true);
       // destinationBitmap = destinationBitmap.copy(Bitmap.Config.RGB_565,true);

        //Bitmap workingBobBitmap = Bitmap.createBitmap(bobBitmap);
        mutableBobBitmap = Bitmap.createScaledBitmap(bobBitmap,700,1075,true);//,50,10,bobBitmap.getWidth(),bobBitmap.getHeight()
        mutableBobBitmap = mutableBobBitmap.copy(Bitmap.Config.ARGB_8888, true);


        // Initialize the Canvas and associate it
        // with the Bitmap to draw on
        myCanvas = new Canvas(mutableBobBitmap);

        // Initialize the Paint
        myPaint = new Paint();

        //myPaint.setTextSize(100);
        myPaint.setStrokeWidth(8);
        // Change the paint to blue
        myPaint.setColor(Color.argb(100, 30, 144, 250));
        // myCanvas.drawText("Hello World!",100, 100, myPaint);

// Associate the drawn upon Bitmap with the ImageView
        imageView.setImageBitmap(mutableBobBitmap);

       imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                int x = (int)event.getX();
                int y = (int)event.getY();

                System.out.println("CLICK X Relative to image:"+x);
                System.out.println("CLICK Y Relative to image :"+y);

                x = x + (int)imageView.getX();
                y = y + (int)imageView.getY();

               // System.out.println("CLICK X Relative to view:"+x);
               // System.out.println("CLICK Y Relative to view:"+y);

                objectAnimatorX = ObjectAnimator.ofFloat(blueDot,"x",x,x);
                objectAnimatorY = ObjectAnimator.ofFloat(blueDot,"y",y,y);

                AnimatorSet animSetXY = new AnimatorSet();
                animSetXY.playTogether(objectAnimatorX, objectAnimatorY);
                animSetXY.start();

              //  System.out.println("bluedotX: "+blueDot.getX());
              //  System.out.println("bluedotY: "+blueDot.getY());

                Xnpos = x/density;
                Ynpos = y/density;
                return true;
            }
        });


    }

    @Override
    public void onInit(int status) {

    }

    protected void onPause() {
        getSensorManager().unregisterListener(this);
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.97f;
        //if(x.size()==20 || x.size()==40 || x.size()==60 || x.size()==90)
          //  getDirections((Button) findViewById(R.id.get_directions_button));

        activityPrediction();
        synchronized (this){

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ){   // && x.size() < N_SAMPLES
                x.add(event.values[0]); //System.out.println("AccX"+event.values[0]);
                y.add(event.values[1]);
                z.add(event.values[2]);

                mGravity[0] = alpha*mGravity[0]+(1-alpha)*event.values[0];
                mGravity[1] = alpha*mGravity[1]+(1-alpha)*event.values[1];
                mGravity[2] = alpha*mGravity[2]+(1-alpha)*event.values[2];
                //  xsize++;
            }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD ){   //&& x1.size() < N_SAMPLES
            mGeomagnetic[0] = alpha*mGeomagnetic[0]+(1-alpha)*event.values[0];  //System.out.println("MagX"+event.values[0]);
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

            if(azimuth>0 && azimuth<30){
                azimuth=0;
            }
            else if(azimuth>30 && azimuth<=60){
                azimuth=45;
            }

            else if(azimuth>60 && azimuth<=90){
                azimuth=90;
            }

            else if(azimuth>90 && azimuth<=120){
                azimuth=90;
            }

            else if(azimuth>120 && azimuth<=150){
                azimuth=135;
            }

            else if(azimuth>150 && azimuth<=180){
                azimuth=180;
            }
            else if(azimuth>180 && azimuth<=210){
                azimuth=180;
            }

            else if(azimuth>210 && azimuth<=240){
                azimuth=225;
            }

            else if(azimuth>240 && azimuth<=270){
                azimuth=270;
            }

            else if(azimuth>270 && azimuth<=300){
                azimuth=270;
            }

            else if(azimuth>300 && azimuth<=330){
                azimuth=315;
            }

            else if(azimuth>330 && azimuth<=360){
                azimuth=0;
            }

         //   System.out.println("AZIMUTH:  "+azimuth);

            animRot = ObjectAnimator.ofFloat(blueDot,"rotation",cAzimuth,azimuth);
            animRot.setDuration(1);
            animRot.start();
      //    Animation animRot = new RotateAnimation(-cAzimuth,-azimuth,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            cAzimuth=azimuth;
           /*
            animRot.setRepeatCount(0);
            animRot.setFillAfter(true);*/
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
            //getDirections();

            activity = activityClassifier.getActivity(results);

            if(activity ==0 || activity ==2) //RUN || WALK
            {
                stepLengthX = distanceEstimator.getStepLengthX(activity);
                stepLengthY = distanceEstimator.getStepLengthY(activity);
                Xcpos = Xnpos;
                Xnpos = Xnpos +  (nSteps*stepLengthX*Math.cos(Math.toRadians(azimuth)));// System.out.println("Azimuth"+(azimuth-90));
            //    System.out.println("Val"+50*Math.cos(Math.toRadians(azimuth-90)));

             //   System.out.println("Cpos "+cpos+"Npos"+npos);

                Ycpos = Ynpos;
                Ynpos = Ynpos +  (nSteps*stepLengthY*Math.sin(Math.toRadians(azimuth)));

                objectAnimatorX = ObjectAnimator.ofFloat(blueDot,"x",density* (float)Xcpos, density* (float)Xnpos);
                objectAnimatorX.setDuration(2400);
                objectAnimatorY = ObjectAnimator.ofFloat(blueDot,"y",density* (float)Ycpos,density* (float)Ynpos);
                objectAnimatorY.setDuration(2400);

                AnimatorSet animSetXY = new AnimatorSet();
                animSetXY.playTogether(objectAnimatorX, objectAnimatorY);
                animSetXY.start();

              /*  System.out.println("getTop "+imageView.getTop());
                System.out.println("getX "+imageView.getX());
                System.out.println("getY "+imageView.getY());
                System.out.println("Width "+imageView.getWidth());
                System.out.println("height "+imageView.getHeight()); */

            }

            x.clear();
            y.clear();
            z.clear();

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

    public float dist(Node node1,Node node2)
    {
        return (float)Math.sqrt( (node2.x - node1.x) * (node2.x - node1.x) + (node2.y - node1.y) * (node2.y - node1.y) );
    }
    public void getDirections(View view) {

      //  paint.setStyle(Paint.Style.STROKE);
     //   paint.setAntiAlias(true);

        int path[];
        int blueDotX = (int)blueDot.getX()-(int)imageView.getX()+(int)blueDot.getWidth()/2;
        int blueDotY = (int)blueDot.getY()-(int)imageView.getY()+(int)blueDot.getHeight()/2;


        Node blueDot = new Node(blueDotX,blueDotY);

        mutableBobBitmap = Bitmap.createScaledBitmap(bobBitmap,700,1075,true);//,50,10,bobBitmap.getWidth(),bobBitmap.getHeight()
        mutableBobBitmap = mutableBobBitmap.copy(Bitmap.Config.ARGB_8888, true);

        myCanvas.setBitmap(mutableBobBitmap);
       // myCanvas.drawLine((int)blueDot.getX()-(int)imageView.getX()+(int)blueDot.getWidth()/2,(int)blueDot.getY()-(int)imageView.getY()+(int)blueDot.getHeight()/2,250,300,myPaint);
       // myCanvas.drawLines(path,myPaint);
        imageView.setImageBitmap(mutableBobBitmap);
        myCanvas.drawBitmap(destinationBitmap,(float)mapNodes[destination].x-(destinationBitmap.getWidth()/2),(float)mapNodes[destination].y-(destinationBitmap.getHeight()),null);

        float small=1000f;
        float d;
        int closestNode=0;
        for(int i=0;i<36;i++)
        {
            d = dist(mapNodes[i],blueDot);
            if( d < small)
            {
                small = d;
                closestNode = i;
            }
        }
      //  myCanvas.drawLine(87,220,140,220,myPaint);
        myCanvas.drawLine((float)blueDotX,(float)blueDotY,(float) mapNodes[closestNode].x , (float) mapNodes[closestNode].y ,myPaint);
        path = map.printShortestDistance(closestNode,destination,36);

        float xOffset = (float)imageView.getX();
        float yOffset = (float)imageView.getY();
        for(int i=0;i<path.length - 1;i++)
        {
            myCanvas.drawLine((float) mapNodes[path[i]].x , (float) mapNodes[path[i]].y ,(float) mapNodes[path[i+1]].x , (float) mapNodes[path[i+1]].y,myPaint);

        }

    }

}
