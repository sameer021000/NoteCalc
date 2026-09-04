package com.example.notecalc.pdf;

import android.graphics.Color;
import android.graphics.Paint;
import com.example.notecalc.MainActivity;
import com.example.notecalc.R;
import com.example.notecalc.ThemeManager;

public class PdfThemeHelper {

    public static class PdfTheme {
        public final Paint bgPaint;
        public final Paint titlePaint;
        public final Paint subPaint;
        public final Paint accentPaint;
        public final Paint cellPaint;
        public final Paint cellMutedPaint;
        public final Paint dividerPaint;
        public final Paint rowEvenPaint;
        public final Paint rowOddPaint;
        public final Paint totalBgPaint;
        public final Paint totalTextPaint;
        public final Paint tableHeaderBgPaint;
        
        public final Paint appendixTitlePaint;
        public final Paint appendixSubPaint;
        
        public final int pageWidth = 595;
        public final int pageHeight = 842;
        public final int margin = 40;
        
        public PdfTheme(MainActivity activity) {
            bgPaint = new Paint();
            bgPaint.setColor(ThemeManager.getBgPrimaryColor(activity));
            
            titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            titlePaint.setColor(activity.getColor(R.color.text_primary));
            titlePaint.setTextSize(22f);
            titlePaint.setFakeBoldText(true);
            
            subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            subPaint.setColor(activity.getColor(R.color.text_tertiary));
            subPaint.setTextSize(11f);
            
            accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            accentPaint.setColor(ThemeManager.getSecondaryAccentColor(activity));
            accentPaint.setTextSize(11f);
            accentPaint.setFakeBoldText(true);
            
            cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            cellPaint.setColor(activity.getColor(R.color.text_primary));
            cellPaint.setTextSize(10f);
            
            cellMutedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            cellMutedPaint.setColor(activity.getColor(R.color.text_tertiary));
            cellMutedPaint.setTextSize(10f);
            
            dividerPaint = new Paint();
            dividerPaint.setColor(ThemeManager.getBorderColor(activity));
            dividerPaint.setStrokeWidth(0.8f);
            
            rowEvenPaint = new Paint();
            rowEvenPaint.setColor(ThemeManager.getBgSecondaryColor(activity));
            
            rowOddPaint = new Paint();
            rowOddPaint.setColor(ThemeManager.getBgTertiaryColor(activity));
            
            totalBgPaint = new Paint();
            totalBgPaint.setColor(ThemeManager.getPrimaryAccentColor(activity));
            
            totalTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            totalTextPaint.setColor(Color.WHITE);
            totalTextPaint.setTextSize(11f);
            totalTextPaint.setFakeBoldText(true);
            
            tableHeaderBgPaint = new Paint();
            tableHeaderBgPaint.setColor(ThemeManager.getBgSecondaryColor(activity));
            
            appendixTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            appendixTitlePaint.setColor(activity.getColor(R.color.text_primary));
            appendixTitlePaint.setTextSize(20f);
            appendixTitlePaint.setFakeBoldText(true);
            
            appendixSubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            appendixSubPaint.setColor(activity.getColor(R.color.text_tertiary));
            appendixSubPaint.setTextSize(12f);
        }
    }
}
