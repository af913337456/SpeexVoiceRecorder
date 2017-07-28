
package com.mogujie.tt.audio;

import android.content.Context;
import android.media.AudioManager;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;


public class AudioPlayerHandler {
    private String currentPlayPath = null;
    private SpeexDecoder speexdec = null;
    private Thread th = null;

    private static AudioPlayerHandler instance = null;

    public static  AudioPlayerHandler getInstance() {
        if (null == instance) {
            synchronized(AudioPlayerHandler.class){
                instance = new AudioPlayerHandler();
                 EventBus.getDefault().register(instance);
            }
        }
        return instance;
    }


    //语音播放的模式
    public  void setAudioMode(int mode,Context ctx) {
        if (mode != AudioManager.MODE_NORMAL && mode != AudioManager.MODE_IN_CALL) {
            return;
        }
        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(mode);
    }

    /**messagePop调用*/
    public int getAudioMode(Context ctx) {
        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getMode();
    }

    public void clear(){
        if (isPlaying()){
            stopPlayer();
        }
        EventBus.getDefault().unregister(instance);
        instance = null;
    }


    private AudioPlayerHandler() {
    }

    /**
     * yingmu modify
     * speexdec 由于线程模型
     * */
    public interface AudioListener{
        public void onStop();
    }

    private AudioListener audioListener;

    public AudioPlayerHandler setAudioListener(AudioListener audioListener) {
        this.audioListener = audioListener;
        return this;
    }

    private void stopAnimation(){
        if(audioListener!=null){
            audioListener.onStop();
        }
    }

    @Subscribe
    public void onEventMainThread(AudioEvent audioEvent){
        switch (audioEvent){
            case AUDIO_STOP_PLAY:{
                currentPlayPath = null;
                stopPlayer();
            }break;
        }
    }

    public void stopPlayer() {
        try {
            if (null != th) {
                th.interrupt();
                th = null;
                Thread.currentThread().interrupt();
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            stopAnimation();
        }
    }

    public boolean isPlaying() {
        return null != th;
    }

    public void startPlay(String filePath) {
        this.currentPlayPath = filePath;
        try {
            speexdec = new SpeexDecoder(new File(this.currentPlayPath));
            RecordPlayThread rpt = new RecordPlayThread();
            if (null == th)
                th = new Thread(rpt);
            th.start();
        } catch (Exception e) {
            // 关闭动画很多地方需要写，是不是需要重新考虑一下
            e.printStackTrace();
            stopAnimation();
        }
    }



    private class RecordPlayThread extends Thread {
        public void run() {
            try {
                if (null != speexdec)
                    speexdec.decode();
            } catch (Exception e) {
                e.printStackTrace();
                stopAnimation();
            }
        }
    };

    public String getCurrentPlayPath() {
        return currentPlayPath;
    }
}
