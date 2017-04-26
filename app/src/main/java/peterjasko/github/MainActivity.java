package peterjasko.github;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import peterjasko.github.events.GetUserEvent;
import peterjasko.github.events.RepositoriesRetrievedEvent;
import peterjasko.github.models.Repository;
import peterjasko.github.models.User;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.username)
    TextView userName;
    @BindView(R.id.user_image)
    ImageView userImage;
    @BindView(R.id.repo_list)
    RecyclerView recyclerView;
    @BindView(R.id.header_view)
    LinearLayout headerView;
    RepoListAdapter adapter;
    ArrayList<Repository> repositories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        repositories = new ArrayList<Repository>();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(!haveNetworkConnection()) {
            enableInternetDialog();
        }
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("login",Context.MODE_PRIVATE);
        String username = sharedPref.getString(("username"), null);
        String password = sharedPref.getString(("password"), null);
        repositories.clear();
        if(username!= null && password!= null ) {
            try {
                GithubTasks tasks = new GithubTasks(username, password);
                tasks.getUser();
                tasks.getRepos();
            } catch (Exception e) {
                Log.v("asdf",e.toString());
            }
        } else {
            launchLoginDialog();
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RepoListAdapter(this,repositories);
        recyclerView.setAdapter(adapter);

    }
    @Override
    protected void onStart() {
        EventBus.getInstance().register(this);
        super.onStart();

    }

    @Override
    protected void onStop() {
        EventBus.getInstance().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onRepositoriesRetrievedEvent(RepositoriesRetrievedEvent event) {
        for(Repository r: event.getRepositories()) {
            repositories.add(r);
        }
        adapter.notifyDataSetChanged();
    }

    public void launchLoginDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.login_dialog, null);

        builder.setView(dialogView)
                .setPositiveButton("Log In", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String username = ((EditText) dialogView.findViewById(R.id.username)).getText().toString();
                        String password = ((EditText) dialogView.findViewById(R.id.password)).getText().toString();
                        SharedPreferences sharedPref =  getApplicationContext().getSharedPreferences("login", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("username", username);
                        editor.putString("password", password);
                        editor.commit();
                        dialog.dismiss();
                        MainActivity.this.recreate();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Subscribe
    public void onGetUserEvent(GetUserEvent event) {
        User u = event.getUser();
        Callback callback = new Callback() {
            @Override
            public void onSuccess() {
                ViewGroup.LayoutParams params = headerView.getLayoutParams();
                params.height = headerView.getHeight();
                headerView.setLayoutParams(params);
                final int max = params.height;
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        ViewGroup.LayoutParams params = headerView.getLayoutParams();
                        if ((params.height - dy ) >= 0 && ((params.height - dy ) <= max + 50)) {
                            params.height = params.height - dy / 2;
                            headerView.setLayoutParams(params);
                        } else {
                            if (params.height != 0) {
                                params.height = params.height - 1;
                                headerView.setLayoutParams(params);
                            }
                        }

                    }
                });
            }

            @Override
            public void onError() {

            }
        };
        Picasso.with(this).load(u.getAvatarUrl()).into(userImage, callback);
        name.setText(u.getName());
        userName.setText(u.getLogin());
    }

    private void enableInternetDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Connect to Wifi to Use Places")
                .setCancelable(false)
                .setPositiveButton("Wifi Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private boolean haveNetworkConnection() {
        boolean connected = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    connected = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    connected = true;
        }
        return connected;
    }
}
