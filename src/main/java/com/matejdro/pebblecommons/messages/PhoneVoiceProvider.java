package com.matejdro.pebblecommons.messages;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Parcel;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.matejdro.pebblecommons.R;
import com.matejdro.pebblecommons.messages.MessageTextProvider;
import com.matejdro.pebblecommons.messages.MessageTextProviderListener;
import com.matejdro.pebblecommons.userprompt.UserPrompter;
import com.matejdro.pebblecommons.util.BluetoothHeadsetListener;

import java.util.ArrayList;
import timber.log.Timber;

/**
 * Created by Matej on 28.9.2014.
 */
public class PhoneVoiceProvider extends BroadcastReceiver implements RecognitionListener, MessageTextProvider
{
    private static final String INTENT_ACTION_VOICE_CANCEL = "com.matejdro.pebblecommon.notificationaction.PHONE_VOICE_CANCEL";
    private static final String INTENT_ACTION_VOICE_RETRY = "com.matejdro.pebblecommon.notificationaction.PHONE_VOICE_RETRY";
    private static final String INTENT_ACTION_VOICE_RESULTS = "com.matejdro.pebblecommon.notificationaction.PHONE_VOICE_RESULTS";

    private static final String INTENT_EXTRA_RESULT_TEXT = "result_text";

    private SpeechRecognizer recognizer;
    private UserPrompter userPrompter;
    private MessageTextProviderListener textListener;
    private boolean waitingForBluetooth;
    private Context context;

    public PhoneVoiceProvider(UserPrompter userPrompter, Context context)
    {
        this.userPrompter = userPrompter;
        this.context = context;

        waitingForBluetooth = false;
    }

    @Override
    public void startRetrievingText(MessageTextProviderListener textListener)
    {
        this.textListener = textListener;
        startVoice();
    }

    public void startVoice()
    {
        Timber.d("startVoice");

        if (waitingForBluetooth)
            return;

        if (recognizer != null)
        {
            recognizer.stopListening();
            recognizer.destroy();
        }

        if (BluetoothHeadsetListener.isHeadsetConnected(context))
        {
            Timber.d("BT Wait");

            sendStatusNotification(context.getString(R.string.voiceInputBluetoothWait));

            context.registerReceiver(new BluetoothAudioListener(), new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
            waitingForBluetooth = true;

            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.startBluetoothSco();
            audioManager.setBluetoothScoOn(true);
        }
        else
        {
            Timber.d("Regular voice start");

            sendStatusNotification(context.getString(R.string.voiceInputSpeakInstructions));
            startRecognizing();
        }
    }

    public void stopVoice()
    {
        recognizer.stopListening();
        recognizer.destroy();

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isBluetoothScoOn())
        {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.stopBluetoothSco();
            audioManager.setBluetoothScoOn(false);
        }
    }

    public void startRecognizing()
    {
        Timber.d("startRecognizing");

        recognizer = SpeechRecognizer.createSpeechRecognizer(context);
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SECURE, true);

        recognizer.setRecognitionListener(this);
        recognizer.startListening(speechRecognizerIntent);
    }

    @Override
    public void onReadyForSpeech(Bundle bundle)
    {
    }

    @Override
    public void onBeginningOfSpeech()
    {
    }

    @Override
    public void onRmsChanged(float v)
    {

    }

    @Override
    public void onBufferReceived(byte[] bytes)
    {

    }

    @Override
    public void onEndOfSpeech()
    {
    }

    @Override
    public void onError(int i)
    {
        Timber.d("voiceError " + i);

        stopVoice();

        switch (i)
        {
            case SpeechRecognizer.ERROR_NETWORK:
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                sendErrorNotification(context.getString(R.string.voiceErrorNoInternet));
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                sendErrorNotification(context.getString(R.string.voiceErrorNoSpeech));
                break;
            default:
                sendErrorNotification(context.getString(R.string.voiceErrorUnknown));
                break;
        }
    }

    @Override
    public void onResults(Bundle bundle)
    {
        stopVoice();

        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        int size = Math.min(matches.size(), 10);

        Timber.d("voiceResults " + size);

        if (size == 0)
        {
            sendErrorNotification(context.getString(R.string.voiceErrorNoSpeech));
            return;
        }

        String resultsText = "";
        for (int i = 0; i < size; i++)
        {
            resultsText = resultsText.concat(context.getString((R.string.voiceInputResultTitle), i + 1));
            resultsText = resultsText.concat(matches.get(i));
            if (i != size - 1)
                resultsText = resultsText.concat("\n\n");
        }

        UserPrompter.PromptAnswer[] answers = new UserPrompter.PromptAnswer[size + 2];
        for (int i = 0; i < size; i++)
        {
            Intent answerIntent = new Intent(INTENT_ACTION_VOICE_RESULTS);
            answerIntent.putExtra(INTENT_EXTRA_RESULT_TEXT, matches.get(i));

            answers[i] = new UserPrompter.PromptAnswer(context.getString(R.string.voiceInputResultActionName, i + 1), answerIntent);
        }
        answers[size] = new UserPrompter.PromptAnswer("Retry", new Intent(INTENT_ACTION_VOICE_RETRY));
        answers[size + 1] = new UserPrompter.PromptAnswer("Cancel", new Intent(INTENT_ACTION_VOICE_CANCEL));

        String title = context.getString(R.string.voiceInputNotificationTitle);
        String body = context.getString(R.string.voiceInputResultNotificationText, resultsText);
        userPrompter.promptUser(title, null, body, answers);

        context.registerReceiver(this, new IntentFilter(INTENT_ACTION_VOICE_RETRY));
        context.registerReceiver(this, new IntentFilter(INTENT_ACTION_VOICE_CANCEL));
        context.registerReceiver(this, new IntentFilter(INTENT_ACTION_VOICE_RESULTS));
    }

    @Override
    public void onPartialResults(Bundle bundle)
    {

    }

    @Override
    public void onEvent(int i, Bundle bundle)
    {

    }

    private void sendErrorNotification(String error)
    {

        String title = context.getString(R.string.voiceInputNotificationTitle);
        String subtitle = context.getString(R.string.voiceInputErrorNotificationSubtitle);
        String body = context.getString(R.string.voiceInputErrorNotificationText, error);

        userPrompter.promptUser(title, subtitle, body,
                                new UserPrompter.PromptAnswer("Retry", new Intent(INTENT_ACTION_VOICE_RETRY)),
                new UserPrompter.PromptAnswer("Cancel", new Intent(INTENT_ACTION_VOICE_CANCEL)));

        context.registerReceiver(this, new IntentFilter(INTENT_ACTION_VOICE_RETRY));
        context.registerReceiver(this, new IntentFilter(INTENT_ACTION_VOICE_CANCEL));
    }

    private void sendStatusNotification(String body)
    {
        String title = context.getString(R.string.voiceInputNotificationTitle);

        userPrompter.promptUser(title, null, body,
                new UserPrompter.PromptAnswer("Cancel", new Intent(INTENT_ACTION_VOICE_CANCEL)));

        context.registerReceiver(this, new IntentFilter(INTENT_ACTION_VOICE_CANCEL));
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {

        if (INTENT_ACTION_VOICE_CANCEL.equals(intent.getAction()))
        {
            stopVoice();
        }
        else if (INTENT_ACTION_VOICE_RETRY.equals(intent.getAction()))
        {
            startVoice();
        }
        else if (INTENT_ACTION_VOICE_RESULTS.equals(intent.getAction()))
        {
            String text = intent.getStringExtra(INTENT_EXTRA_RESULT_TEXT);
            textListener.gotText(text);
        }
        else
        {
            return;
        }

        context.unregisterReceiver(this);
    }

    private class BluetoothAudioListener extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_DISCONNECTED);
            if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED)
            {
                startRecognizing();
                context.unregisterReceiver(this);
                sendStatusNotification(context.getString(R.string.voiceInputSpeakNow));
            }
        }
    }

}
