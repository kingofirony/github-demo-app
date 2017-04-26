package peterjasko.github;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import peterjasko.github.events.EditIssueStateEvent;
import peterjasko.github.events.EditLaunchEvent;
import peterjasko.github.models.Issue;


public class IssueListAdapter extends RecyclerView.Adapter<IssueListAdapter.ViewHolder> {
    private List<Issue> issues;
    private Context context;
    private String currentUser;

    public IssueListAdapter(Context c, List<Issue> issues, String currentUser) {
        this.issues = issues;
        this.context = c;
        this.currentUser = currentUser;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.issue_list_item, parent
                , false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Issue issue = issues.get(position);
        final boolean open;
        final String issueNum = issue.getNumber().toString();

        holder.username.setText(issue.getUser().getLogin());
        holder.issueTitle.setText(issue.getTitle());
        holder.issueText.setText(issue.getBody());
        Picasso.with(context).load(issue.getUser().getAvatarUrl()).resize(150,150).into(holder.userImage);

        if(issue.getState().equals("open")) {
            holder.statusButton.setBackgroundColor(context.getResources().getColor(R.color.green));
            holder.statusButton.setText("Open");
            open = true;
        } else {
            holder.statusButton.setBackgroundColor(context.getResources().getColor(R.color.red));
            holder.statusButton.setText("Closed");
            open = false;
        }

        if(!issue.getUser().getLogin().equals(currentUser)) {
            holder.editButton.setVisibility(View.GONE);
        } else {
            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getInstance().post(new EditLaunchEvent(issue.getNumber()+ "",issue.getTitle(),issue.getBody()));
                }
            });
        }
        holder.statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getInstance().post(new EditIssueStateEvent(!open,issueNum));
            }
        });
    }

    @Override
    public int getItemCount() {
        return issues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.issue_username) TextView username;
        @BindView(R.id.issue_title) TextView issueTitle;
        @BindView(R.id.issue_text) TextView issueText;
        @BindView(R.id.edit_button) Button editButton;
        @BindView(R.id.status_button) Button statusButton;
        @BindView(R.id.user_image)
        ImageView userImage;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

