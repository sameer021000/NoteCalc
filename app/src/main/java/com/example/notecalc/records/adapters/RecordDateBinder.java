package com.example.notecalc.records.adapters;
import com.example.notecalc.core.utils.*;
import com.example.notecalc.records.models.Record;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordDateBinder {

    public static void bindDateInteraction(Record record, RecordsAdapter.RecordViewHolder holder) {
        if (holder.revertDateTask != null) {
            holder.tvDate.removeCallbacks(holder.revertDateTask);
            holder.revertDateTask = null;
        }
        holder.isShowingDay = false;
        holder.tvDate.setText(DateUtils.formatDateCompact(record.getDate()));

        holder.tvDate.setOnClickListener(v -> {
            if (holder.isShowingDay) {
                if (holder.revertDateTask != null) {
                    holder.tvDate.removeCallbacks(holder.revertDateTask);
                    holder.revertDateTask = null;
                }
                holder.isShowingDay = false;
                holder.tvDate.setText(DateUtils.formatDateCompact(record.getDate()));
            } else {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    Date d = sdf.parse(record.getDate());
                    if (d != null) {
                        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                        holder.tvDate.setText(dayFormat.format(d));
                        holder.isShowingDay = true;

                        if (holder.revertDateTask != null) {
                            holder.tvDate.removeCallbacks(holder.revertDateTask);
                        }
                        holder.revertDateTask = () -> {
                            holder.isShowingDay = false;
                            holder.tvDate.setText(DateUtils.formatDateCompact(record.getDate()));
                            holder.revertDateTask = null;
                        };
                        holder.tvDate.postDelayed(holder.revertDateTask, 5000);
                    }
                } catch (Exception e) {
                    android.util.Log.e("NoteCalc", "Error resetting date", e);
                }
            }
        });
    }
}