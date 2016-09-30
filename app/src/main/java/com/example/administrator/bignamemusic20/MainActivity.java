package com.example.administrator.bignamemusic20;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //扫描音乐的路径
    String musicPath1 = "/storage/sdcard1/netease/cloudmusic/Music";
    String musicPath2 = Environment.getExternalStorageDirectory().getAbsolutePath();
    //控件
    private ImageView imageview;
    private ListView listview;
    private ProgressBar pb;
    private TextView musicname;
    private SeekBar sb;
    //动画
    private Animation animation;
    //集合
    private List<File> musicFilesList;
    //当前播放的歌曲索引
    private int currentPosition = 0;
    //记录是否第一次进来
    private boolean isFirstCome = true;
    //两种播放模式，顺序或者随机
    private static final int ORDER_PLAY = 0;
    private static final int RAMDOM_PLAY = 1;
    //记录播放模式默认为顺序播放
    private int play_mode = ORDER_PLAY;
    //颜色主题，黄色，蓝色，红色，灰色
    private static final int THEME_YELLOW = 0;
    private static final int THEME_BLUE = 1;
    private static final int THEME_RED = 2;
    private static final int THEME_HUI = 3;
    //当前的主题颜色
    private int themecolor = THEME_YELLOW; //默认为黄色
    //记录当前的进度
    private int currentProgress = 0;
    //记录总进度
    private int totalProgress = 0;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    pb.setVisibility(View.GONE);
                    listview.setAdapter(new ListViewAdapter(MainActivity.this, musicFilesList));
                    break;
                case 1:
                    if (mediaPlay.isPlaying()) {
                        currentProgress = mediaPlay.getCurrentPosition();
                        sb.setProgress(currentProgress);
                        currentTv.setText(new SimpleDateFormat("mm:ss").format(new Date(currentProgress)));
                        handler.sendEmptyMessageDelayed(1, 1000);
                    }
                    break;
            }
        }
    };
    private ServiceConnection conn;
    private MediaPlayer mediaPlay;
    private TextView currentTv;
    private TextView totalTv;
    private ImageButton btn_playOrPause;
    private TextView author;
    private Toolbar toolbar;
    private RelativeLayout layout;
    private Button btnY,btnB,btnR,btnH;
    private DrawerLayout drawer;

    /*注意，切换歌曲一点要改变了currentPosition ，再去调用resetMediaResoure重新设置资源，在去调用play 播放*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViewAndData(); //初始化控件，设置actionbar，设置动画,初始化集合
        startThread();   //开启线程，将MP3文件保存到musicFilesList
        initbindService(); //得到mediaplayer对象
        initListVIewListener(); //设置listview点击事件
        initSeekBarListener(); //设置seekbar的拖动时间

    }

    private void initSeekBarListener() {
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //滑动时候调用，
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !isFirstCome) {
                    mediaPlay.seekTo(progress);
                }
            }

            //刚开始滑动时候调用
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            //刚结束滑动时侯调用
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initMediaComleteListenr() {
        mediaPlay.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                switch (play_mode) {
                    case ORDER_PLAY:
                        currentPosition++;
                        currentPosition = currentPosition % musicFilesList.size();
                        break;
                    case RAMDOM_PLAY:
                        //随机播放模式
                        //随机生成一个0-size（）-1范围的数字
                        createNumber(currentPosition);
                        break;
                }
                resetMediaResoure();
                play();
            }
        });
    }

    /*
    * 参数：当前的播放的索引
    * 产生一个0-（size-1）的的数，如果跟原本的不一样则将随机数赋值给播放索引
    * 否则递归再产生一个随机数，直至不跟原本的索引相等
    * */
    public void createNumber(int currentPosition) {
        int ramdonnumber = (int) (Math.random() * musicFilesList.size());
        if (ramdonnumber != currentPosition) {
            this.currentPosition = ramdonnumber;//注意+this，不然改变的参数
        } else {
            createNumber(currentPosition);
        }
    }

    private void initListVIewListener() {
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentPosition = position;
                isFirstCome = false;
                resetMediaResoure();
                play();
            }
        });
    }


    public void btnPauseOrStart(View view) {
        if (isFirstCome) {
            resetMediaResoure();
        }
        if (mediaPlay != null) {
            if (mediaPlay.isPlaying()) {
                pause();
                btn_playOrPause.setImageResource(R.drawable.yq);
            } else {
                play();
                btn_playOrPause.setImageResource(R.drawable.a2p);
            }
        }
        isFirstCome = false;
    }

    public void btnNext(View view) {
        backOrNext(1);
    }

    public void btnLast(View view) {
        backOrNext(-1);
    }

    private void backOrNext(int goorBack) {//-1代表上一首，1代表下一首
        int index = currentPosition + goorBack;
        if (index < 0) {
            Toast.makeText(this, "已经是最顶部", Toast.LENGTH_SHORT).show();
            return;
        } else if (index >= musicFilesList.size()) {
            Toast.makeText(this, "已经是最底部", Toast.LENGTH_SHORT).show();
            return;
        }
        currentPosition = index;
        resetMediaResoure();
        play();
    }

    private void play() {
        mediaPlay.start();
        btn_playOrPause.setImageResource(R.drawable.a2p);
        setAnim();
        musicname.setText(getName(musicFilesList.get(currentPosition).getName()));
        author.setText(getAuthor(musicFilesList.get(currentPosition).getName()));
        Message message = Message.obtain();
        message.what = 1;
        handler.sendMessage(message);
        totalProgress = mediaPlay.getDuration();
        sb.setMax(totalProgress);
        totalTv.setText(new SimpleDateFormat("mm:ss").format(new Date(totalProgress)).toString());
    }

    private void pause() {
        mediaPlay.pause();
        cancelAnim();
        musicname.setText("暂无播放...");
        author.setText(getAuthor(musicFilesList.get(currentPosition).getName()));
    }

    public String getName(String name) {
        return spil(name)[1];
    }

    public String getAuthor(String name) {
        return spil(name)[0];
    }

    private String[] spil(String name) {
        //Tez Cadey - Seve.mp3
        //这里面接受的参数是正则表达式，所以想.*这种特殊符号就要做如下的处理
        String temp = name.subSequence(0, name.length() - 4).toString();
//        String[] split1 = name.split("[\\.]");//出现bugger，有的歌名好多个点

        String[] split2 = temp.split("-");
        return split2;
    }

    private void resetMediaResoure() {
        if (mediaPlay == null) {
            return;
        }
        mediaPlay.reset();
        try {
            mediaPlay.setDataSource(MainActivity.this,
                    Uri.fromFile(new File(musicFilesList.get(currentPosition).getAbsolutePath()))
            );
            mediaPlay.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void initbindService() {
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        conn = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mediaPlay = ((MusicService.MyBinder) service).getMediaPlay();
                Log.d("bigname_log", "onServiceConnected: media赋值");
                //这个方法会在服务创建之后回调，所以在这个方法执行之前使用mediaplayer会报空指针
                initMediaComleteListenr(); //设置media播放完成后的事件 //所以我把这个media的事件监听放在这了
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intent, conn, BIND_AUTO_CREATE);
        Log.d("bigname_log", "initbindService: " + mediaPlay);
    }

    private void initViewAndData() {

        toolbar = (Toolbar) findViewById(R.id.actionbar);
        imageview = ((ImageView) findViewById(R.id.image));
        listview = ((ListView) findViewById(R.id.listview));
        musicname = ((TextView) findViewById(R.id.musicname));
        author = ((TextView) findViewById(R.id.author));
        pb = ((ProgressBar) findViewById(R.id.pb));
        sb = (SeekBar) findViewById(R.id.sb);
        currentTv = ((TextView) findViewById(R.id.currenttimeTx));
        totalTv = ((TextView) findViewById(R.id.totletimeTv));
        btn_playOrPause = (ImageButton) findViewById(R.id.play_btn);
        layout = ((RelativeLayout) findViewById(R.id.layout));
        drawer = ((DrawerLayout) findViewById(R.id.drawer));
        btnY = ((Button) findViewById(R.id.btnY));
        btnB = ((Button) findViewById(R.id.btnB));
        btnR = ((Button) findViewById(R.id.btnR));
        btnH = ((Button) findViewById(R.id.btnH));

        setSupportActionBar(toolbar);

        animation = AnimationUtils.loadAnimation(this, R.anim.image_scale);
        animation.setInterpolator(new LinearInterpolator());

        musicFilesList = new ArrayList<>();

        ActionBar supportActionBar = getSupportActionBar();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, 0, 0);
        toggle.syncState();
        drawer.addDrawerListener(toggle);
        supportActionBar.setDisplayHomeAsUpEnabled(true);

    }



    /*
* 开启线程便利文件夹
* */
    private void startThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                findMusicFromPath(musicPath2);
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    /*
    * 该方法根据参数s文件地址得到对应的file对象然后遍历该file对象的所有file，并将以mp3结尾的file名称存到list中
    * */
    private void findMusicFromPath(String s) {
        File file = new File(s);
        find(file);
    }

    /*
    * 里边一个文件夹的递归思路：
    *   判断当前的文件为文件还是目录，文件则结束，目录则继续遍历,
    * */
    private void find(File file) {
        if (file.isDirectory()) { //目录的话
            File[] files = file.listFiles();
            if (files != null) {  //得到的内容不为空的话
                for (File f : files) {
                    if (f.isDirectory()) {
                        find(f);
                    } else {
                        getMp3FromFile(f);
                    }
                }
            }
        } else {
            getMp3FromFile(file);
        }
    }

    private void getMp3FromFile(File file) {
        String name = file.getName();
        if (name.endsWith("mp3") || name.endsWith("MP3")) {
            musicFilesList.add(file);
        }
    }

    public void setAnim() {
        imageview.startAnimation(animation);
    }

    public void cancelAnim() {
        imageview.clearAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        mediaPlay.stop();
        mediaPlay.release();
    }

    /*
    * 切换播放模式
    * */
    public void btnLeftChangePlayMode(View view) {
        if (play_mode == ORDER_PLAY) {
            play_mode = RAMDOM_PLAY;
            Toast.makeText(this, "随机播放", Toast.LENGTH_SHORT).show();
            return;
        }
        play_mode = ORDER_PLAY;
        Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();

    }

    public void btnLeftTheme(View view) {
        setBtnVisibal(View.VISIBLE);
    }

    private void setBtnVisibal(int VIEW) {
        btnY.setVisibility(VIEW);
        btnB.setVisibility(VIEW);
        btnR.setVisibility(VIEW);
        btnH.setVisibility(VIEW);
    }



    public void btnLeftDestory(View view) {
        mediaPlay.start();
        mediaPlay.release();
        mediaPlay = null;
        //自定义退出程序的方法
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /*
    * 如果setTheme(int theme){
    *   switch(theme)
    * }
    * 这样的话不能直接创一个final类型的数据作为参数，直接写1也同样是final类型的，解决的办法：定义一个变量 V 记录值，作为中转
    * 注意：以下没有采用这种方法
    * */
    public void setTheme() {
        int colorId = 0;
        switch (themecolor) {
            case THEME_YELLOW:
                colorId = R.color.colorHuang;
                break;
            case THEME_BLUE:
                colorId = R.color.colorBlue;
                break;
            case THEME_RED:
                colorId = R.color.colorRed;

                break;
            case THEME_HUI:
                colorId = R.color.colorHui;
                break;
        }
        toolbar.setBackgroundResource(colorId);
        sb.setBackgroundResource(colorId);
        currentTv.setBackgroundResource(colorId);
        totalTv.setBackgroundResource(colorId);
        layout.setBackgroundResource(colorId);
        listview.setDivider(getResources().getDrawable(colorId));
        listview.setDividerHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
    }

    private void toSetTheme(String news) {
        setTheme();
        setBtnVisibal(View.GONE);
        Toast.makeText(this, news, Toast.LENGTH_SHORT).show();
    }

    public void btnY(View view) {
        themecolor = THEME_YELLOW;
        toSetTheme("黄");
    }

    public void btnB(View view) {
        themecolor = THEME_BLUE;
        toSetTheme("蓝");
    }

    public void btnR(View view) {
        themecolor = THEME_RED;
        toSetTheme("红");
    }

    public void btnH(View view) {
        themecolor = THEME_HUI;
        toSetTheme("灰");
    }
    @Override
    public void onBackPressed() {
//        super.onBackPressed(); 注释掉，不然还是销毁了
        /*
        * 实现home键的功能
        * */
        if (drawer.isDrawerOpen(GravityCompat.START)) { //如果正在打开，则返回键先关闭菜单
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                break;
        }
        return true;
    }
}

