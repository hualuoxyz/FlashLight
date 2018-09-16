package cn.com.hualuox;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private CameraManager cameraManager;
    private ImageButton btFlash;
    private boolean isOpen = true;
    private static final int MY_PERMISSION_REQUEST_CODE = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        getWindow().setStatusBarColor(Color.parseColor("#000000"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isAllGranted = checkPermissionAllGranted(
                new String[]
                        {
                                android.Manifest.permission.CAMERA
                        }
        );

        if(!isAllGranted)
        {
            ActivityCompat.requestPermissions(this,new String[]
                            {
                                    android.Manifest.permission.CAMERA
                            },
                    MY_PERMISSION_REQUEST_CODE);
        }


        btFlash = (ImageButton) findViewById(R.id.torch_btn);
        cameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);



        btFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String[] ids = cameraManager.getCameraIdList();
                    for (String id : ids) {
                        CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                        //查询该摄像头组件是否包含闪光灯
                        Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                        /*
                         * 获取相机面对的方向
                         * CameraCharacteristics.LENS_FACING_FRONT 前置摄像头
                         * CameraCharacteristics.LENS_FACING_BACK 后只摄像头
                         * CameraCharacteristics.LENS_FACING_EXTERNAL 外部的摄像头
                         */
                        Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                        if (flashAvailable != null && flashAvailable && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                            //打开或关闭手电筒
                            if (isOpen) {
                                try {
                                    cameraManager.setTorchMode(id, true);
                                    btFlash.setBackgroundResource(R.drawable.flashlight_on);
                                    isOpen = false;
                                }catch (CameraAccessException e){
                                    e.printStackTrace();
                                }
                            }
                            else {
                                try{
                                    cameraManager.setTorchMode(id,false);
                                    btFlash.setBackgroundResource(R.drawable.flashlight_off);
                                    isOpen = true;
                                }catch (CameraAccessException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean checkPermissionAllGranted(String[] permissions)
    {
        for (String permission:permissions)
        {
            if(ContextCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode == MY_PERMISSION_REQUEST_CODE)
        {
            boolean isAllGranted = true;
            for(int grant:grantResults)
            {
                if (grant!=PackageManager.PERMISSION_GRANTED)
                {
                    isAllGranted = false;
                    break;
                }
            }
            if(!isAllGranted)
                openAppDetails();
        }
    }
    private void openAppDetails()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("本应用需要调用后置摄像头权限以便打开手电筒，请到“应用信息->权限中授予！否则应用退出！");
        builder.setPositiveButton("去手动授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:"+getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.finish();
            }
        });
        builder.show();
    }
}
