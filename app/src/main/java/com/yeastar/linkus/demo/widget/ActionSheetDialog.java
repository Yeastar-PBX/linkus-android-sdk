package com.yeastar.linkus.demo.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;


import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuqiang on 10/23/15.
 */
public class ActionSheetDialog {

    private Context context;
    private Display display;
    private AlertDialog dialog;
    private TextView tvTitle;
    private View titleLine;
    private LinearLayout lLayoutContent;
    private ScrollView sLayoutContent;
    private List<SheetItem> sheetItemList;

    public ActionSheetDialog(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            display = windowManager.getDefaultDisplay();
        }
    }

    public ActionSheetDialog builder() {
        // 获取Dialog布局
        View view = LayoutInflater.from(context).inflate(R.layout.layout_actionsheet, null);
        // 获取自定义Dialog布局中的控件
        sLayoutContent = view.findViewById(R.id.sLayout_content);
        lLayoutContent = view.findViewById(R.id.lLayout_content);
        tvTitle = view.findViewById(R.id.txt_title);
        titleLine = view.findViewById(R.id.title_line);
        TextView txt_cancel = view.findViewById(R.id.txt_cancel);
        txt_cancel.setOnClickListener(v -> dialog.dismiss());
        // 定义Dialog布局和参数
        dialog = new AlertDialog.Builder(context).create();
        dialog.setView(view, 0, 0, 0, 0);
        Window dialogWindow = dialog.getWindow();
        if(dialogWindow != null) {
            dialogWindow.setGravity(Gravity.CENTER_VERTICAL);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.x = 0;
            lp.y = 0;
            dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialogWindow.setAttributes(lp);
        }
        return this;
    }

    public int getSheetItemCount() {
        return sheetItemList == null ? 0 : sheetItemList.size();
    }

    public ActionSheetDialog setTitle(String title) {
        tvTitle.setVisibility(View.VISIBLE);
        titleLine.setVisibility(View.VISIBLE);
        tvTitle.setText(title);
        return this;
    }

    public ActionSheetDialog setTitle(int titleResId) {
        String title = context.getString(titleResId);
        return setTitle(title);
    }

    public ActionSheetDialog setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);
        return this;
    }

    public ActionSheetDialog setCanceledOnTouchOutside(boolean cancel) {
        dialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    /**
     * @param tag item名
     * @param tagColor item颜色
     * @param tagResId item左边图标
     * @param rightRedId item右边图标
     * @param l1 item点击监听
     * @param l2 item长按监听
     * @param l3 item左右子控件监听
     * @return 本身
     */
    private ActionSheetDialog addSheetItem(String tag, SheetItemColor tagColor, int tagResId, int rightRedId, OnItemClickListener l1, OnItemLongClickListener l2, OnItemChildClickListener l3) {
        if (sheetItemList == null) {
            sheetItemList = new ArrayList<>();
        }
        sheetItemList.add(new SheetItem(tag, tagColor, tagResId, rightRedId, l1, l2, l3));
        return this;
    }

    public ActionSheetDialog addSheetItem(int intTag, SheetItemColor color, OnItemClickListener listener) {
        return addSheetItem(intTag, color, listener, null);
    }

    public void addSheetItem(String tag, SheetItemColor color, int rightResId, OnItemClickListener listener) {
        addSheetItem(tag, color, 0, rightResId, listener, null, null);
    }

    public void addSheetItem(String tag, SheetItemColor color, int rightResId, OnItemChildClickListener listener) {
        addSheetItem(tag, color, 0, rightResId, null, null, listener);
    }

    public ActionSheetDialog addSheetItem(String tag, SheetItemColor color, OnItemClickListener l1, OnItemLongClickListener l2) {
        return addSheetItem(tag, color, 0, 0, l1, l2, null);
    }

    public ActionSheetDialog addSheetItem(int tag, int tagResId, int rightResId, OnItemClickListener l1, OnItemLongClickListener l2) {
        return addSheetItem(context.getString(tag), null, tagResId, rightResId, l1, l2, null);
    }

    public ActionSheetDialog addSheetItem(int intTag, SheetItemColor color, OnItemClickListener l1, OnItemLongClickListener l2) {
        String strItem = context.getString(intTag);
        return addSheetItem(strItem, color, l1, l2);
    }

    /**
     * 设置条目布局
     */
    private void setSheetItems() {
        if (sheetItemList == null || sheetItemList.size() <= 0) {
            return;
        }
        int size = sheetItemList.size();
        // 添加条目过多的时候控制高度
        if (size >= 8) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) sLayoutContent.getLayoutParams();
            params.height = DensityUtil.dp2px(context, 444);
            sLayoutContent.setLayoutParams(params);
        }
        // 循环添加条目
        for (int i = 1; i <= size; i++) {
            final int index = i;
            SheetItem sheetItem = sheetItemList.get(i - 1);
            String strItem = sheetItem.name;
            int tagResId = sheetItem.tagResId;
            int rightResId = sheetItem.rightResId;
            SheetItemColor color = sheetItem.color;
            final OnItemClickListener clickListener = sheetItem.itemClickListener;
            final OnItemLongClickListener longClickListener = sheetItem.itemOnLongClickListener;
            final OnItemChildClickListener childClickListener = sheetItem.itemChildClickListener;
            View inflate = LayoutInflater.from(context).inflate(R.layout.item_sheet_dialog, null);
            LinearLayout content = inflate.findViewById(R.id.ll_content);
            TextView textView = inflate.findViewById(R.id.textView);
            content.setOrientation(LinearLayout.HORIZONTAL);
            textView.setText(strItem);
            textView.setTextSize(16);
            textView.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
            ImageView ivTag = inflate.findViewById(R.id.ivTag);
            ImageView ivSelect = inflate.findViewById(R.id.ivSelect);
            // 背景图片
            content.setBackgroundResource(R.drawable.actionsheet_middle_selector);
            // 字体颜色
            if (color == null) {
                textView.setTextColor(ContextCompat.getColor(context, R.color.text_title));
            } else {
                textView.setTextColor(ContextCompat.getColor(context, color.getName()));
            }
            // 高度
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            float scale = dm.density;
            int height = (int) (55 * scale + 0.5f);
            content.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
            content.setGravity(Gravity.CENTER);
            ivSelect.setImageResource(rightResId);
            if (tagResId == 0) {
                ivTag.setVisibility(View.GONE);
            } else {
                ivTag.setVisibility(View.VISIBLE);
                ivTag.setImageResource(tagResId);
            }
            // 点击事件
            content.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onClick(index);
                }
                dialog.dismiss();
            });
            //长按事件
            content.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onLongClick(index);
                }
                dialog.dismiss();
                return false;
            });
            //左侧点击监听
            if (childClickListener != null) {
                textView.setOnClickListener(v -> {
                        childClickListener.onLeftClick(index);
                        dialog.dismiss();
                });
            }
            //右侧点击监听
            if (childClickListener != null) {
                ivSelect.setOnClickListener(v -> {
                        childClickListener.onRightClick(index);
                        dialog.dismiss();
                });
            }
            TextView line = new TextView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (0.5 * scale));
            params.leftMargin = DensityUtil.dp2px(context,16);
//            params.rightMargin = DensityUtil.dp2px(context,26);
            line.setLayoutParams(params);
            line.setBackgroundColor(ContextCompat.getColor(context, R.color.separator_line_color));
            lLayoutContent.addView(inflate);
            if (size != 1 && i != size) {
                lLayoutContent.addView(line);
            }
        }
    }

    public void show() {
        setSheetItems();
        dialog.show();
        setWidth();
    }

    private void setWidth() {
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams attributes = window.getAttributes();
            Point size = new Point();
            display.getSize(size);
            attributes.width = (int) (size.x * 0.85);
            window.setAttributes(attributes);
        }
    }

    public interface OnItemClickListener {
        void onClick(int which);
    }

    public interface OnItemLongClickListener {
        void onLongClick(int which);
    }

    public interface OnItemChildClickListener {
        void onLeftClick(int which);
        void onRightClick(int which);
    }

    public static class SheetItem implements Comparable<SheetItem> {
        String name;
        int tagResId;//左边图标资源文件
        int rightResId;//右边图标资源文件
        OnItemClickListener itemClickListener;
        OnItemLongClickListener itemOnLongClickListener;
        OnItemChildClickListener itemChildClickListener;
        SheetItemColor color;

        SheetItem(String tag, SheetItemColor color, int tagResId, int rightResId, OnItemClickListener l1, OnItemLongClickListener l2, OnItemChildClickListener l3) {
            this.name = tag;
            this.color = color;
            this.tagResId = tagResId;
            this.rightResId = rightResId;
            this.itemClickListener = l1;
            this.itemOnLongClickListener = l2;
            this.itemChildClickListener = l3;
        }

        @Override
        public int compareTo(@NonNull SheetItem o) {
            return o.name.length() - this.name.length();
        }
    }

    public enum SheetItemColor {

        Blue(R.color.blue), Red(R.color.red_1), Black(R.color.text_title);

        private int name;

        SheetItemColor(int name) {
            this.name = name;
        }

        public int getName() {
            return name;
        }
    }
}
