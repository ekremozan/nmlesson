# Özellik Spesifikasyonu: AI ile Üretilmiş Çoktan Seçmeli Quiz

**Özellik Dalı**: `007-ai-quiz-generation`

**Oluşturulma Tarihi**: 2026-08-16

**Durum**: Taslak

**Girdi**: Kullanıcı açıklaması: "ai ile üretilmiş çoktan seçmeli soru cevaplama sayfası yapacağım"

## Kullanıcı Senaryoları ve Testleri *(zorunlu)*

### Kullanıcı Hikayesi 1 - Hikaye içeriğinden anlık soru üretme ve cevaplama (Öncelik: P1)

Premium bir kullanıcı, okuduğu bir hikayenin okuma ekranında "Test" eylemini tetikler. Sistem yeni
bir sayfaya geçer ve o hikayenin tüm metnini temel alarak AI'dan (Gemini) anlık olarak çoktan
seçmeli tek bir soru (4 şık, 1 doğru cevap) üretir. Kullanıcı bir şık seçtiğinde, seçimi doğruysa
"doğru" hâli, yanlışsa "yanlış" hâli (doğru şık da işaretlenmiş olarak) ve her durumda AI'ın
verdiği kısa açıklama gösterilir.

**Neden bu öncelik**: Bu, case study'nin zorunlu "premium AI özelliği" gereksinimini karşılayan tek
uçtan uca akıştır; bu olmadan teslim edilecek bir MVP yoktur.

**Bağımsız Test**: Premium bir hesapla bir hikaye açılıp "Test" eylemi tetiklenerek, üretilen
soruya doğru ve yanlış şıklarla ayrı ayrı yanıt verilip her iki durumda da doğru görsel hâlin ve
açıklamanın göründüğü doğrulanarak bağımsız olarak test edilebilir.

**Kabul Senaryoları**:

1. **Given** premium kullanıcı bir hikayeyi okuyor, **When** "Test" eylemini tetikler, **Then**
   sistem yeni bir sayfaya geçer ve o hikayeye özgü, 4 seçenekli ve tek doğru cevaplı bir soru
   gösterir.
2. **Given** kullanıcı soru ekranında bir şık seçer ve seçtiği şık doğrudur, **When** seçim
   onaylanır, **Then** sistem "doğru" görsel hâlini ve AI'dan gelen kısa açıklamayı gösterir.
3. **Given** kullanıcı soru ekranında bir şık seçer ve seçtiği şık yanlıştır, **When** seçim
   onaylanır, **Then** sistem "yanlış" görsel hâlini, doğru şıkkı işaretli biçimde ve AI'dan gelen
   kısa açıklamayı gösterir.
4. **Given** kullanıcı soruyu cevaplamış, **When** sonuç ekranındaki eylemi tetikler, **Then**
   sistem kullanıcıyı hikaye okuma ekranına geri döndürür.

---

### Kullanıcı Hikayesi 2 - Premium olmayan kullanıcı için erişim engeli (Öncelik: P2)

Ücretsiz (premium olmayan) bir kullanıcı bir hikaye okuma ekranında "Test" özelliğini görür ancak
kilitlidir. Özelliği açmaya çalıştığında mevcut satın alma/paywall ekranına yönlendirilir.

**Neden bu öncelik**: Case study'nin "premium gating" gereksinimiyle tutarlı olması ve AI
özelliğinin ücretsiz kullanıcılara sızmaması gerekir; P1 çalışır hale geldikten hemen sonra en
kritik kısıttır.

**Bağımsız Test**: Premium olmayan bir hesapla bir hikaye sayfası açılıp "Test" eylemi
tetiklenerek, kullanıcının paywall ekranına yönlendirildiği ve Gemini'ye hiçbir istek
gitmediği doğrulanarak bağımsız test edilebilir.

**Kabul Senaryoları**:

1. **Given** kullanıcı premium abone değil, **When** hikaye okuma ekranında "Test" özelliğini
   görür, **Then** özellik kilitli olarak işaretlenir (premium rozeti/ikonuyla).
2. **Given** premium olmayan kullanıcı kilitli "Test" eylemine dokunur, **When** eylem
   tetiklenir, **Then** sistem AI çağrısı yapmadan kullanıcıyı mevcut paywall ekranına yönlendirir.

---

### Uç Durumlar

- Hikaye metni quiz için yeterli uzunlukta/zengin içerikte değilse ne olur?
- AI servisi yanıt vermezse veya hatalı/eksik biçimde (ör. eksik açıklama, 4'ten az/çok şık)
  yanıt dönerse sistem nasıl davranır?
- Kullanıcı soru üretimi sırasında ağ bağlantısını kaybederse ne olur?
- Kullanıcı soru üretimi tamamlanmadan sayfadan çıkarsa (ör. geri tuşu) ne olur?
- Kullanıcı hiç şık seçmeden geri dönmeye çalışırsa ne olur?
- Kullanıcı "Test" eylemini aynı hikaye için art arda birden çok kez tetiklerse (her seferinde
  yeni bir AI çağrısı) sistem nasıl davranır?

## Gereksinimler *(zorunlu)*

### Fonksiyonel Gereksinimler

- **FR-001**: Sistem, premium kullanıcının açık olan bir hikaye için "Test" eylemiyle AI'dan
  (Gemini) anlık olarak üretilmiş çoktan seçmeli bir soru talep etmesine izin VERMELİDİR.
- **FR-002**: "Test" eylemi ayrı bir sayfaya GEÇMELİDİR; bu sayfa o hikayenin tüm metninden
  türetilmiş, tam olarak 4 seçenekli ve tam olarak bir doğru cevabı olan tek bir soru
  GÖSTERMELİDİR.
- **FR-003**: Sistem, premium olmayan kullanıcıların soru sayfasını açmasını ENGELLEMELİ ve bu
  kullanıcıları mevcut paywall/satış ekranına YÖNLENDİRMELİDİR; bu durumda Gemini'ye istek
  ATILMAMALIDIR.
- **FR-004**: Kullanıcı bir şık seçtiğinde sistem seçimi DEĞERLENDİRMELİ ve doğruysa "doğru" görsel
  hâlini, yanlışsa "yanlış" görsel hâlini (doğru şık işaretli biçimde) ANINDA GÖSTERMELİDİR.
- **FR-005**: Sistem, doğru cevap için AI'ın ürettiği kısa açıklamayı, kullanıcının seçimi doğru ya
  da yanlış olsun her durumda GÖSTERMELİDİR.
- **FR-006**: Soru üretimi başarısız olduğunda (ağ hatası, AI servis hatası, geçersiz/eksik yanıt
  vb.) sistem kullanıcıya anlaşılır bir hata durumu ve yeniden deneme eylemi GÖSTERMELİDİR.
- **FR-007**: Sistem, soru üretimi veya değerlendirmesi sırasında oluşan hataları sessizce
  yutmadan mevcut hata raporlama (crash/error reporting) altyapısına RAPORLAMALIDIR.
- **FR-008**: Sistem, test başlatma ve test tamamlama (doğru/yanlış sonucuyla birlikte) için mevcut
  analitik altyapısına UYUMLU olay (event) KAYDETMELİDİR.
- **FR-009**: Soru üretimi ağ bağlantısı GEREKTİRİR; sistem bunu kalıcı olarak önbelleğe almaz —
  "Test" her tetiklendiğinde Gemini'ye yeni bir istek ATILIR.
- **FR-010**: Kullanıcı bir şık seçmeden soru sayfasından geri döndüğünde sistem bunu bir hata
  olarak DEĞERLENDİRMEMELİ, kullanıcıyı sorunsuz biçimde hikaye okuma ekranına GERİ
  DÖNDÜRMELİDİR.

### Anahtar Varlıklar

- **Quiz Sorusu**: Belirli bir hikayenin tüm metninden AI tarafından üretilen tek soru; soru
  metni, 4 seçenek ve seçeneklerden hangisinin doğru olduğu bilgisini taşır. Bir hikayeyle
  ilişkilidir ve kalıcı olarak saklanmaz.
- **Doğru Cevap Açıklaması**: AI'ın doğru şıkla birlikte ürettiği, kullanıcının seçimi ne olursa
  olsun gösterilen kısa metin; bir Quiz Sorusu'na aittir.

## Başarı Kriterleri *(zorunlu)*

### Ölçülebilir Sonuçlar

- **SC-001**: Premium bir kullanıcı, "Test" eylemini tetikledikten sonra 10 saniye içinde
  soruyla karşılaşır (normal ağ koşullarında).
- **SC-002**: Soru üretimi denemelerinin en az %95'i kullanıcıya ya geçerli bir soru ya da
  anlaşılır bir hata ve yeniden deneme seçeneğiyle sonuçlanır (sessiz başarısızlık yoktur).
- **SC-003**: Kullanıcıların en az %80'i açtıkları soru sayfasında bir şık seçerek soruyu
  tamamlar.
- **SC-004**: Premium olmayan bir kullanıcı hiçbir koşulda Gemini'ye istek göndermeden paywall
  ekranına yönlendirilir (sıfır yetkisiz AI çağrısı).

## Varsayımlar

- Soru, uygulamada zaten var olan bir hikayenin tüm metnine dayanır; hikayeden bağımsız, serbest
  konulu bir soru oluşturma bu kapsamın dışındadır.
- Her "Test" tetiklemesi tam olarak 1 soru, 4 seçenek, 1 doğru cevap ve doğru cevabın kısa bir
  açıklamasından oluşan tek bir sonuç üretir; çok soruluk bir quiz seti bu kapsamın dışındadır.
- AI ile soru üretimi, uygulamadan doğrudan Gemini API'sine yapılan bir çağrı ile gerçekleşir
  (sunucu tarafında bir ara katman/proxy yoktur); bu nedenle soru üretimi ağ bağlantısı
  gerektirir.
- Üretilen soru kalıcı olarak saklanmaz; "Test" her tetiklendiğinde yeni bir AI çağrısı yapılır.
  Bu, offline-first ilkesinin hikaye metni/sesi için geçerli olan "ağsız okunabilirlik" gereğini
  değiştirmez — yalnızca bu AI özelliği ağ ister; ağ olmadığında kullanıcıya anlaşılır bir hata
  gösterilir.
- Quiz özelliği, mevcut premium/abonelik kısıtlama (gating) mekanizmasını kullanır; ayrı bir
  yetkilendirme sistemi kurulmaz.
- Soru sonucu (doğru/yanlış ve açıklama) yalnızca o oturumda gösterilir; ayrı bir "geçmiş
  performans" veya "istatistik" ekranı bu özelliğin kapsamı dışındadır.
