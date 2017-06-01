package com.feldens.lorenzo.moviepicker;
        import android.app.Activity;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Matrix;
        import android.os.Bundle;
        import android.view.GestureDetector;
        import android.view.GestureDetector.SimpleOnGestureListener;
        import android.view.MotionEvent;
        import android.view.View;
        import android.view.View.OnTouchListener;
        import android.view.ViewTreeObserver.OnGlobalLayoutListener;
        import android.widget.ImageView;
        import android.widget.TextView;

        import java.util.Random;

/**
 * <a href="http://www.vrallev.net">About the author</a>
 *
 * @author Ralf Wondratschek
 * @version 1.0
 *
 */
public class MainActivity extends Activity {

    private static Bitmap imageOriginal, imageScaled;
    private static Matrix matrix;

    private double angle;

    private ImageView dialer;
    private int dialerHeight, dialerWidth;

    private GestureDetector detector;

    // needed for detecting the inversed rotations
    private boolean[] quadrantTouched;

    private boolean allowRotating;

    private TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // load the image only once
        if (imageOriginal == null) {
            imageOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.wheel);
        }

        // initialize the matrix only once
        if (matrix == null) {
            matrix = new Matrix();
        } else {
            // not needed, you can also post the matrix immediately to restore the old state
            matrix.reset();
        }

        detector = new GestureDetector(this, new MyGestureDetector());

        // there is no 0th quadrant, to keep it simple the first value gets ignored
        quadrantTouched = new boolean[] { false, false, false, false, false };

        allowRotating = true;

        angle = 0;

        textView = (TextView) findViewById(R.id.textView);
        textView.setText("Lord of The Rings");

        dialer = (ImageView) findViewById(R.id.imageView_ring);
        dialer.setOnTouchListener(new MyOnTouchListener());
        dialer.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // method called more than once, but the values only need to be initialized one time
                if (dialerHeight == 0 || dialerWidth == 0) {
                    dialerHeight = dialer.getHeight();
                    dialerWidth = dialer.getWidth();

                    // resize
                    Matrix resize = new Matrix();
                    resize.postScale((float)Math.min(dialerWidth, dialerHeight) / (float)imageOriginal.getWidth(), (float)Math.min(dialerWidth, dialerHeight) / (float)imageOriginal.getHeight());
                    imageScaled = Bitmap.createBitmap(imageOriginal, 0, 0, imageOriginal.getWidth(), imageOriginal.getHeight(), resize, false);

                    // translate to the image view's center
                    float translateX = dialerWidth / 2 - imageScaled.getWidth() / 2;
                    float translateY = dialerHeight / 2 - imageScaled.getHeight() / 2;
                    matrix.postTranslate(translateX, translateY);

                    dialer.setImageBitmap(imageScaled);
                    dialer.setImageMatrix(matrix);
                }
            }
        });

    }

    public void button(View v){
        Random r = new Random();
        textView.setText("Rotating..");
        dialer.post(new FlingRunnable(6000 + r.nextInt(4000)));
    }

    /**
     * Rotate the dialer.
     *
     * @param degrees The degrees, the dialer should get rotated.
     */
    private void rotateDialer(float degrees) {
        angle += degrees;
        matrix.postRotate(degrees, dialerWidth / 2, dialerHeight / 2);

        dialer.setImageMatrix(matrix);
    }

    /**
     * @return The angle of the unit circle with the image view's center
     */
    private double getAngle(double xTouch, double yTouch) {
        double x = xTouch - (dialerWidth / 2d);
        double y = dialerHeight - yTouch - (dialerHeight / 2d);

        switch (getQuadrant(x, y)) {
            case 1:
                return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;

            case 2:
            case 3:
                return 180 - (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);

            case 4:
                return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;

            default:
                // ignore, does not happen
                return 0;
        }
    }

    /**
     * @return The selected quadrant.
     */
    private static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }

    /**
     * Simple implementation of an {@link OnTouchListener} for registering the dialer's touch events.
     */
    private class MyOnTouchListener implements OnTouchListener {

        private double startAngle;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:

                    // reset the touched quadrants
                    for (int i = 0; i < quadrantTouched.length; i++) {
                        quadrantTouched[i] = false;
                    }

                    allowRotating = false;

                    startAngle = getAngle(event.getX(), event.getY());
                    break;

                case MotionEvent.ACTION_MOVE:
                    double currentAngle = getAngle(event.getX(), event.getY());
                    rotateDialer((float) (startAngle - currentAngle));
                    startAngle = currentAngle;
                    break;

                case MotionEvent.ACTION_UP:
                    allowRotating = true;
                    break;
            }

            // set the touched quadrant to true
            quadrantTouched[getQuadrant(event.getX() - (dialerWidth / 2), dialerHeight - event.getY() - (dialerHeight / 2))] = true;

            detector.onTouchEvent(event);

            return true;
        }
    }

    /**
     * Simple implementation of a {@link SimpleOnGestureListener} for detecting a fling event.
     */
    private class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            // get the quadrant of the start and the end of the fling
            int q1 = getQuadrant(e1.getX() - (dialerWidth / 2), dialerHeight - e1.getY() - (dialerHeight / 2));
            int q2 = getQuadrant(e2.getX() - (dialerWidth / 2), dialerHeight - e2.getY() - (dialerHeight / 2));

            // the inversed rotations
            textView.setText("Rotating..");
            if ((q1 == 2 && q2 == 2 && Math.abs(velocityX) < Math.abs(velocityY))
                    || (q1 == 3 && q2 == 3)
                    || (q1 == 1 && q2 == 3)
                    || (q1 == 4 && q2 == 4 && Math.abs(velocityX) > Math.abs(velocityY))
                    || ((q1 == 2 && q2 == 3) || (q1 == 3 && q2 == 2))
                    || ((q1 == 3 && q2 == 4) || (q1 == 4 && q2 == 3))
                    || (q1 == 2 && q2 == 4 && quadrantTouched[3])
                    || (q1 == 4 && q2 == 2 && quadrantTouched[3])) {

                dialer.post(new FlingRunnable(-1 * (velocityX + velocityY)));
            } else {
                // the normal rotation
                dialer.post(new FlingRunnable(velocityX + velocityY));
            }

            return true;
        }
    }

    /**
     * A {@link Runnable} for animating the the dialer's fling.
     */
    private class FlingRunnable implements Runnable {

        private float velocity;

        public FlingRunnable(float velocity) {
            this.velocity = velocity;
        }

        @Override
        public void run() {
            System.out.println("V=="+velocity);
            if (Math.abs(velocity) > 5 && allowRotating) {
                rotateDialer(velocity / 75);
                velocity /= 1.0666F;

                // post this instance again
                dialer.post(this);
            }else{
                double currentAngle = angle;
                while (currentAngle>=360){
                    currentAngle-=360;
                }
                while (currentAngle<0){
                    currentAngle+=360;
                }
                angle = currentAngle;

                if(currentAngle<=15){
                    textView.setText("Lord of the Rings");
                }else{
                    if(currentAngle<=45){
                        textView.setText("Donnie Darko");
                    }else{
                        if(currentAngle<=75){
                            textView.setText("Filth");
                        }else{
                            if(currentAngle<=105){
                                textView.setText("Monty Python");
                            }else{
                                if(currentAngle<=135){
                                    textView.setText("Pulp Fiction");
                                }else{
                                    if(currentAngle<=165){
                                        textView.setText("Star Wars");
                                    }else{
                                        if(currentAngle<=195){
                                            textView.setText("Jurassic Park");
                                        }else{
                                            if(currentAngle<=225){
                                                textView.setText("A Clockwork Orange");
                                            }else{
                                                if(currentAngle<=255){
                                                    textView.setText("Shaun of the Dead");
                                                }else{
                                                    if(currentAngle<=285){
                                                        textView.setText("Superbad");
                                                    }else{
                                                        if(currentAngle<=315){
                                                            textView.setText("American Beauty");
                                                        }else{
                                                            if(currentAngle<=345){
                                                                textView.setText("Scott Pilgrim vs. the World");
                                                            }else{
                                                                textView.setText("Lord of the Rings");
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
