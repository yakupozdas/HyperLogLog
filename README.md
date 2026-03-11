# HyperLogLog

Java ile sıfırdan yazılmış bir HyperLogLog implementasyonu. Milyonlarca eleman içeren veri setlerinde benzersiz eleman sayısını, tam listeyi bellekte tutmadan yaklaşık olarak hesaplar.

## HyperLogLog Nedir?

HyperLogLog, kardinalite tahmini (cardinality estimation) için kullanılan bir olasılıksal veri yapısıdır. Kesin sayım yerine istatistiksel bir tahmin sunar; bunun karşılığında bellek kullanımı sabit ve son derece düşük kalır.

Klasik `HashSet` ile 1.000.000 eleman saymak megabaytlarca bellek gerektirirken, HyperLogLog aynı işlemi yalnızca birkaç kilobaytla ve teorik `%1.63` hata payıyla gerçekleştirir.

## Proje Yapısı

```
src/
└── com/hll/core/
    ├── Main.java          # Test senaryosu ve çıktı
    ├── HyperLogLog.java   # Çekirdek veri yapısı
    └── HashUtil.java      # SHA-256 tabanlı hash fonksiyonu
```

## Nasıl Çalışır?

### 1. Hashing

Her eleman SHA-256 ile 64-bit'lik bir hash değerine dönüştürülür.

### 2. Bucketing (Kovaya Atama)

Hash'in ilk `p` biti, elemanın hangi register'a (kovaya) yazılacağını belirler. Toplam kova sayısı `m = 2^p`'dir.

### 3. Leading Zero Sayımı

Kalan bitlerdeki ardışık sıfır sayısı (`rho`) hesaplanır. Bu değer, ilgili register'daki mevcut maksimum değerle karşılaştırılır; daha büyükse güncellenir.

### 4. Tahmin

Tüm register'ların harmonik ortalaması alınarak ham bir kardinalite tahmini üretilir. Küçük ve büyük veri setleri için ek düzeltmeler uygulanır:

- **Linear Counting:** Tahmin `2.5 * m`'den küçükse ve boş register varsa devreye girer.
- **Büyük Aralık Düzeltmesi:** Tahmin `2^32 / 30`'u aşarsa logaritmik düzeltme uygulanır.

### 5. Merge

İki ayrı HyperLogLog yapısı, her register için maksimum değer alınarak veri kaybı olmadan birleştirilebilir. Bu özellik, dağıtık sistemlerde farklı sunucularda toplanan HLL yapılarını tek bir tahmine indirgemek için kullanılır.

## Örnek Çıktı

```
--- HyperLogLog Test Süreci Başladı ---
Kova Sayısı (m): 4.096
Üretilecek Toplam Veri: 1.000.000
HLL-1 Tahmini (İlk Yarısı): 499.412
HLL-2 Tahmini (İkinci Yarısı): 500.231

--- HLL Yapıları Birleştiriliyor (Merge) ---
Gerçek Eşsiz Eleman Sayısı: 1.000.000
HLL Nihai Tahmini:          998.107
Ölçülen Hata Payı:          % 0,19
Teorik Hata Sınırı:         % 1,62
```



## Kaynaklar

- Flajolet, P. et al. — [HyperLogLog: the analysis of a near-optimal cardinality estimation algorithm](http://algo.inria.fr/flajolet/Publications/FlFuGaMe07.pdf)
