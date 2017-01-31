package com.odde.bbuddy.account;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odde.bbuddy.authentication.AuthenticationToken;
import com.odde.bbuddy.common.Consumer;
import com.odde.bbuddy.common.JsonBackend;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AllAccountsTest {

    JsonBackend mockJsonBackend = mock(JsonBackend.class);
    AuthenticationToken mockAuthenticationToken = mock(AuthenticationToken.class);
    Map<String, String> authenticationHeaders = new HashMap<>();
    Map<String, String> responseHeaders = new HashMap<>();
    Accounts accounts = new Accounts(mockJsonBackend, mockAuthenticationToken);
    Consumer mockConsumer = mock(Consumer.class);

    @Before
    public void given_authentication_headers() {
        when(mockAuthenticationToken.getHeaders()).thenReturn(authenticationHeaders);
    }

    @Test
    public void all_accounts_with_authentication_headers() {
        processAllAccounts();

        verify(mockJsonBackend).getRequestForJsonArray(eq("/accounts"), eq(authenticationHeaders), any(Consumer.class), any(Consumer.class));
    }

    @Test
    public void all_accounts_with_some_data_from_backend() {
        given_backend_return_json_with_account(new Account("name", 1000));

        processAllAccounts();

        verifyAccountConsumed("name", 1000);
    }

    @Test
    public void all_accounts_will_update_authentication_headers() {
        given_backend_will_return(responseHeaders());

        processAllAccounts();

        verify(mockAuthenticationToken).updateByHeaders(responseHeaders);
    }

    @NonNull
    private Answer responseHeaders() {
        return new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Consumer consumer = invocation.getArgument(3);
                consumer.accept(responseHeaders);
                return null;
            }
        };
    }

    private void verifyAccountConsumed(String expectedName, int expectedBalanceBroughtForward) {
        ArgumentCaptor<List<Account>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockConsumer).accept(captor.capture());
        assertEquals(expectedName, captor.getValue().get(0).getName());
        assertEquals(expectedBalanceBroughtForward, captor.getValue().get(0).getBalanceBroughtForward());
    }

    private void given_backend_return_json_with_account(final Account account) {
        given_backend_will_return(jsonArrayOf(account));
    }

    private void given_backend_will_return(Answer answer) {
        doAnswer(answer).when(mockJsonBackend).getRequestForJsonArray(anyString(), ArgumentMatchers.<String, String>anyMap(), any(Consumer.class), any(Consumer.class));
    }

    @NonNull
    private Answer jsonArrayOf(final Account account) {
        return new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Consumer consumer = invocation.getArgument(2);
                consumer.accept(new JSONArray(new ObjectMapper().writeValueAsString(asList(account))));
                return null;
            }
        };
    }

    private void processAllAccounts() {
        accounts.processAllAccounts(mockConsumer);
    }

}