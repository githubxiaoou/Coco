package cn.rongcloud.im.ui.activity;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.qiniu.android.storage.UploadManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.BaseAction;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.SetPortraitResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.photo.PhotoUtils;
import cn.rongcloud.im.server.widget.BottomMenuDialog;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import top.zibin.luban.Luban;

/**
 * 个人账号
 */
public class MyAccountActivity extends BaseActivity implements View.OnClickListener {

    private static final int UP_LOAD_PORTRAIT = 8;
    private static final int GET_QI_NIU_TOKEN = 128;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private SelectableRoundedImageView mImageView;
    private TextView mName;
    private PhotoUtils photoUtils;
    private BottomMenuDialog dialog;
    private UploadManager uploadManager;
    private String imageUrl;
    private Uri selectUri;
    private TextView mTvAccount;
    private TextView mTvCode;
    private TextView mTvMyPic;
    private TextView mTvGender;
    private TextView mTvMail;
    private String mUserId;
    private String mLoginId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myaccount);
        setTitle(R.string.de_actionbar_myacc);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();
        initView();
    }

    private void initView() {
        TextView mPhone = (TextView) findViewById(R.id.tv_my_phone);
        RelativeLayout portraitItem = (RelativeLayout) findViewById(R.id.rl_my_portrait);
        RelativeLayout nameItem = (RelativeLayout) findViewById(R.id.rl_my_username);
        RelativeLayout accountItem = (RelativeLayout) findViewById(R.id.rl_account);
        RelativeLayout codeItem = (RelativeLayout) findViewById(R.id.rl_code);
        RelativeLayout picItem = (RelativeLayout) findViewById(R.id.rl_my_pic);
        RelativeLayout genderItem = (RelativeLayout) findViewById(R.id.rl_gender);
        RelativeLayout mailItem = (RelativeLayout) findViewById(R.id.rl_mail);
        mImageView = (SelectableRoundedImageView) findViewById(R.id.img_my_portrait);
        mName = (TextView) findViewById(R.id.tv_my_username);
        mTvAccount = ((TextView) findViewById(R.id.tv_account));
        mTvCode = ((TextView) findViewById(R.id.tv_code));
        mTvMyPic = ((TextView) findViewById(R.id.tv_my_pic));
        mTvGender = ((TextView) findViewById(R.id.tv_gender));
        mTvMail = ((TextView) findViewById(R.id.tv_mail));

        portraitItem.setOnClickListener(this);
        nameItem.setOnClickListener(this);
        accountItem.setOnClickListener(this);
        codeItem.setOnClickListener(this);
        picItem.setOnClickListener(this);
        genderItem.setOnClickListener(this);
        mailItem.setOnClickListener(this);
        String cacheName = sp.getString(SealConst.SEALTALK_LOGIN_NAME, "");
        String cachePortrait = sp.getString(SealConst.SEALTALK_LOGING_PORTRAIT, "");
        String cachePhone = sp.getString(SealConst.SEALTALK_LOGING_PHONE, "");
        String cacheRegion = sp.getString(SealConst.SEALTALK_LOGIN_REGION, "86");
        String cacheMail = sp.getString(SealConst.SEALTALK_MAIL, "");
        String cacheGender = sp.getString(SealConst.SEALTALK_GENDER, "");
        if (!TextUtils.isEmpty(cachePhone)) {
            mPhone.setText("+" + cacheRegion + " " + cachePhone);
        }
        if (!TextUtils.isEmpty(cacheName)) {
            mName.setText(cacheName);
            String cacheId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "a");
            String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(new UserInfo(
                    cacheId, cacheName, Uri.parse(cachePortrait)));
            ImageLoader.getInstance().displayImage(portraitUri, mImageView, App.getOptions());
        }
        if (!TextUtils.isEmpty(cacheMail)) {
            mTvMail.setText(cacheMail);
        }
        if (!TextUtils.isEmpty(cacheGender) && !"0".equals(cacheGender)) {
            mTvGender.setText("1".equals(cacheGender) ? "男" : "女");
        }

        setPortraitChangeListener();
        BroadcastManager.getInstance(mContext).addAction(SealConst.CHANGEINFO, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mName.setText(sp.getString(SealConst.SEALTALK_LOGIN_NAME, ""));
                mTvMail.setText(sp.getString(SealConst.SEALTALK_MAIL, ""));
                String gender = sp.getString(SealConst.SEALTALK_GENDER, "");
                mTvGender.setText("1".equals(gender) ? "男" : "女");
            }
        });

        initOptionPicker();
        mUserId = sp.getString(SealConst.SEALTALK_USER_ID, "");
        mLoginId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "");
        mTvAccount.setText(mLoginId);
    }

    private void setPortraitChangeListener() {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                    selectUri = uri;
                    LoadDialog.show(mContext);
                    // 原来的
//                    request(GET_QI_NIU_TOKEN);
                    // 改成直接上传图片
                    uploadImage(selectUri);
                }
            }

            @Override
            public void onPhotoCancel() {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_my_portrait:
                showPhotoDialog();
                break;
            case R.id.rl_my_username:
                startActivity(new Intent(this, UpdateNameActivity.class));
                break;
            case R.id.rl_account:
                break;
            case R.id.rl_code:
                MyCodeActivity.actionStart(this, mLoginId);
                break;
            case R.id.rl_my_pic:
                break;
            case R.id.rl_gender:
                pvOptions.show();
                break;
            case R.id.rl_mail:
                startActivity(new Intent(this, UpdateMailActivity.class));
                break;
        }
    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case UP_LOAD_PORTRAIT:
                return action.setPortrait(imageUrl);
            case GET_QI_NIU_TOKEN:
                return action.getQiNiuToken();
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case UP_LOAD_PORTRAIT:
                    SetPortraitResponse spRes = (SetPortraitResponse) result;
                    if (spRes.getCode() == 200) {
                        editor.putString(SealConst.SEALTALK_LOGING_PORTRAIT, imageUrl);
                        editor.commit();
                        ImageLoader.getInstance().displayImage(imageUrl, mImageView, App.getOptions());
                        if (RongIM.getInstance() != null) {
                            RongIM.getInstance().setCurrentUserInfo(new UserInfo(sp.getString(SealConst.SEALTALK_LOGIN_ID, ""), sp.getString(SealConst.SEALTALK_LOGIN_NAME, ""), Uri.parse(imageUrl)));
                        }
                        BroadcastManager.getInstance(mContext).sendBroadcast(SealConst.CHANGEINFO);
                        NToast.shortToast(mContext, getString(R.string.portrait_update_success));
                    }
                    LoadDialog.dismiss(mContext);
                    break;
                case GET_QI_NIU_TOKEN:
//                    QiNiuTokenResponse response = (QiNiuTokenResponse) result;
//                    if (response.getCode() == 200) {
//                        uploadImage(response.getResult().getDomain(), response.getResult().getToken(), selectUri);
//                    }
                    break;
            }
        }
    }


    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case GET_QI_NIU_TOKEN:
            case UP_LOAD_PORTRAIT:
                NToast.shortToast(mContext, mContext.getString(R.string.set_avatar_request_failed));
                LoadDialog.dismiss(mContext);
                break;
        }
    }

    static public final int REQUEST_CODE_ASK_PERMISSIONS = 101;

    /**
     * 弹出底部框
     */
    @TargetApi(23)
    private void showPhotoDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new BottomMenuDialog(mContext);
        dialog.setConfirmListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (Build.VERSION.SDK_INT >= 23) {
                    int checkPermission = checkSelfPermission(Manifest.permission.CAMERA);
                    if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_ASK_PERMISSIONS);
                        } else {
                            new AlertDialog.Builder(mContext)
                                    .setMessage(mContext.getString(R.string.camera_permission_request_prompt))
                                    .setPositiveButton(mContext.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_ASK_PERMISSIONS);
                                        }
                                    })
                                    .setNegativeButton(mContext.getString(R.string.cancel), null)
                                    .create().show();
                        }
                        return;
                    }

                    // 从6.0系统(API 23)开始，访问外置存储需要动态申请权限
                    checkPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
                        return;
                    }
                }

                photoUtils.takePicture(MyAccountActivity.this);
            }
        });
        dialog.setMiddleListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                // 从6.0系统(API 23)开始，访问外置存储需要动态申请权限
                if (Build.VERSION.SDK_INT >= 23) {
                    int checkPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
                        return;
                    }
                }
                photoUtils.selectPicture(MyAccountActivity.this);
            }
        });
        dialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PhotoUtils.INTENT_CROP:
            case PhotoUtils.INTENT_TAKE:
            case PhotoUtils.INTENT_SELECT:
                photoUtils.onActivityResult(MyAccountActivity.this, requestCode, resultCode, data);
                break;
        }
    }

    public void uploadImage(Uri imagePath) {
        // for debug
//        final File imageFile = new File("/storage/emulated/0/crop_file.jpg");
        final File imageFile = new File(imagePath.getPath());
//        if (imageFile.exists()) {
//            Toast.makeText(this, "文件是存在的", Toast.LENGTH_SHORT).show();
//        }
//        if (!imageFile.isFile()) {
//            return;
//        }

        Observable.just(imageFile)
                .observeOn(Schedulers.io())
                .map(new Function<File, List<File>>() {
                    @Override
                    public List<File> apply(File file) throws Exception {
                        return Luban.with(MyAccountActivity.this).load(imageFile).ignoreBy(1024).get();
                    }
                })
                .flatMap(new Function<List<File>, ObservableSource<NetData<String>>>() {
                    @Override
                    public ObservableSource<NetData<String>> apply(List<File> files) throws Exception {
                        File image = files.get(0);// 第一张
                        RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), image);
                        MultipartBody.Part file = MultipartBody.Part.createFormData("photo", image.getName(), body);
                        return HttpUtil.apiS().uploadImageInfo(file);
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        LoadDialog.dismiss(mContext);
                    }
                })
                .subscribe(new NetObserver<NetData<String>>() {
                    @Override
                    public void Successful(NetData<String> stringNetData) {
//                        NToast.shortToast(mContext, "图片上传成功");
                        Log.e("swo", "图片上传成功");
                        if (stringNetData.code == 200) {
                            imageUrl = BaseAction.DOMAIN_APP + stringNetData.url;
                            Log.e("uploadImage", imageUrl);
                            if (!TextUtils.isEmpty(imageUrl)) {
                                request(UP_LOAD_PORTRAIT);
                            }
                        }
                    }

                    @Override
                    public void Failure(Throwable t) {
                        NToast.shortToast(mContext, "图片上传失败");
                    }
                });
    }

    private OptionsPickerView pvOptions;
    private ArrayList<String> genders = new ArrayList<>();

    private void initOptionPicker() {//条件选择器初始化
        genders.add("男");
        genders.add("女");
        pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(final int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                //  0男1女 接口是1男2女
                LoadDialog.show(mContext);
                HttpUtil.apiS().updateUserInfo(mUserId, "", "", String.valueOf(options1 + 1), "", "")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnTerminate(new Action() {
                            @Override
                            public void run() throws Exception {
                                LoadDialog.dismiss(mContext);
                            }
                        })
                        .subscribe(new NetObserver<NetData>() {
                            @Override
                            public void Successful(NetData netData) {
                                if (netData.code == 200) {
                                    editor.putString(SealConst.SEALTALK_GENDER, String.valueOf(options1 + 1));
                                    editor.commit();

                                    BroadcastManager.getInstance(mContext).sendBroadcast(SealConst.CHANGEINFO);

                                    NToast.shortToast(mContext, "修改成功");
                                }
                            }

                            @Override
                            public void Failure(Throwable t) {
                                NToast.shortToast(mContext, "修改失败");
                            }
                        });
            }
        })
                .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .build();
        pvOptions.setPicker(genders);//一级选择器
    }

}
