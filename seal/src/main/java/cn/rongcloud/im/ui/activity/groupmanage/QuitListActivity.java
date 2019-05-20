package cn.rongcloud.im.ui.activity.groupmanage;

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
import cn.rongcloud.im.server.response.QuitListResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.BaseActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.rong.imageloader.core.ImageLoader;

/**
 * 退群成员列表
 */
public class QuitListActivity extends BaseActivity {
    private String groupId;
    private ListView mLv;
    List<QuitListResponse> sourceDataList = new ArrayList<>();// 列表展示的数据源
    private QuitListAdapter mAdapter;
    private TextView mTvNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quit_list);
        initView();
        initData();
    }

    private void initData() {
        getQuitList();
    }

    private void getQuitList() {
        LoadDialog.show(mContext);
        HttpUtil.apiS()
                .getQuitList(groupId, "2")
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
                        if (null == sourceDataList || sourceDataList.size() == 0) {
                            mTvNoData.setVisibility(View.VISIBLE);
                        } else {
                            mTvNoData.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void Failure(Throwable t) {
                        NToast.shortToast(mContext, "网络错误");
                    }
                });
    }

    private void initView() {
        setTitle("退群成员列表");
        groupId = getIntent().getStringExtra("GroupId");
        mLv = ((ListView) findViewById(R.id.lv_member));
        mTvNoData = ((TextView) findViewById(R.id.tv_no_data));
        mAdapter = new QuitListAdapter();
        mLv.setAdapter(mAdapter);
    }

    class QuitListAdapter extends BaseAdapter {

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
            holder.mTvTime.setText("于 " + sourceDataList.get(position).createdAt + " 退群");
            return convertView;
        }

        final class ViewHolder {
            TextView tvTitle;
            SelectableRoundedImageView mImageView;
            private final TextView mTvTime;

            public ViewHolder(View view) {
                tvTitle = (TextView) view.findViewById(R.id.dis_friendname);
                mImageView = (SelectableRoundedImageView) view.findViewById(R.id.dis_frienduri);
                mTvTime = ((TextView) view.findViewById(R.id.dis_time));
            }
        }
    }
}
