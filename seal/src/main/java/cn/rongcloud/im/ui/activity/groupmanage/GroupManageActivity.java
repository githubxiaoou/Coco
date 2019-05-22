package cn.rongcloud.im.ui.activity.groupmanage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.response.GetGroupDetailResponse;
import cn.rongcloud.im.server.response.QuitListResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.BaseActivity;
import cn.rongcloud.im.ui.activity.liveness.LivenessActivity;
import cn.rongcloud.im.ui.widget.switchbutton.SwitchButton;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.rong.imageloader.core.ImageLoader;

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
    private String mUserId;
    private TextView mTvAuthTip;
    private ListView mLvInvite;
    List<QuitListResponse> sourceDataList = new ArrayList<>();// 列表展示的数据源
    private InviteListAdapter mAdapter;
    private SharedPreferences sp;
    private LinearLayout mLlMasterContainer;

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
        sp = getSharedPreferences("config", MODE_PRIVATE);
        mUserId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "");
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
                            mTvManagerCount.setText(String.format("%s人", result.adminCount));
                            mTvGroupId.setText(result.otherId);
                            if ("1".equals(result.isProtected)) {
                                mSwGroupProtect.setChecked(true);
                            }
                            mSwGroupProtect.setOnCheckedChangeListener(GroupManageActivity.this);
                            // 群认证如果开启了，需要请求邀请列表，所以先设置监听
                            mSwGroupAuth.setOnCheckedChangeListener(GroupManageActivity.this);
                            if ("1".equals(result.isNeedVerification)) {
                                mSwGroupAuth.setChecked(true);
                            }

                            // 群主显示“设置管理员”和“群主权限转让”
                            if (mUserId.equals(result.creatorId)) {
                                mRlAdjustManager.setVisibility(View.VISIBLE);
                                mLlMasterContainer.setVisibility(View.VISIBLE);
                            } else {
                                mRlAdjustManager.setVisibility(View.GONE);
                                mLlMasterContainer.setVisibility(View.GONE);
                            }
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
        mLlQuitList.setOnClickListener(this);
        mTvGroupId = (TextView) findViewById(R.id.tv_group_id);
        mLlGroupId = (LinearLayout) findViewById(R.id.ll_group_id);
        mLlGroupId.setOnClickListener(this);
        mTvGroupHelper = (TextView) findViewById(R.id.tv_group_helper);
        mGroupHelper = (LinearLayout) findViewById(R.id.group_helper);
        mGroupHelper.setOnClickListener(this);
        mLlOwnerTransfer = (LinearLayout) findViewById(R.id.ll_owner_transfer);
        mLlOwnerTransfer.setOnClickListener(this);
        mLlMasterContainer = ((LinearLayout) findViewById(R.id.ll_master_container));
        mSwGroupProtect = (SwitchButton) findViewById(R.id.sw_group_protect);
        mSwGroupAuth = (SwitchButton) findViewById(R.id.sw_group_auth);
        mTvAuthTip = ((TextView) findViewById(R.id.tv_auth_tip));
        mLvInvite = ((ListView) findViewById(R.id.lv_invite));
        groupId = getIntent().getStringExtra("GroupId");

        mAdapter = new InviteListAdapter();
        mLvInvite.setAdapter(mAdapter);
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
            case R.id.ll_quit_list:
                intent = new Intent(mContext, QuitListActivity.class);
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
                if (buttonView.isChecked()) {
                    setGroupParams("","1");
                    mTvAuthTip.setVisibility(View.GONE);
                    mLvInvite.setVisibility(View.VISIBLE);
                } else {
                    setGroupParams("", "0");
                }
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
                        getInviteList();
                    }

                    @Override
                    public void Failure(Throwable t) {
                        NToast.shortToast(mContext, "网络错误");
                    }
                });
    }

    private void getInviteList() {
        LoadDialog.show(mContext);
        HttpUtil.apiS()
                .getQuitList(groupId, "1")
                .subscribeOn(Schedulers.io())
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        LoadDialog.dismiss(mContext);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<NetData<List<QuitListResponse>>>() {
                    @Override
                    public void Successful(NetData<List<QuitListResponse>> listNetData) {
                        sourceDataList = listNetData.result;
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Throwable t) {
                        NToast.shortToast(mContext, "网络错误");
                    }
                });
    }

    class InviteListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return sourceDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return sourceDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_quit_list, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ImageLoader.getInstance().displayImage(sourceDataList.get(position).friendPortraitUri, holder.mImageView, App.getOptions());
            holder.tvTitle.setText(sourceDataList.get(position).friendName);
            holder.mTvTime.setText("邀请人: " + sourceDataList.get(position).userName);
            holder.mTvAgree.setVisibility(View.VISIBLE);
            return convertView;
        }

        final class ViewHolder {
            private final TextView mTvAgree;
            TextView tvTitle;
            SelectableRoundedImageView mImageView;
            private final TextView mTvTime;

            public ViewHolder(View view) {
                tvTitle = (TextView) view.findViewById(R.id.dis_friendname);
                mImageView = (SelectableRoundedImageView) view.findViewById(R.id.dis_frienduri);
                mTvTime = ((TextView) view.findViewById(R.id.dis_time));
                mTvAgree = ((TextView) view.findViewById(R.id.tv_agree));
            }
        }
    }
}
