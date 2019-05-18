package cn.rongcloud.im.ui.activity.groupmanage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.response.GetAdminListResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.BaseActivity;
import cn.rongcloud.im.ui.activity.SelectFriendsActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.rong.imageloader.core.ImageLoader;

/**
 * 设置管理员页面
 */
public class GroupSetManagerActivity extends BaseActivity implements View.OnClickListener {
    private String groupId;
    private Button mRightButton;
    private ListView mLv;
    private List<GetAdminListResponse.Info> mSourceDataList = new ArrayList<>();
    private SetManagerAdapter mAdapter;
    private SelectableRoundedImageView mIvMasterHead;
    private TextView mTvMasterName;
    private TextView mTopRight;
    private LinearLayout mLlBottom;
    private boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_set_manager);
        groupId = getIntent().getStringExtra("GroupId");
        initView();
        initData();
    }

    private void initData() {
        getManagerList();
    }

    private void getManagerList() {
        HttpUtil.apiS()
                .groupGetAdminList(groupId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<NetData<GetAdminListResponse>>() {
                    @Override
                    public void Successful(NetData<GetAdminListResponse> data) {
                        List<GetAdminListResponse.Info> list = data.result.master;
                        if (list != null && list.size() != 0) {
                            GetAdminListResponse.Info master = list.get(0);
                            if (!TextUtils.isEmpty(master.portraitUri)) {
                                ImageLoader.getInstance().displayImage(master.portraitUri, mIvMasterHead, App.getOptions());
                            }
                            if (!TextUtils.isEmpty(master.nickname)) {
                                mTvMasterName.setText(master.nickname);
                            }
                        }
                        mSourceDataList = data.result.admin;
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Throwable t) {
                        NToast.shortToast(mContext, "网络错误");
                    }
                });
    }

    private void initView() {
        setTitle("设置管理员");
        mLv = ((ListView) findViewById(R.id.lv_manager));
        mTopRight = ((TextView) findViewById(R.id.text_right));
        mLlBottom = ((LinearLayout) findViewById(R.id.ll_bottom));
        mLlBottom.setOnClickListener(this);
        mTopRight.setText("编辑");
        mTopRight.setVisibility(View.VISIBLE);
        mTopRight.setTextColor(getResources().getColor(R.color.blue));
        mTopRight.setOnClickListener(this);
        mAdapter = new SetManagerAdapter();
        mLv.setAdapter(mAdapter);
        View head = LayoutInflater.from(mContext).inflate(R.layout.head_set_manager, null);
        mIvMasterHead = head.findViewById(R.id.iv_url);
        mTvMasterName = head.findViewById(R.id.tv_name);
        mLv.addHeaderView(head);
    }

    @Override
    public void onHeadLeftButtonClick(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_bottom:
                Intent intent = new Intent(this, SelectFriendsActivity.class);
                intent.putExtra("GroupId", groupId);
                intent.putExtra("isSetManager", true);
                startActivityForResult(intent, 100);
                break;
            case R.id.text_right:
                if (isEdit) {
                    mTopRight.setText("编辑");
                    isEdit = false;
                    mAdapter.editNotify(false);
                    mLlBottom.setVisibility(View.VISIBLE);
                } else {
                    mTopRight.setText("完成");
                    isEdit = true;
                    mAdapter.editNotify(true);
                    mLlBottom.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == 100) {
            getManagerList();
        }
    }

    class SetManagerAdapter extends BaseAdapter {
        private boolean isEdit;

        @Override
        public int getCount() {
            return mSourceDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mSourceDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_set_manager, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ImageLoader.getInstance().displayImage(mSourceDataList.get(position).portraitUri, holder.mIvUrl, App.getOptions());
            holder.mTvName.setText(mSourceDataList.get(position).nickname);
            holder.mBtnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NToast.shortToast(mContext, position + "");
                }
            });
            holder.mBtnDelete.setVisibility(isEdit ? View.VISIBLE : View.GONE);
            return convertView;
        }

        public void editNotify(boolean isEdit) {
            this.isEdit = isEdit;
            notifyDataSetChanged();
        }

        class ViewHolder {

            public Button mBtnDelete;
            public TextView mTvName;
            public SelectableRoundedImageView mIvUrl;

            public ViewHolder(View v) {
                mIvUrl = ((SelectableRoundedImageView) v.findViewById(R.id.dis_frienduri));
                mTvName = ((TextView) v.findViewById(R.id.dis_friendname));
                mBtnDelete = ((Button) v.findViewById(R.id.btn_delete));
            }
        }
    }

}
