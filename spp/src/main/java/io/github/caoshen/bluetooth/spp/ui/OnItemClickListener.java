package io.github.caoshen.bluetooth.spp.ui;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @author caoshen
 * @date 2020/12/15
 */
interface OnItemClickListener<T> {
    void onItemClick(RecyclerView parent, View view, int position, T data);
}
