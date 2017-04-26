package peterjasko.github;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import peterjasko.github.models.Repository;


public class RepoListAdapter extends RecyclerView.Adapter<RepoListAdapter.ViewHolder> {
    private List<Repository> repos;
    private Context context;

    public RepoListAdapter(Context c, List<Repository> repos) {
        this.repos = repos;
        this.context = c;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.repository_list_item, parent
                , false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Repository repo = repos.get(position);

        holder.reponame.setText(repo.getName());

        if( repo.getDescription() != null && !repo.getDescription().isEmpty()) {
            holder.description.setText(repo.getDescription());
        } else {
            holder.description.setVisibility(View.GONE);
        }
        holder.issueCount.setText(repo.getOpenIssuesCount() + " open issues");
        holder.reponame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context ,IssueActivity.class);
                i.putExtra("repoName", repo.getFullName());
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return repos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.repo_name) TextView reponame;
        @BindView(R.id.open_issue_count) TextView issueCount;
        @BindView(R.id.repo_description) TextView description;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

