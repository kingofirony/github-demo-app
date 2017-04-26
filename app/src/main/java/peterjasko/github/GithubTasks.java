package peterjasko.github;

import android.os.AsyncTask;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import peterjasko.github.events.GetUserEvent;
import peterjasko.github.events.IssuesRetrievedEvent;
import peterjasko.github.events.RefreshIssuesEvent;
import peterjasko.github.events.RepositoriesRetrievedEvent;
import peterjasko.github.models.Issue;
import peterjasko.github.models.Repository;
import peterjasko.github.models.User;

public class GithubTasks {

    private final OkHttpClient client;

    public GithubTasks(final String username, final String password) {
        client = new OkHttpClient.Builder()
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        if (response.request().header("Authorization") != null) {
                            return null;
                        }

                        String credential = Credentials.basic(username, password);
                        return response.request().newBuilder()
                                .header("Authorization", credential)
                                .build();
                    }
                }).addInterceptor(new BasicAuthInterceptor(username, password))
                .build();
    }

    public void getUser() {
        GetUserTask getUser = new GetUserTask();
        getUser.execute();
    }


    public void getRepos() {
        GetRepositories getRepositories = new GetRepositories();
        getRepositories.execute();
    }

    public void getIssues(String repoName) {
        GetIssues getIssues = new GetIssues();
        getIssues.execute(repoName);
    }

    public void putIssue(String repoName, String title, String body) {
        PutIssue putIssue = new PutIssue();
        putIssue.execute(repoName, title, body);
    }

    public void editIssue(String repoName, String title, String body, String num) {
        EditIssue editIssue = new EditIssue();
        editIssue.execute(repoName, title, body, num);
    }

    public void updateIssueState(String repoName, Boolean open, String num) {
        String state = open ? "open" : "closed";
        UpdateIssueState updateIssueState = new UpdateIssueState();
        updateIssueState.execute(repoName, state, num);
    }

    private class GetUserTask extends AsyncTask<Void, Integer, User> {
        protected User doInBackground(Void... urls) {
            Request request = new Request.Builder()
                    .url("https://api.github.com/user")
                    .build();
            User u;
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                Gson gson = new Gson();
                String r = response.body().string();
                u = gson.fromJson(r,User.class);
            } catch (IOException e) {
                u = new User();
                e.printStackTrace();
            }
            return u;

        }

        protected void onPostExecute(User u) {
            EventBus.getInstance().post(new GetUserEvent(u));
        }
    }

    private class GetRepositories extends AsyncTask<Void, Integer, Repository[]> {
        protected Repository[] doInBackground(Void... urls) {
            Request request = new Request.Builder()
                    .url("https://api.github.com/user/repos")
                    .build();
            Repository[] repositories;
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                Gson gson = new Gson();
                String r = response.body().string();
                repositories = gson.fromJson(r,Repository[].class);
            } catch (IOException e) {
                repositories = new Repository[0];
            }
            return repositories;
        }

        protected void onPostExecute(Repository[] repositories) {
            EventBus.getInstance().post(new RepositoriesRetrievedEvent(repositories));
        }
    }

    private class GetIssues extends AsyncTask<String, Integer, Issue[]> {
        protected Issue[] doInBackground(String... repoName) {
            Request request = new Request.Builder()
                    .url("https://api.github.com/repos/"+repoName[0] +"/issues?state=all")
                    .build();
            Issue[] issues;

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                Gson gson = new Gson();
                String r = response.body().string();
                issues = gson.fromJson(r,Issue[].class);
            } catch (IOException e) {
                issues = new Issue[0];
                e.printStackTrace();
            }
            return issues;
        }

        protected void onPostExecute(Issue[] issues) {
                EventBus.getInstance().post(new IssuesRetrievedEvent(issues));
        }
    }


    private class PutIssue extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... issue) {
            String repoName = issue[0];
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject obj = new JSONObject();
            try {
                obj.put("body", issue[2]);
                obj.put("title", issue[1]);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestBody requestBody = RequestBody.create(JSON,obj.toString());

            Request request = new Request.Builder()
                    .url("https://api.github.com/repos/"+issue[0]+"/issues")
                    .post(requestBody)
                    .build();

            boolean success;
            try (Response response = client.newCall(request).execute()) {
                success = response.isSuccessful();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            } catch (IOException e) {
                return false;
            }
            return success;
        }

        protected void onPostExecute(Boolean success) {
            EventBus.getInstance().post(new RefreshIssuesEvent(success));
        }
    }

    private class EditIssue extends AsyncTask<String, Void, Boolean> {
        String repoName = "";
        protected Boolean doInBackground(String... issue) {
            repoName = issue[0];
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject obj = new JSONObject();
            try {
                obj.put("body", issue[2]);
                obj.put("title", issue[1]);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestBody requestBody = RequestBody.create(JSON,obj.toString());

            Request request = new Request.Builder()
                    .url("https://api.github.com/repos/"+issue[0]+"/issues/" +issue[3])
                    .post(requestBody)
                    .build();
            Issue[] issues;
            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            } catch (IOException e) {
                issues = null;
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean success) {
                EventBus.getInstance().post(new RefreshIssuesEvent(success));
            }

    }

    private class UpdateIssueState extends AsyncTask<String, Void, Boolean> {
        String repoName = "";
        protected Boolean doInBackground(String... issue) {
            repoName = issue[0];
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject obj = new JSONObject();
            try {
                obj.put("state", issue[1]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestBody requestBody = RequestBody.create(JSON,obj.toString());

            Request request = new Request.Builder()
                    .url("https://api.github.com/repos/"+issue[0]+"/issues/" +issue[2])
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean success) {
            EventBus.getInstance().post(new RefreshIssuesEvent(success));
        }
    }

    public class BasicAuthInterceptor implements Interceptor {

        private String credentials;

        public BasicAuthInterceptor(String user, String password) {
            this.credentials = Credentials.basic(user, password);
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Request authenticatedRequest = request.newBuilder()
                    .header("Authorization", credentials).build();
            return chain.proceed(authenticatedRequest);
        }

    }
}
