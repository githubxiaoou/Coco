package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.bingoogolapple.qrcode.core.BGAQRCodeUtil;
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.model.UserInfo;

public class MyCodeActivity extends BaseActivity {

    private SelectableRoundedImageView mIvPortrait;
    private TextView mTvName;
    private ImageView mIvCode;
    private String mLoginId;
    private UserInfo mUserInfo;

    public static void actionStart(BaseActivity activity, String loginId) {
        Intent intent = new Intent(activity, MyCodeActivity.class);
        intent.putExtra("loginId", loginId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_code);
        setTitle("我的二维码");
        initView();
        initData();
    }

    private void initData() {
        mUserInfo = RongUserInfoManager.getInstance().getUserInfo(mLoginId);
        ImageLoader.getInstance().displayImage(mUserInfo.getPortraitUri().toString(), mIvPortrait, App.getOptions());
        mTvName.setText(mUserInfo.getName());
        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> emitter) throws Exception {
                emitter.onNext(QRCodeEncoder.syncEncodeQRCode(mLoginId, BGAQRCodeUtil.dp2px(MyCodeActivity.this, 150)));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        mIvCode.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(Throwable e) {
                        NToast.shortToast(mContext, "二维码生成失败");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void initView() {
        mLoginId = getIntent().getStringExtra("loginId");
        mIvPortrait = (SelectableRoundedImageView) findViewById(R.id.iv_portrait);
        mTvName = (TextView) findViewById(R.id.tv_name);
        mIvCode = (ImageView) findViewById(R.id.iv_code);
        setHeadRightButtonVisibility(View.VISIBLE);
    }

    @Override
    public void onHeadRightButtonClick(View v) {
        NToast.shortToast(this, "更多");
    }

    @Override
    public void onHeadLeftButtonClick(View v) {
        finish();
    }
}
