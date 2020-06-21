package club.zhuol.examnationtest;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.VideoView;

import java.util.Random;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

public class MainActivity extends AppCompatActivity {

    boolean showDanmaku;
    DanmakuContext danmakuContext;
    EditText edt_text;
    Button btn_send;
    VideoView videoView;
    DanmakuView danmakuView;
    LinearLayout sendLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        //初始化控件
        initView();
        //播放视频
        playVideo();
        //初始化danmaku
        initDanmaku();
    }

    /**
     * 初始化弹幕
     */
    private void initDanmaku() {
        //设置回调函数
        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                showDanmaku = true;
                danmakuView.start();//开始弹幕
                generateDanmaku();//调用随机生成弹幕方法
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });
        danmakuContext = DanmakuContext.create();
        //提升屏幕绘制效率
        danmakuView.enableDanmakuDrawingCache(true);
        //进行弹幕准备
        danmakuView.prepare(parser, danmakuContext);
        //为danmakuView设置点击事件
        danmakuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendLayout.getVisibility() == View.GONE) {
                    //显示布局
                    sendLayout.setVisibility(View.VISIBLE);
                } else {
                    //隐藏布局
                    sendLayout.setVisibility(View.GONE);
                }
            }
        });
        //为发送按钮设置监听
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String context = edt_text.getText().toString();
                if (!TextUtils.isEmpty(context)) {
                    //添加新增弹幕 并为之添加一个边框方便识别
                    addDanmaku(context, true);
                    edt_text.setText("");
                }
            }
        });
    }


    /**
     * 随机生成一些弹幕内容
     */
    private void generateDanmaku() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (showDanmaku) {
                    int num = new Random().nextInt(300);
                    String context = "" + num;
                    addDanmaku(context, false);
                    try {
                        Thread.sleep(num);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 添加一条弹幕
     */
    private void addDanmaku(String content, boolean border) {
        //创建弹幕对象,TYPE_SCROLL_RL 表示从右向左滚动的弹幕
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = content;
        danmaku.padding = 6;
        danmaku.textSize = 50;
        //弹幕颜色 白色
        danmaku.textColor = Color.WHITE;
        danmaku.setTime(danmakuView.getCurrentTime());
        if (border){
            //弹幕文字边框的颜色
            danmaku.borderColor = Color.BLUE;
        }
        //添加一条弹幕
        danmakuView.addDanmaku(danmaku);
    }

    private void playVideo() {
        String uri = "android.resource://" + getPackageName() + "/" + R.raw.autumn_longingi;
        if (uri != null) {
            videoView.setVideoURI(Uri.parse(uri));
            videoView.start();
        } else {
            //设置背景为透明
            videoView.getBackground().setAlpha(0);
        }
    }

    /**
     * 创建弹幕解析器
     */
    private BaseDanmakuParser parser = new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };


    private void initView() {
        btn_send = findViewById(R.id.btn_send);
        edt_text = findViewById(R.id.edt_text);
        danmakuView = findViewById(R.id.danmaku);
        videoView = findViewById(R.id.videoView);
        sendLayout = findViewById(R.id.ly_send);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showDanmaku = false;
        if (danmakuView != null) {
            danmakuView.release();
            danmakuView = null;
        }
    }
}
