package com.nihongo.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;

public class AudioListActivity extends AppCompatActivity {

    private int lessonNum;
    private ArrayList<String> files = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_list);

        lessonNum = getIntent().getIntExtra("lesson", 1);
        
        // Set header title
        TextView header = findViewById(R.id.header);
        header.setText(getString(R.string.lesson_format, lessonNum));
        
        TextView headerSub = findViewById(R.id.headerSub);
        headerSub.setText("Lesson " + lessonNum);
        
        TextView audioCount = findViewById(R.id.audioCount);

        loadAudioFiles();

        if (files.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_audio_files) + " - Lesson " + lessonNum, Toast.LENGTH_LONG).show();
            audioCount.setText(getString(R.string.no_audio_files));
        } else {
            audioCount.setText(files.size() + " audio files");
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new AudioAdapter());
    }

    private void loadAudioFiles() {
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
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error_loading) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {

        @NonNull
        @Override
        public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_audio, parent, false);
            return new AudioViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
            String item = files.get(position);
            String displayName = item.substring(item.lastIndexOf("/") + 1).replace(".mp3", "");
            
            // Format display name in Japanese style (Minna no Nihongo)
            if (displayName.contains("main")) {
                holder.titleView.setText("🎵 会話 (Kaiwa)");  // Main Dialogue
            } else if (displayName.contains("_q")) {
                String qNum = displayName.replaceAll(".*_q", "");
                holder.titleView.setText("❓ 問題" + qNum + " (Mondai " + qNum + ")");  // Question
            } else if (displayName.contains("renshu") || displayName.contains("practice")) {
                holder.titleView.setText("📝 練習 (Renshuu)");  // Practice
            } else if (displayName.contains("vocab") || displayName.contains("tango")) {
                holder.titleView.setText("📖 単語 (Tango)");  // Vocabulary
            } else if (displayName.contains("bunpou") || displayName.contains("grammar")) {
                holder.titleView.setText("📚 文法 (Bunpou)");  // Grammar
            } else {
                holder.titleView.setText("🎶 " + displayName.toUpperCase());
            }
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(AudioListActivity.this, PlayerActivity.class);
                intent.putStringArrayListExtra("files", files);
                intent.putExtra("index", position);
                intent.putExtra("lesson", lessonNum);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return files.size();
        }

        class AudioViewHolder extends RecyclerView.ViewHolder {
            final TextView titleView;

            AudioViewHolder(@NonNull View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.audioTitle);
            }
        }
    }
}
