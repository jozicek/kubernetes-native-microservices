package quarkus;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Path("accounts")
public class AccountResource {

    Set<Account> accounts = new HashSet<>();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Account> allAccounts() {
        return accounts;
    }

    @Path("{accountNumber}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Account getAccount(@PathParam("accountNumber") Long accountNumber) {
        return accounts.stream().
                filter(account -> account.getAccountNumber().equals(accountNumber)).
                findFirst().orElseThrow(() -> new WebApplicationException("Account with id of " +
                        accountNumber + " does not exist.", 404));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)

    public Response createAccount(Account account) {
        if (account.getAccountNumber() == null) {
            throw new WebApplicationException("Account number not specified", 400);
        }
        accounts.add(account);
        return Response.created(UriBuilder.fromResource(AccountResource.class).
                        path(account.getAccountNumber().toString()).
                        build()).
                entity(account).
                build();
    }

    @PUT
    @Path("{accountNumber}/withdrawal")
    public Account withdrawal(@PathParam("accountNumber") final Long accountNumber, final String amount) {
        Account account = getAccount(accountNumber);
        account.withdrawFunds(new BigDecimal(amount));
        return account;
    }

    @PUT
    @Path("{accountNumber}/deposit")
    public Account deposit(@PathParam("accountNumber") final Long accountNumber, final String amount) {
        Account account = getAccount(accountNumber);
        account.addFunds(new BigDecimal(amount));
        return account;
    }

    @DELETE
    @Path("{accountNumber}")
    public Response closeAccount(@PathParam("accountNumber") final Long accountNumber) {
        Account account = getAccount(accountNumber);
        accounts.remove(account);
        return Response.noContent().build();
    }

    @PostConstruct
    public void setup() {
        accounts.add(new Account(123456789L, 987654321L,
                "George Baird", new BigDecimal("354.23")));
        accounts.add(new Account(111111111L, 2222222222L,
                "Mary Taylor", new BigDecimal("560.03")));
        accounts.add(new Account(555555555L, 4444444444L,
                "Diana Rig", new BigDecimal("422.00")));

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
