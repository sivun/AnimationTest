package com.demo.animationtest.activity;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.animationtest.R;
import com.demo.animationtest.anima.FramesSequenceAnimation;


/**
 * TITLE
 * Created by shixiaoming on 16/12/27.
 */

public class TestActivity extends Activity {
    private ImageView imageView;
    private TextView textView;
    private Button playBtn;
    private AnimationDrawable animationDrawable;
    private int mode;
    private boolean start = false;
    FramesSequenceAnimation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = getIntent().getIntExtra("mode", 1);
        setContentView(R.layout.test_activity);

        imageView = (ImageView) findViewById(R.id.imgview);
        textView = (TextView) findViewById(R.id.title);
        playBtn = (Button) findViewById(R.id.play_btn);

        if (mode == 1) {
            textView.setText("普通帧动画");
            playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (imageView.getDrawable() == null) {
                        imageView.setImageResource(R.drawable.loading_anim);
                        animationDrawable = (AnimationDrawable) imageView.getDrawable();
                    }
                    if (!switchBtn()) {
                        animationDrawable.start();
                    } else {
                        animationDrawable.stop();
                    }

                }
            });

        } else {
            textView.setText("优化帧动画");
            playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (animation == null)
                        animation = FramesSequenceAnimation.createAnima(imageView,
                                R.array.loading_anim_res, R.array.loading_anim_duration, true);
                    if (!switchBtn()) {
                        animation.start();
                    } else {
                        animation.stop();
                    }
                }
            });
        }

    }

    //控制开关
    private boolean switchBtn() {
        boolean returnV = start;
        start = !start;

        playBtn.setText(start == false ? "START" : "STOP");
        return returnV;
    }
}
