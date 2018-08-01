/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.bits.bic.server;

import com.oracle.bits.bic.common.exception.AccountInactiveException;
import com.oracle.bits.bic.common.exception.AccountLockedException;
import com.oracle.bits.bic.common.exception.EntityNotFoundException;
import com.oracle.bits.bic.common.exception.InvalidCredentialsException;
import com.oracle.bits.bic.common.exception.ValidationException;
import com.oracle.bits.bic.converter.Converter;
import com.oracle.bits.bic.domain.AccountEntity;
import com.oracle.bits.bic.domain.PersonEntity;
import com.oracle.bits.bic.domain.RoleEntity;
import com.oracle.bits.bic.em.util.EntityUtil;
import com.oracle.bits.bic.to.Credential;
import com.oracle.bits.bic.to.UserTO;
import com.oracle.bits.bic.util.ProjectConstants;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.apache.log4j.Logger;

/**
 *
 * @author Parashar Joshi<parashar.joshi@oracle.com>
 */
public class LoginServiceBean {

    private static final Logger LOG = Logger.getLogger("LoginServiceBean");

    private boolean validateAccount(AccountEntity accountEntity) throws AccountInactiveException, AccountLockedException, EntityNotFoundException {
        try {
            if (!accountEntity.getActive()) {
                throw new AccountInactiveException();
            }
            else if (accountEntity.getLocked()) {
                throw new AccountLockedException();
            }
            return true;
        }
        catch (NoResultException e) {
            throw new EntityNotFoundException();
        }
    }

    private AccountEntity getAccountByCreds(Credential cred) throws EntityNotFoundException {
        if (cred != null) {
            EntityManager entityManager = EntityUtil.getNewEntityManager();
            Query query = entityManager.createQuery("select e from " + AccountEntity.class
                    .getSimpleName()
                    + " e where e.userName = :userName and e.password = :password");
            query.setParameter("userName", cred.getUserName());
            query.setParameter("password", cred.getPassword());
            try {
                return (AccountEntity) query.getSingleResult();
            }
            catch (NoResultException e) {
                throw new EntityNotFoundException();
            }
        }
        else {
            throw new EntityNotFoundException();
        }
    }
    
    public PersonEntity getPersonByUsername(String username) throws EntityNotFoundException {
        if (username != null) {
            EntityManager entityManager = EntityUtil.getNewEntityManager();
            Query query = entityManager.createQuery("select e from " + PersonEntity.class
                    .getSimpleName()
                    + " e where e.userName = :userName");
            query.setParameter("userName", username);
            try {
                return (PersonEntity) query.getSingleResult();
            }
            catch (NoResultException e) {
                throw new EntityNotFoundException();
            }
        }
        else {
            throw new EntityNotFoundException();
        }
    }

    private boolean isUserNameAvailable(String userName) throws ValidationException {
        if (userName != null) {
            EntityManager entityManager = EntityUtil.getNewEntityManager();
            Query query = entityManager.createQuery("select e from " + AccountEntity.class
                    .getSimpleName()
                    + " e where e.userName = :userName");
            query.setParameter("userName", userName);
            try {
                AccountEntity accentity = (AccountEntity) query.getSingleResult();
                return false;
            }
            catch (NoResultException e) {
                return true;
            }
        }
        else {
            throw new ValidationException();
        }
    }

    private AccountEntity createAccountPerson(UserTO user) throws EntityNotFoundException, ValidationException {
        if (user != null) {
            EntityManager entityManager = EntityUtil.getNewEntityManager();
            AccountEntity accEntity = new AccountEntity();
            PersonEntity perEntity = new PersonEntity();
            Date date = new Date();
            accEntity.setPassword(user.getPassword());
            accEntity.setModificationDate(date);
            accEntity.setCreationDate(date);
            accEntity.setUserName(user.getUserName());
            perEntity.setEmailId(user.getEmailId());
            perEntity.setCreationDate(date);
            perEntity.setModificationDate(date);
            perEntity.setFirstName(user.getFirstName());
            perEntity.setLastName(user.getLastName());
            perEntity.setRole(getRoleByRoleKey(ProjectConstants.USER_ROLE));
            perEntity.setUserName(user.getUserName());
            accEntity.setUser(perEntity);
            entityManager.getTransaction().begin();
            try {
                entityManager.persist(accEntity);
                entityManager.getTransaction().commit();
                System.out.println("Person persisted is " + perEntity.getId());
                System.out.println("Account persisted is " + accEntity.getId());
                //user.setId(accEntity.getId());
            }
            catch (NoResultException e) {
                entityManager.getTransaction().rollback();
                throw e;
            }
            return accEntity;
        }
        else {
            throw new ValidationException("Invalid/No data passed");
        }
    }

    private RoleEntity getRoleByRoleKey(String roleKey) throws EntityNotFoundException {
        if (roleKey != null) {
            EntityManager entityManager = EntityUtil.getNewEntityManager();
            Query query = entityManager.createQuery("select e from " + RoleEntity.class
                    .getSimpleName()
                    + " e where e.roleKey = :roleKey");
            query.setParameter("roleKey", roleKey);
            try {
                return (RoleEntity) query.getSingleResult();
            }
            catch (NoResultException e) {
                LOG.error("No result exception.", e);
                throw new EntityNotFoundException();
            }
        }
        else {
            throw new EntityNotFoundException();
        }
    }

    /**
     * Method to perform login action which involves validation of account and
     * credentials.
     *
     * @param cred
     * @return
     * @throws InvalidCredentialsException
     * @throws AccountLockedException
     * @throws AccountInactiveException
     */
    public UserTO performLoginAction(Credential cred) {
        UserTO user = new UserTO();
        if (cred != null) {
            try {
                AccountEntity accEnt = getAccountByCreds(cred);
                validateAccount(accEnt);
                Converter.convertAccountEntityToUserTO(accEnt, user);
                System.out.println("Returning User...");
            }
            catch (AccountInactiveException ex) {
                LOG.error("Entity not found.", ex);
                user.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.ACCOUNT_INACTIVE_ERROR_CODE, ProjectConstants.ACCOUNT_INACTIVE_ERROR_MSG));
            }
            catch (AccountLockedException ex) {
                LOG.error("Entity not found.", ex);
                user.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.ACCOUNT_LOCKED_ERROR_CODE, ProjectConstants.ACCOUNT_LOCKED_ERROR_MSG));
            }
            catch (EntityNotFoundException ex) {
                LOG.error("Entity not found.", ex);
                user.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.INVALID_CREDS_ERROR_CODE, ProjectConstants.INVALID_CREDS_ERROR_MSG));
            }
        }
        else {
            user.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.INVALID_CREDS_ERROR_CODE, ProjectConstants.INVALID_CREDS_ERROR_MSG));
        }
        return user;
    }

    public void signUp(UserTO user) {
        try {
            if (isUserNameAvailable(user.getUserName())) {
                AccountEntity accEnt = createAccountPerson(user);
                Converter.convertAccountEntityToUserTO(accEnt, user);
            }else{
               user.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.SIGNUP_USERNAME_TAKEN_ERROR_CODE, ProjectConstants.SIGNUP_USERNAME_TAKEN_ERROR_MSG)); 
            }
        }
        catch (EntityNotFoundException ex) {
            LOG.error("Entity not found.", ex);
            user.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.SIGNUP_FAILED_ERROR_CODE, ProjectConstants.SIGNUP_FAILED_ERROR_MSG));
        }
        catch (ValidationException ex) {
            LOG.error("Entity not found.", ex);
            user.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.SIGNUP_FAILED_ERROR_CODE, ProjectConstants.INVALID_CREDS_ERROR_MSG + "Error: " + ex.getMessage()));
        }
        catch (Exception ex) {
            LOG.error("Exception.", ex);
            user.setError(new com.oracle.bits.bic.to.Error(ProjectConstants.UNKNOWN_ERROR_CODE, ProjectConstants.UNKNOWN_ERROR_MSG + "Error: " + ex.getMessage()));
        }
    }
}
