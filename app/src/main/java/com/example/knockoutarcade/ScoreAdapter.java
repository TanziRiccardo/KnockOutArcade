package com.example.knockoutarcade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.BaseAdapter;

import java.util.List;

public class ScoreAdapter extends BaseAdapter {
    private final Context context;
    private final List<Integer> scores;

    public ScoreAdapter(Context context, List<Integer> scores) {
        this.context = context;
        this.scores = scores;
    }

    @Override
    public int getCount() {
        return scores.size();
    }

    @Override
    public Object getItem(int position) {
        return scores.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.score_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.scoreTextView);
        int score = scores.get(position);
        textView.setText((position + 1) + ". " + score);
        return convertView;
    }
}
