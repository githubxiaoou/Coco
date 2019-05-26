package cn.rongcloud.im.ui.activity.records;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.model.SealSearchConversationResult;
import cn.rongcloud.im.ui.activity.SealSearchChattingDetailActivity;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.FileMessage;

/**
 * A simple {@link Fragment} subclass.
 */
public class FileFragment extends Fragment {

    private SealSearchConversationResult mResult;
    private List<Message> mSourceDataList = new ArrayList<>();
    private ListView mLvFile;
    private FileAdapter mAdapter;
    private SharedPreferences sp;
    private int mOldestMessageId;
    private boolean mCompleteFlag;

    public static FileFragment newInstance(SealSearchConversationResult result) {

        Bundle args = new Bundle();
        args.putParcelable("searchConversationResult", result);
        FileFragment fragment = new FileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public FileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
    }

    private void initView(View view) {
        mResult = getArguments().getParcelable("searchConversationResult");
        mLvFile = ((ListView) view.findViewById(R.id.lv_file));
        mAdapter = new FileAdapter();
        mLvFile.setAdapter(mAdapter);
        sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        mLvFile.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                    if (mCompleteFlag) {
                        mCompleteFlag = false;
                        getHistoryMessages(mOldestMessageId);
                    }
                }
            }
        });

        mLvFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object object = parent.getItemAtPosition(position);
                if (object instanceof Message) {
                    Message message = (Message)object;
                    Conversation conversation = mResult.getConversation();
                    RongIM.getInstance().startConversation(getActivity(), conversation.getConversationType(), conversation.getTargetId(), mResult.getTitle(), mSourceDataList.get(position).getSentTime());
                }
            }
        });
    }

    private void initData() {
        Log.e("swo", mResult.getConversation().getConversationType().toString() + "  " + mResult.getConversation().getTargetId());
        mCompleteFlag = true;
        getHistoryMessages(-1);

    }

    private void getHistoryMessages(int oldestMessageId) {
        RongIMClient.getInstance().getHistoryMessages(mResult.getConversation().getConversationType(),
                mResult.getConversation().getTargetId(), "RC:FileMsg",
                oldestMessageId, 20, new RongIMClient.ResultCallback<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        mCompleteFlag = true;
                        if (null != messages) {
                            Log.e("swo", messages.size() + "");
                            mSourceDataList.addAll(messages);
                            mAdapter.notifyDataSetChanged();
                            if (messages.size() > 0) {
                                mOldestMessageId = messages.get(messages.size() - 1).getMessageId();
                            }
                        }
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        Log.e("swo", errorCode.toString());
                    }
                });
    }

    class FileAdapter extends BaseAdapter {
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_history_file, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = ((ViewHolder) convertView.getTag());
            }

            Message message = mSourceDataList.get(position);


            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            String date = simpleDateFormat.format(new Date(message.getSentTime()));
            String formatDate = date.replace("-", "/");
            holder.mTvSendTime.setText(formatDate);
            holder.mTvFileName.setText(((FileMessage) message.getContent()).getName());
            long size = ((FileMessage) message.getContent()).getSize();
            if (size < 1000) {
                holder.mTvFileSize.setText(size + "B");
            } else if (size > 1000 && size < 1000 * 1000) {
                holder.mTvFileSize.setText(size / 1000 + "KB");
            } else {
                holder.mTvFileSize.setText(size / 1000 / 1000 + "MB");
            }

            // 先模仿demo中样子，直接使用群名，群title
            Friend friendByID = new Friend(message.getSenderUserId(), mResult.getTitle(), Uri.parse(mResult.getPortraitUri()));
            ImageLoader.getInstance().displayImage(friendByID.getPortraitUri().toString(), holder.mSenderPortrait, App.getOptions());
            holder.mTvSenderName.setText(friendByID.getName());

            return convertView;
        }

        class ViewHolder {

            private final ImageView mSenderPortrait;
            private final TextView mTvSenderName;
            private final TextView mTvSendTime;
            private final TextView mTvFileName;
            private final TextView mTvFileSize;

            public ViewHolder(View view) {
                mSenderPortrait = ((ImageView) view.findViewById(R.id.iv_sender_portrait));
                mTvSenderName = ((TextView) view.findViewById(R.id.tv_sender_name));
                mTvSendTime = ((TextView) view.findViewById(R.id.tv_send_time));
                mTvFileName = ((TextView) view.findViewById(R.id.tv_file_name));
                mTvFileSize = ((TextView) view.findViewById(R.id.tv_file_size));
            }
        }
    }

}
