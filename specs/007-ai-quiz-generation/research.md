# Araştırma: AI ile Üretilmiş Çoktan Seçmeli Test

## R1 — Gemini istemci kütüphanesi

**Karar (DÜZELTİLDİ — gerçek cihazda çöktüğü görüldükten sonra)**: İlk planda seçilen
`com.google.ai.client.generativeai:generativeai` (resmi Android/Kotlin SDK'sı) **terk edildi**;
onun yerine mevcut Ktor `HttpClient` (Supabase için zaten kurulu olan `ktor-client-okhttp`) ile
Gemini REST API'sine (`generativelanguage.googleapis.com/v1beta/.../generateContent`) doğrudan
istek atılıyor.

**Neden değişti**: `/speckit-implement` sırasında derleme ve birim testleri sorunsuz geçti, ama
gerçek cihazda "Test"e ilk basışta uygulama çöktü:
`ClassNotFoundException: io.ktor.client.plugins.HttpTimeout`. Kök neden: `generativeai:0.9.0`
Ktor **2.3.2**'ye göre derlenmiş, ama projenin Supabase bağımlılığı Ktor **3.3.1** istiyor.
Gradle tüm modüllerde `io.ktor:ktor-client-core` için tek bir sürüm çözer (en yüksek talep
edilen), yani `generativeai`'ın transitive 2.3.2 isteği sessizce 3.3.1'e yükseltiliyor — ve
Ktor 3.x'te `HttpTimeout` artık bir `class` değil, top-level bir `val` (kaynak-uyumsuz bir API
değişikliği). Sonuç: derleme başarılı, ama çalışma zamanında dex'te aranan sınıf hiç yok.
Bu, yalnızca gerçek cihazda "Test"e basılınca ortaya çıktı — birim testleri Hilt grafiğini veya
gerçek Gemini SDK sınıf yüklemesini tetiklemiyordu.

**Düzeltme**: `generativeai` bağımlılığı tamamen kaldırıldı; `core/data/.../remote/quiz/`
altında düz bir Ktor `HttpClient` (zaten var olan `ktor-client-okhttp` + yeni eklenen
`ktor-client-content-negotiation`/`ktor-serialization-kotlinx-json`) ile REST isteği elle
kuruldu (`GeminiGenerateContentDto` ailesi). Bu, ilk planda "gereksiz karmaşıklık" diye
reddedilen alternatifti — ama gerçek cihaz testi, resmi SDK'nın bu proje için pratikte
*çalışmadığını* gösterdi, bu yüzden "daha az kod" argümanı geçersizleşti.

**Ders**: `./gradlew compileDebugKotlin` ve birim testlerinin geçmesi, üçüncü taraf bir SDK'nın
projenin geri kalanıyla runtime'da uyumlu olduğunu KANITLAMAZ — sınıf yükleme zamanlı hatalar
yalnızca gerçek bir cihaz/emülatörde, gerçek bir kullanıcı akışı tetiklenince ortaya çıkar.

## R2 — Yapılandırılmış çıktı (JSON şeması) ve model adı

**Karar**: İstek gövdesinde `generationConfig.responseMimeType = "application/json"` ve açık bir
`responseSchema` (question: STRING, options: ARRAY[4] of STRING, correctOptionIndex: INTEGER,
explanation: STRING) gönderiliyor. Model adı **`gemini-flash-latest`** (ilk planda
`gemini-2.5-flash` seçilmişti — bkz. aşağıdaki düzeltme notu).

**Model adı düzeltmesi (gerçek cihazda keşfedildi)**: `gemini-2.5-flash` ile yapılan gerçek
istek `404 NOT_FOUND — "This model models/gemini-2.5-flash is no longer available to new
users"` döndü: Google bu modeli yeni oluşturulan API anahtarları için kaldırmış. `curl` ile
`ListModels` uç noktası sorgulanıp birkaç güncel model denendi; `gemini-flash-latest` (her zaman
güncel kararlı flash modeline işaret eden bir takma ad, şu an `gemini-3.7-flash`'a çözülüyor)
hem `responseSchema` ile yapılandırılmış çıktıyı hem de düz metni doğru döndürdü. Sabit bir
sürüm numarası yerine `-latest` takma adı tercih edildi ki model bir sonraki sürüme
geçtiğinde kod değişmesin.

**Gerekçe**: Serbest metin yanıtını regex/heuristik ile ayrıştırmak kırılgandır (FR-006'daki
"geçersiz/eksik yanıt" uç durumunu büyütür). Şema zorlaması, ayrıştırma hatalarını modelin
kendisine (JSON şemasına uymaya zorlanmış hâliyle) devrederek istemci tarafı hata yüzeyini
daraltır.

**Değerlendirilen alternatifler**: Serbest metinden regex ile 4 şık + doğru cevap çıkarmak —
reddedildi, kırılgan ve test edilmesi zor.

## R3 — API anahtarı yönetimi

**Karar**: `GEMINI_API_KEY`, Supabase anahtarlarıyla birebir aynı desenle sağlanır: kök
`local.properties` dosyasına eklenir (git-ignored), `core/data/build.gradle.kts` bunu okuyup
`buildConfigField("String", "GEMINI_API_KEY", ...)` olarak enjekte eder, `NetworkModule` bunu
`GenerativeModel` oluştururken kullanır.

**Gerekçe**: Projede zaten kurulu, çalışan ve dokümante bir desen var (`SUPABASE_URL`/
`SUPABASE_ANON_KEY`); yeni bir secrets-yönetim aracı eklemek (ör. secrets-gradle-plugin)
gerekçesiz karmaşıklık olur.

**Not (Cut Corner)**: Anahtar yine de derlenmiş APK içinde (BuildConfig sabiti olarak) yer alır;
bu, case study kapsamında kabul edilen bir kısayoldur (kullanıcı "doğrudan istemciden" çağrı
yapılmasını seçti). Production'da bir backend/proxy arkasına alınması gerekir — bu README "Cut
Corners / Assumptions"a eklenecek.

## R4 — Kalıcılık yok

**Karar**: Üretilen soru Room'da veya başka bir kalıcı depoda saklanmaz; yalnızca `QuizViewModel`
state'inde (ekran ömrü boyunca) tutulur. Ekran her açıldığında (`lessonId` değiştiğinde veya
yeniden deneme tetiklendiğinde) yeni bir Gemini çağrısı yapılır.

**Gerekçe**: Kullanıcı özelliği açıkça "anlık" (canlı, her seferinde taze) olarak tanımladı; bu,
spec'teki ilk taslağın varsaydığı "5 sorulu, önbelleğe alınan quiz" senaryosundan farklı ve daha
basit bir kapsam. Kalıcılık eklemek (Room entity/DAO, senkronizasyon, "yeniden üret" ayrımı)
istenmeyen bir özelliği desteklemek için gereksiz karmaşıklık olurdu — "İstenenin ötesinde özellik
ekleme" ilkesiyle çelişir.

**Değerlendirilen alternatifler**: `LessonContentEntity` desenini izleyen bir `QuizEntity` ile
son üretilen soruyu önbelleğe almak — reddedildi (istenmeyen kapsam; offline-first ilkesi yalnızca
hikaye içeriği/sesi için ağsız erişimi zorunlu kılıyor, bu isteğe bağlı AI özelliği için değil).

## R5 — Premium gating uygulaması

**Karar**: "Test" butonu, Reader ekranında yalnızca `ReaderAccess` sonucu `Full` olduğunda (yani
kullanıcı zaten premium ve hikayenin tamamını görebiliyorken) render edilir — tasarımdaki 2a
(tam erişim) ekranında buton var, 2b (ücretsiz önizleme) ekranında ise zaten tüm okuma alanı bir
paywall kartıyla kaplı ve böyle bir buton yok. Buna ek olarak, `GenerateQuizUseCase` UI'a
güvenmeden `EntitlementRepository.isPremium()`'u kendisi de kontrol eder ve premium değilse
Gemini'ye hiç istek atmadan `QuizGenerationResult.Locked` döner.

**Gerekçe**: Tasarımda ayrı bir "kilitli test butonu" görsel durumu yok; var olan tasarımın
gerçek davranışını olduğu gibi izlemek (buton sadece tam erişimde görünür) en az sürprizli
seçenek. Yine de UI katmanına güvenmemek (savunma amaçlı çift kontrol) SC-004'ün ("sıfır yetkisiz
AI çağrısı") her koşulda doğru olmasını garanti eder — ekran bir şekilde yanlışlıkla açılsa bile.

**Değerlendirilen alternatifler**: Serbest kullanıcılara da kilitli bir "Test" rozeti göstermek —
tasarım referansı bulunmadığı için reddedildi; gerekirse ayrı bir tasarım isteğiyle sonradan
eklenebilir.

## R6 — Analitik olayları

**Karar**: Mevcut `AnalyticsEvent` sealed class'ına iki yeni durum eklenir:
`QuizRequested(lessonId: Long)` ve `QuizAnswered(lessonId: Long, isCorrect: Boolean)`. Zaten
tanımlı ama hiçbir yerden tetiklenmeyen `AiFeatureUsed(lessonId, featureName)` olayı, soru
başarıyla üretildiğinde `featureName = "quiz"` ile bir kez loglanır (bu olayı ilk kez gerçek bir
özelliğe bağlayan kullanım budur).

**Gerekçe**: Mevcut `AnalyticsReporter`/`AnalyticsEvent`/`toFirebaseEvent()` deseniyle birebir
tutarlı (yeni `data class`, `toFirebaseEvent()`'te yeni `when` dalı, snake_case isimler:
`quiz_requested`, `quiz_answered`). `AiFeatureUsed`'ı kullanmak, case study'nin "AI feature used"
zorunlu funnel olayını gerçek bir çağrıyla karşılıyor.

## R7 — Prompt tasarımı

**Karar**: Tek bir kullanıcı mesajı gönderilir: hikaye başlığı + yazarı + tam gövde metni, ardından
"Bu hikayeye dayalı, tam olarak 4 şıklı ve tek doğru cevaplı bir okuma anlama sorusu üret. Doğru
cevabın kısa (1-2 cümlelik) bir açıklamasını da ver. Yanıtı yalnızca verilen şemaya uygun JSON
olarak döndür." talimatı Türkçe olarak eklenir (uygulama arayüzü Türkçe olduğundan soru da Türkçe
üretilmeli — hikaye metni İngilizce olsa bile).

**Gerekçe**: Case study'nin mevcut içerik seti İngilizce (`The Lighthouse Keeper's Last Letter`
vb.) ama uygulama arayüzü ve tasarım metinleri Türkçe (`Soru 1 / 1`, `Doğru`, `Yanlış — doğrusu B
şıkkı`). Sorunun da Türkçe üretilmesi, kullanıcı deneyiminde dil tutarlılığını korur.

**Değerlendirilen alternatifler**: Hikaye dilinde (İngilizce) soru üretmek — reddedildi, arayüzün
geri kalanıyla tutarsız olurdu.
