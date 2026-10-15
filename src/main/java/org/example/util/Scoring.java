package org.example.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Scoring {

    public static long score(double balance) {
        double x = balance / 1000.0;
        double s = 0;

        for (int i = 1; i <= 500; i++) {
            s += Math.sin(x * i) * Math.cos(x / i) / i;

        }
        return (long) (Math.sqrt(Math.abs(s)) * 1.5);
    }
}
