package com.example.android27;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    SeekBar sbPosition;
    TextView tvElapsed, tvRemain;
    Button btPlay;

    private MainActivity3 myBinder;
    private Handler mHandler = new Handler();
    Intent MediaServiceIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**開始使用播放音樂的Service*/
        MediaServiceIntent = new Intent(this, MainActivity2.class);
        startService(MediaServiceIntent);
        /**將Service的播放狀態進行監聽，並綁定給介面*/
        bindService(MediaServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        /**取得介面*/
        btPlay = findViewById(R.id.button_Play);
        sbPosition = findViewById(R.id.seekBar_Position);
        tvElapsed = findViewById(R.id.textview_ElapsedTime);
        tvRemain = findViewById(R.id.textview_RemainingTime);

    }
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /**如果介面有和MediaService成功綁定時，便會跳至onServiceConnected，反之onServiceDisconnected*/
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /**取用Binder中的各種音樂操作的方法*/
            myBinder = (MainActivity3) service;
            if (myBinder.isPlaying()) {
                btPlay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            } else {
                btPlay.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
            }
            sbPosition.setMax(myBinder.getProgress());
            sbPosition.setOnSeekBarChangeListener(position);

            btPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setIsPlayButton(btPlay);
                }
            });
            /**開始使用執行緒，使之每秒更新一次進度條*/
            mHandler.post(runnable);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    /**使用Handler配合runnable，始進度條每秒進行更新*/
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                /**取得現在音樂播放的進度，並顯示在SeekBar上*/
                sbPosition.setProgress(myBinder.getPlayPosition());
                /**設置現在播放的的時間*/
                String elapsedTime = createTimeLabel(myBinder.getPlayPosition());
                tvElapsed.setText(elapsedTime);
                String remainingTime = createTimeLabel(myBinder.getProgress() - myBinder.getPlayPosition());
                tvRemain.setText("- " + remainingTime);
                /**使這個執行緒每秒跑一次*/
                mHandler.postDelayed(runnable, 1000);
            }catch (Exception e){

            }
        }
    };
    /**設置播放音樂與暫停音樂*/
    private void setIsPlayButton(Button bt) {
        if (myBinder.isPlaying()) {
            myBinder.pauseMusic();
            bt.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        } else {
            myBinder.playMusic();
            bt.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        }
    }
    /**畫面消失時，則不監聽目前播放狀態*/
    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(runnable);
        myBinder.closeMedia();
        stopService(MediaServiceIntent);
        finish();
    }

    /**設置SeekBar拉動事件，使之能調整音樂播放的進度*/
    private SeekBar.OnSeekBarChangeListener position = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean isSeekbarOnTouch) {
            /**isSeekbarOnTouch是我自己取的，本判斷是為偵測Seekbar是否有被使用所碰觸而改變進度條*/
            if (isSeekbarOnTouch) {
                myBinder.seekToPosition(i);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    /**將MediaPlayer傳回的進度轉換為時間*/
    private String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }
}