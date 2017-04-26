package peterjasko.github.events;

import peterjasko.github.models.User;


public class GetUserEvent {
    private User user;

    public GetUserEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
