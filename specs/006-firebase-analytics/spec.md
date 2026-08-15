# Özellik Spesifikasyonu: Firebase Analytics Entegrasyonu

**Özellik Dalı**: `006-firebase-analytics`

**Oluşturulma Tarihi**: 2026-08-15

**Durum**: Taslak

**Girdi**: Kullanıcı açıklaması: "projeye analitics eklemek istiyorum firebase" + takip talebi: "kişi nereye girmiş uygulamanın içinde nasıl gezmiş anlayacağımız şekilde gerekli olan her yere koymamız lazım. paywall tıkladı aldı almadı vazgeçti geri gitti. hangi dersi seçti hangi dersi dinledi hangi dersi durdurdu vs. parametre olanlarda parametre alalım"

## Kullanıcı Senaryoları ve Testler *(zorunlu)*

### Kullanıcı Hikayesi 1 - Uygulama içi gezinmenin (ekran akışı) izlenmesi (Öncelik: P1)

Ürün ekibi olarak, bir kullanıcının uygulamaya nereden girdiğini ve hangi ekranlar arasında nasıl gezindiğini görmek istiyorum; böylece gerçek kullanım yollarını (ana ekran → okuma, ana ekran → paywall vb.) anlayıp darboğazları tespit edebilirim.

**Neden bu öncelik**: Diğer tüm olaylar (içerik, paywall, AI) belirli bir ekran bağlamında gerçekleşir; ekran akışı izlenmeden huni verileri hangi yoldan geldiği bilinmeyen izole noktalar olarak kalır. Bu yüzden en temel katmandır.

**Bağımsız Test**: Uygulama açılıp ana ekrandan okuma ekranına, oradan paywall'a geçildiğinde, analiz panelinde sırayla ilgili ekran adlarını ve önceki ekranı taşıyan "ekran görüntülendi" olaylarının göründüğü doğrulanır.

**Kabul Senaryoları**:

1. **Given** kullanıcı uygulamayı açar, **When** bir ekran (ana ekran, okuma, paywall, satın alma sonrası, ayarlar) görüntülenir hale gelirse, **Then** o ekranın adını ve kullanıcının geldiği önceki ekranı içeren bir "ekran görüntülendi" olayı kaydedilir.
2. **Given** kullanıcı bir ekrandadır, **When** geri tuşu veya geri gezinme ile bir önceki ekrana dönerse, **Then** bu gezinme de normal bir ekran görüntüleme olayı olarak (kaynağı "geri gezinme" olacak şekilde) kaydedilir.

---

### Kullanıcı Hikayesi 2 - Ders seçimi ve dinleme kullanımının izlenmesi (Öncelik: P1)

Ürün ekibi olarak, kullanıcıların hangi dersleri listeden seçtiğini, hangilerini görüntülediğini, hangilerini dinlemeye başlayıp hangi noktada durdurduğunu görmek istiyorum; böylece içerik etkileşimini ders bazında ölçüp önceliklendirebilirim.

**Neden bu öncelik**: Bu, uygulamanın temel değer önerisi olan "oku ve dinle" deneyiminin kullanılıp kullanılmadığını gösteren en temel içerik sinyalidir; diğer tüm huni adımları (paywall, abonelik, AI) bu temel etkileşim üzerine kuruludur.

**Bağımsız Test**: Bir ders listeden seçilip açıldığında, dinleme başlatıldığında ve durdurulduğunda, analiz panelinde ilgili olayların (ders seçildi, içerik görüntülendi, dinleme başladı, dinleme durduruldu) ilişkili ders kimliği ve başlığıyla birlikte göründüğü doğrulanır.

**Kabul Senaryoları**:

1. **Given** kullanıcı ders listesindedir, **When** listeden bir dersi seçerse, **Then** dersin kimliğini, başlığını ve listedeki konumunu (index) içeren bir "ders seçildi" olayı kaydedilir.
2. **Given** kullanıcı bir dersin okuma ekranındadır, **When** içerik görüntülenir hale gelirse, **Then** ilgili ders kimliğini ve erişim durumunu (tam/önizleme) içeren bir "içerik görüntülendi" olayı kaydedilir.
3. **Given** kullanıcı bir dersin okuma ekranındadır, **When** dinlemeyi başlatırsa, **Then** ilgili ders kimliğini içeren bir "dinleme başladı" olayı kaydedilir.
4. **Given** bir ders dinlenmektedir, **When** kullanıcı dinlemeyi durdurur/duraklatırsa ya da ekrandan ayrılırsa, **Then** ders kimliğini, durma sebebini (kullanıcı duraklattı, tamamlandı, ekrandan çıkıldı) ve dinlenen yaklaşık ilerlemeyi (yüzde) içeren bir "dinleme durduruldu" olayı kaydedilir.
5. **Given** cihaz çevrimdışıdır, **When** kullanıcı içerik görüntüler veya dinlerse, **Then** olaylar cihazda tutulur ve bağlantı geri geldiğinde iletilir; kullanıcı akışı bundan etkilenmez.

---

### Kullanıcı Hikayesi 3 - Paywall'dan satın almaya kadar olan huninin uçtan uca izlenmesi (Öncelik: P2)

Ürün ekibi olarak, paywall'ın ne zaman gösterildiğini, kullanıcının satın alma butonuna dokunup dokunmadığını, satın almayı tamamlayıp tamamlamadığını, vazgeçip vazgeçmediğini ve paywall'dan geri gidip gitmediğini görmek istiyorum; böylece ücretsiz-ücretli dönüşüm huninin tam olarak hangi adımında kullanıcı kaybı olduğunu anlayabilirim.

**Neden bu öncelik**: Premium gating ve dönüşüm, projenin iş modeli varsayımını doğrulayan ikinci en kritik sinyaldir; ancak ekran/içerik etkileşimi olmadan anlamsızdır, bu yüzden P1'den sonra gelir.

**Bağımsız Test**: Ücretsiz bir kullanıcı kilitli içeriğe eriştiğinde paywall görüntülenir; satın alma butonuna dokunulup akış tamamlandığında veya iptal/geri gidildiğinde, analiz panelinde huninin her adımının (gösterildi → tıklandı → satın alındı/vazgeçildi/geri gidildi) ayrı ayrı ve doğru sırada göründüğü doğrulanır.

**Kabul Senaryoları**:

1. **Given** kullanıcı premium olmayan bir hesaptadır, **When** kilitli/önizleme içeriğe ulaşıp paywall görüntülenirse, **Then** paywall'ı tetikleyen kaynağı (hangi ders/ekran) içeren bir "paywall gösterildi" olayı kaydedilir.
2. **Given** paywall görüntülenmektedir, **When** kullanıcı satın alma/abone ol butonuna dokunursa, **Then** seçilen plan/fiyat bilgisini içeren bir "paywall satın alma tıklandı" olayı kaydedilir.
3. **Given** kullanıcı satın alma butonuna dokunmuştur, **When** (sahte/sandbox) satın alma akışı başarıyla tamamlanırsa, **Then** plan/fiyat bilgisini içeren bir "abonelik başlatıldı" (satın alındı) olayı kaydedilir.
4. **Given** kullanıcı satın alma butonuna dokunmuştur, **When** satın alma akışı kullanıcı tarafından iptal edilir veya başarısız olursa, **Then** iptal/hata sebebini içeren bir "satın alma vazgeçildi" olayı kaydedilir.
5. **Given** paywall görüntülenmektedir, **When** kullanıcı satın alma butonuna hiç dokunmadan geri gider veya paywall'ı kapatırsa, **Then** bir "paywall'dan geri gidildi" olayı kaydedilir.

---

### Kullanıcı Hikayesi 4 - AI özelliği kullanımının izlenmesi (Öncelik: P3)

Ürün ekibi olarak, premium AI özelliğinin ne sıklıkla kullanıldığını görmek istiyorum; böylece bu özelliğin gerçek kullanıcı değeri yaratıp yaratmadığını değerlendirebilirim.

**Neden bu öncelik**: AI özelliği yalnızca premium kullanıcılara açık olduğundan, önce temel içerik ve dönüşüm huninin izlenebilir olması gerekir; AI kullanım verisi bu ikisinin üzerine eklenen bir ölçümdür.

**Bağımsız Test**: Premium bir kullanıcı AI özelliğini tetiklediğinde, analiz panelinde "AI özelliği kullanıldı" olayının göründüğü doğrulanır.

**Kabul Senaryoları**:

1. **Given** kullanıcı premium erişime sahiptir, **When** AI özelliğini tetiklerse, **Then** bir "AI özelliği kullanıldı" olayı kaydedilir.

---

### Uç Durumlar

- Analiz olayı kaydı başarısız olursa (örn. SDK hatası) kullanıcı akışı kesintiye uğramamalı; hata sessizce yutulmadan crash raporlamaya iletilmelidir.
- Aynı kullanıcı eylemi çok kısa sürede tekrarlanırsa (örn. ekranı hızlıca açıp kapatma) olaylar tekilleştirilmeden, gerçekleşen her etkileşim ayrı ayrı kaydedilir; huni analizindeki gürültü pay/oran hesaplamalarıyla ürün tarafında yönetilir.
- Kullanıcı cihazında Google Play Hizmetleri/Firebase erişilemez durumdaysa olay kaydı sessizce atlanır, uygulama işlevselliği etkilenmez.
- Olay parametreleri arasında kişisel olarak tanımlayıcı bilgi (ad, e-posta, ödeme bilgisi) yer almaz; yalnızca ders/ekran kimlikleri gibi ürün içi tanımlayıcılar taşınır.
- Kullanıcı bir dersi dinlerken uygulamadan tamamen çıkarsa (görev yöneticisinden kapatma, sistem tarafından sonlandırma) "dinleme durduruldu" olayı en son bilinen ilerlemeyle en iyi çaba (best-effort) prensibiyle kaydedilir; garanti edilmez.
- Kullanıcı paywall'ı birden fazla kez art arda açıp kapatırsa (örn. iki farklı dersten tetiklenerek) her gösterim kendi kaynağıyla ayrı bir olay olarak kaydedilir.

## Gereksinimler *(zorunlu)*

### Fonksiyonel Gereksinimler

**Gezinme**

- **FR-001**: Sistem, kullanıcı bir ekranı (ana ekran, okuma, paywall, satın alma sonrası, ayarlar) görüntülediğinde, ekran adını ve geldiği önceki ekranı içeren bir "ekran görüntülendi" olayı kaydetmelidir.
- **FR-002**: Geri gezinme (sistem/uygulama içi geri tuşu) ile yapılan ekran değişiklikleri de aynı "ekran görüntülendi" olayı ile, kaynağı geri gezinme olacak şekilde kaydedilmelidir.

**Ders/İçerik ve Dinleme**

- **FR-003**: Sistem, kullanıcı ders listesinden bir dersi seçtiğinde, ders kimliğini, başlığını ve listedeki konumunu (index) içeren bir "ders seçildi" olayı kaydetmelidir.
- **FR-004**: Sistem, bir dersin içeriği görüntülendiğinde, ders kimliğini ve erişim durumunu (tam/önizleme) içeren bir "içerik görüntülendi" olayı kaydetmelidir.
- **FR-005**: Sistem, bir kullanıcı bir dersi dinlemeye başladığında ilgili ders kimliğini taşıyan bir "dinleme başladı" olayı kaydetmelidir.
- **FR-006**: Sistem, dinleme durduğunda (kullanıcı duraklattı, tamamlandı veya ekrandan çıkıldı) ders kimliğini, durma sebebini ve yaklaşık ilerleme yüzdesini içeren bir "dinleme durduruldu" olayı kaydetmelidir.

**Paywall ve Satın Alma**

- **FR-007**: Sistem, premium olmayan bir kullanıcıya paywall gösterildiğinde, paywall'ı tetikleyen kaynağı (hangi ders/ekran) içeren bir "paywall gösterildi" olayı kaydetmelidir.
- **FR-008**: Sistem, kullanıcı paywall'daki satın alma/abone ol butonuna dokunduğunda, seçilen plan/fiyat bilgisini içeren bir "paywall satın alma tıklandı" olayı kaydetmelidir.
- **FR-009**: Sistem, (sahte/sandbox) satın alma akışı başarıyla tamamlandığında, plan/fiyat bilgisini içeren bir "abonelik başlatıldı" olayı kaydetmelidir.
- **FR-010**: Sistem, satın alma akışı kullanıcı tarafından iptal edildiğinde veya başarısız olduğunda, iptal/hata sebebini içeren bir "satın alma vazgeçildi" olayı kaydetmelidir.
- **FR-011**: Sistem, kullanıcı satın alma butonuna dokunmadan paywall'dan geri gittiğinde/kapattığında bir "paywall'dan geri gidildi" olayı kaydetmelidir.

**AI Özelliği**

- **FR-012**: Sistem, premium kullanıcı AI özelliğini kullandığında ilgili bağlamı (ör. ders kimliği) içeren bir "AI özelliği kullanıldı" olayı kaydetmelidir.

**Ortak Kurallar**

- **FR-013**: Parametre taşıyabilecek her olay (ders seçildi, içerik görüntülendi, dinleme başladı/durduruldu, ekran görüntülendi, paywall olayları, AI özelliği kullanıldı) ilgili bağlam parametrelerini (ders/ekran kimliği, kaynak, plan/fiyat, ilerleme yüzdesi vb.) taşımalıdır; parametresiz genel bir olay adıyla yetinilmemelidir.
- **FR-014**: Analiz olayı kaydı, kullanıcı akışını engellememeli veya geciktirmemelidir (asenkron/best-effort davranmalıdır).
- **FR-015**: Analiz olayı kaydında oluşan bir hata, uygulamayı çökertmemeli ve sessizce yutulmadan crash raporlama mekanizmasına iletilmelidir.
- **FR-016**: Sistem, cihaz çevrimdışıyken oluşan olayları saklamalı ve bağlantı geri geldiğinde otomatik olarak iletmelidir.
- **FR-017**: Analiz olayı parametreleri kişisel olarak tanımlayıcı bilgi (ad, e-posta, ödeme bilgisi) içermemelidir.
- **FR-018**: Analiz kaydı, uygulamanın geri kalanından (özellik modüllerinden) tek bir soyutlama üzerinden erişilebilir olmalıdır; hiçbir özellik modülü doğrudan bir analiz SDK'sını çağırmamalıdır.

### Anahtar Varlıklar

- **Analiz Olayı (Analytics Event)**: Belirli bir kullanıcı eylemini temsil eder (ekran görüntülendi, ders seçildi, içerik görüntülendi, dinleme başladı, dinleme durduruldu, paywall gösterildi, paywall satın alma tıklandı, abonelik başlatıldı, satın alma vazgeçildi, paywall'dan geri gidildi, AI özelliği kullanıldı). Bir olay adı, gerçekleşme zamanı ve ilgili bağlam parametrelerini (ör. ders kimliği, ekran adı, kaynak, plan/fiyat, ilerleme yüzdesi) taşır.
- **Gezinme Bağlamı**: Bir ekran görüntüleme olayının, kullanıcının geldiği önceki ekranı ve gezinme yönünü (ileri/geri) tanımlayan bağlamı.

## Başarı Kriterleri *(zorunlu)*

### Ölçülebilir Sonuçlar

- **SC-001**: Ürün ekibi, tanımlanan tüm gezinme, içerik/dinleme, paywall/satın alma ve AI olaylarının her biri için, ilgili kullanıcı eylemi gerçekleştikten sonraki 24 saat içinde analiz panelinde veri görebilir.
- **SC-002**: Analiz kaydındaki bir hata, kullanıcı akışlarının hiçbirini (%0) kesintiye uğratmaz veya çökmeye yol açmaz.
- **SC-003**: Çevrimdışı sırasında oluşan olaylar, normal çevrimdışı süreler (24 saate kadar) için veri kaybı olmadan bağlantı geri geldiğinde iletilir.
- **SC-004**: Ürün ekibi, yayından sonraki ilk sprint içinde, ek bir manuel enstrümantasyon eklemeden yalnızca analiz panelindeki verilerle uçtan uca bir kullanıcı yolunu (ör. "ana ekrandan ders seçip dinlemeye başlayan kullanıcı oranı", "paywall gösterilip satın alan/vazgeçen/geri giden kullanıcı dağılımı") yeniden kurabilir.
- **SC-005**: Paywall hunisindeki dört olası çıkış (satın aldı, vazgeçti/başarısız oldu, geri gitti, hâlâ paywall'da) analiz panelinde birbirinden ayırt edilebilir şekilde raporlanır; toplamları paywall gösterim sayısıyla tutarlıdır.

## Varsayımlar

- Analiz için Firebase Analytics kullanılacaktır (kullanıcı tarafından açıkça belirtildi); bu, CLAUDE.md'deki "Firebase Analytics + Crashlytics" gözlemlenebilirlik kararıyla uyumludur.
- Kullanıcılar Firebase'in varsayılan anonim cihaz/uygulama örneği tanımlayıcısıyla izlenir; ayrı bir kullanıcı kimliği (ör. hesap ID'si) olayla ilişkilendirilmez, çünkü uygulamada hesap sistemi yoktur.
- Bu sürüm kapsamında ayrı bir analiz izni/onay (consent) ekranı yoktur; bu proje bir vaka çalışması olduğundan ve gerçek kullanıcı verisi toplanmadığından, veri toplama uygulama kullanımıyla otomatik başlar. Gerçek bir üretim ortamında bu varsayım KVKK/GDPR gereksinimleri nedeniyle değişecektir.
- "Gerekli olan her yere koymamız lazım" talebi, uygulamanın mevcut dört ekranını (ana ekran, okuma, paywall, satın alma sonrası, ayarlar) ve bu ekranlar üzerindeki kullanıcı eylemlerini kapsar; ileride eklenecek yeni ekranlar bu spesifikasyonun kapsamı dışındadır ve kendi analiz olaylarını ayrıca tanımlamalıdır.
- "Dinleme durduruldu" olayındaki ilerleme yüzdesi yaklaşık bir değerdir (saniye hassasiyetinde kesin senkronizasyon gerekmez); bu, mevcut TTS tabanlı dinleme mimarisinin (cümle bazlı ilerleme) doğal bir sonucudur.
- Analiz panelinin kendisi (Firebase konsolu) bu özelliğin bir parçası değildir; sadece olayların doğru ve güvenilir şekilde bu panele ulaşması bu özelliğin kapsamındadır.
