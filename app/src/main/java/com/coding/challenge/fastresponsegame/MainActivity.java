package com.coding.challenge.fastresponsegame;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.coding.challenge.fastresponsegame.utils.ColourTypes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity {

    private ColourTypes colourTypes;
    private AlertDialog dialogBuilder;
    private TextView tvScoreView;
    private FloatingActionButton fabRetryGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colourTypes = ColourTypes.getInstance(getApplicationContext());

        tvScoreView = findViewById(R.id.tvScoreView);
        fabRetryGame = findViewById(R.id.fabRetryGame);

        //Create custom Dialog to select the colour of the arrows
        dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_select_arrow_color, null);

        ImageView ivArrowBlue = dialogView.findViewById(R.id.ivArrowBlue);
        ImageView ivArrowRed = dialogView.findViewById(R.id.ivArrowRed);
        ImageView ivArrowGreen = dialogView.findViewById(R.id.ivArrowGreen);
        ImageView ivArrowPurple = dialogView.findViewById(R.id.ivArrowPurple);

        //Add onClick listeners on the Arrow images (Set value of the Arrow Colour)
        ivArrowBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colourTypes.setIndex(ColourTypes.COLOUR_BLUE);
                startGameActivity();
                dialogBuilder.dismiss();
            }
        });

        ivArrowRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colourTypes.setIndex(ColourTypes.COLOUR_RED);
                startGameActivity();
                dialogBuilder.dismiss();
            }
        });

        ivArrowGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colourTypes.setIndex(ColourTypes.COLOUR_GREEN);
                startGameActivity();
                dialogBuilder.dismiss();
            }
        });

        ivArrowPurple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colourTypes.setIndex(ColourTypes.COLOUR_PURPLE);
                startGameActivity();
                dialogBuilder.dismiss();
            }
        });

        //Add Dismiss handler on Dialog so that if user dismisses the dialog without selecting
        //a color for the arrows the retry button is showed which enables the user to try again.
        dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                fabRetryGame.show();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    //Show the Dialog to select the colour of the arrows again
    public void restartGame(View v) {
        dialogBuilder.show();
    }

    public void startGameActivity() {
        //Start activity for result
        Intent intent = new Intent(this, GameActivity.class);
        startActivityForResult(intent, GameActivity.REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Get the results from GameActivity, and display the results in the tvScoreView.
        if (requestCode == GameActivity.REQUEST_CODE) {
            int gameScore = data.getIntExtra("GAME_SCORE", 0);

            tvScoreView.setVisibility(View.VISIBLE);
            fabRetryGame.show();
            tvScoreView.setText(getString(R.string.main_act_score, gameScore));
        }
    }

}
