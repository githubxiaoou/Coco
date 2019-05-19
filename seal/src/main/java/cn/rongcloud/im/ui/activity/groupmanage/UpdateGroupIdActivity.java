package cn.rongcloud.im.ui.activity.groupmanage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.ClearWriteEditText;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.activity.BaseActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class UpdateGroupIdActivity extends BaseActivity implements View.OnClickListener {
    private ClearWriteEditText mNameEditText;
    private String newName;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_group_id);
        setTitle("群聊号");
        Button rightButton = getHeadRightButton();
        rightButton.setVisibility(View.GONE);
        mHeadRightText.setVisibility(View.VISIBLE);
        mHeadRightText.setText(getString(R.string.save_update));
        mHeadRightText.setOnClickListener(this);
        mNameEditText = (ClearWriteEditText) findViewById(R.id.update_name);
        groupId = getIntent().getStringExtra("GroupId");
    }

    @Override
    public void onClick(View v) {
        newName = mNameEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(newName)) {
            LoadDialog.show(mContext);
            updateGroupId();
        } else {
            NToast.shortToast(mContext, "群聊号不能为空");
            mNameEditText.setShakeAnimation();
        }
    }

    private void updateGroupId() {
        HttpUtil.apiS()
                .groupSetGroupNumber(groupId, newName)
                .subscribeOn(Schedulers.io())
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        LoadDialog.dismiss(mContext);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<NetData<List<String>>>() {
                    @Override
                    public void Successful(NetData<List<String>> listNetData) {
                        Intent intent = new Intent();
                        intent.putExtra("newName", newName);
                        setResult(RESULT_OK, intent);
                        finish();
                        NToast.shortToast(mContext, "设置成功");
                    }

                    @Override
                    public void Failure(Throwable t) {
                        NToast.shortToast(mContext, "设置失败");
                    }
                });
    }
}
