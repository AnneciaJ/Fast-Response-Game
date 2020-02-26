package com.coding.challenge.fastresponsegame;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.coding.challenge.fastresponsegame.utils.ColourTypes;

import java.util.Locale;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity implements SensorEventListener {

    public static final int REQUEST_CODE = 100;

    final Random random = new Random();

    private CountDownTimer currentRoundTimer;
    private ImageView ivArrow;
    private TextView tvCurrentRound, tvCurrentScore, tvCountDown;

    private int maxNumberOfRounds = 10;
    private int countDownNumber = 3; //Countdown starts at 3.
    private int currentRound = 1; //Round starts at 1
    private int currentScore = 0;
    private int currentRoundDirection = 0;
    private boolean currentRoundActive = false;
    private int currentPlayerDirection = SENSOR_DIRECTION_NONE; //Default direction is none

    public static int ROTATION_DEGREES_LEFT = 0;
    public static int ROTATION_DEGREES_UP = 90;
    public static int ROTATION_DEGREES_RIGHT = 180;
    public static int ROTATION_DEGREES_DOWN = 270;

    public static int SENSOR_DIRECTION_LEFT = 0;
    public static int SENSOR_DIRECTION_UP = 1;
    public static int SENSOR_DIRECTION_RIGHT = 2;
    public static int SENSOR_DIRECTION_DOWN = 3;
    public static int SENSOR_DIRECTION_NONE = -1;

    private int[] rotations = {ROTATION_DEGREES_LEFT, ROTATION_DEGREES_UP, ROTATION_DEGREES_RIGHT, ROTATION_DEGREES_DOWN};
//    private String[] directions_str = {"DIRECTION_LEFT", "DIRECTION_UP", "DIRECTION_RIGHT", "DIRECTION_DOWN"};

    //Sensor related code was adapted from the SensorProcessorActivity.java file found at
    //  https://github.com/bazilio91/android-grass-cutter
    private SensorManager mSensorManager = null;

    // magnetic field vector
    private float[] magnet = new float[3];

    // accelerometer vector
    private float[] accel = new float[3];

    // orientation angles from accel and magnet
    protected float[] accMagOrientation = new float[3];

    // accelerometer and magnetometer based rotation matrix
    private float[] rotationMatrix = new float[9];

    public GameActivity() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //Get SensorManager and initialize the listeners
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        initListeners();

        ivArrow = findViewById(R.id.ivArrow);
        tvCurrentRound = findViewById(R.id.tvCurrentRound);
        tvCurrentScore = findViewById(R.id.tvCurrentScore);
        tvCountDown = findViewById(R.id.tvCountDown);

        ColourTypes colourTypes = ColourTypes.getInstance(getApplicationContext());
        ivArrow.setColorFilter(colourTypes.getCurrentColourId());

        startGameCountDown();
    }

    public void startGameCountDown() {
        //Create a count down timer that counts down every second from 3(countDownNumber)
        new CountDownTimer((countDownNumber*1000), 1000) {
            @Override
            public void onTick(long l) {
                countDownNumber = countDownNumber - 1;
                updateCountDown();
            }

            @Override
            public void onFinish() {
                //After the countdown, hide the countdown view. Display a message to show the game has began
                tvCountDown.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Game has Begun!" , Toast.LENGTH_SHORT).show();
                startGame();
            }
        }.start();
    }

    //Update the countdown text view with the new count down number
    private void updateCountDown() {
        tvCountDown.setText(String.format(Locale.getDefault(),"%d", countDownNumber + 1));
    }

    //Method called when the game begins.
    private void startGame() {
        updateUI(); //Update the currentRound and currentScore text views to display the default values or start values.
        tvCurrentRound.setVisibility(View.VISIBLE);
        tvCurrentScore.setVisibility(View.VISIBLE);

        //Delay the start of the first round by 1 second.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startNewRound();
            }
        }, 1000);

    }

    private void startNewRound() {
        //Check if the current round is still less/equal than the max number of round (Default no round is 10)
        if (currentRound <= maxNumberOfRounds) {
            //Get random number between 0 and 3 to determine the rotation(Direction of the arrows)
            int i1 = random.nextInt(4);
            ivArrow.setRotation(rotations[i1]);
            ivArrow.setVisibility(View.VISIBLE);
            currentRoundDirection = i1;

            //Get random number between 2 and 5 to determine the time interval for current round.
            int roundInterval = random.nextInt(5 - 2 + 1) + 2;

            //Set currentRoundActive as true and start count down timer based on the random number generated.
            currentRoundActive = true;
            currentRoundTimer = new CountDownTimer(roundInterval*1000, 100) {
                @Override
                public void onTick(long l) {
                    //Check if sensor direction has changed every 0.1 seconds
                    if (currentPlayerDirection != SENSOR_DIRECTION_NONE) {
                        playerAction();
                    }
                }

                @Override
                public void onFinish() {
                    currentRoundActive = false;
                    updateUI();
                    ivArrow.setVisibility(View.GONE);
                    currentRound++;
                    startNewRound();
                }
            }.start();

        } else {
            finishedGame();
        }
    }

    private void playerAction() {
        //Check if the action occurred while the round is still active(No actions taken yet)
        if(currentRoundActive) {
            //If the round is still active and the user tilted the phone in the correct direction
            //Increase the score by one, set currentRoundActive to false and hide the arrow image.
            if (currentRoundDirection == currentPlayerDirection) {
                currentScore++;
                currentRoundActive = false;
                ivArrow.setVisibility(View.GONE);
            }
            //Currently nothing happens if the user tilted the phone in the wrong direction.
        } else {
            //If the user tilted the phone while the round is no longer active (Meaning already won the current
            //round, but next round hasn't started yet) the user is penalized by having his score decreased by one.
            if (currentPlayerDirection != SENSOR_DIRECTION_NONE) {
                Toast.makeText(getApplicationContext(), "Round hasn't started yet!" , Toast.LENGTH_SHORT).show();
                currentScore--;
            }
        }

        //Pause the listeners for the sensors for 0.5 seconds after every action
        //give the use time to return phone to start position or else
        //code will run multiple times for a 'single' action resulting the user being penalized.
        pauseListenersAfterAction();
        updateUI(); //Update the currentScore and currentRound views with the new scores.

    }

    //Pause the listeners for the sensors for 0.5 seconds and set the current direction of the
    //player/user to NONE
    private void pauseListenersAfterAction() {
        mSensorManager.unregisterListener(this);
        currentPlayerDirection = SENSOR_DIRECTION_NONE;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initListeners();
            }
        }, 500);
    }

    private void updateUI() {
        //Method to update Text on Screen (Update Round + score)
        tvCurrentRound.setText(getString(R.string.game_act_current_round, currentRound));
        tvCurrentScore.setText(getString(R.string.game_act_current_score, currentScore));

    }

    public void finishedGame() {
        //At the end of the game/After final round(10) send the score to Main Activity to display.
        Intent data = new Intent();
        data.putExtra("GAME_SCORE", currentScore);

        setResult(RESULT_OK, data);
        finish();
    }



    @Override
    public void onStop() {
        super.onStop();
        // unregister sensor listeners to prevent the activity from draining the device's battery.
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister sensor listeners to prevent the activity from draining the device's battery.
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // restore the sensor listeners when user resumes the application.
        initListeners();
    }

//    ////////////////////////////////////////////////////////////////////////////////////////////
//    Methods related to Sensors
//    ///////////////////////////////////////////////////////////////////////////////////////////

    // This function registers sensor listeners for the accelerometer, magnetometer and gyroscope.
    public void initListeners() {
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // copy new accelerometer data into accel array and calculate orientation
                System.arraycopy(sensorEvent.values, 0, accel, 0, 3);
                calculateAccMagOrientation(sensorEvent.values);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                // copy new magnetometer data into magnet array
                System.arraycopy(sensorEvent.values, 0, magnet, 0, 3);
                break;
        }

    }

    // calculates orientation angles from accelerometer and magnetometer output
    public void calculateAccMagOrientation(float[] values) {

        //Code at : https://stackoverflow.com/questions/38711705/android-device-orientation-without-geomagnetic
        //For devices device orientation without geomagnetic
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        } else {
            double gx, gy, gz;
            gx = accel[0] / 9.81f;
            gy = accel[1] / 9.81f;
            gz = accel[2] / 9.81f;
            // http://theccontinuum.com/2012/09/24/arduino-imu-pitch-roll-from-accelerometer/
            float pitch = (float) -Math.atan(gy / Math.sqrt(gx * gx + gz * gz));
            float roll = (float) -Math.atan(gx / Math.sqrt(gy * gy + gz * gz));
            float azimuth = 0; // Impossible to guess

            accMagOrientation[0] = azimuth;
            accMagOrientation[1] = pitch;
            accMagOrientation[2] = roll;
            accMagOrientation = getRotationMatrixFromOrientation(accMagOrientation);
        }

        //https://github.com/kevinvanzyl/showdown-at-high-noon/blob/master/app/src/main/java/codes/kevinvanzyl/showdownathighnoon/sensors/TiltSensor.java
        float x = values[0];
        float y = values[1];
        if (Math.abs(x) > Math.abs(y)) {
            if (x < 0) {

                if (Math.toDegrees(accMagOrientation[2]) >= 50) {
                    currentPlayerDirection = SENSOR_DIRECTION_UP;
                }
            }
            if (x > 0) {

                if (Math.toDegrees(accMagOrientation[2]) <= -50) {
                    currentPlayerDirection = SENSOR_DIRECTION_DOWN;
                }
            }
        } else {
            if (y < 0) {

                if (Math.toDegrees(accMagOrientation[1]) >= 50) {
                    currentPlayerDirection = SENSOR_DIRECTION_LEFT;
                }
            }
            if (y > 0) {

                if (Math.toDegrees(accMagOrientation[1]) <= -50) {
                    currentPlayerDirection = SENSOR_DIRECTION_RIGHT;
                }
            }
        }
        if (x > (-2) && x < (2) && y > (-2) && y < (2)) {

            currentPlayerDirection = SENSOR_DIRECTION_NONE;
        }
    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float) Math.sin(o[1]);
        float cosX = (float) Math.cos(o[1]);
        float sinY = (float) Math.sin(o[2]);
        float cosY = (float) Math.cos(o[2]);
        float sinZ = (float) Math.sin(o[0]);
        float cosZ = (float) Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f;
        xM[1] = 0.0f;
        xM[2] = 0.0f;
        xM[3] = 0.0f;
        xM[4] = cosX;
        xM[5] = sinX;
        xM[6] = 0.0f;
        xM[7] = -sinX;
        xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY;
        yM[1] = 0.0f;
        yM[2] = sinY;
        yM[3] = 0.0f;
        yM[4] = 1.0f;
        yM[5] = 0.0f;
        yM[6] = -sinY;
        yM[7] = 0.0f;
        yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ;
        zM[1] = sinZ;
        zM[2] = 0.0f;
        zM[3] = -sinZ;
        zM[4] = cosZ;
        zM[5] = 0.0f;
        zM[6] = 0.0f;
        zM[7] = 0.0f;
        zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Must be implemented to satisfy the SensorEventListener interface;
    }

}
