package com.example.who.speexdemo;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.mogujie.tt.VoiceRecorderHelper;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private VoiceRecorderHelper voiceRecorderHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                },
                1
        );

        findViewById(R.id.record).setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent motionEvent) {
                        switch (motionEvent.getAction()){
                            case MotionEvent.ACTION_DOWN:
                                voiceRecorderHelper.Action_Down(v,motionEvent);
                                return true;
                            case MotionEvent.ACTION_MOVE:
                                voiceRecorderHelper.Action_Move(v,motionEvent);
                                return true;
                            case MotionEvent.ACTION_UP:
                                voiceRecorderHelper.Action_Up(v,motionEvent);
                                return true;
                        }
                        return false;
                    }
                }
        );

        voiceRecorderHelper =
                new VoiceRecorderHelper(
                        this,
                        new VoiceRecorderHelper.CallBack()
                {
                        @Override
                        public String setOutPutPath() {
                            /** 设置录音结果路径，你的格式也在这里设置 */
                            return getAudioSavePath("LinGuanHong");
                        }

                        @Override
                        public void onDown(View v) {
                            /** 纯粹的 down 事件回调 */
                        }

                        @Override
                        public void onMove_in_limit(View v) {
                            /** 手指移动的范围在限制内 */
                        }

                        @Override
                        public void onMove_out_limit(View v) {
                            /** 手指移动超过范围，内部做了显示取消的提示 */
                        }

                        @Override
                        public void onUp_start(View v) {
                            /** 纯粹的 Up 事件回调 */
                        }

                        @Override
                        public void onUp_cancel(View v) {
                            /** 这个时候已经因为手指移动超过范围取消了录音 */
                        }

                        @Override
                        public void onFinishRecord() {
                            /** 录音结束 */
                        }

                        @Override
                        public void onRecordSuccess(float len, String savePath) {
                            /** 录音解码并且保存成功 */
                            Log.e("aaaaa", "录音的路径 " + savePath + " 长度 " + len);
                        }

                        @Override
                        public void onRecordVolumeChange(int voiceValue) {
                            /** 录音声音强度的变化，单位分贝 */
                        }
                    }
                );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        voiceRecorderHelper.onDestroy();
        voiceRecorderHelper = null;
    }

    private static String getAudioSavePath(String userId) {
        String path = getAudioPathWithoutFile(userId);
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        return path;
    }

    private static String getAudioPathWithoutFile(String userId){
        return getSavePath() + userId
                + "_" + String.valueOf(System.currentTimeMillis())
                + ".mp3";
    }

    private static String getSavePath() {
        String path;
        String floder = "audio";
        if (checkSDCard()) {
            path = Environment.getExternalStorageDirectory().toString()
                    + File.separator + "MGJ-IM" + File.separator + floder
                    + File.separator;

        } else {
            path = Environment.getDataDirectory().toString() + File.separator
                    + "MGJ-IM" + File.separator + floder + File.separator;
        }
        return path;
    }

    private static boolean checkSDCard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }


}





















