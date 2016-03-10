package com.veyndan.generic.util;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.TypedValue;
import android.widget.ImageView;

public class UIUtils {
    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public static void grayscale(ImageView view) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        view.setColorFilter(new ColorMatrixColorFilter(matrix));
    }
}
