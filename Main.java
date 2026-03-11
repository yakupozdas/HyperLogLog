package com.hll.core;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        // Sayı formatının yerel ayarlardan (nokta/virgül) etkilenmemesi için Locale.US veya Locale.GERMANY kullanılabilir.
        // Locale.GERMANY binlik ayıracı olarak nokta (.) kullanır.
        Locale.setDefault(Locale.GERMANY);

        int p = 12;
        HyperLogLog hll1 = new HyperLogLog(p);
        HyperLogLog hll2 = new HyperLogLog(p);

        Set<String> actualUniqueElements = new HashSet<>();
        int n = 1_000_000;

        System.out.println("--- HyperLogLog Test Süreci Başladı ---");
        System.out.printf("Kova Sayısı (m): %,d%n", hll1.getM());
        System.out.printf("Üretilecek Toplam Veri: %,d%n", n);

        for (int i = 0; i < n / 2; i++) {
            String element = UUID.randomUUID().toString();
            hll1.add(element);
            actualUniqueElements.add(element);
        }

        for (int i = 0; i < n / 2; i++) {
            String element = UUID.randomUUID().toString();
            hll2.add(element);
            actualUniqueElements.add(element);
        }

        System.out.printf("HLL-1 Tahmini (İlk Yarısı): %,d%n", hll1.estimate());
        System.out.printf("HLL-2 Tahmini (İkinci Yarısı): %,d%n", hll2.estimate());

        System.out.println("\n--- HLL Yapıları Birleştiriliyor (Merge) ---");
        hll1.merge(hll2);

        long finalEstimate = hll1.estimate();
        int realCount = actualUniqueElements.size();
        double error = Math.abs((double) (finalEstimate - realCount) / realCount) * 100;

        System.out.printf("Gerçek Eşsiz Eleman Sayısı: %,d%n", realCount);
        System.out.printf("HLL Nihai Tahmini: %,d%n", finalEstimate);
        System.out.printf("Ölçülen Hata Payı: %% %.2f%n", error);

        double theoreticalError = (1.04 / Math.sqrt(hll1.getM())) * 100;
        System.out.printf("Teorik Hata Sınırı: %% %.2f%n", theoreticalError);
    }
}