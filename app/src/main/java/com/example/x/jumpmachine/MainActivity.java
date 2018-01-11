package com.example.x.jumpmachine;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = MainActivity.this;
        AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage("\n此程序完全免费开源\n\n需要Root和悬浮窗权限\n\n1.打开游戏\n2.开始画点\n3.确定\n4.跳 !\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tJian_033@\n"); //设置内容
        builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可

        builder.setPositiveButton("运行", new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); //关闭dialog

                Toast.makeText(MainActivity.this, "正在跳转到微信", Toast.LENGTH_SHORT).show();
                //打开微信
                PackageManager packageManager = context.getPackageManager();
                Intent mm= packageManager.getLaunchIntentForPackage("com.tencent.mm");
                startActivity(mm);

                //启动悬浮窗
                Intent intent = new Intent(MainActivity.this,MainService.class);
                startService(intent);
                finish();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.setNeutralButton("打开权限设置", new DialogInterface.OnClickListener() {//设置忽略按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Intent localIntent = new Intent();
                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= 9) {
                    localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    localIntent.setData(Uri.fromParts("package", getPackageName(), null));
                } else if (Build.VERSION.SDK_INT <= 8) {
                    localIntent.setAction(Intent.ACTION_VIEW);
                    localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                    localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
                }

                startActivity(localIntent);
                finish();
            }
        });
        builder.create().show();
    }
}
