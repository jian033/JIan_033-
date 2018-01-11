package com.example.x.jumpmachine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by X on 2018/1/7.
 */

public class Paint extends Activity {

    //用来保存屏幕参数
    int pxW,pxH,DPI,count,pianyi;
    double length;//距离
            //起点 X Y     终点  X Y
    float nowX,nowY,endX,endY;
    //显示   起点       终点    距离       起点锁定     终点锁定   屏幕参数
    TextView tv_start,tv_end,tv_length,tv_lock1,tv_lock2,tv_px;
    //用于参数修改
    EditText editText;
    //     确定     修改参数    重画
    Button bt_dump,bt_rec,bt_clear;
    //用于画点
    RelativeLayout mylayout;

    private Bitmap bitmap;
    private Canvas canvas;
    private android.graphics.Paint paint;
    Drawable drawable;
    //              延时参数(不同分辨率可能不同 微调即可)    实现原理  两点间距离 * 延时系数 = 屏幕按压毫秒数
    static double num=0;
    static boolean pm = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.paint_layout);
        //初始化
        Init();

        //屏幕适配
        if(pm){
            if(pxW == 1080 && pxH == 1920)
                num = 1.392;
            else if(pxW == 720 && pxH == 1280)
                num = 2.099;
            else if(pxW == 1440 && pxH == 2560 | pxH == 2960)
                num = 1.045;
            else if(pxW == 1080 && pxH == 2040 | pxH == 2060)
                num = 1.392;
            else if(pxW == 1080 && pxH == 2160)
                num = 1.38;
            else {
                Toast.makeText(Paint.this, "未适配的机型!请微调修改参数", Toast.LENGTH_LONG).show();
                num = 1.392;
            }
            pm = false;
        }

        editText.setText(String.valueOf(num));

        //修改参数
        bt_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    //判断是否为数字
                if(isNumeric(editText.getText().toString())){
                    num = Double.parseDouble(editText.getText().toString());
                    Toast.makeText(Paint.this, "修改成功!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(Paint.this, "请输入数字!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 重画
        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                nowX = 0;
                nowY = 0;
                endX = 0;
                endY = 0;
                length = 0;
                count = 1;
                tv_length.setText("距离: " + 0);
                tv_start.setText("起点: " + 0 + " , " + 0);
                tv_end.setText("终点: " + 0 + " , " + 0);
                tv_lock1.setText("终点: 未锁定");
                tv_lock2.setText("终点: 未锁定");
                Toast.makeText(Paint.this, "已清空!", Toast.LENGTH_SHORT).show();
            }
        });
        //确定
        bt_dump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(count>=3){
                                //计算按压时间    写入到文件
                    String s = String.valueOf(length * num);
                    length = 0;
                    FileOutputStream fileOutputStream;
                    try {
                        fileOutputStream = openFileOutput("data.txt", Context.MODE_PRIVATE);
                        fileOutputStream.write(s.getBytes());
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    finish();
                }else {
                    Toast.makeText(Paint.this, "请画点!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //画图                       pianyi 是为了 不让手挡住点   方便观察定位
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (count == 1) {
                    canvas.drawColor(0, PorterDuff.Mode.CLEAR);//清空画板
                    drawInit();//重置画板
                    canvas.drawPoint(event.getX(), event.getY() - pianyi, paint);//画起点
                    nowX = event.getX();                                               //起点X
                    nowY = event.getY() - pianyi;                                      //起点Y
                    tv_start.setText("起点: " + (int) nowX + " , " + (int) nowY);
                }
                if (count == 2) {
                    canvas.drawColor(0, PorterDuff.Mode.CLEAR);//清空画板
                    drawInit();
                    canvas.drawPoint(nowX, nowY, paint);            //还原第一个点
                    canvas.drawPoint(event.getX(), event.getY() - pianyi, paint);//画第二个点
                    endX = event.getX();                                            //终点X
                    endY = event.getY() - pianyi;                                   //终点Y
                    tv_end.setText("终点: " + (int) endX + " , " + (int) endY);

                }
                break;

            case MotionEvent.ACTION_UP:
                count++;
                if (count == 2)
                    tv_lock1.setText("起点: 已锁定");

                if (count == 3) {
                    canvas.drawLine(nowX, nowY, endX, endY, paint);//两点相连
                    length = 0;
                    length = Math.sqrt((nowX - endX) * (nowX - endX) + (nowY - endY) * (nowY - endY));//计算两点间距离
                    tv_length.setText("距离: " + (int) length + "px");
                    tv_lock2.setText("终点: 已锁定");
                }
                if (count > 3)
                    Toast.makeText(Paint.this, "2点已锁定！", Toast.LENGTH_SHORT).show();
        }
        return super.onTouchEvent(event);
    }


    private void Init(){

        nowX = 0;
        nowY = 0;
        endX = 0;
        endY = 0;
        pianyi = 200;// 偏移200 像素 方便观察
        length = 0;//距离初始化
        count = 1;  // 为 1 表示 起点没画   2 表示 起点已确定 终点没画  3 表示 起点终点 已确定

        //获取屏幕大小
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        pxW = dm.widthPixels;
        pxH = dm.heightPixels;
        DPI = dm.densityDpi;

        //绑定按钮
        bt_rec = (Button)findViewById(R.id.button13);
        bt_dump = (Button)findViewById(R.id.button3);
        bt_clear = (Button)findViewById(R.id.bt_clear);

        //绑定TextView
        tv_start = (TextView)findViewById(R.id.textView);
        tv_end = (TextView)findViewById(R.id.textView2);
        tv_length = (TextView)findViewById(R.id.textView3);
        tv_lock1 = (TextView)findViewById(R.id.tx_lock2) ;
        tv_lock2 = (TextView)findViewById(R.id.tx_lock1);
        tv_px = (TextView)findViewById(R.id.tv_px);

        editText = (EditText)findViewById(R.id.editText);

        tv_start.setText("起点: " + nowX + " , " + nowY);
        tv_end.setText("终点: " + endX + " , " + endY);
        tv_length.setText("距离: " + length);
        tv_px.setText(pxW + "x" + pxH +":"+ DPI+"dpi");

        paint = new android.graphics.Paint();
        paint.setStrokeWidth(10);//笔宽5像素
        paint.setColor(Color.RED);//设置为红笔
        paint.setAntiAlias(false);//锯齿不显示
        paint.setStyle(android.graphics.Paint.Style.FILL);
        bitmap = Bitmap.createBitmap(pxW, pxH, Bitmap.Config.ARGB_8888); //设置位图的宽高,bitmap为透明
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);//设置为透明，画布也是透明
    }
    public void drawInit(){
        drawable = new BitmapDrawable(bitmap) ;
        mylayout = (RelativeLayout)findViewById(R.id.paint_layout);
        mylayout.setBackgroundDrawable(drawable);
    }
    // 判断是否为数字
    public boolean isNumeric(String str){

        if(str.matches("-?[0-9]+.*[0-9]*")){
            return true;
        }
        return false;
    }
}
