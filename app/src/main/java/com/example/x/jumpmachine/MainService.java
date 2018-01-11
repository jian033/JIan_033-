package com.example.x.jumpmachine;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.app.Service;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by X on 2018/1/7.
 */

public class MainService extends Service {

    private static final String TAG = "MainService";

    RelativeLayout toucherLayout;
    WindowManager.LayoutParams params;
    WindowManager windowManager;
    Intent paint_intent;
    DisplayMetrics dm;

    //用来判断是否画点
    static boolean one = false;
    //跳按钮 开始按钮 退出按钮
    Button bt_jump,bt_open,bt_exit;

    //状态栏高度.
    int statusBarHeight = -1;

    //不与Activity进行绑定.
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    //执行shell
    private void execShellCmd(String cmd) {

        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.i(TAG,"MainService Created");
        createToucher();
    }

    @SuppressLint("ResourceType")
    private void createToucher()
    {
        //赋值WindowManager&LayoutParam.
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //设置窗口初始停靠位置.
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        //设置悬浮窗口长宽数据.
        dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        params.width = (int)(screenWidth*0.4);
        params.height = (int)(screenHeight*0.4);
        Toast.makeText(MainService.this,"当前设备分辨率为:"+screenWidth+"x"+screenHeight,Toast.LENGTH_SHORT).show();

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        toucherLayout = (RelativeLayout) inflater.inflate(R.layout.layout_jump,null);
        //添加toucherlayout
        windowManager.addView(toucherLayout,params);

        Log.i(TAG,"toucherlayout-->left:" + toucherLayout.getLeft());
        Log.i(TAG,"toucherlayout-->right:" + toucherLayout.getRight());
        Log.i(TAG,"toucherlayout-->top:" + toucherLayout.getTop());
        Log.i(TAG,"toucherlayout-->bottom:" + toucherLayout.getBottom());

        //主动计算出当前View的宽高信息.
        toucherLayout.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);

        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height","dimen","android");
        if (resourceId > 0)
        {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        Log.i(TAG,"状态栏高度为:" + statusBarHeight);

        //按钮绑定.
        bt_jump = (Button) toucherLayout.findViewById(R.id.jump);
        bt_open = (Button) toucherLayout.findViewById(R.id.open);
        bt_exit = (Button) toucherLayout.findViewById(R.id.bt_exit) ;

        //绑定画图Activty
        paint_intent = new Intent(this,Paint.class);
        paint_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //获取ROOT权限
        execShellCmd("su");

        Toast.makeText(MainService.this,"把跳一跳开起来!",Toast.LENGTH_SHORT).show();

        //开始画点
        bt_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //启动画点Activity
               startActivity(paint_intent);
               one = true;//设置已画点
            }
        });

        //跳
        bt_jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //判断是否画点
                if (one){
                    double lenght = Double.valueOf(get_length());//获取距离 单位px
                    int random=(int) ((Math.random()*30+1)+dm.heightPixels*0.4);  //产生随机数 使每次点击位置不一样
                    int random2=(int)(Math.random()*10+1)+random;
                    execShellCmd("input swipe " + random + " " + random +" "+ random2+ " " + random2 + " " + (int)lenght);//shell 点击命令

                    System.out.println("屏幕点击位置: "+random+" - "+random2);
                    one = false;//设置未画点
                }else {
                    Toast.makeText(MainService.this,"请画点",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //退出
        bt_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSelf();
            }
        });

    }
    @Override
    public void onDestroy()
    {
        windowManager.removeView(toucherLayout);
        stopSelf();//停止服务
        super.onDestroy();
    }

    //从文件获取距离
    private String get_length(){
        String str = "0";
        FileInputStream fileInputStream;
        try {
            fileInputStream = openFileInput("data.txt");
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] bufffer = new byte[fileInputStream.available()];
            int len = 0 ;
            while((len = fileInputStream.read(bufffer)) != -1){
                bout.write(bufffer,0,len);
            }
            byte[] content = bout.toByteArray();
            str = new String(content);
            Toast.makeText(MainService.this,"点击时间为:"+str+"ms",Toast.LENGTH_SHORT).show();
            fileInputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
}
