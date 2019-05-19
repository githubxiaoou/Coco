package cn.rongcloud.im.ui.activity.groupmanage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetGroupDetailResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.activity.BaseActivity;
import cn.rongcloud.im.ui.activity.liveness.LivenessActivity;
import cn.rongcloud.im.ui.widget.switchbutton.SwitchButton;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

/**
 * 群管理
 */
public class GroupManageActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

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
        initData();
    }

    private void initData() {
        getGroupDetail();
    }

    private void getGroupDetail() {
        LoadDialog.show(mContext);
        HttpUtil.apiS()
                .getGroupDetail(groupId)
                .subscribeOn(Schedulers.io())
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        LoadDialog.dismiss(mContext);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<NetData<GetGroupDetailResponse>>() {
                    @Override
                    public void Successful(NetData<GetGroupDetailResponse> response) {
                        GetGroupDetailResponse result = response.result;
                        if (result != null) {
                            mTvManagerCount.setText(result.memberCount + "人");
                            mTvGroupId.setText(result.otherId);
                            if ("1".equals(result.isProtected)) {
                                mSwGroupProtect.setChecked(true);
                            }
                            if ("1".equals(result.isNeedVerification)) {
                                mSwGroupAuth.setChecked(true);
                            }
                            mSwGroupProtect.setOnCheckedChangeListener(GroupManageActivity.this);
                            mSwGroupAuth.setOnCheckedChangeListener(GroupManageActivity.this);
                        }
                    }

                    @Override
                    public void Failure(Throwable t) {

                    }
                });
    }

    private void initView() {
        mTvManagerCount = (TextView) findViewById(R.id.tv_manager_count);
        mRlAdjustManager = (RelativeLayout) findViewById(R.id.rl_adjust_manager);
        mRlAdjustManager.setOnClickListener(this);
        mLlBanned = (LinearLayout) findViewById(R.id.ll_banned);
        mLlBanned.setOnClickListener(this);
        mLlLiveness = (LinearLayout) findViewById(R.id.ll_liveness);
        mLlLiveness.setOnClickListener(this);
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
                startActivityForResult(intent, 100);
                break;
            case R.id.ll_banned:
                intent = new Intent(mContext, GroupJinyanActivity.class);
                intent.putExtra("GroupId", groupId);
                startActivity(intent);
                break;
            case R.id.ll_group_id:
                intent = new Intent(mContext, UpdateGroupIdActivity.class);
                intent.putExtra("GroupId", groupId);
                startActivityForResult(intent, 200);
                break;
            case R.id.ll_liveness:
                intent = new Intent(this, LivenessActivity.class);
                intent.putExtra("GroupId", groupId);
                startActivity(intent);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            switch (requestCode) {
                case 100:
                    mTvManagerCount.setText(data.getIntExtra("managerCount", 0) + "人");
                    break;
                case 200:
                    mTvGroupId.setText(data.getStringExtra("newName"));
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sw_group_protect:
                setGroupParams(buttonView.isChecked() ? "1": "0", "");
                break;
            case R.id.sw_group_auth:
                setGroupParams("", buttonView.isChecked() ? "1": "0");
                break;
        }
    }

    private void setGroupParams(String protect, String auth) {
        LoadDialog.show(mContext);
        HttpUtil.apiS()
                .setGroupParams(groupId, protect, auth)
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
                    public void Successful(NetData listNetData) {
                        NToast.shortToast(mContext, "设置成功");
                    }

                    @Override
                    public void Failure(Throwable t) {
                        NToast.shortToast(mContext, "网络错误");
                    }
                });
    }
}
