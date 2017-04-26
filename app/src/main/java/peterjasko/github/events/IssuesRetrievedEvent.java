package peterjasko.github.events;

import peterjasko.github.models.Issue;


public class IssuesRetrievedEvent {
    private Issue[] issues;

    public IssuesRetrievedEvent(Issue[] issues) {
        this.issues = issues;
    }

    public Issue[] getIssues() {
        return issues;
    }
}
