package com.example.kyle.patiencetraining.reward.unlocked;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.example.kyle.patiencetraining.reward.ClickedRewardDialog;
import com.example.kyle.patiencetraining.R;
import com.example.kyle.patiencetraining.reward.Reward;
import com.example.kyle.patiencetraining.util.TimeString;

import java.util.Date;

import androidx.annotation.NonNull;

class UnlockedClickedReward extends ClickedRewardDialog {

    UnlockedClickedReward(@NonNull final Context context, final Reward reward, final int position, final OnDeleteListener deleteListener){
        super(context, reward, position, deleteListener);
        TextView textView = findViewById(R.id.descriptionTextView);
        Button button = findViewById(R.id.collectButton);
        String time = TimeString.getTimeStringBetween(new Date(reward.getStart()), new Date(reward.getFinish()),context);
        String description = context.getString(R.string.waited_unlocked, time, reward.getName(), reward.getPrice());
        textView.setText(description);
        button.setText(R.string.collect_button);
        button.setOnClickListener(view -> {
            if(!reward.getLink().isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(reward.getLink()));
                context.startActivity(browserIntent);
            }
            dismiss();
            deleteListener.onDelete(position);
        });

    }
}
