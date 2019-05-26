package cn.rongcloud.im.ui.activity.records;


import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.SealSearchConversationResult;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;

/**
 * A simple {@link Fragment} subclass.
 */
public class PicFragment extends Fragment {
    private SealSearchConversationResult mResult;
    private List<Message> mSourceDataList = new ArrayList<>();
    private GridView mGvPic;
    private SharedPreferences sp;
    private int mOldestMessageId;
    private boolean mCompleteFlag;
    private PicAdapter mAdapter;

    public static PicFragment newInstance(SealSearchConversationResult result) {

        Bundle args = new Bundle();
        args.putParcelable("searchConversationResult", result);
        PicFragment fragment = new PicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public PicFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pic, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
    }

    private void initData() {
        mCompleteFlag = true;
        getHistoryMessages(-1);
    }

    private void getHistoryMessages(int oldestMessageId) {
        RongIMClient.getInstance().getHistoryMessages(mResult.getConversation().getConversationType(),
                mResult.getConversation().getTargetId(), "RC:ImgMsg",
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

    private void initView(View view) {
        mResult = getArguments().getParcelable("searchConversationResult");
        mGvPic = ((GridView) view.findViewById(R.id.gv_pic));
        sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        mAdapter = new PicAdapter();
        mGvPic.setAdapter(mAdapter);
        mGvPic.setOnScrollListener(new AbsListView.OnScrollListener() {
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

        mGvPic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

    class PicAdapter extends BaseAdapter {
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
            Viewholder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_history_pic, parent, false);
                holder = new Viewholder(convertView);
                convertView.setTag(holder);
            } else {
                holder = ((Viewholder) convertView.getTag());
            }
            ImageLoader.getInstance().displayImage(((ImageMessage) mSourceDataList.get(position).getContent()).getRemoteUri().toString(),
                    holder.mGvPic, App.getOptions());
            return convertView;
        }

        class Viewholder {

            private final ImageView mGvPic;

            public Viewholder(View view) {
                mGvPic = ((ImageView) view.findViewById(R.id.iv_pic));
            }
        }

    }

}
