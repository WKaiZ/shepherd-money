package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class CreditCardController {

    @Autowired
    CreditCardRepository creditCardRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        Optional<User> user = userRepository.findById(payload.getUserId());

        if (user.isEmpty()) {
            return new ResponseEntity<>(payload.getUserId(), HttpStatus.BAD_REQUEST);
        }

        if (payload.getCardIssuanceBank().isBlank()) {
            return new ResponseEntity<>(payload.getUserId(), HttpStatus.BAD_REQUEST);
        }
        CreditCard creditCard = new CreditCard();
        creditCard.setUser(user.get());
        creditCard.setNumber(payload.getCardNumber());
        creditCard.setIssuanceBank(payload.getCardIssuanceBank());
        creditCard.setBalanceHistories(new ArrayList<>());
        creditCardRepository.save(creditCard);

        user.orElseThrow().getCreditCards().add(creditCard);

        return new ResponseEntity<>(creditCard.getId(), HttpStatus.OK);

    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        }

        List<CreditCardView> creditCardViews = user.get().getCreditCards().stream().map(creditCard -> new CreditCardView(creditCard.getIssuanceBank(), creditCard.getNumber())).toList();

        return new ResponseEntity<>(creditCardViews, HttpStatus.OK);
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        Optional<CreditCard> creditCard = creditCardRepository.findByNumber(creditCardNumber);
        User user = creditCard.orElseThrow().getUser();
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return creditCard.map(card -> new ResponseEntity<>(card.getUser().getId(), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<Void> postMethodName(@RequestBody UpdateBalancePayload[] payload) {
        Arrays.sort(payload, Comparator.comparing(UpdateBalancePayload::getBalanceDate));
        for (UpdateBalancePayload updateBalancePayload : payload) {
            Optional<CreditCard> creditCard = creditCardRepository.findByNumber(updateBalancePayload.getCreditCardNumber());
            if (creditCard.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            CreditCard card = creditCard.get();
            List<BalanceHistory> balanceHistories = card.getBalanceHistories();
            Optional<BalanceHistory> balanceHistory = balanceHistories.stream().filter(history -> history.getDate().equals(updateBalancePayload.getBalanceDate())).findFirst();

            if (balanceHistory.isPresent()) {
                balanceHistory.get().setBalance(updateBalancePayload.getBalanceAmount() + balanceHistory.get().getBalance());
            } else {
                balanceHistories.add(new BalanceHistory(updateBalancePayload.getBalanceDate(), updateBalancePayload.getBalanceAmount(), card));
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
}
