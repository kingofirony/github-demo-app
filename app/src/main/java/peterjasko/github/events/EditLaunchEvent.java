package peterjasko.github.events;

public class EditLaunchEvent {
    private String num ,title, body;

    public EditLaunchEvent(String num, String title, String body) {
        this.num = num;
        this.title = title;
        this.body = body;
    }

    public String getNum() {
        return num;
    }
    public String getBody() {
        return body;
    }
    public String getTitle() {
        return title;
    }

}
