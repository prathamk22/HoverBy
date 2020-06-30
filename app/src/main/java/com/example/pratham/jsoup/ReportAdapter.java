package com.example.pratham.jsoup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
    Context context;

    String[] list = {"I just don't like it",
            "Nudity or pornography",
            "Hate speech or Symbols",
            "Violence or threat of violence",
            "Sale or Promotion",
            "Harassment or bullying",
            "Self injury",
            "Intellectual property violation"};
    String id;

    public ReportAdapter(Context context,String id ) {
        this.context = context;
        this.id = id;
    }

    @NonNull
    @Override
    public ReportAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.report_adapter,null,false);

        return new ReportAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final String report = "I want to report this post holding id:"+id +" as I find it as "+list[i]+".";
        viewHolder.items.setText(list[i]);
        viewHolder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToGMail(new String[]{"","prathamkhurana43@gmail.com"},"Report "+ id,report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView items;
        RelativeLayout parent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            items = itemView.findViewById(R.id.items);
            parent = itemView.findViewById(R.id.parent);
        }
    }

    public void shareToGMail(String[] email, String subject, String content) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, email);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
        final PackageManager pm = context.getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
        ResolveInfo best = null;
        for(final ResolveInfo info : matches)
            if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail"))
                best = info;
        if (best != null)
            emailIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(emailIntent);
    }
}
