package quarkus;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.print.attribute.standard.Media;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("accounts")
public class AccountResource {

    @Inject
    EntityManager entityManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Account> allAccounts() {
        return entityManager.createNamedQuery("Accounts.findAll", Account.class).getResultList();
    }

    @Path("{accountNumber}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Account getAccount(@PathParam("accountNumber") Long accountNumber) {
        try {
            return entityManager.createNamedQuery("Accounts.findByAccountNumber", Account.class).
                    setParameter("accountNumber", accountNumber).getSingleResult();
        } catch (NoResultException e) {
            throw new WebApplicationException("Account with id of " +
                    accountNumber + " does not exist.", 404);
        }
    }

    @Transactional
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAccount(Account account) {
        if (account.getAccountNumber() == null) {
            throw new WebApplicationException("Account number not specified", 400);
        }
        entityManager.persist(account);
        return Response.created(UriBuilder.fromResource(AccountResource.class).
                        path(account.getAccountNumber().toString()).
                        build()).
                entity(account).
                build();
    }

    @Transactional
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{accountNumber}/withdrawal/{amount}")
    public Account withdrawal(@PathParam("accountNumber") final Long accountNumber,
                              @PathParam("amount") final String amount) {
        Account account = getAccount(accountNumber);
        account.withdrawFunds(new BigDecimal(amount));
        return account;
    }

    @Transactional
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{accountNumber}/deposit/{amount}")
    public Account deposit(@PathParam("accountNumber") final Long accountNumber,
                           @PathParam("amount") final String amount) {
        Account account = getAccount(accountNumber);
        account.addFunds(new BigDecimal(amount));
        return account;
    }

    @Transactional
    @DELETE
    @Path("{accountNumber}")
    public Response closeAccount(@PathParam("accountNumber") final Long accountNumber) {
        Account account = getAccount(accountNumber);
        entityManager.remove(account);
        return Response.noContent().build();
    }

    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Override
        public Response toResponse(Exception e) {
            int code = 500;

            if (e instanceof WebApplicationException) {
                code = ((WebApplicationException) e).getResponse().getStatus();
            }

            JsonObjectBuilder entityBuilder = Json.createObjectBuilder()
                    .add("exceptionType", e.getClass().getName())
                    .add("code", code);

            if (e.getMessage() != null) {
                entityBuilder.add("error", e.getMessage());
            }

            return Response.status(code).entity(entityBuilder.build()).build();
        }
    }

}
