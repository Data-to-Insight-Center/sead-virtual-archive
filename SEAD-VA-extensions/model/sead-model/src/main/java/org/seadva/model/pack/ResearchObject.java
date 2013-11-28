package org.seadva.model.pack;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.support.Assertion;
import org.seadva.model.SeadRepository;

import java.util.HashSet;
import java.util.Set;

/**
 * SEAD Virtual Archive Data Model
 */
public class ResearchObject extends Dcp {
    public ResearchObject(ResearchObject tocopy){
        super(tocopy);
        this.repositories = tocopy.repositories;
    }
    public ResearchObject(){}
    private Set<SeadRepository> repositories = new HashSet<SeadRepository>();

    public Set<SeadRepository> getRepositories() {
        final Set<SeadRepository> repositories = new HashSet<SeadRepository>(this.repositories.size());
        for (SeadRepository repo : this.repositories) {
            repositories.add(new SeadRepository(repo));
        }
        return repositories;
    }

    public void setRepositories(Set<SeadRepository> repositories) {
        Assertion.notNull(repositories);
        this.repositories = new HashSet<SeadRepository>(repositories.size());
        for ( SeadRepository repo : repositories ) {
            Assertion.notNull(repo);
            this.repositories.add(repo);
        }
    }

    public void addRepository(SeadRepository... repositories) {
        Assertion.notNull(repositories);
        for (SeadRepository repo : repositories) {
            Assertion.notNull(repo);
            this.repositories.add(repo);
        }
    }
}
