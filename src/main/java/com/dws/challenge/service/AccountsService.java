package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountIdNotFoundException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  private EmailNotificationService emailNotificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
      this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  // Lock the accounts to ensure thread safety by using @Transactional annotation
  @Transactional
  public void transferMoney(String accountFromId, String accountToId, BigDecimal amount) throws AccountIdNotFoundException {
    Account accountFrom = this.accountsRepository.getAccount(accountFromId);
    Account accountTo = this.accountsRepository.getAccount(accountToId);

    if (Objects.nonNull(accountFrom) && Objects.nonNull(accountTo)) {

      // Check if the source account has enough balance to transfer the money
      if (accountFrom.getBalance().compareTo(amount) >= 1) {
        accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
        accountTo.setBalance(accountTo.getBalance().add(amount));

        this.accountsRepository.save(accountFrom);
        this.accountsRepository.save(accountTo);

        emailNotificationService.notifyAboutTransfer(accountFrom, "The amount "+ amount +"has been debited from the "+ accountFrom);
        emailNotificationService.notifyAboutTransfer(accountFrom, "The amount "+ amount +"has been credited to the "+ accountTo);
      } else {
        throw new InsufficientBalanceException("Insufficient balance in accountFrom");
      }
    } else {
      throw new AccountIdNotFoundException("One of the account is not found");
    }
  }
}
