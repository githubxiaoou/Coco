package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.bingoogolapple.qrcode.core.BGAQRCodeUtil;
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.model.Group;

/**
 * 群二维码
 * 由groupId,userId生成
 */
public class GroupCodeActivity extends BaseActivity implements View.OnClickListener {
    private SelectableRoundedImageView mIvPortrait;
    private TextView mTvName;
    private ImageView mIvCode;
    private String mLoginId;
    private String mGId;
    private boolean needAuth;
    private Group mGroupInfo;
    private TextView mTvCover;

    public static void actionStart(BaseActivity activity, String gId, String loginId, boolean needAuth) {
        Intent intent = new Intent(activity, GroupCodeActivity.class);
        intent.putExtra("groupId", gId);
        intent.putExtra("loginId", loginId);
        intent.putExtra("needAuth", needAuth);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_code);
        setTitle("群二维码");
        initView();
        initData();
    }

    private void initData() {
        mGroupInfo = RongUserInfoManager.getInstance().getGroupInfo(mGId);
        ImageLoader.getInstance().displayImage(mGroupInfo.getPortraitUri().toString(), mIvPortrait, App.getOptions());
        mTvName.setText(mGroupInfo.getName());
        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> emitter) throws Exception {
                emitter.onNext(QRCodeEncoder.syncEncodeQRCode(mGId + "," + mLoginId, BGAQRCodeUtil.dp2px(mContext, 150)));
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
        mGId = getIntent().getStringExtra("groupId");
        needAuth = getIntent().getBooleanExtra("needAuth", false);
        ImageView ivRight = (ImageView) findViewById(R.id.iv_right);
        mIvPortrait = (SelectableRoundedImageView) findViewById(R.id.iv_portrait);
        mTvName = (TextView) findViewById(R.id.tv_name);
        mIvCode = (ImageView) findViewById(R.id.iv_code);
//        setHeadRightButtonVisibility(View.GONE);
//        ivRight.setVisibility(View.VISIBLE);
        ivRight.setOnClickListener(this);
        mTvCover = ((TextView) findViewById(R.id.tv_cover));
        mTvCover.setVisibility(needAuth ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onHeadLeftButtonClick(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_right:
                NToast.shortToast(mContext, "更多");
                break;
        }
    }
}
