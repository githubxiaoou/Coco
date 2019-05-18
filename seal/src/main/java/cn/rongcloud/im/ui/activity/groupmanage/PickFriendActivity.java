package cn.rongcloud.im.ui.activity.groupmanage;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.db.GroupMember;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.pinyin.PinyinComparator;
import cn.rongcloud.im.server.pinyin.SideBar;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.BaseActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.model.UserInfo;

public class PickFriendActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private String groupId;
    private List<GroupMember> deleteGroupMemberList;// 群中除群主之外的成员
    private boolean isSetMaster;// 是否是设置群主
    private List<Friend> data_list = new ArrayList<>();// 过滤前的数据源
    private List<Friend> sourceDataList = new ArrayList<>();// 列表展示的数据源
    private ListView mListView;
    private TextView mNoFriends;
    private PickFriendAdapter adapter;
    public TextView dialog;//中部展示的字母提示
    private PinyinComparator pinyinComparator;//根据拼音来排列ListView里面的数据类
    private CharacterParser mCharacterParser;//汉字转换成拼音的类

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_friend);
        Button rightButton = getHeadRightButton();
        rightButton.setVisibility(View.GONE);
        isSetMaster = getIntent().getBooleanExtra("isSetMaster", false);
        groupId = getIntent().getStringExtra("GroupId");
        initGroupMemberList();
        initView();
    }

    private void initView() {
        if (isSetMaster) {
            setTitle("选择新群主");
        }
        //实例化汉字转拼音类
        mCharacterParser = CharacterParser.getInstance();
        pinyinComparator = PinyinComparator.getInstance();
        mListView = (ListView) findViewById(R.id.dis_friendlistview);
        mNoFriends = (TextView) findViewById(R.id.dis_show_no_friend);
        SideBar mSidBar = (SideBar) findViewById(R.id.dis_sidrbar);
        dialog = (TextView) findViewById(R.id.dis_dialog);
        mSidBar.setTextView(dialog);
        //设置右侧触摸监听
        mSidBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }
            }
        });

        adapter = new PickFriendAdapter(mContext, sourceDataList);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
    }

    private void initGroupMemberList() {
        SealUserInfoManager.getInstance().getGroupMembers(groupId, new SealUserInfoManager.ResultCallback<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                    deleteGroupMemberList = groupMembers;
                    fillSourceDataListForDeleteGroupMember();
            }

            @Override
            public void onError(String errString) {

            }
        });
    }

    private void fillSourceDataListForDeleteGroupMember() {
        if (deleteGroupMemberList != null && deleteGroupMemberList.size() > 0) {
            for (GroupMember deleteMember : deleteGroupMemberList) {
                if (deleteMember.getUserId().contains(getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, ""))) {
                    continue;
                }
                data_list.add(new Friend(deleteMember.getUserId(),
                        deleteMember.getName(), deleteMember.getPortraitUri(),
                        null //TODO displayName 需要处理 暂为 null
                ));
            }
            fillSourceDataList();
            updateAdapter();
        }
    }

    private void updateAdapter() {
        adapter.setData(sourceDataList);
        adapter.notifyDataSetChanged();
    }

    private void fillSourceDataList() {
        if (data_list != null && data_list.size() > 0) {
            sourceDataList = filledData(data_list); //过滤数据为有字母的字段  现在有字母 别的数据没有
        } else {
            mNoFriends.setVisibility(View.VISIBLE);
        }

        //还原除了带字母字段的其他数据
        for (int i = 0; i < data_list.size(); i++) {
            sourceDataList.get(i).setName(data_list.get(i).getName());
            sourceDataList.get(i).setUserId(data_list.get(i).getUserId());
            sourceDataList.get(i).setPortraitUri(data_list.get(i).getPortraitUri());
            sourceDataList.get(i).setDisplayName(data_list.get(i).getDisplayName());
        }
        // 根据a-z进行排序源数据
        Collections.sort(sourceDataList, pinyinComparator);
    }

    /**
     * 为ListView填充数据
     */
    private List<Friend> filledData(List<Friend> list) {
        List<Friend> mFriendList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Friend friendModel = new Friend(list.get(i).getUserId(), list.get(i).getName(), list.get(i).getPortraitUri());
            //汉字转换成拼音
            String pinyin = null;
            if (!TextUtils.isEmpty(list.get(i).getDisplayName())) {
                pinyin = mCharacterParser.getSpelling(list.get(i).getDisplayName());
            } else if (!TextUtils.isEmpty(list.get(i).getName())) {
                pinyin = mCharacterParser.getSpelling(list.get(i).getName());
            } else {
                UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(list.get(i).getUserId());
                if (userInfo != null) {
                    pinyin = mCharacterParser.getSpelling(userInfo.getName());
                }
            }
            String sortString;
            if (!TextUtils.isEmpty(pinyin)) {
                sortString = pinyin.substring(0, 1).toUpperCase();
            } else {
                sortString = "#";
            }

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                friendModel.setLetters(sortString);
            } else {
                friendModel.setLetters("#");
            }

            mFriendList.add(friendModel);
        }
        return mFriendList;

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
//        NToast.shortToast(mContext, sourceDataList.get(position).getName());
        new AlertDialog.Builder(mContext)
                .setMessage("确定选择" + sourceDataList.get(position).getName() + "为新群主，你将自动放弃群主身份。")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setMaster();
                    }

                    private void setMaster() {
                        HttpUtil.apiS().groupSetGroupMaster(groupId, sourceDataList.get(position).getUserId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new NetObserver<NetData<List<String>>>() {
                                    @Override
                                    public void Successful(NetData<List<String>> stringNetData) {
                                        NToast.shortToast(mContext, "设置成功");
                                        finish();
                                    }

                                    @Override
                                    public void Failure(Throwable t) {
                                        NToast.shortToast(mContext, "设置失败");
                                    }
                                });

                    }
                })
                .create().show();
    }

    public List<Friend> adapterList;// 参照SelectFriendsActivity页面的的写法，但其实就是sourceDataList
    class PickFriendAdapter extends BaseAdapter implements SectionIndexer {

        private Context context;
        private ArrayList<CheckBox> checkBoxList = new ArrayList<>();

        public PickFriendAdapter(Context context, List<Friend> list) {
            this.context = context;
            adapterList = list;
        }

        public void setData(List<Friend> friends) {
            adapterList = friends;
        }

        /**
         * 传入新的数据 刷新UI的方法
         */
        public void updateListView(List<Friend> list) {
            adapterList = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return adapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return adapterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final PickFriendAdapter.ViewHolder viewHolder;
            final Friend friend = adapterList.get(position);
            if (convertView == null) {
                viewHolder = new PickFriendAdapter.ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.item_pick_friend, parent, false);
                viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.dis_friendname);
                viewHolder.tvLetter = (TextView) convertView.findViewById(R.id.dis_catalog);
                viewHolder.mImageView = (SelectableRoundedImageView) convertView.findViewById(R.id.dis_frienduri);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (PickFriendAdapter.ViewHolder) convertView.getTag();
            }

            //根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                viewHolder.tvLetter.setVisibility(View.VISIBLE);
                viewHolder.tvLetter.setText(friend.getLetters());
            } else {
                viewHolder.tvLetter.setVisibility(View.GONE);
            }

            if (TextUtils.isEmpty(adapterList.get(position).getDisplayName())) {
                viewHolder.tvTitle.setText(adapterList.get(position).getName());
            } else {
                viewHolder.tvTitle.setText(adapterList.get(position).getDisplayName());
            }

            String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(adapterList.get(position));
            ImageLoader.getInstance().displayImage(portraitUri, viewHolder.mImageView, App.getOptions());
            return convertView;
        }

        @Override
        public Object[] getSections() {
            return new Object[0];
        }

        /**
         * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
         */
        @Override
        public int getPositionForSection(int sectionIndex) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = adapterList.get(i).getLetters();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == sectionIndex) {
                    return i;
                }
            }

            return -1;
        }

        /**
         * 根据ListView的当前位置获取分类的首字母的Char ascii值
         */
        @Override
        public int getSectionForPosition(int position) {
            return adapterList.get(position).getLetters().charAt(0);
        }


        final class ViewHolder {
            TextView tvLetter;
            TextView tvTitle;
            SelectableRoundedImageView mImageView;
        }

    }
}
