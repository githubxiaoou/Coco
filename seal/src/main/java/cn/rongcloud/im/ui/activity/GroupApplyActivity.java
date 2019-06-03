package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.GroupMember;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.AddGroupMemberResponse;
import cn.rongcloud.im.server.response.GetGroupDetailResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.rong.imageloader.core.ImageLoader;

/**
 * 加入群组申请页面
 */
public class GroupApplyActivity extends BaseActivity implements View.OnClickListener {
    private static final int ADD_GROUP_MEMBER = 21;
    private String mMemberId;
    private String mGId;
    private SelectableRoundedImageView mIvPortrait;
    private TextView mTvName;
    private TextView mTvCount;
    private TextView mTvTip;
    private Button mBtnJoin;
    private GetGroupDetailResponse mGetGroupDetailResponse;
    private SharedPreferences sp;
    private String mUserId;

    public static void actionStart(BaseActivity activity, String gId, String memberId) {
        Intent intent = new Intent(activity, GroupApplyActivity.class);
        intent.putExtra("groupId", gId);
        intent.putExtra("memberId", memberId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_apply);
        setTitle("群聊邀请");
        initView();
        initData();
    }

    private void initData() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        mUserId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "");
        getGroupDetail();
    }

    // 判断角色，判断是否开启认证
    private void getGroupDetail() {
        LoadDialog.show(mContext);
        HttpUtil.apiS()
                .getGroupDetail(mGId, mMemberId)
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
                        mGetGroupDetailResponse = response.result;
                        ImageLoader.getInstance().displayImage(mGetGroupDetailResponse.portraitUri.toString(), mIvPortrait, App.getOptions());
                        mTvName.setText(mGetGroupDetailResponse.name);
                        mTvCount.setText(String.format("%s人", mGetGroupDetailResponse.memberCount));
                        mTvTip.setVisibility(View.VISIBLE);
                        if ("1".equals(mGetGroupDetailResponse.isNeedVerification)) {
                            mTvTip.setText("管理员已开启群认证，只能通过邀请方式进群");
                            mBtnJoin.setVisibility(View.GONE);
                        } else {
                            mTvTip.setText("确认加入该群聊");
                            mBtnJoin.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void Failure(Throwable t) {

                    }
                });
    }

    private void initView() {
        mMemberId = getIntent().getStringExtra("memberId");
        mGId = getIntent().getStringExtra("groupId");
        mIvPortrait = (SelectableRoundedImageView) findViewById(R.id.iv_portrait);
        mTvName = (TextView) findViewById(R.id.tv_name);
        mTvCount = (TextView) findViewById(R.id.tv_count);
        mTvTip = (TextView) findViewById(R.id.tv_tip);
        mBtnJoin = (Button) findViewById(R.id.btn_join);
        mBtnJoin.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_join:
                request(ADD_GROUP_MEMBER);
                break;
        }
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case ADD_GROUP_MEMBER:
                return action.addGroupMember(mGId, mMemberId, Collections.singletonList(mUserId));
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case ADD_GROUP_MEMBER:
                    LoadDialog.dismiss(mContext);
                    NToast.shortToast(mContext, getString(R.string.add_successful));
                    finish();
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case ADD_GROUP_MEMBER:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, mContext.getString(R.string.add_group_member_request_failed));
                break;
        }
    }
}
