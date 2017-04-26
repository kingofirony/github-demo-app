package peterjasko.github.events;

public class EditIssueStateEvent {
    private boolean state;
    private String num;

    public EditIssueStateEvent(boolean state, String num) {
        this.state = state;
        this.num = num;
    }

    public boolean getState() {
        return state;
    }
    public String getNum() {
        return num;
    }

}
