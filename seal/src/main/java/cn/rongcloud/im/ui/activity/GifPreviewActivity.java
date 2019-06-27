//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.message.GifMessage;
import io.rong.common.FileUtils;
import io.rong.common.RLog;
import io.rong.eventbus.EventBus;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.R.drawable;
import io.rong.imkit.R.id;
import io.rong.imkit.R.layout;
import io.rong.imkit.R.string;
import io.rong.imkit.RongBaseNoActionbarActivity;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.image.HackyViewPager;
import io.rong.imkit.utilities.OptionsPopupDialog;
import io.rong.imkit.utilities.OptionsPopupDialog.OnOptionsItemClickedListener;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imkit.utilities.RongUtils;
import io.rong.imlib.RongCommonDefine.GetMessageDirection;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.OperationCallback;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.destruct.DestructionTaskManager;
import io.rong.imlib.destruct.DestructionTaskManager.OnOverTimeChangeListener;
import io.rong.imlib.destruct.MessageBufferPool;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.Message.MessageDirection;
import io.rong.message.DestructionCmdMessage;
import io.rong.message.ImageMessage;

public class GifPreviewActivity extends RongBaseNoActionbarActivity implements OnLongClickListener {
    private static final String TAG = "GifPreviewActivity";
    private static final int IMAGE_MESSAGE_COUNT = 10;
    private HackyViewPager mViewPager;
    private GifMessage mCurrentImageMessage;
    private Message mMessage;
    private ConversationType mConversationType;
    private int mCurrentMessageId;
    private String mTargetId = null;
    private int mCurrentIndex = 0;
    private GifPreviewActivity.ImageAdapter mImageAdapter;
    private boolean isFirstTime = false;
    private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
            RLog.i("GifPreviewActivity", "onPageSelected. position:" + position);
            GifPreviewActivity.this.mCurrentIndex = position;
            View view = GifPreviewActivity.this.mViewPager.findViewById(position);
            if (view != null) {
                GifPreviewActivity.this.mImageAdapter.updatePhotoView(position, view);
            }

            if (position == GifPreviewActivity.this.mImageAdapter.getCount() - 1) {
                GifPreviewActivity.this.getConversationImageUris(GifPreviewActivity.this.mImageAdapter.getItem(position).getMessageId().getMessageId(), GetMessageDirection.BEHIND);
            } else if (position == 0) {
                GifPreviewActivity.this.getConversationImageUris(GifPreviewActivity.this.mImageAdapter.getItem(position).getMessageId().getMessageId(), GetMessageDirection.FRONT);
            }

        }

        public void onPageScrollStateChanged(int state) {
        }
    };

    public GifPreviewActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layout.rc_fr_photo);
        Message currentMessage = (Message)this.getIntent().getParcelableExtra("message");
        this.mMessage = currentMessage;
        this.mCurrentImageMessage = (GifMessage)currentMessage.getContent();
        this.mConversationType = currentMessage.getConversationType();
        this.mCurrentMessageId = currentMessage.getMessageId();
        this.mTargetId = currentMessage.getTargetId();
        this.mViewPager = (HackyViewPager)this.findViewById(id.viewpager);
        this.mViewPager.setOnPageChangeListener(this.mPageChangeListener);
        this.mImageAdapter = new GifPreviewActivity.ImageAdapter();
        this.isFirstTime = true;
        this.getConversationImageUris(this.mCurrentMessageId, GetMessageDirection.FRONT);
        this.getConversationImageUris(this.mCurrentMessageId, GetMessageDirection.BEHIND);
    }

    private void getConversationImageUris(int mesageId, final GetMessageDirection direction) {
        if (this.mConversationType != null && !TextUtils.isEmpty(this.mTargetId)) {
            RongIMClient.getInstance().getHistoryMessages(this.mConversationType, this.mTargetId, "RCD:GifMsg", mesageId, 1, direction, new ResultCallback<List<Message>>() {
                public void onSuccess(List<Message> messages) {
                    ArrayList<GifPreviewActivity.ImageInfo> lists = new ArrayList();
                    if (messages != null) {
                        if (direction.equals(GetMessageDirection.FRONT)) {
                            Collections.reverse(messages);
                        }

                        for(int i = 0; i < messages.size(); ++i) {
                            Message message = (Message)messages.get(i);
                            if (message.getContent() instanceof ImageMessage) {
                                ImageMessage imageMessage = (ImageMessage)message.getContent();
                                Uri largeImageUri = imageMessage.getLocalUri() == null ? imageMessage.getRemoteUri() : imageMessage.getLocalUri();
                                if (imageMessage.getThumUri() != null && largeImageUri != null) {
                                    lists.add(GifPreviewActivity.this.new ImageInfo(message, imageMessage.getThumUri(), largeImageUri));
                                }
                            }
                        }
                    }

                    if (direction.equals(GetMessageDirection.FRONT) && GifPreviewActivity.this.isFirstTime) {
                        lists.add(GifPreviewActivity.this.new ImageInfo(GifPreviewActivity.this.mMessage, GifPreviewActivity.this.mCurrentImageMessage.getThumUri(), GifPreviewActivity.this.mCurrentImageMessage.getLocalUri() == null ? GifPreviewActivity.this.mCurrentImageMessage.getRemoteUri() : GifPreviewActivity.this.mCurrentImageMessage.getLocalUri()));
                        GifPreviewActivity.this.mImageAdapter.addData(lists, direction.equals(GetMessageDirection.FRONT));
                        GifPreviewActivity.this.mViewPager.setAdapter(GifPreviewActivity.this.mImageAdapter);
                        GifPreviewActivity.this.isFirstTime = false;
                        GifPreviewActivity.this.mViewPager.setCurrentItem(lists.size() - 1);
                        GifPreviewActivity.this.mCurrentIndex = lists.size() - 1;
                    } else if (lists.size() > 0) {
                        GifPreviewActivity.this.mImageAdapter.addData(lists, direction.equals(GetMessageDirection.FRONT));
                        GifPreviewActivity.this.mImageAdapter.notifyDataSetChanged();
                        if (direction.equals(GetMessageDirection.FRONT)) {
                            GifPreviewActivity.this.mViewPager.setCurrentItem(lists.size());
                            GifPreviewActivity.this.mCurrentIndex = lists.size();
                        }
                    }

                }

                public void onError(ErrorCode e) {
                }
            });
        }

    }

    protected void onPause() {
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        DestructionTaskManager.getInstance().removeListeners("GifPreviewActivity");
    }

    public boolean onPictureLongClick(View v, Uri thumbUri, Uri largeImageUri) {
        return false;
    }

    public boolean onLongClick(View v) {
        GifPreviewActivity.ImageInfo imageInfo = this.mImageAdapter.getImageInfo(this.mCurrentIndex);
        if (imageInfo != null) {
            Uri thumbUri = imageInfo.getThumbUri();
            Uri largeImageUri = imageInfo.getLargeImageUri();
            if (this.onPictureLongClick(v, thumbUri, largeImageUri)) {
                return true;
            }

            if (largeImageUri == null) {
                return false;
            }

            final File file;
            if (!largeImageUri.getScheme().startsWith("http") && !largeImageUri.getScheme().startsWith("https")) {
                file = new File(largeImageUri.getPath());
            } else {
                file = ImageLoader.getInstance().getDiskCache().get(largeImageUri.toString());
            }

            String[] items = new String[]{this.getString(string.rc_save_picture)};
            OptionsPopupDialog.newInstance(this, items).setOptionsPopupDialogListener(new OnOptionsItemClickedListener() {
                public void onOptionsItemClicked(int which) {
                    if (which == 0) {
                        String[] permissions = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
                        if (!PermissionCheckUtil.requestPermissions(GifPreviewActivity.this, permissions)) {
                            return;
                        }

                        String saveImagePath = RongUtils.getImageSavePath(GifPreviewActivity.this);
                        if (file != null && file.exists()) {
                            String name = System.currentTimeMillis() + ".gif";
                            FileUtils.copyFile(file, saveImagePath + File.separator, name);
                            MediaScannerConnection.scanFile(GifPreviewActivity.this, new String[]{saveImagePath + File.separator + name}, (String[])null, (OnScanCompletedListener)null);
                            Toast.makeText(GifPreviewActivity.this, GifPreviewActivity.this.getString(string.rc_save_picture_at), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GifPreviewActivity.this, GifPreviewActivity.this.getString(string.rc_src_file_not_found), View.VISIBLE).show();
                        }
                    }

                }
            }).show();
        }

        return true;
    }

    private class ImageInfo {
        private Message message;
        private Uri thumbUri;
        private Uri largeImageUri;

        ImageInfo(Message message, Uri thumbnail, Uri largeImageUri) {
            this.message = message;
            this.thumbUri = thumbnail;
            this.largeImageUri = largeImageUri;
        }

        public Message getMessageId() {
            return this.message;
        }

        public Uri getLargeImageUri() {
            return this.largeImageUri;
        }

        public Uri getThumbUri() {
            return this.thumbUri;
        }
    }

    private class ImageAdapter extends PagerAdapter {
        private ArrayList<GifPreviewActivity.ImageInfo> mImageList;

        private ImageAdapter() {
            this.mImageList = new ArrayList();
        }

        private View newView(Context context, GifPreviewActivity.ImageInfo imageInfo) {
            View result = LayoutInflater.from(context).inflate(layout.rc_fr_gif, (ViewGroup)null);
            GifPreviewActivity.ImageAdapter.ViewHolder holder = new GifPreviewActivity.ImageAdapter.ViewHolder();
            holder.progressBar = (ProgressBar)result.findViewById(id.rc_progress);
            holder.progressText = (TextView)result.findViewById(id.rc_txt);
            holder.photoView = (ImageView)result.findViewById(id.rc_photoView);
            holder.mCountDownView = (TextView)result.findViewById(id.rc_count_down);
            holder.photoView.setOnLongClickListener(GifPreviewActivity.this);
            holder.photoView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    GifPreviewActivity.this.finish();
                }
            });
            result.setTag(holder);
            return result;
        }

        public void addData(ArrayList<GifPreviewActivity.ImageInfo> newImages, boolean direction) {
            if (newImages != null && newImages.size() != 0) {
                if (this.mImageList.size() == 0) {
                    this.mImageList.addAll(newImages);
                } else if (direction && !GifPreviewActivity.this.isFirstTime && !this.isDuplicate(((GifPreviewActivity.ImageInfo)newImages.get(0)).getMessageId().getMessageId())) {
                    ArrayList<GifPreviewActivity.ImageInfo> temp = new ArrayList();
                    temp.addAll(this.mImageList);
                    this.mImageList.clear();
                    this.mImageList.addAll(newImages);
                    this.mImageList.addAll(this.mImageList.size(), temp);
                } else if (!GifPreviewActivity.this.isFirstTime && !this.isDuplicate(((GifPreviewActivity.ImageInfo)newImages.get(0)).getMessageId().getMessageId())) {
                    this.mImageList.addAll(this.mImageList.size(), newImages);
                }

            }
        }

        private boolean isDuplicate(int messageId) {
            Iterator var2 = this.mImageList.iterator();

            GifPreviewActivity.ImageInfo info;
            do {
                if (!var2.hasNext()) {
                    return false;
                }

                info = (GifPreviewActivity.ImageInfo)var2.next();
            } while(info.getMessageId().getMessageId() != messageId);

            return true;
        }

        public GifPreviewActivity.ImageInfo getItem(int index) {
            return (GifPreviewActivity.ImageInfo)this.mImageList.get(index);
        }

        public int getItemPosition(Object object) {
            return -2;
        }

        public int getCount() {
            return this.mImageList.size();
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public Object instantiateItem(ViewGroup container, int position) {
            RLog.i("GifPreviewActivity", "instantiateItem.position:" + position);
            View imageView = this.newView(container.getContext(), (GifPreviewActivity.ImageInfo)this.mImageList.get(position));
            this.updatePhotoView(position, imageView);
            imageView.setId(position);
            container.addView(imageView);
            return imageView;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            RLog.i("GifPreviewActivity", "destroyItem.position:" + position);
            container.removeView((View)object);
        }

        private void updatePhotoView(final int position, View view) {
            final GifPreviewActivity.ImageAdapter.ViewHolder holder = (GifPreviewActivity.ImageAdapter.ViewHolder)view.getTag();
            Uri originalUri = ((GifPreviewActivity.ImageInfo)this.mImageList.get(position)).getLargeImageUri();
            final Uri thumbUri = ((GifPreviewActivity.ImageInfo)this.mImageList.get(position)).getThumbUri();
            Glide.with(view).applyDefaultRequestOptions(new RequestOptions()
                    .placeholder(R.drawable.loadfailure))
                    .asGif().load(thumbUri == null ? originalUri : thumbUri).into(holder.photoView);
        }

        private void sendDestructingMsg(Message message) {
            if (message.getContent().isDestruct() && message.getMessageDirection() == MessageDirection.RECEIVE && message.getReadTime() <= 0L && !TextUtils.isEmpty(message.getUId())) {
                long currentTimeMillis = System.currentTimeMillis();
                RongIMClient.getInstance().setMessageReadTime((long)message.getMessageId(), currentTimeMillis, (OperationCallback)null);
                message.setReadTime(currentTimeMillis);
                DestructionCmdMessage destructionCmdMessage = new DestructionCmdMessage();
                destructionCmdMessage.addBurnMessageUId(message.getUId());
                MessageBufferPool.getInstance().putMessageInBuffer(Message.obtain(message.getTargetId(), message.getConversationType(), destructionCmdMessage));
                EventBus.getDefault().post(message);
            }

        }

        private void handleDestructionImage(int position, final GifPreviewActivity.ImageAdapter.ViewHolder holder) {
            final Message message = ((GifPreviewActivity.ImageInfo)this.mImageList.get(position)).message;
            if (message.getContent().isDestruct() && message.getReadTime() > 0L) {
                holder.mCountDownView.setVisibility(View.VISIBLE);
                RongIM.getInstance().createDestructionTask(GifPreviewActivity.this.mMessage, new OnOverTimeChangeListener() {
                    public void onOverTimeChanged(final int messageId, final long leftTime) {
                        holder.mCountDownView.post(new Runnable() {
                            public void run() {
                                if (messageId == message.getMessageId()) {
                                    if (leftTime <= 30L) {
                                        holder.mCountDownView.setBackgroundResource(drawable.rc_count_down_preview_count);
                                        holder.mCountDownView.setText(GifPreviewActivity.this.getResources().getString(string.rc_time_count_down, new Object[]{leftTime}));
                                    } else {
                                        holder.mCountDownView.setBackgroundResource(drawable.rc_count_down_preview_no_count);
                                    }

                                    if (leftTime <= 0L) {
                                        String toast = GifPreviewActivity.this.getResources().getString(string.rc_toast_message_destruct);
                                        Toast.makeText(GifPreviewActivity.this, toast, Toast.LENGTH_LONG).show();
                                        GifPreviewActivity.this.finish();
                                    }

                                }
                            }
                        });
                    }

                    public void onMessageDestruct(int messageId) {
                    }
                }, "GifPreviewActivity");
            } else {
                holder.mCountDownView.setVisibility(View.GONE);
            }

        }

        public GifPreviewActivity.ImageInfo getImageInfo(int position) {
            return (GifPreviewActivity.ImageInfo)this.mImageList.get(position);
        }

        public class ViewHolder {
            ProgressBar progressBar;
            TextView progressText;
            ImageView photoView;
            TextView mCountDownView;

            public ViewHolder() {
            }
        }
    }
}
