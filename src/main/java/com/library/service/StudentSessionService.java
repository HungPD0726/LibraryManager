package com.library.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class StudentSessionService {

    public static final String BORROW_CART = "borrowCart";
    public static final String BUY_WAITLIST = "buyWaitlist";

    @SuppressWarnings("unchecked")
    public Map<Integer, Integer> borrowCart(HttpSession session) {
        Object existing = session.getAttribute(BORROW_CART);
        if (existing instanceof Map<?, ?> map) {
            return (Map<Integer, Integer>) map;
        }
        Map<Integer, Integer> cart = new LinkedHashMap<>();
        session.setAttribute(BORROW_CART, cart);
        return cart;
    }

    @SuppressWarnings("unchecked")
    public Map<Integer, Integer> waitlist(HttpSession session) {
        Object existing = session.getAttribute(BUY_WAITLIST);
        if (existing instanceof Map<?, ?> map) {
            return (Map<Integer, Integer>) map;
        }
        Map<Integer, Integer> waitlist = new LinkedHashMap<>();
        session.setAttribute(BUY_WAITLIST, waitlist);
        return waitlist;
    }

    public void clearBorrowCart(HttpSession session) {
        session.removeAttribute(BORROW_CART);
    }

    public void clearWaitlist(HttpSession session) {
        session.removeAttribute(BUY_WAITLIST);
    }
}
