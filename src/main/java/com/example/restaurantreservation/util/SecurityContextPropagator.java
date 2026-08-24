package com.example.restaurantreservation.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.Callable;

public class SecurityContextPropagator {

    public static <T> Callable<T> propagate(Callable<T> task) {
        // Store a copy of the authentication object
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return () -> {
            try {
                // Set the authentication directly
                SecurityContextHolder.getContext().setAuthentication(authentication);
                return task.call();
            } finally {
                // Clear after task completes
                SecurityContextHolder.clearContext();
            }
        };
    }

    public static Runnable propagate(Runnable task) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return () -> {
            try {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                task.run();
            } finally {
                SecurityContextHolder.clearContext();
            }
        };
    }
}