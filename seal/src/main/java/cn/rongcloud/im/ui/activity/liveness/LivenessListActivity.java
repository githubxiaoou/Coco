package cn.rongcloud.im.ui.activity.liveness;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.response.JinyanResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.BaseActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.rong.imageloader.core.ImageLoader;

public class LivenessListActivity extends BaseActivity {
    private String type;
    private String groupId;
    private ListView mLv;
    List<JinyanResponse> sourceDataList = new ArrayList<>();// 列表展示的数据源
    private LivenessAdapter mAdapter;
    private TextView mTvTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveness_list);
        initView();
        initData();
    }

    private void initData() {
        getLivenessList();
    }

    private void getLivenessList() {
        LoadDialog.show(mContext);
        HttpUtil.apiS()
                .groupInactiveMember(groupId, type)
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
                        sourceDataList = listNetData.result;
                        mAdapter.notifyDataSetChanged();
                        if (null != sourceDataList && sourceDataList.size() > 0) {
                            refreshTip(Integer.valueOf(type));
                        } else {
                            mTvTip.setText("没有数据");
                        }
                    }

                    @Override
                    public void Failure(Throwable t) {
                        NToast.shortToast(mContext, "网络错误");
                    }
                });
    }

    private void refreshTip(Integer type) {
        switch (type) {
            case 1:
                mTvTip.setText("三天不活跃(" + sourceDataList.size() + "人)");
                break;
            case 2:
                mTvTip.setText("一周不活跃(" + sourceDataList.size() + "人)");
                break;
            case 3:
                mTvTip.setText("一个月不活跃(" + sourceDataList.size() + "人)");
                break;
        }
    }

    private void initView() {
        setTitle("不活跃群成员");
        type = getIntent().getStringExtra("type");
        groupId = getIntent().getStringExtra("GroupId");
        mLv = ((ListView) findViewById(R.id.lv_member));
        mAdapter = new LivenessAdapter();
        mLv.setAdapter(mAdapter);
        View head = LayoutInflater.from(mContext).inflate(R.layout.head_liveness, null);
        mTvTip = head.findViewById(R.id.tv_tip);
        mLv.addHeaderView(head);
    }

    class LivenessAdapter extends BaseAdapter {
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_member, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ImageLoader.getInstance().displayImage(sourceDataList.get(position).portraitUri, holder.mImageView, App.getOptions());
            holder.tvTitle.setText(sourceDataList.get(position).nickname);
            return convertView;
        }

        final class ViewHolder {
            TextView tvTitle;
            SelectableRoundedImageView mImageView;

            public ViewHolder(View view) {
                tvTitle = (TextView) view.findViewById(R.id.dis_friendname);
                mImageView = (SelectableRoundedImageView) view.findViewById(R.id.dis_frienduri);
            }
        }
    }


}
