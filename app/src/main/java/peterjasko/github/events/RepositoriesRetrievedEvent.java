package peterjasko.github.events;

import peterjasko.github.models.Repository;


public class RepositoriesRetrievedEvent {
    private Repository[] repositories;

    public RepositoriesRetrievedEvent(Repository[] repositories) {
        this.repositories = repositories;
    }

    public Repository[] getRepositories() {
        return repositories;
    }
}
