package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.rong.imageloader.core.ImageLoader;

public class ChatBackgroundPreviewActivity extends BaseActivity implements View.OnClickListener {
    private String mChatId;
    private String mType;// 1:群组聊天 2:单人聊天 3:全局聊天
    private String mUserId;
    private String mImgUrl;
    private ImageView mIvBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_background_preview);
        initView();
        initData();
    }

    private void initData() {
    }

    private void initView() {
        setTitle("聊天背景");
        mChatId = getIntent().getStringExtra("chatId");
        mType = getIntent().getStringExtra("type");
        mUserId = getIntent().getStringExtra("userId");
        mImgUrl = getIntent().getStringExtra("imgUrl");
        mHeadRightText.setVisibility(View.VISIBLE);
        mHeadRightText.setText("使用");
        mHeadRightText.setTextColor(getResources().getColor(R.color.blue));
        mHeadRightText.setOnClickListener(this);
        mIvBg = ((ImageView) findViewById(R.id.cl_bg));
        ImageLoader.getInstance().displayImage(mImgUrl, mIvBg, App.getOptions());
    }

    private void setBgImage() {
        LoadDialog.show(mContext);
        HttpUtil.apiS()
                .setChatBackgroundImage(mUserId, mChatId, mImgUrl, mType)
                .subscribeOn(Schedulers.io())
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        LoadDialog.dismiss(mContext);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<NetData>() {
                    @Override
                    public void Successful(NetData netData) {
                        if (netData.code == 200) {
                            NToast.shortToast(mContext, "设置成功");
                            setResult(100, getIntent());
                            finish();
                        }
                    }

                    @Override
                    public void Failure(Throwable t) {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_right:
                setBgImage();
                break;
        }
    }
}
