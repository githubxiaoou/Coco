package cn.rongcloud.im.ui.activity.groupmanage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.response.GetAdminListResponse;
import cn.rongcloud.im.server.response.JinyanResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.BaseActivity;
import cn.rongcloud.im.ui.activity.SelectFriendsActivity;
import cn.rongcloud.im.ui.widget.switchbutton.SwitchButton;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.rong.imageloader.core.ImageLoader;

public class GroupJinyanActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private SwitchButton mSwJinyan;
    private LinearLayout mLlBottom;
    private String groupId;
    private ListView mLv;
    private ArrayList<String> jinyanIdList = new ArrayList<>();// 保存已经禁言的成员id，给添加禁言页面过滤使用。
    private List<JinyanResponse> mSourceDataList = new ArrayList<>();
    private SetJinyanAdapter adapter;
    private SharedPreferences sp;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_jinyan);
        groupId = getIntent().getStringExtra("GroupId");
        initView();
        initData();
    }

    private void initData() {
        getJinyanList();
    }

    private void getJinyanList() {
        LoadDialog.show(mContext);
        HttpUtil.apiS().groupJinyanList(groupId)
                .subscribeOn(Schedulers.io())
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        LoadDialog.dismiss(mContext);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<NetData<List<JinyanResponse>>>() {
                    @Override
                    public void Successful(NetData<List<JinyanResponse>> listNetData) {
                        mSwJinyan.setChecked("1".equals(listNetData.isAllForbiddenWords));
                        mSwJinyan.setOnCheckedChangeListener(GroupJinyanActivity.this);

                        mSourceDataList = listNetData.result;
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Throwable t) {

                    }
                });
    }

    private void initView() {
        setTitle("群内禁言");
        mSwJinyan = (SwitchButton) findViewById(R.id.sw_jinyan);
        mLlBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        mLv = ((ListView) findViewById(R.id.lv));
        mLlBottom.setOnClickListener(this);
        adapter = new SetJinyanAdapter();
        mLv.setAdapter(adapter);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        mUserId = sp.getString(SealConst.SEALTALK_USER_ID, "");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.ll_bottom:
                setJinyanList();
                Intent intent = new Intent(mContext, SelectFriendsActivity.class);
                intent.putExtra("GroupId", groupId);
                intent.putExtra("isSetJinyan", true);
                intent.putStringArrayListExtra("jinyanIdList", jinyanIdList);
                startActivityForResult(intent, 100);
                break;
        }
    }

    private void setJinyanList() {
        jinyanIdList.clear();
        for (JinyanResponse response : mSourceDataList) {
            jinyanIdList.add(response.id);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == 100) {
            getJinyanList();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        LoadDialog.show(mContext);
        HttpUtil.apiS().groupJinyanAll(groupId, mUserId, isChecked ? "1" : "2")
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
                        NToast.shortToast(mContext, "操作成功");
                    }

                    @Override
                    public void Failure(Throwable t) {
                        NToast.shortToast(mContext, "网络错误");
                    }
                });
    }

    class SetJinyanAdapter extends BaseAdapter {

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
                    showDeleteDialog(position);
                }
            });
            holder.mBtnDelete.setVisibility(View.VISIBLE);
            return convertView;
        }

        private void showDeleteDialog(final int position) {
            new AlertDialog.Builder(mContext)
                    .setMessage("确认取消" + mSourceDataList.get(position).nickname + "的禁言？")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LoadDialog.show(mContext);
                            HttpUtil.apiS().groupJinyan(groupId, mSourceDataList.get(position).id, "")
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
                                            NToast.shortToast(mContext, "操作成功");
                                            mSourceDataList.remove(position);
                                            notifyDataSetChanged();
                                        }

                                        @Override
                                        public void Failure(Throwable t) {
                                            NToast.shortToast(mContext, "网络错误");
                                        }
                                    });
                        }
                    }).create().show();
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
