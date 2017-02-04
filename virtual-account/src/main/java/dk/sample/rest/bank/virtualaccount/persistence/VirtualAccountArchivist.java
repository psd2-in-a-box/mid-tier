package dk.sample.rest.bank.virtualaccount.persistence;

import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import dk.sample.rest.bank.virtualaccount.model.VirtualAccount;
import dk.sample.rest.common.core.logging.LogDuration;


/**
 * Handles archiving (persistence) tasks for the virtual account domain model.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class VirtualAccountArchivist {

    @PersistenceContext(unitName = "virtualAccountPersistenceUnit")
    private EntityManager em;

    @LogDuration(limit = 50)
    public List<VirtualAccount> listAccounts() {
        TypedQuery<VirtualAccount> q = em.createQuery("select va from VirtualAccount va", VirtualAccount.class);
        return q.getResultList();
    }

    /**
     * Find account by its semantic key. Note this will throw {@link NoResultException} which will roll back the
     * transaction if the customer is not found.
     */
    @LogDuration(limit = 50)
    public VirtualAccount getAccount(Long vaNumber) {
        TypedQuery<VirtualAccount> q = em.createQuery("select va from VirtualAccount va where va.vaNumber=:number",
                VirtualAccount.class);
        q.setParameter("number", vaNumber);
        return q.getSingleResult();
    }

    /**
     * Find account by names. Note this will throw {@link NoResultException} which will roll back the
     * transaction if the account is not found.
     */
    @LogDuration(limit = 50)
    public VirtualAccount getAccountByAccountNumber(Long vaNumber) {
        TypedQuery<VirtualAccount> q = em.createQuery("select va from VirtualAccount va where va.vaNumber=:number",
                VirtualAccount.class);
        q.setParameter("number", vaNumber);
        return q.getSingleResult();
    }

    public Optional<VirtualAccount> findAccountByAccountNumber(Long no) {
        try {
            return Optional.of(getAccount(no));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public void save(VirtualAccount ac) {
        em.persist(ac);
    }
}
