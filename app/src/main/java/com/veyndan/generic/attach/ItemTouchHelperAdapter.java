package com.veyndan.generic.attach;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;

/**
 * Created by veyndan on 12/03/2016.
 */
public interface ItemTouchHelperAdapter {
    /**
     *
     */
    void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                     float dX, float dY, int actionState, boolean isCurrentlyActive);
}
