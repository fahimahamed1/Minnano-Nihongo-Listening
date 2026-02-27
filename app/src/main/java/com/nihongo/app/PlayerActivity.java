package com.nihongo.app;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {

    private static final int MAX_LESSON = 50;
    
    private MediaPlayer mediaPlayer;
    private ArrayList<String> files;
    private int currentIndex;
    private int lessonNum;
    private boolean autoPlayEnabled = true;

    private SeekBar seekBar;
    private TextView titleView, timeTotal, timeCurrent, subtitleView;
    private Button btnPlay, btnPause;
    private TextView lessonIndicator;
    private Handler handler;
    private PowerManager.WakeLock wakeLock;
    private UpdateRunnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        files = getIntent().getStringArrayListExtra("files");
        
        // Safety check for files
        if (files == null || files.isEmpty()) {
            Toast.makeText(this, R.string.no_audio_files, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        currentIndex = getIntent().getIntExtra("index", 0);
        lessonNum = getIntent().getIntExtra("lesson", 1);
        
        // Validate index bounds
        if (currentIndex < 0 || currentIndex >= files.size()) {
            currentIndex = 0;
        }

        initViews();
        initMediaPlayer();
        initControls();
        
        // Start playing
        playCurrentTrack();
    }

    private void initViews() {
        titleView = findViewById(R.id.title);
        timeTotal = findViewById(R.id.time);
        timeCurrent = findViewById(R.id.timeCurrent);
        subtitleView = findViewById(R.id.subtitle);
        seekBar = findViewById(R.id.seekBar);
        btnPlay = findViewById(R.id.play);
        btnPause = findViewById(R.id.pause);
        lessonIndicator = findViewById(R.id.lessonIndicator);
        
        updateLessonInfo();
        
        handler = new Handler(Looper.getMainLooper());
        updateRunnable = new UpdateRunnable(this);
    }
    
    private void updateLessonInfo() {
        subtitleView.setText(getString(R.string.lesson_format, lessonNum) + " - Minna no Nihongo");
        if (lessonIndicator != null) {
            lessonIndicator.setText("Lesson " + lessonNum + " / " + MAX_LESSON);
        }
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        mediaPlayer.setOnCompletionListener(mp -> onTrackCompleted());
        
        // Acquire wake lock
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MinnanoNihongo:Audio");
    }
    
    private void onTrackCompleted() {
        // Check if there are more tracks in current lesson
        if (currentIndex < files.size() - 1) {
            // Play next track in current lesson
            playNext();
        } else if (autoPlayEnabled && lessonNum < MAX_LESSON) {
            // All tracks in current lesson completed, go to next lesson
            goToNextLesson();
        } else if (lessonNum >= MAX_LESSON) {
            // All lessons completed!
            Toast.makeText(this, "🎉 Congratulations! All lessons completed!", Toast.LENGTH_LONG).show();
            updatePlayPauseButtons(false);
            releaseWakeLock();
        }
    }
    
    private void goToNextLesson() {
        lessonNum++;
        currentIndex = 0;
        
        // Load audio files for new lesson
        loadLessonFiles();
        
        if (!files.isEmpty()) {
            updateLessonInfo();
            Toast.makeText(this, "▶ Starting Lesson " + lessonNum, Toast.LENGTH_SHORT).show();
            playCurrentTrack();
        } else {
            // No files in this lesson, try next
            if (lessonNum < MAX_LESSON) {
                goToNextLesson();
            } else {
                Toast.makeText(this, "🎉 All lessons completed!", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void goToPreviousLesson() {
        if (lessonNum > 1) {
            lessonNum--;
            loadLessonFiles();
            currentIndex = 0;
            
            if (!files.isEmpty()) {
                updateLessonInfo();
                Toast.makeText(this, "◀ Going to Lesson " + lessonNum, Toast.LENGTH_SHORT).show();
                playCurrentTrack();
            }
        }
    }
    
    private void loadLessonFiles() {
        files.clear();
        try {
            String folder = "audio/lesson_" + lessonNum;
            String[] assetFiles = getAssets().list(folder);
            
            if (assetFiles != null) {
                for (String f : assetFiles) {
                    if (f.endsWith(".mp3")) {
                        files.add("lesson_" + lessonNum + "/" + f);
                    }
                }
            }
            
            // Sort files: main first, then q1, q2, q3...
            Collections.sort(files, (f1, f2) -> {
                if (f1.contains("main")) return -1;
                if (f2.contains("main")) return 1;
                return f1.compareTo(f2);
            });
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initControls() {
        btnPlay.setOnClickListener(v -> resumePlayback());
        btnPause.setOnClickListener(v -> pausePlayback());
        findViewById(R.id.next).setOnClickListener(v -> playNext());
        findViewById(R.id.prev).setOnClickListener(v -> playPrevious());
        
        // Lesson navigation buttons
        findViewById(R.id.nextLesson).setOnClickListener(v -> {
            if (lessonNum < MAX_LESSON) {
                goToNextLesson();
            } else {
                Toast.makeText(this, "This is the last lesson", Toast.LENGTH_SHORT).show();
            }
        });
        
        findViewById(R.id.prevLesson).setOnClickListener(v -> {
            if (lessonNum > 1) {
                goToPreviousLesson();
            } else {
                Toast.makeText(this, "This is the first lesson", Toast.LENGTH_SHORT).show();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void playCurrentTrack() {
        if (files == null || files.isEmpty() || currentIndex < 0 || currentIndex >= files.size()) {
            Toast.makeText(this, R.string.no_audio_files, Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            mediaPlayer.reset();
            String file = files.get(currentIndex);
            
            // Update title display
            updateTrackTitle(file);
            
            mediaPlayer.setDataSource(getAssets().openFd("audio/" + file));
            mediaPlayer.prepare();
            mediaPlayer.start();

            seekBar.setMax(mediaPlayer.getDuration());
            timeTotal.setText(formatTime(mediaPlayer.getDuration()));
            
            acquireWakeLock();
            startProgressUpdates();
            updatePlayPauseButtons(true);

            // Save last played index
            getSharedPreferences("player", MODE_PRIVATE)
                    .edit()
                    .putInt("last_index", currentIndex)
                    .putInt("last_lesson", lessonNum)
                    .apply();
                    
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error_loading) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateTrackTitle(String file) {
        String displayName = file.substring(file.lastIndexOf("/") + 1).replace(".mp3", "");
        
        // Format display name in Japanese style (Minna no Nihongo)
        if (displayName.contains("main")) {
            titleView.setText("会話 (Kaiwa)");  // Main Dialogue
        } else if (displayName.contains("_q")) {
            String qNum = displayName.replaceAll(".*_q", "");
            titleView.setText("問題" + qNum + " (Mondai " + qNum + ")");  // Question
        } else if (displayName.contains("renshu") || displayName.contains("practice")) {
            titleView.setText("練習 (Renshuu)");  // Practice
        } else if (displayName.contains("vocab") || displayName.contains("tango")) {
            titleView.setText("単語 (Tango)");  // Vocabulary  
        } else if (displayName.contains("bunpou") || displayName.contains("grammar")) {
            titleView.setText("文法 (Bunpou)");  // Grammar
        } else {
            titleView.setText(displayName.toUpperCase(Locale.ROOT));
        }
    }

    private void resumePlayback() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            acquireWakeLock();
            startProgressUpdates();
            updatePlayPauseButtons(true);
        }
    }

    private void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            releaseWakeLock();
            stopProgressUpdates();
            updatePlayPauseButtons(false);
        }
    }

    private void playNext() {
        if (currentIndex < files.size() - 1) {
            currentIndex++;
            playCurrentTrack();
        } else if (autoPlayEnabled && lessonNum < MAX_LESSON) {
            // Go to next lesson
            goToNextLesson();
        }
    }

    private void playPrevious() {
        if (currentIndex > 0) {
            currentIndex--;
            playCurrentTrack();
        } else if (lessonNum > 1) {
            // Go to previous lesson (start from last track)
            lessonNum--;
            loadLessonFiles();
            if (!files.isEmpty()) {
                currentIndex = files.size() - 1;
                updateLessonInfo();
                playCurrentTrack();
            }
        }
    }

    private void startProgressUpdates() {
        handler.removeCallbacks(updateRunnable);
        handler.post(updateRunnable);
    }

    private void stopProgressUpdates() {
        handler.removeCallbacks(updateRunnable);
    }

    private void updatePlayPauseButtons(boolean isPlaying) {
        btnPlay.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
        btnPause.setVisibility(isPlaying ? View.VISIBLE : View.GONE);
    }

    private void acquireWakeLock() {
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire(60 * 60 * 1000L); // 1 hour max
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        return String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60);
    }

    private void updateProgress() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            timeCurrent.setText(formatTime(mediaPlayer.getCurrentPosition()));
        }
    }

    // Static inner class to avoid memory leaks
    private static class UpdateRunnable implements Runnable {
        private final WeakReference<PlayerActivity> activityRef;

        UpdateRunnable(PlayerActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            PlayerActivity activity = activityRef.get();
            if (activity != null && activity.mediaPlayer != null) {
                activity.updateProgress();
                activity.handler.postDelayed(this, 500);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopProgressUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            startProgressUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopProgressUpdates();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        releaseWakeLock();
    }
}
