# Feature Specification: API ve Çökme Hatalarında Best-Practice Hata Yönetimi ve Crashlytics Entegrasyonu

**Feature Branch**: `005-error-handling-crashlytics`

**Created**: 2026-08-15

**Status**: Draft

**Input**: User description: "projemdeki tüm api istekleri ya da crash hata alabilcek yerleri bulup bunları için best practice bir hata yönetimi yapmak istiyorum. bunun dışında crash olan bir yer olursa firebase crashlitics göndermek istiyorum."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ağ/API hatası alan kullanıcı ne olduğunu anlar ve devam edebilir (Priority: P1)

Bir kullanıcı içerik listesini yenilerken, bir dersin içeriğini indirirken veya uygulama içinde herhangi bir uzak veri isteği başarısız olduğunda; kullanıcı teknik bir hata koduyla değil, ne olduğunu anlayan ve ne yapabileceğini bilen bir mesajla karşılaşır (ör. "İçerik yüklenemedi, tekrar dene"). Elindeki önbelleğe alınmış içerik erişilebilir kalmaya devam eder.

**Why this priority**: Bu, case study'nin "production concerns" kriterinin doğrudan parçasıdır ve kullanıcının uygulamayı güvenip kullanmaya devam etmesini sağlayan en temel katmandır. Bu olmadan tek bir başarısız istek kullanıcıyı donmuş bir ekranda ya da anlaşılmaz bir hatayla baş başa bırakabilir.

**Independent Test**: Ağ bağlantısı kapatılıp bir içerik yenileme/indirme işlemi tetiklenerek; kullanıcıya anlaşılır bir hata mesajı ve yeniden deneme seçeneği gösterildiği, önbellekteki içeriğin okunabilir kaldığı doğrulanarak test edilebilir.

**Acceptance Scenarios**:

1. **Given** kullanıcı çevrimdışı, **When** kullanıcı ders kataloğunu yenilemeye çalışır, **Then** kullanıcı "bağlantı yok" bilgisini görür ve daha önce indirilmiş dersler listede görünmeye devam eder.
2. **Given** kullanıcı çevrimiçi ama uzak servis geçici olarak yanıt vermiyor, **When** kullanıcı bir ders içeriğini açmaya çalışır, **Then** kullanıcı anlaşılır bir hata mesajı ve yeniden deneme seçeneği görür.
3. **Given** kullanıcı yeniden deneme seçeneğine dokunur, **When** ağ isteği bu kez başarılı olur, **Then** hata mesajı kaybolur ve içerik normal şekilde gösterilir.

---

### User Story 2 - Beklenmedik çökmeler otomatik olarak raporlanır (Priority: P1)

Uygulama, öngörülmemiş bir hata nedeniyle beklenmedik şekilde kapandığında (çökme), bu olay kullanıcıdan herhangi bir ek işlem gerektirmeden otomatik olarak bir çökme raporlama sistemine (Firebase Crashlytics) iletilir; böylece geliştirici bu çökmeyi görüp cihaz/uygulama sürümü ve oluştuğu ekran gibi bağlamıyla birlikte inceleyebilir.

**Why this priority**: Case study'nin "observability" kriterinin zorunlu bir parçası. Görünürlüğü olmayan bir çökme, üretimde asla düzeltilemez.

**Independent Test**: Bilinçli olarak bir çökmeye neden olunup (test amaçlı) uygulamanın kapandığı, bir sonraki açılışta raporun crash raporlama panelinde göründüğü doğrulanarak test edilebilir.

**Acceptance Scenarios**:

1. **Given** uygulama çalışırken beklenmedik bir hata oluşuyor, **When** uygulama bu nedenle kapanıyor, **Then** çökme olayı cihaz/uygulama bağlamıyla (sürüm, ekran, zaman) birlikte raporlama sistemine iletilir.
2. **Given** çökme anında internet bağlantısı yok, **When** bağlantı daha sonra geri geliyor, **Then** bekleyen çökme raporu gecikmeli olarak iletilir, kaybolmaz.
3. **Given** bir geliştirici raporlama panelini açıyor, **When** yakın zamanda oluşmuş bir çökmeye bakıyor, **Then** hangi ekranda/işlemde oluştuğunu anlayabilecek yeterli bağlam bilgisini görür.

---

### User Story 3 - Yakalanmış hatalar sessizce kaybolmaz (Priority: P2)

Uygulama kodu bir hatayı yakalayıp kullanıcıya nazik bir mesajla gösterdiğinde (çökmeye izin vermeden), bu hata da — kullanıcı fark etmese bile — geliştiricinin görebileceği bir kayıt/raporlama kanalına, hatanın nerede ve hangi bağlamda oluştuğu bilgisiyle birlikte düşer. Böylece üretimde sessizce oluşan ama kullanıcıyı çökme ile karşılaştırmayan hatalar da görünür kalır.

**Why this priority**: Best-practice hata yönetiminin ayırt edici noktası budur — sadece çökmeleri değil, "yönetilmiş" hataları da izlenebilir kılmak. P1'lerden sonra gelir çünkü mevcut akışta kullanıcı deneyimi zaten korunuyor; bu madde görünürlüğü derinleştirir.

**Independent Test**: Bilinçli olarak yönetilen bir hata senaryosu (ör. geçersiz veri, önbellek okuma hatası) tetiklenip; kullanıcının çökme yaşamadığı ama hatanın raporlama sisteminde bağlamıyla birlikte kayıtlı olduğu doğrulanarak test edilebilir.

**Acceptance Scenarios**:

1. **Given** uygulama içinde bir işlem hata ile karşılaşıp bu hatayı yakalıyor, **When** kullanıcıya hata durumu nazikçe gösteriliyor, **Then** aynı hata olayı, oluştuğu bağlam bilgisiyle birlikte raporlama sistemine de iletilir.
2. **Given** aynı hata art arda birden çok kez oluşuyor, **When** raporlama sistemine iletiliyor, **Then** kullanıcı arayüzünde tekrar eden hata mesajlarıyla spam edilmez.

---

### Edge Cases

- Çökme veya hata raporlama servisine ulaşılamıyorsa (kullanıcı tamamen çevrimdışıysa) rapor ne olur? Sistem, bağlantı geri geldiğinde raporu gecikmeli olarak göndermelidir; rapor sessizce kaybolmamalıdır.
- Aynı hata çok kısa sürede tekrar tekrar oluşursa (ör. bir döngü içinde), kullanıcı arayüzü ve raporlama sistemi spam ile boğulmamalıdır.
- Bir ağ isteği kısmen tamamlanmış veri döndürürse (ör. boş/eksik liste), sistem mevcut önbellek verisini korumalı, yanlışlıkla üzerine yazmamalıdır.
- Hata veya çökme raporlarına, kullanıcıyı doğrudan tanımlayabilecek kişisel/hassas veri (ör. e-posta, tam ad) sızmamalıdır.
- Premium olmayan bir kullanıcı, paywall ile kısıtlı bir alanda hata alırsa, hata mesajı yanlışlıkla premium içeriğe dair bilgi sızdırmamalıdır.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Sistem, başarısız olan her ağ/API isteğinde kullanıcıya, hatanın türünü (ör. bağlantı yok, sunucu hatası) yansıtan, teknik olmayan ve anlaşılır bir mesaj göstermelidir.
- **FR-002**: Sistem, kurtarılabilir ağ/API hatalarında kullanıcıya yeniden deneme imkânı sunmalıdır.
- **FR-003**: Sistem, ağ bağlantısı olmadığında kullanıcıyı bilgilendirmeli ve daha önce cihazda önbelleğe alınmış içeriği erişilebilir tutmaya devam etmelidir.
- **FR-004**: Sistem, kod içinde yakalanan (uygulamayı çökertmeyen) her hatayı, oluştuğu bağlam bilgisiyle (nerede, hangi işlemde) birlikte bir geliştirici görünürlüğü kanalına iletmelidir; hiçbir hata sessizce yutulmamalıdır. Bu kanal, FR-010 uyarınca Crashlytics'i de kapsar.
- **FR-005**: Sistem, yakalanamayan (uygulamayı sonlandıran) her çökmeyi otomatik olarak Firebase Crashlytics'e göndermelidir.
- **FR-006**: Çökme raporları; cihaz/uygulama sürümü, oluştuğu zaman ve mümkünse hangi ekran/akışta olduğu bilgisini içermelidir, böylece geliştirici sorunu yeniden üretebilir.
- **FR-007**: Hata ve çökme raporları, kullanıcıyı doğrudan tanımlayabilecek kişisel veya hassas veri içermemelidir.
- **FR-008**: Raporlama servisine ulaşılamadığı anlarda (çevrimdışı) oluşan çökme raporları cihazda tutulmalı ve bağlantı geri geldiğinde iletilmelidir.
- **FR-009**: Kullanıcı arayüzü, kısa sürede tekrar eden aynı hatayı defalarca göstererek kullanıcıyı rahatsız etmemelidir.
- **FR-010**: Sistem, yakalanmış (uygulamayı çökertmeyen) hataları da Firebase Crashlytics'e "non-fatal" olay olarak iletmelidir; böylece hem gerçek çökmeler hem de yönetilmiş hatalar aynı raporlama panelinde, oluştukları bağlam bilgisiyle birlikte incelenebilir.

### Key Entities *(include if feature involves data)*

- **Hata Olayı (Handled Error Event)**: Uygulama içinde yakalanan, kullanıcıyı çökertmeyen bir hatayı temsil eder. Oluştuğu bağlam (ekran/işlem adı), hata türü ve zaman bilgisini taşır; kullanıcıyı tanımlayan veri taşımaz.
- **Çökme Raporu (Crash Report)**: Uygulamanın beklenmedik şekilde sonlandığı bir olayı temsil eder. Cihaz/uygulama sürümü, yığın izi (stack trace), oluştuğu zaman ve bağlam bilgisini taşır; Firebase Crashlytics'te saklanır.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Ağ/API hatası ile karşılaşan kullanıcıların %100'ü, teknik olmayan, anlaşılır bir hata mesajı ve (uygulanabilir olduğunda) yeniden deneme seçeneği görür.
- **SC-002**: Yakalanamayan çökmelerin %100'ü, cihaz internete bağlandığında en geç birkaç dakika içinde Crashlytics panelinde görünür hale gelir.
- **SC-003**: Üretimde, ne kullanıcıya yansıyan ne de herhangi bir raporlama kanalına düşen "sessiz" hata sayısı sıfırdır (kod incelemesiyle doğrulanabilir: her `catch`/`Result.failure` yolu bir raporlama çağrısına bağlıdır).
- **SC-004**: Bir geliştirici, üretimde oluşan bir çökmenin hangi ekranda/işlemde gerçekleştiğini, ek loglara ihtiyaç duymadan yalnızca crash raporundan anlayabilir.
- **SC-005**: Çevrimdışı durumda oluşan hatalarda kullanıcı, önbellekteki mevcut içeriğe erişimini kaybetmez.

## Assumptions

- Projenin tek uzak veri kaynağı, ders kataloğu ve içeriğini sağlayan mevcut API'dir; ileride eklenecek yeni uzak kaynaklar da aynı hata yönetimi ve raporlama desenini izlemelidir.
- Çökme raporlama için Firebase Crashlytics kullanılacaktır (kullanıcı isteğinde açıkça belirtildi); bu, projeye yeni bir bağımlılık olarak eklenecek ve README'nin "Key Decisions" bölümünde gerekçelendirilecektir.
- Kullanıcı davranış analitiği (analytics) bu kapsamın dışındadır; yalnızca hata ve çökme raporlama ele alınmaktadır.
- Mevcut hata bildirim altyapısı (geliştiriciye hata bildiren mevcut mekanizma) bu özelliğin üzerine inşa edileceği temel olarak kabul edilir; tamamen yeniden yazılmayacaktır.
- Yeniden deneme (retry) davranışı kullanıcı tarafından tetiklenir (elle "tekrar dene"); otomatik/arka planda sessiz yeniden deneme bu kapsamda varsayılan değildir.
- Çökme ve hata raporlarında kullanıcıyı kişisel olarak tanımlamayan, anonim bir cihaz/oturum kimliği kullanılabilir; e-posta, ad gibi doğrudan kimlik bilgisi kullanılmaz.
