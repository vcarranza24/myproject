package com.bootcamp.myproject.application.restservice;

import com.bootcamp.myproject.application.restrepository.AccountsRepository;
import com.bootcamp.myproject.application.restrepository.CreditsRepository;
import com.bootcamp.myproject.application.restrepository.TypeAccountRepository;
import com.bootcamp.myproject.application.restrepository.TypeCreditRepository;
import com.bootcamp.myproject.application.util.BalanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final AccountsRepository accountsRepository;
    private final CreditsRepository creditsRepository;
    private final TypeAccountRepository typeAccountRepository;
    private final TypeCreditRepository typeCreditRepository;

    public Flux<BalanceResponse> getAvailableBalances(String numDocument) {
        // Cuentas bancarias
        Flux<BalanceResponse> accountBalances = accountsRepository.findByNumDocumentAndState(numDocument, 1).flatMap(account -> typeAccountRepository.findById(account.getIdTypeAccount()).map(acc -> new BalanceResponse(acc.getNameAccount(), account.getNumAccount(), account.getBalance(), null)).onErrorResume(e -> Mono.empty()));

        // Créditos
        Flux<BalanceResponse> creditBalances = creditsRepository.findByNumDocumentAndState(numDocument, 1).flatMap(credit -> typeCreditRepository.findById(credit.getIdTypeCredit()).map(cred -> new BalanceResponse(cred.getNameCredit(), credit.getNumCard(), credit.getBalance(), credit.getLimitCredit())).onErrorResume(e -> Mono.empty()));

        // Unir ambos flujos
        return accountBalances.mergeWith(creditBalances);
    }

}
