package peterjasko.github.events;

public class RefreshIssuesEvent {
    private boolean success = false;

    public RefreshIssuesEvent(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
