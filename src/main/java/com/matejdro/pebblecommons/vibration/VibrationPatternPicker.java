package com.matejdro.pebblecommons.vibration;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.matejdro.pebblecommons.R;

public class VibrationPatternPicker extends RelativeLayout
{
    private Vibrator vibrator;

    private EditText vibrationPatternBox;
    private Button tapActivationButton;
    private View vibrationTapperBox;

    private boolean tapMode = false;
    private long lastChangeTime = 0;
    private int currentTapTotalLength;
    private int currentTapSegmentCount;
    private String currentTapPattern;

    private int addedPause;

    public VibrationPatternPicker(Context context)
    {
        super(context);
        init();
    }

    public VibrationPatternPicker(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public VibrationPatternPicker(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VibrationPatternPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init()
    {
        if (!isInEditMode())
        {
            vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        }

        LayoutInflater.from(getContext()).inflate(R.layout.vibration_pattern_picker, this);

        vibrationPatternBox = (EditText) findViewById(R.id.pattern_box);
        tapActivationButton = (Button) findViewById(R.id.tap_button);
        vibrationTapperBox = findViewById(R.id.tap_box);

        tapActivationButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                toggleTapMode();
            }
        });

        findViewById(R.id.test_button).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                testVibration();
            }
        });

        vibrationTapperBox.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                onTapEvent(event);
                return false;
            }
        });
    }

    private void toggleTapMode()
    {
        tapMode = !tapMode;

        if (tapMode)
        {
            vibrationTapperBox.setVisibility(VISIBLE);
            tapActivationButton.setText(R.string.finish_tapping);
            lastChangeTime = 0;
            currentTapTotalLength = 0;
            currentTapSegmentCount = 0;
            currentTapPattern = "";
        }
        else
        {
            vibrationTapperBox.setVisibility(GONE);
            tapActivationButton.setText(R.string.tap_vibration_pattern);

            if (addedPause > 0)
                currentTapPattern += ", " + addedPause;

            vibrationPatternBox.setText(currentTapPattern);
            vibrator.cancel();
        }
    }

    private void testVibration()
    {
        vibrator.cancel();

        String pattern = validateAndGetCurrentPattern();
        if (pattern != null)
        {
            vibrator.vibrate(PebbleVibrationPattern.convertToAndroidPattern(pattern), -1);
        }
    }

    private void onTapEvent(MotionEvent event)
    {
        int eventType = event.getActionMasked();
        if (eventType == MotionEvent.ACTION_DOWN)
            tapModeFingerDown();
        else if (eventType == MotionEvent.ACTION_UP || eventType == MotionEvent.ACTION_OUTSIDE || eventType == MotionEvent.ACTION_CANCEL)
            tapModeFingerUp();
    }

    private void addChange()
    {
        int diffMs = (int) (System.currentTimeMillis() - lastChangeTime);
        lastChangeTime = System.currentTimeMillis();

        if (currentTapPattern.isEmpty())
            currentTapPattern += Integer.toString(diffMs);
        else
            currentTapPattern += ", " + Integer.toString(diffMs);

        currentTapTotalLength += diffMs;
        currentTapSegmentCount++;

        if (currentTapSegmentCount >= PebbleVibrationPattern.MAX_SEGMENTS || currentTapTotalLength >= PebbleVibrationPattern.MAX_LENGTH_MS)
        {
            Toast.makeText(getContext(), R.string.vibration_pattern_limit_reached, Toast.LENGTH_SHORT).show();
            toggleTapMode();
        }
    }

    private void tapModeFingerDown()
    {
        vibrator.vibrate(PebbleVibrationPattern.MAX_LENGTH_MS);

        if (lastChangeTime == 0)
        {
            lastChangeTime = System.currentTimeMillis();
        }
        else
        {
            addChange();
        }
    }

    private void tapModeFingerUp()
    {
        vibrator.cancel();
        addChange();
    }

    public String getCurrentPattern()
    {
        return vibrationPatternBox.getText().toString();
    }

    public void setCurrentPattern(String pattern)
    {
        vibrationPatternBox.setText(pattern);
    }

    public @Nullable String validateAndGetCurrentPattern()
    {
        String currentPattern = getCurrentPattern();

        if (!PebbleVibrationPattern.validateVibrationPattern(currentPattern))
        {
            vibrationPatternBox.setError(getContext().getString(R.string.invalid_vibration_pattern));
            return null;
        }

        return currentPattern;
    }

    public int getAddedPause()
    {
        return addedPause;
    }

    public void setAddedPause(int addedPause)
    {
        this.addedPause = addedPause;
    }
}
