package com.nihongo.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private static final String FAHIM_FB = "https://www.facebook.com/fahimahamed4";
    private static final String FAHAD_FB = "https://www.facebook.com/fahadahamed4";

    // Japanese lesson titles for Minna no Nihongo (みんなの日本語)
    // Official lesson titles from the textbook - All 50 lessons
    private static final String[] LESSON_TITLES_JP = {
        "はじめまして",                    // Lesson 1: Nice to meet you
        "これは何ですか",                  // Lesson 2: What is this?
        "ここはデパートです",              // Lesson 3: This is a department store
        "今何時ですか",                    // Lesson 4: What time is it?
        "甲子園へ行きますか",              // Lesson 5: Do you go to Koshien?
        "いっしょに行きませんか",          // Lesson 6: Won't you go together?
        "いらっしゃいませ",                // Lesson 7: Welcome
        "そろそろ失礼します",              // Lesson 8: I should be leaving
        "残念ですが",                      // Lesson 9: Unfortunately...
        "あります",                        // Lesson 10: There is/are
        "いくつありますか",                // Lesson 11: How many are there?
        "お祭りはどうでしたか",            // Lesson 12: How was the festival?
        "別々にお願いします",              // Lesson 13: Separately please
        "みどり町までお願いします",        // Lesson 14: To Midori town please
        "ご家族は",                        // Lesson 15: Your family?
        "使い方を教えてください",          // Lesson 16: Please teach me how to use
        "どうしましたか",                  // Lesson 17: What happened?
        "趣味は何ですか",                  // Lesson 18: What are your hobbies?
        "ダイエットは明日から",            // Lesson 19: Diet starts tomorrow
        "夏休みはどうでしたか",            // Lesson 20: How was summer vacation?
        "わたしもそう思います",            // Lesson 21: I think so too
        "どんなアパートがいいですか",      // Lesson 22: What kind of apartment?
        "どうやって行きますか",            // Lesson 23: How do you get there?
        "手伝ってくれませんか",            // Lesson 24: Would you help me?
        "いろいろお世話になりました",      // Lesson 25: Thank you for everything
        "どこかで会ったことが",            // Lesson 26: Have we met somewhere?
        "何でも相談してください",          // Lesson 27: Please consult me
        "最近どうですか",                  // Lesson 28: How are things lately?
        "夢がかなう",                      // Lesson 29: Dreams come true
        "せっかくですから",                // Lesson 30: Since we've come this far
        "このごろすごく元気ですね",        // Lesson 31: You're very energetic lately
        "味はどうですか",                  // Lesson 32: How's the taste?
        "何をしているんですか",            // Lesson 33: What are you doing?
        "旅行はいかがでしたか",            // Lesson 34: How was your trip?
        "とにかく急いで",                  // Lesson 35: Anyway, hurry!
        "地震です",                        // Lesson 36: It's an earthquake!
        "いつできますか",                  // Lesson 37: When will it be ready?
        "直しておいてください",            // Lesson 38: Please fix it
        "残業で遅くなりました",            // Lesson 39: Late due to overtime
        "サービスはいかがですか",          // Lesson 40: How about the service?
        "とてもきれいですね",              // Lesson 41: It's very beautiful
        "いただいた荷物",                  // Lesson 42: The package I received
        "お元気で",                        // Lesson 43: Take care
        "ニュースを見ましたか",            // Lesson 44: Did you watch the news?
        "ぶつかったらどうしますか",        // Lesson 45: What if we collide?
        "来てください",                    // Lesson 46: Please come
        "女の人はどなたですか",            // Lesson 47: Who is the woman?
        "やりがいがあります",              // Lesson 48: It's rewarding
        "帰りたいですね",                  // Lesson 49: I want to go home
        "心から感謝します"                 // Lesson 50: Grateful from the heart
    };

    private static final int LESSON_COUNT = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new LessonAdapter());
        
        // Developer click listeners
        findViewById(R.id.devFahim).setOnClickListener(v -> openUrl(FAHIM_FB));
        findViewById(R.id.devFahad).setOnClickListener(v -> openUrl(FAHAD_FB));
    }
    
    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

        @NonNull
        @Override
        public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_lesson, parent, false);
            return new LessonViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
            int lessonNum = position + 1;
            holder.numView.setText(String.valueOf(lessonNum));
            holder.titleView.setText(getString(R.string.lesson_format, lessonNum));
            
            if (position < LESSON_TITLES_JP.length) {
                holder.subtitleView.setText(LESSON_TITLES_JP[position]);
            } else {
                holder.subtitleView.setText("Lesson " + lessonNum);
            }
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AudioListActivity.class);
                intent.putExtra("lesson", lessonNum);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return LESSON_COUNT;
        }

        class LessonViewHolder extends RecyclerView.ViewHolder {
            final TextView numView;
            final TextView titleView;
            final TextView subtitleView;

            LessonViewHolder(@NonNull View itemView) {
                super(itemView);
                numView = itemView.findViewById(R.id.lessonNumber);
                titleView = itemView.findViewById(R.id.lessonTitle);
                subtitleView = itemView.findViewById(R.id.lessonSubtitle);
            }
        }
    }
}
