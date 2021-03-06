package me.relex.recorder.media;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.io.File;
import java.io.IOException;
import me.relex.recorder.BaseRecordActivity;
import me.relex.recorder.R;
import me.relex.recorder.tools.FileUtil;
import me.relex.recorder.tools.RecordListener;

public class MediaRecordActivity extends BaseRecordActivity
        implements RecordListener, MediaPlayListener {

    private MediaRecordHelper mMediaRecordHelper;
    private MediaPlayHelper mMediaPlayHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_record);
        configToolbar();

        mMediaRecordHelper = new MediaRecordHelper();
        mMediaRecordHelper.setMediaRecordListener(this);

        mMediaPlayHelper = new MediaPlayHelper();
        mMediaPlayHelper.setMediaPlayListener(this);



        mStartButton = (Button) findViewById(R.id.start_record);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {

                File tempFile = createTempAudioFile();
                if (tempFile == null) {
                    return;
                }

                mTempRecordFile = tempFile;
                mMediaRecordHelper.startRecord(tempFile);
            }
        });

        mStopButton = (Button) findViewById(R.id.stop_record);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                mMediaRecordHelper.stopRecord();
            }
        });

        mPlayButton = (Button) findViewById(R.id.play_record);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (mTempRecordFile == null || !mTempRecordFile.exists()) {
                    return;
                }
                mMediaPlayHelper.play(mTempRecordFile);
            }
        });

        switchButton(false, true);
    }

    // Record
    @Override public void onRecordStart() {
        switchButton(false, false);
        mStartTime = 0L;
        mHandler.post(mRecordAutoCountRunnable);
    }

    @Override public void onRecordStop() {
        mHandler.removeCallbacks(mRecordAutoCountRunnable);
        switchButton(true, true);
    }

    // Play
    @Override public void onPlayPrepare() {
        switchButton(false, false);
        mStopButton.setEnabled(false);
        mPlayButton.setText(R.string.play_preparing);
    }

    @Override public void onPlayStart() {
        mStartTime = 0L;
        mHandler.post(mPlayAutoCountRunnable);
    }

    @Override public void onPlayStop() {
        mHandler.removeCallbacks(mPlayAutoCountRunnable);
        switchButton(false, true);
    }

    private File createTempAudioFile() {
        File tempFile = null;
        try {
            File fileDir = FileUtil.getAudioCacheDirectory(MediaRecordActivity.this);
            tempFile = new File(fileDir, System.currentTimeMillis() + ".amr");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    @Override protected void onDestroy() {
        mHandler.removeCallbacks(mRecordAutoCountRunnable);
        mHandler.removeCallbacks(mPlayAutoCountRunnable);

        mMediaRecordHelper.stopRecord();
        mMediaPlayHelper.release();
        super.onDestroy();
    }
}
