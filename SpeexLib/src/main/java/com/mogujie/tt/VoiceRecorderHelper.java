package com.mogujie.tt;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.speexlib.R;
import com.mogujie.tt.audio.AudioPlayerHandler;
import com.mogujie.tt.audio.AudioRecordHandler;
import com.mogujie.tt.audio.HandlerConstant;

import java.util.Timer;
import java.util.TimerTask;

import static com.mogujie.tt.audio.AudioRecordHandler.MAX_SOUND_RECORD_TIME;

/**
 * 作者：林冠宏
 *
 * author: LinGuanHong,lzq is my dear wife.
 *
 * My GitHub : https://github.com/af913337456/
 *
 * My Blog   : http://www.cnblogs.com/linguanh/
 *
 * on 2017/7/26.
 *
 * function：高度封装的 语音 录制工具库
 *
 */

@SuppressWarnings("dialog 已经在里面被封装好了")
public class VoiceRecorderHelper {

    private final static int RECORD_MOVE_DIS_LIMIT = 180;

    private float y1 = 0,y2 = 0;
    private String audioSavePath;
    private CallBack callBack;
    private AudioRecordHandler audioRecorderInstance = null;
    private Thread audioRecorderThread = null;
    private Handler uiHandler;
    private Activity activity;

    private ImageView soundVolumeImg = null;
    private Dialog soundVolumeDialog = null;
    private LinearLayout soundVolumeLayout = null;

    @SuppressWarnings("捞底回收")
    public void onDestroy(){
        audioRecorderInstance=null;
        audioSavePath=null;
        activity = null;
        if(uiHandler!=null) {
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler = null;
        }
    }

    public void onPause(){

    }

    public VoiceRecorderHelper(Activity activity, CallBack callBack){
        this.callBack = callBack;
        this.activity = activity;
        initSoundVolumeDialog();
        uiHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case HandlerConstant.HANDLER_RECORD_FINISHED:
                        onRecordVoiceEnd((Float) msg.obj);
                        break;
                    // 录音结束
                    case HandlerConstant.HANDLER_STOP_PLAY:

                        break;
                    case HandlerConstant.RECEIVE_MAX_VOLUME:
                        onReceiveMaxVolume((Integer) msg.obj);
                        break;

                    case HandlerConstant.RECORD_AUDIO_TOO_LONG:
                        doFinishRecordAudio();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void initSoundVolumeDialog(){
        try {
            soundVolumeDialog = new Dialog(activity, R.style.SoundVolumeStyle);
            soundVolumeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            soundVolumeDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            soundVolumeDialog.setContentView(R.layout.sound_volume_dialog);
            soundVolumeDialog.setCanceledOnTouchOutside(true);
            soundVolumeImg = (ImageView) soundVolumeDialog.findViewById(R.id.sound_volume_img);
            soundVolumeLayout = (LinearLayout) soundVolumeDialog.findViewById(R.id.sound_volume_bg);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public interface CallBack {

        String setOutPutPath();/** 设置输出路径 */

        void onDown(View v);
        void onMove_in_limit(View v);
        void onMove_out_limit(View v);
        void onUp_start(View v);
        void onUp_cancel(View v);

        void onFinishRecord();
        void onRecordSuccess(float len, String savePath);
        void onRecordVolumeChange(int voiceValue);
    }

    private void doFinishRecordAudio() {
        try {
            if (audioRecorderInstance.isRecording()) {
                audioRecorderInstance.setRecording(false);
            }
            callBack.onFinishRecord();

            if (soundVolumeDialog.isShowing()) {
                soundVolumeDialog.dismiss();
            }
            audioRecorderInstance.setRecordTime(MAX_SOUND_RECORD_TIME);
            onRecordVoiceEnd(MAX_SOUND_RECORD_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void onRecordVoiceEnd(float audioLen) {
        callBack.onRecordSuccess(audioLen,audioSavePath);
    }

    /**
     *  根据分贝值设置录音时的音量动画
     */
    private void onReceiveMaxVolume(int voiceValue) {
        callBack.onRecordVolumeChange(voiceValue);
        if (voiceValue < 200.0) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_01);
        } else if (voiceValue > 200.0 && voiceValue < 600) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_02);
        } else if (voiceValue > 600.0 && voiceValue < 1200) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_03);
        } else if (voiceValue > 1200.0 && voiceValue < 2400) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_04);
        } else if (voiceValue > 2400.0 && voiceValue < 10000) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_05);
        } else if (voiceValue > 10000.0 && voiceValue < 28000.0) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_06);
        } else if (voiceValue > 28000.0) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_07);
        }
    }

    public void Action_Down(View v, MotionEvent event){
        if (AudioPlayerHandler.getInstance().isPlaying())
            AudioPlayerHandler.getInstance().stopPlayer();
        y1 = event.getY();

        callBack.onDown(v);
        soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_01);
        soundVolumeImg.setVisibility(View.VISIBLE);
        soundVolumeLayout.setBackgroundResource(R.drawable.tt_sound_volume_default_bk);
        soundVolumeDialog.show();

        audioSavePath = callBack.setOutPutPath();

        audioRecorderInstance = new AudioRecordHandler(audioSavePath);
        audioRecorderInstance.setHandleCallback(new AudioRecordHandler.UiHandleCallback() {
            @Override
            public Handler getUiHandle() {
                return uiHandler;
            }
        });
        audioRecorderThread = new Thread(audioRecorderInstance);
        audioRecorderInstance.setRecording(true);
        audioRecorderThread.start();
    }

    public void Action_Move(View v, MotionEvent event){
        y2 = event.getY();
        if (y1 - y2 > RECORD_MOVE_DIS_LIMIT) {
            callBack.onMove_in_limit(v);
            soundVolumeImg.setVisibility(View.GONE);
            soundVolumeLayout.setBackgroundResource(R.drawable.tt_sound_volume_cancel_bk);
        } else {
            callBack.onMove_out_limit(v);
            soundVolumeImg.setVisibility(View.VISIBLE);
            soundVolumeLayout.setBackgroundResource(R.drawable.tt_sound_volume_default_bk);
        }
    }

    public void Action_Up(View v, MotionEvent event){
        y2 = event.getY();
        if(audioRecorderInstance == null){
            return;
        }
        if (audioRecorderInstance.isRecording()) {
            audioRecorderInstance.setRecording(false);
        }
        if (soundVolumeDialog.isShowing()) {
            soundVolumeDialog.dismiss();
        }
        callBack.onUp_start(v);
        if (y1 - y2 <= RECORD_MOVE_DIS_LIMIT) {
            if (audioRecorderInstance.getRecordTime() >= 0.5) {
                if (audioRecorderInstance.getRecordTime() < MAX_SOUND_RECORD_TIME) {
                    Message msg = uiHandler.obtainMessage();
                    msg.what = HandlerConstant.HANDLER_RECORD_FINISHED;
                    msg.obj = audioRecorderInstance.getRecordTime();
                    uiHandler.sendMessage(msg);
                }
            } else {
                /** 在取消的范围 */
                callBack.onUp_cancel(v);
                soundVolumeImg.setVisibility(View.GONE);
                soundVolumeLayout
                        .setBackgroundResource(R.drawable.tt_sound_volume_short_tip_bk);
                soundVolumeDialog.show();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    public void run() {
                        if (soundVolumeDialog.isShowing())
                            soundVolumeDialog.dismiss();
                        this.cancel();
                    }
                }, 700);
            }
        }
    }

}
