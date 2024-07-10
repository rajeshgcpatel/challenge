package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountIdNotFoundException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsService;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {

    @MockBean
    private AccountsRepository accountsRepository;

    @Autowired
    private AccountsService accountService;

    @Test
    void testCreateAccount() {
        Account account = new Account("1", new BigDecimal("100.00"));
        accountService.createAccount(account);
        verify(accountsRepository, times(1)).save(account);
    }

    @Test
    public void testGetAccount_ValidId() {
        String accountId = "12345";
        Account mockAccount = new Account(accountId, new BigDecimal("100.00"));
        Mockito.when(accountsRepository.getAccount(accountId)).thenReturn(mockAccount);

        Account account = accountService.getAccount(accountId);

        Assertions.assertNotNull(account);
        Assertions.assertEquals(accountId, account.getAccountId());
    }

    @Test
    public void testGetAccount_InvalidId() {
        // Arrange
        String accountId = "44444";
        Account mockAccount = new Account(accountId, new BigDecimal("100.00"));
        Mockito.when(accountsRepository.getAccount(accountId)).thenReturn(mockAccount);

        // Act & Assert
        Assertions.assertThrows(AccountNotFoundException.class, () -> {
            accountService.getAccount(accountId);
        });
    }

    @Test
    public void testTransferMoney() throws AccountIdNotFoundException {
        String accountFromId = "123";
        String accountToId = "456";
        BigDecimal amount = new BigDecimal("100.00");

        Account accountFrom = new Account(accountFromId, new BigDecimal("500.00"));
        Account accountTo = new Account(accountToId, new BigDecimal("200.00"));

        when(accountsRepository.getAccount(accountFromId)).thenReturn(accountFrom);
        when(accountsRepository.getAccount(accountToId)).thenReturn(accountTo);

        accountService.transferMoney(accountFromId, accountToId, amount);

        assertEquals(new BigDecimal("400.00"), accountFrom.getBalance());
        assertEquals(new BigDecimal("300.00"), accountTo.getBalance());

        verify(accountsRepository, times(1)).save(accountFrom);
        verify(accountsRepository, times(1)).save(accountTo);
    }

}
