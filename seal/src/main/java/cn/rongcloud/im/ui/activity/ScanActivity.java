package cn.rongcloud.im.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import cn.rongcloud.im.BuildConfig;
import cn.rongcloud.im.R;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.utils.photo.PhotoUtils;
import cn.rongcloud.im.server.widget.LoadDialog;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.model.UserInfo;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by will
 * on 2018/1/31.
 * 一维码二维码扫描
 */

public class ScanActivity extends BaseActivity implements QRCodeView.Delegate ,EasyPermissions.PermissionCallbacks, View.OnClickListener {

    public static final int REQUEST_CODE = 10001;
    public static final String SCAN_RESULT = "scan_result";
    private static final int CLICK_CONVERSATION_USER_PORTRAIT = 1;
    private static final String[] PERMISSION = {Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    TextView mTvTitle;
    ZXingView mZXingView;
    CheckBox mCbScan;
    private SoundPool mSoundPool = null;
    private SparseIntArray soundID = new SparseIntArray();
    private PhotoUtils mPhotoUtils;


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
        initSP();
    }

    private void initView() {
        mZXingView = findViewById(R.id.zxingview);
        mTvTitle = findViewById(R.id.tv_title);
        mCbScan = findViewById(R.id.cb_scan);
        TextView tvRight = findViewById(R.id.text_right);
        tvRight.setText("图片");
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setOnClickListener(this);
//        mZXingView.setType(BarcodeType.TWO_DIMENSION, null); // 只识别二维条码
//        mZXingView.setType(BarcodeType.ONE_DIMENSION, null); // 只识别一维条码
        setTitle("二维码");
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

        setPortraitChangeListener();
    }

    private void setPortraitChangeListener() {
        mPhotoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                    // 这边返回的都是content://media/external/images/media/919789格式的uri
                    Log.e("swo", uri.toString());
                    LoadDialog.show(mContext);
                    mZXingView.decodeQRCode(PhotoUtils.getRealPathFromUri(ScanActivity.this, uri));
                }
            }

            @Override
            public void onPhotoCancel() {
                Log.e("swo", "onPhotoCancel");
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
    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(200);
        }
    }

    private void initSP() {
        //当前系统的SDK版本大于等于21(Android 5.0)时
        if (Build.VERSION.SDK_INT >= 21) {
            mSoundPool = new SoundPool.Builder()
                    .setMaxStreams(1)  //传入音频数量
                    .setAudioAttributes(
                            //AudioAttributes是一个封装音频各种属性的方法
                            new AudioAttributes.Builder()
                                    //设置音频流的合适的属性
                                    .setLegacyStreamType(AudioManager.STREAM_MUSIC).build())
                    .build();
        } else {
            //设置最多可容纳2个音频流，音频的品质为5
            mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
        }

        soundID.put(1, mSoundPool.load(this, R.raw.di, 1));
    }


    @Override
    public void onScanQRCodeSuccess(String result) {
        if (mSoundPool != null) {
            mSoundPool.play(soundID.get(1), 1, 1, 0, 0, 1);
        }
//        vibrate();
        LoadDialog.dismiss(mContext);
        Intent data = new Intent();
        data.putExtra(SCAN_RESULT, result);
//        setResult(RESULT_OK, data);
//        result = "z2mlAvb0c";// 测试用
        Intent intent = new Intent(this, UserDetailActivity.class);
//        UserInfo userInfo = new UserInfo(result,
//                "Sync111",
//                Uri.parse(RongGenerate.generateDefaultAvatar("Sync111", result)));

        // TODO: 2019/5/16 不确定是不是这样就可以了,还是需要手动调用网络接口
        UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(result);
        if (userInfo == null) {
            if (BuildConfig.DEBUG) {
                NToast.shortToast(mContext, result);
            }
            mZXingView.startSpot(); // 重新开始识别
            return;
        }
        Friend friend = CharacterParser.getInstance().generateFriendFromUserInfo(userInfo);
        intent.putExtra("friend", friend);
        intent.putExtra("type", CLICK_CONVERSATION_USER_PORTRAIT);
        startActivity(intent);
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
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
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
        switch (requestCode) {
            case AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE:
                if (!EasyPermissions.hasPermissions(this, PERMISSION)) {
                    finish();
                }
                break;
            case PhotoUtils.INTENT_SELECT:
                mPhotoUtils.onActivityResult(ScanActivity.this, requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_right:
                mPhotoUtils.selectPictureWithoutCrop(ScanActivity.this);
                break;
        }
    }
}
