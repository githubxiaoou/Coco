package cn.rongcloud.im.ui.activity.groupmanage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.ui.activity.BaseActivity;
import cn.rongcloud.im.ui.widget.switchbutton.SwitchButton;

/**
 * 群管理
 */
public class GroupManageActivity extends BaseActivity implements View.OnClickListener {

    TextView mTvManagerCount;
    RelativeLayout mRlAdjustManager;
    LinearLayout mLlBanned;
    LinearLayout mLlLiveness;
    LinearLayout mLlQuitList;
    TextView mTvGroupId;
    LinearLayout mLlGroupId;
    TextView mTvGroupHelper;
    LinearLayout mGroupHelper;
    LinearLayout mLlOwnerTransfer;
    SwitchButton mSwGroupProtect;
    SwitchButton mSwGroupAuth;

    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manage);
        initView();
        setTitle("群管理");
        SealAppContext.getInstance().pushActivity(this);
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {

            }
        }
    }


    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
        }
    }


    private void initView() {
        mTvManagerCount = (TextView) findViewById(R.id.tv_manager_count);
        mRlAdjustManager = (RelativeLayout) findViewById(R.id.rl_adjust_manager);
        mRlAdjustManager.setOnClickListener(this);
        mLlBanned = (LinearLayout) findViewById(R.id.ll_banned);
        mLlBanned.setOnClickListener(this);
        mLlLiveness = (LinearLayout) findViewById(R.id.ll_liveness);
        mLlQuitList = (LinearLayout) findViewById(R.id.ll_quit_list);
        mTvGroupId = (TextView) findViewById(R.id.tv_group_id);
        mLlGroupId = (LinearLayout) findViewById(R.id.ll_group_id);
        mLlGroupId.setOnClickListener(this);
        mTvGroupHelper = (TextView) findViewById(R.id.tv_group_helper);
        mGroupHelper = (LinearLayout) findViewById(R.id.group_helper);
        mGroupHelper.setOnClickListener(this);
        mLlOwnerTransfer = (LinearLayout) findViewById(R.id.ll_owner_transfer);
        mLlOwnerTransfer.setOnClickListener(this);
        mSwGroupProtect = (SwitchButton) findViewById(R.id.sw_group_protect);
        mSwGroupAuth = (SwitchButton) findViewById(R.id.sw_group_auth);
        groupId = getIntent().getStringExtra("GroupId");
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            default:
                break;
            case R.id.rl_adjust_manager:
                intent = new Intent(mContext, GroupSetManagerActivity.class);
                intent.putExtra("GroupId", groupId);
                startActivity(intent);
                break;
            case R.id.ll_banned:
                break;
            case R.id.ll_group_id:
                break;
            case R.id.group_helper:
                break;
            case R.id.ll_owner_transfer:
                intent = new Intent(mContext, PickFriendActivity.class);
                intent.putExtra("isSetMaster", true);
                intent.putExtra("GroupId", groupId);
                startActivity(intent);
                break;
        }
    }
}
