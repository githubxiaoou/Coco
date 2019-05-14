package cn.rongcloud.im.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.SparseIntArray;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.server.utils.NToast;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by will
 * on 2018/1/31.
 * 一维码二维码扫描
 */

public class ScanActivity extends BaseActivity implements QRCodeView.Delegate ,EasyPermissions.PermissionCallbacks{

    public static final int REQUEST_CODE = 10001;
    public static final String SCAN_RESULT = "scan_result";
    private static final String PERMISSION = Manifest.permission.CAMERA;
    TextView mTvTitle;
    ZXingView mZXingView;
    CheckBox mCbScan;
    private SoundPool mSoundPool = null;
    private SparseIntArray soundID = new SparseIntArray();


    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, ScanActivity.class);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    public static void actionStartFagment(Fragment activity) {
        Intent intent = new Intent(activity.getContext(), ScanActivity.class);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initView();
        mZXingView.setDelegate(this);
//        initSP();
    }

    private void initView() {
        mZXingView = findViewById(R.id.zxingview);
        mTvTitle = findViewById(R.id.tv_title);
        mCbScan = findViewById(R.id.cb_scan);
//        mZXingView.setType(BarcodeType.TWO_DIMENSION, null); // 只识别二维条码
//        mZXingView.setType(BarcodeType.ONE_DIMENSION, null); // 只识别一维条码
        setTitle("扫描");
        mCbScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCbScan.setText("关灯");
                    mZXingView.openFlashlight();
                } else {
                    mCbScan.setText("开灯");
                    mZXingView.closeFlashlight();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        if (checkPermission()) {
            initCamera();
        }
        super.onStart();
    }

    private void initCamera() {
        mZXingView.startCamera();
        mZXingView.showScanRect();
        mZXingView.startSpot();
    }

    @Override
    protected void onStop() {
        mZXingView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mZXingView.onDestroy();
        super.onDestroy();
    }

    /*震动*/
//    private void vibrate() {
//        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//        if (vibrator != null) {
//            vibrator.vibrate(200);
//        }
//    }

//    private void initSP() {
//        //当前系统的SDK版本大于等于21(Android 5.0)时
//        if (Build.VERSION.SDK_INT >= 21) {
//            mSoundPool = new SoundPool.Builder()
//                    .setMaxStreams(1)  //传入音频数量
//                    .setAudioAttributes(
//                            //AudioAttributes是一个封装音频各种属性的方法
//                            new AudioAttributes.Builder()
//                                    //设置音频流的合适的属性
//                                    .setLegacyStreamType(AudioManager.STREAM_MUSIC).build())
//                    .build();
//        } else {
//            //设置最多可容纳2个音频流，音频的品质为5
//            mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
//        }
//
//        soundID.put(1, mSoundPool.load(this, R.raw.di, 1));
//    }


    @Override
    public void onScanQRCodeSuccess(String result) {
        if (mSoundPool != null) {
            mSoundPool.play(soundID.get(1), 1, 1, 0, 0, 1);
        }
        Intent data = new Intent();
        data.putExtra(SCAN_RESULT, result);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {

    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        NToast.shortToast(mContext, "打开相机出错");
    }

    private boolean checkPermission() {
        if (!EasyPermissions.hasPermissions(this, PERMISSION)) {
            EasyPermissions.requestPermissions(this, "应用需要:\n相机权限用于扫描", 100, PERMISSION);
            return false;
        }
        return true;
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (checkPermission()) {
            initCamera();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, Collections.singletonList(PERMISSION))) {
            new AppSettingsDialog.Builder(this)
                    .setTitle("权限申请")
                    .setRationale("无相机权限功能将无法使用。")
                    .setPositiveButton("去设置")
                    .build().show();
        } else {
            finish();
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (!EasyPermissions.hasPermissions(this, PERMISSION)) {
                finish();
            }
        }
    }

}
