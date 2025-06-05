package com.fivebear.platform;

public class SimpleVsGenerator {
    public static int generateVs() {
        return 300 + (int) (Math.random() * 101); // 300~400 (含300和400)
    }
}
