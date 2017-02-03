package dk.sample.rest.bank.location.persistence;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import dk.nykredit.api.capabilities.Interval;

import dk.sample.rest.bank.location.model.Event;
import dk.sample.rest.bank.location.model.Location;
import dk.sample.rest.common.core.logging.LogDuration;

/**
 * Handles archiving (persistence) tasks for the location domain model.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class LocationArchivist {

    @PersistenceContext(unitName = "locationPersistenceUnit")
    private EntityManager em;

    @LogDuration(limit = 50)
    public List<Location> listLocations() {
        TypedQuery<Location> q = em.createQuery("select l from Location l", Location.class);
        return q.getResultList();
    }

    /**
     * Find customer by its semantic key. Note this will throw {@link NoResultException} which will roll back the
     * transaction if the customer is not found - if this is a problem consider using
     * {@link #findPosition(String, String)}.
     */
    @LogDuration(limit = 50)
    public Location getLocation(String latitude, String longitude) {
        TypedQuery<Location> q = em.createQuery("select l from Location l where l.location=:long and " +
        "l.latitude=:lat", Location.class);
        q.setParameter("lat", latitude);
        q.setParameter("long", longitude);
        return q.getSingleResult();
    }

    /**
     * Find customer by names. Note this will throw {@link NoResultException} which will roll back the
     * transaction if the customer is not found - if this is a problem consider using
     * {@link #findLocationByCoordinates(String, String)}.
     */
    @LogDuration(limit = 50)
    public Location findLocationByCoordinates(String latitude, String longitude) {
        TypedQuery<Location> q = em.createQuery("select l from Location l where l.location=:long and " +
                "l.latitude=:lat", Location.class);
        q.setParameter("long", longitude);
        q.setParameter("lat", latitude);
        return q.getSingleResult();
    }

    @LogDuration(limit = 50)
    public Optional<Location> findPosition(String latitude, String longitude) {
        try {
            return Optional.of(getLocation(latitude, longitude));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @LogDuration(limit = 50)
    public void save(Location location) {
        em.persist(location);
    }

    public List<Event> findEvents(Optional<Interval> withIn) {
        StringBuilder qs = new StringBuilder("select e from Event e");
        if (withIn.isPresent()) {
            qs.append(" and t.lastModifiedTime>:startsAt and t.lastModifiedTime<:endsAt");
        }
        TypedQuery<Event> q = em.createQuery(qs.toString(), Event.class);
        if (withIn.isPresent()) {
            Timestamp ts = Timestamp.from(withIn.get().getStart().toInstant());
            q.setParameter("startsAt", ts);
            Timestamp te = Timestamp.from(withIn.get().getEnd().toInstant());
            q.setParameter("endsAt", te);
        }
        return q.getResultList();
    }

    public List<Event> getEventsForCategory(String category, Optional<Interval> withIn) {
        StringBuilder qs = new StringBuilder("select e from Event e where e.category=:category");
        if (withIn.isPresent()) {
            qs.append(" and t.lastModifiedTime>:startsAt and t.lastModifiedTime<:endsAt");
        }
        TypedQuery<Event> q = em.createQuery(qs.toString(), Event.class);
        q.setParameter("category", category);
        if (withIn.isPresent()) {
            Interval intv = withIn.get();
            q.setParameter("startsAt", Timestamp.from(intv.getStart().toInstant()));
            q.setParameter("endsAt", Timestamp.from(intv.getEnd().toInstant()));
        }
        return q.getResultList();
    }

    public Event getEvent(String category, String id) {
        TypedQuery<Event> q = em.createQuery("select e from Event e where e.category=:category and e.id=:sid", Event.class);
        q.setParameter("category", category);
        q.setParameter("sid", id);
        return q.getResultList().get(0);
    }

    public void save(Event locationEvent) {
        try {
            em.persist(locationEvent);
        } catch (PersistenceException pe) {
            // in the example there is no check for collision in sequence, the check for time and sequence uniqueness.
            // This check must be done in a real life example in order to have choice for that.
            // The sequence is imho less attractive as a chronological order for the events.
            // The sequence is that is dependent on central persistence and thus is not very cloud capable
            // and is going to pose a challenge (taking it to the NP level) in a true distributed setup.
        }
    }

}
