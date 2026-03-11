package com.hll.core;

public class HyperLogLog {
    private final int p;          // Hassasiyet (Precision)
    private final int m;          // Kova sayısı (2^p)
    private final byte[] registers; // Kovalar
    private final double alphaMM; // Düzeltme katsayısı

    public HyperLogLog(int p) {
        this.p = p;
        this.m = 1 << p;
        this.registers = new byte[m];

        // Sabit alpha değerleri (m'e göre teorik değerler)
        if (m == 16) alphaMM = 0.673;
        else if (m == 32) alphaMM = 0.697;
        else if (m == 64) alphaMM = 0.709;
        else alphaMM = 0.7213 / (1 + 1.079 / m);
    }

    // Veri ekleme (HLL çekirdek mantığı)
    public void add(String item) {
        long x = HashUtil.hash(item);

        // 1. Bucketing: İlk p bit ile kova seçimi
        int j = (int) (x >>> (64 - p));

        // 2. Leading Zeros: Kalan bitlerdeki ilk 1'in konumu
        long w = (x << p) | (1L << (p - 1));
        int rho = Long.numberOfLeadingZeros(w) + 1;

        // 3. Register Güncelleme: Sadece daha büyük bir değer bulursak
        registers[j] = (byte) Math.max(registers[j], rho);
    }

    // Tahmin yürütme (Harmonik Ortalama ve Düzeltmeler)
    public long estimate() {
        double sum = 0;
        for (int j = 0; j < m; j++) {
            sum += Math.pow(2, -registers[j]);
        }

        // Ham Tahmin Formülü
        double estimate = alphaMM * m * m / sum;

        // Küçük Veri Seti Düzeltmesi (Linear Counting)
        if (estimate <= 2.5 * m) {
            int zeroCount = countZeros();
            if (zeroCount > 0) {
                estimate = m * Math.log((double) m / zeroCount);
            }
        }
        // Büyük Veri Seti Düzeltmesi
        else if (estimate > (1.0 / 30.0) * Math.pow(2, 32)) {
            estimate = -Math.pow(2, 32) * Math.log(1.0 - (estimate / Math.pow(2, 32)));
        }

        return Math.round(estimate);
    }

    // Ödev maddesi: İki HLL yapısını veri kaybı olmadan birleştirme
    public void merge(HyperLogLog other) {
        if (this.p != other.p) {
            throw new IllegalArgumentException("Hassasiyet değerleri (p) aynı olmalıdır!");
        }
        for (int i = 0; i < m; i++) {
            // Her kovanın maksimumunu alarak birleştiriyoruz
            this.registers[i] = (byte) Math.max(this.registers[i], other.getRegisters()[i]);
        }
    }

    private int countZeros() {
        int count = 0;
        for (byte r : registers) {
            if (r == 0) count++;
        }
        return count;
    }

    // Getters
    public byte[] getRegisters() { return registers; }
    public int getM() { return m; }
}