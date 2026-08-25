package com.example.notecalc;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ResponsiveUI {
    private static final float REFERENCE_WIDTH_DP = 360f; // Standard reference screen width
    private static float scaleFactor = -1f;

    // Get the dynamic scaling factor based on the physical screen width
    public static float getScaleFactor(Context context) {
        if (scaleFactor > 0) {
            return scaleFactor;
        }
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = metrics.widthPixels / metrics.density;
        // Proportional scale factor
        float factor = screenWidthDp / REFERENCE_WIDTH_DP;
        // Clamp scale factor to prevent extreme stretching on large devices/tablets
        scaleFactor = Math.max(0.85f, Math.min(factor, 1.35f));
        return scaleFactor;
    }

    // Scale a DP value into pixels based on the screen responsiveness factor
    public static int scalePx(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        float factor = getScaleFactor(context);
        return Math.round(dp * factor * density);
    }

    // Apply scaling recursively to a view hierarchy (margins, paddings, text sizes)
    public static void applyResponsiveness(View view) {
        if (view == null) return;
        Context context = view.getContext();
        float factor = getScaleFactor(context);

        // Scale Layout Parameters (Width, Height, Margins)
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
            marginParams.leftMargin = Math.round(marginParams.leftMargin * factor);
            marginParams.rightMargin = Math.round(marginParams.rightMargin * factor);
            marginParams.topMargin = Math.round(marginParams.topMargin * factor);
            marginParams.bottomMargin = Math.round(marginParams.bottomMargin * factor);

            if (marginParams.width > 0) {
                marginParams.width = Math.round(marginParams.width * factor);
            }
            if (marginParams.height > 0) {
                marginParams.height = Math.round(marginParams.height * factor);
            }
            view.setLayoutParams(marginParams);
        } else if (params != null) {
            if (params.width > 0) {
                params.width = Math.round(params.width * factor);
            }
            if (params.height > 0) {
                params.height = Math.round(params.height * factor);
            }
            view.setLayoutParams(params);
        }

        // Scale Padding
        int pLeft = Math.round(view.getPaddingLeft() * factor);
        int pTop = Math.round(view.getPaddingTop() * factor);
        int pRight = Math.round(view.getPaddingRight() * factor);
        int pBottom = Math.round(view.getPaddingBottom() * factor);
        view.setPadding(pLeft, pTop, pRight, pBottom);

        // Scale Text Size
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            float currentSizePx = textView.getTextSize();
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentSizePx * factor);
        }

        // Recurse down children
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyResponsiveness(vg.getChildAt(i));
            }
        }
    }

    // Dynamic Helper to build responsive rounded monochrome backgrounds programmatically
    public static GradientDrawable createRoundedBg(Context context, int bgColor, int strokeColor, float strokeWidthDp, float cornerRadiusDp) {
        float factor = getScaleFactor(context);
        float density = context.getResources().getDisplayMetrics().density;

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(cornerRadiusDp * factor * density);

        if (strokeColor != 0 && strokeWidthDp > 0) {
            int strokePx = Math.round(strokeWidthDp * factor * density);
            gd.setStroke(strokePx, strokeColor);
        }
        return gd;
    }

    // Dynamic Helper to build responsive rounded backgrounds with a ripple touch effect
    public static android.graphics.drawable.RippleDrawable createRippleRoundedBg(Context context, int bgColor, int strokeColor, float strokeWidthDp, float cornerRadiusDp) {
        GradientDrawable content = createRoundedBg(context, bgColor, strokeColor, strokeWidthDp, cornerRadiusDp);
        android.content.res.ColorStateList rippleColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#40888888"));
        return new android.graphics.drawable.RippleDrawable(rippleColor, content, content);
    }
}
