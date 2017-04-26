package peterjasko.github;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import peterjasko.github.events.EditIssueStateEvent;
import peterjasko.github.events.EditLaunchEvent;
import peterjasko.github.events.IssuesRetrievedEvent;
import peterjasko.github.events.RefreshIssuesEvent;
import peterjasko.github.models.Issue;

public class IssueActivity extends AppCompatActivity {
    ArrayList<Issue> issues;
    @BindView(R.id.issue_list)
    RecyclerView recyclerView;
    @BindView(R.id.fab)
    FloatingActionButton fabIcon;
    IssueListAdapter adapter;
    String repoName;
    GithubTasks tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);
        ButterKnife.bind(this);
        issues = new ArrayList<Issue>();
    }

    @Override
    protected void onStart() {
        EventBus.getInstance().register(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("login",Context.MODE_PRIVATE);
        String username = sharedPref.getString(("username"), null);
        String password = sharedPref.getString(("password"), null);

        if(username!= null && password!= null ) {
            try {
                tasks = new GithubTasks(username,password);
                tasks.getIssues(getIntent().getExtras().getString("repoName"));
            } catch (Exception e) {
            }
        }

        adapter = new IssueListAdapter(this, issues, username);
        recyclerView.setAdapter(adapter);
        repoName = getIntent().getExtras().getString("repoName");

        super.onStart();

    }

    @Override
    protected void onStop() {
        EventBus.getInstance().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onIssuesRetrievedEvent(IssuesRetrievedEvent event) {
        issues.clear();
        if(event.getIssues() != null || event.getIssues().length ==0) {
            for(Issue i: event.getIssues()) {
                issues.add(i);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onRefreshIssuesEvent(RefreshIssuesEvent event) {
        if(event.isSuccess()) {
            try {
                tasks.getIssues(repoName);
                Toast.makeText(IssueActivity.this,"Posted!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
            }
        } else {
            Toast.makeText(IssueActivity.this,"Error.Try to post again later.", Toast.LENGTH_SHORT).show();
        }
    }


    @Subscribe
    public void onEditIssueState(EditIssueStateEvent event) {
            try {
                tasks.updateIssueState(repoName,event.getState(),event.getNum());
            } catch (Exception e) {
                Toast.makeText(IssueActivity.this,"Error.Try again later.", Toast.LENGTH_SHORT).show();
            }
        }


    @Subscribe
    public void onEditLaunchEvent(EditLaunchEvent event) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(IssueActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.post_issue_dialog, null);
        ((EditText)dialogView.findViewById(R.id.titlePost)).setText(event.getTitle());
        ((EditText)dialogView.findViewById(R.id.bodyPost)).setText(event.getBody());
        final String eventNum  = event.getNum();

        builder.setView(dialogView)
                .setPositiveButton("Post", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String titlePost = ((EditText)dialogView.findViewById(R.id.titlePost)).getText().toString();
                        String bodyPost = ((EditText)dialogView.findViewById(R.id.bodyPost)).getText().toString();
                        tasks.editIssue(repoName,titlePost,bodyPost, eventNum);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @OnClick(R.id.fab)
    public void openPostDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(IssueActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.post_issue_dialog, null);
        builder.setView(dialogView)
                .setPositiveButton("Post", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String titlePost = ((EditText)dialogView.findViewById(R.id.titlePost)).getText().toString();
                        String bodyPost = ((EditText)dialogView.findViewById(R.id.bodyPost)).getText().toString();
                        tasks.putIssue(repoName,titlePost,bodyPost);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
