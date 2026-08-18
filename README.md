# NativeMinds

Kısa ders içerikleri sunan, okunabilen ve dinlenebilen bir Android uygulaması.

## Teslimatlar

- **Mimari diyagram (Miro)**: [NativeMinds Mimari Diyagramı](https://miro.com/app/board/uXjVHxPA-AE=/) — modül bağımlılık grafiği ve kullanıcı akış şeması. Link herkese açık, giriş gerektirmez.
- **Demo videosu**: [docs/demo.mp4](docs/demo.mp4)
- **Kurulabilir build (signed APK)**: [docs/nativeminds-release.apk](docs/nativeminds-release.apk)

## Kurulum

`google-services.json` repoda mevcut. Bu repo private olduğu için Firebase projesini ve API key'lerini burada tutmakta sakınca görmedim — public bir repoda aynı tercihi yapmazdım.

Uygulamanın Supabase ve Gemini'ye bağlanabilmesi için proje kökündeki `local.properties` dosyasına şunları eklemek gerekiyor:

```properties
SUPABASE_URL=https://zqkbnifxyfztpkcpezaw.supabase.co
SUPABASE_ANON_KEY=sb_publishable_O1E_S1IEcDf6_vx7jXiIMQ_mdcGFYCc
GEMINI_API_KEY=<Google AI Studio key>
```

Bu değerler build sırasında `BuildConfig` alanlarına gömülüyor ([core/data/build.gradle.kts](core/data/build.gradle.kts)). Boş bırakılırsa proje derlenir ama içerik senkronu ve quiz üretimi çalışmaz.

Supabase tarafındaki tablolar ve örnek içerik için [supabase/schema.sql](supabase/schema.sql) ve [supabase/seed.sql](supabase/seed.sql) sırayla çalıştırılmalı.

```bash
./gradlew assembleDebug
```

## Mimari kararlar ve nedenleri

### 1. MVI, MVVM değil

Ekranlarda tek bir `onIntent()` girişi var, state'i sadece saf bir `reduce()` fonksiyonu yazıyor. Snackbar, navigasyon gibi tek seferlik olaylar için `Channel(BUFFERED)` kullandım, `SharedFlow` değil — çünkü `SharedFlow` ekran durdurulmuşken gelen bir event'i sessizce kaybedebiliyor, ben bunu istemedim.

Neden bu kadar uğraştım: Reader, Audio player, Paywall, Quiz gibi ekranların hepsinde aynı navigasyon/hata/snackbar ihtiyacı çıkacaktı. Bunu bir kere doğru kurmak, dört kere aynı şeyi elle yazmaktan daha mantıklıydı. Tek ekran için biraz fazla mühendislik gibi duruyor kabul ediyorum, ama gelecekteki ekranlara yatırım olarak düşündüm.

### 2. Katmanlı, modüler yapı

`:core:domain` tamamen saf Kotlin — Android'e, Hilt'e dair hiçbir şey yok. `:core:data`, `:core:database`, `:core:audio` bu domain'in gerçek implementasyonları. `:feature:*` modülleri sadece domain'e bağımlı, Room'u görmüyor.

Bunu yapmamın sebebi diyagramda güzel durması değil — gerçekten derleme zamanında zorlanıyor. `:feature:home` içinden yanlışlıkla Room entity'sine erişmeye çalışsan derlenmez. Evet, daha fazla modül, daha fazla boilerplate demek ama premium kontrolü ya da AI işlemesi gibi şeyler eklenince feature modüllerine dokunmadan use case katmanında hallediliyor.

### 3. Hilt, Koin değil

Bağımlılık grafiği derleme zamanında doğrulanıyor. Yeni bir feature eklerken binding'i unutursan Koin runtime'da patlar, kullanıcı ekranı açtığı anda çöker — Hilt'te ise hiç derlenmez. "Hatalar sessizce yutulmasın" kuralına bu daha çok uyuyordu.

### 4. Backend olarak Supabase, offline-first Room ile senkron

Kendi backend'imi yazmak ya da Firebase gibi bir şeye gitmek yerine Supabase'i seçtim. Sebebi basit: içerik tarafında ihtiyacım olan tek şey birkaç tablo — Supabase bana hazır bir Postgres, SQL editöründen içerik girme imkanı ve satır bazlı erişim kontrolü (RLS) veriyordu, hiç kendi auth/backend sunucumu kurmama gerek kalmadan. Kullanıcı girişi yok zaten, uygulama sadece herkese açık `anon` key ile okuma yapıyor; yazma yetkisi (`insert`/`update`/`delete`) hiç verilmedi, RLS politikası bunu veritabanı seviyesinde zaten engelliyor. İçerik değişikliği gerektiğinde Supabase'in kendi tablo arayüzünden veya SQL editöründen elle giriyorum, ayrı bir admin panel yazmadım.

Room tek doğruluk kaynağı. Senkronizasyon tüm tabloyu tek transaction içinde günceller — ya tamamen başarılı olur ya da hiç dokunmaz, yarım kalmış bir senkron durumu yaşanmaz.

Burada bir hata yakaladım geliştirirken: ders içerikleri ilk başta ekran açıldıkça tek tek çekiliyordu. Cihazı test ederken (uçak modu açıp bakınca) 40 dersten sadece 3'ünün offline okunabildiğini fark ettim — çünkü sadece daha önce açılmış olanlar cache'lenmişti. Çözüm basit: artık splash ekranında tüm ders içerikleri toplu çekiliyor, tek tek değil.

### 5. Sesli anlatım: cihaz üstü TTS, hazır ses dosyası değil

Android'in kendi `TextToSpeech` motorunu kullandım, önceden kaydedilmiş ses dosyaları yerine. Sunucu maliyeti sıfır, tamamen offline çalışıyor. Ses kalitesi elbette stüdyo kaydı gibi değil, cihazın varsayılan sesi — ama bu case study'nin odağı bu değildi.

Media3'ün `MediaSessionService`'ini kullanarak kilit ekranında ve arka planda çalabiliyor, sistem bildirimiyle birlikte.

Burada gerçekten uğraştıran bir bug vardı: TTS motoru tamamlanma bildirimini ayrı bir thread'den (binder thread) gönderiyor, ana thread'deki tıklamayla yarışıyordu. Sonuç: bazen duraklat-devam et yaptığınızda hikaye baştan başlıyordu. Çözümü, oynatma sırasını (queue) saf bir state machine olarak ayrı bir sınıfa çıkarıp thread güvenliğini tek noktadan garanti altına almak oldu. On tane birim testi bu tam senaryoyu (duraklatmanın kesintiye uğrattığı bir "tamamlandı" bildirimi, kuyruğu ilerletmemeli) kapsıyor artık.

### 6. Premium kısıtlama tek bir yerden yönetiliyor

`EntitlementRepository` tek doğruluk kaynağı, dağınık `isPremium` kontrolleri yok. Kısıtlı içerik gösterilirken kullanılan tip (`ReaderAccess.Preview`) içinde kilitli metni tutmuyor bile — yani bir UI hatası olsa da gösterilecek bir şey yok, çünkü elde hiç yok.

Ayrıca kilit kontrolünü iki yerde yapıyorum: buton UI'da gizli, ama arkadaki use case da ayrıca kontrol ediyor. Sadece butonu gizlemek güvenlik değil, biri deep link ile doğrudan erişmeye çalışırsa diye asıl kontrol use case seviyesinde.

### 7. AI özelliği (Gemini quiz) — SDK'dan vazgeçtim

İlk başta Google'ın resmi Gemini SDK'sını kullandım. Gerçek cihazda test ederken "Test" butonuna basar basmaz uygulama çöktü. Sebebini bulmak biraz uğraştırdı: SDK, Ktor'un eski bir sürümüne göre derlenmiş, ama projede zaten Supabase için daha yeni bir Ktor sürümü vardı. Gradle tek bir sürüme zorlayınca SDK'nın beklediği bir sınıf ortadan kayboluyordu — bu hem derleme zamanında hem testlerde hiç görünmüyordu, sadece gerçek cihazda ortaya çıkıyordu.

Çözüm: SDK'yı bırakıp Gemini'ye doğrudan Ktor ile REST isteği atmak. Biraz daha fazla elle yazılmış kod demek ama bağımlılık çakışması riski yok.

Ayrıca ilk denediğim model adı (`gemini-2.5-flash`) API'den 404 döndü — o anahtar için artık kullanılamıyormuş. Gerçek bir istekle deneyerek `gemini-flash-latest` diye bir alias buldum, bu daima geçerli bir modele işaret ediyor.

Modelin döndürdüğü JSON'u şemayla sınırlıyorum ama buna da güvenmiyorum — mapper içinde ayrıca soru sayısı, doğru cevap index'i gibi şeyleri kontrol ediyorum. Model teknik olarak geçerli ama anlamsız bir JSON dönebilir, ikisi ayrı hatalar.

### 8. Hata takibi ve analitik ayrı modüllerde

Crashlytics ve Firebase Analytics'i domain katmanındaki birer arayüzün (`ErrorReporter`, `AnalyticsReporter`) arkasına sakladım. Yarın başka bir servise geçmek istersem sadece implementasyonu değiştiriyorum, kodun geri kalanı bundan habersiz.

### 9. Güvenlik için sonradan sıkılaştırdığım iki şey

Release build'de R8 kapalıydı, açtım — obfuscation olmadan APK'yı decompile etmek çok kolay. Gemini API key'i de başta URL'nin query parametresinde gidiyordu, bunu header'a taşıdım çünkü URL'ler proxy/access log'larda tam haliyle kayıt altına alınabiliyor.

### 10. İçerik modelini "hikaye"den "ders"e çevirdim

Uygulama başta kurgu hikayeler için tasarlanmıştı, sonra ders/konu içeriğine döndü. Bu sadece isim değişikliği değildi — `Story` her yerde `Lesson` oldu, veritabanı migration'ı yazıldı, eski verinin yeni yapıyla bir karşılığı olmadığı için eski satırları silip yeniden seed ettim. 100'den fazla dosyayı etkileyen bir değişiklik olduğu için bunu tek başına, içerik değişikliğinden ayrı bir adım olarak yaptım — karışmasın diye.

## AI'ı nasıl yönlendirdim

Genelde direkt "şunu kodla" demedim, önce plan yaptırdım. Bir özellik isteyeceğim zaman önce spec, sonra plan/araştırma, sonra görev listesi çıkarttırdım (`specs/` klasöründeki her alt klasör böyle oluştu) — kodu bu üçünü okuyup onayladıktan sonra yazdırdım. Bunun kazandırdığı şey şuydu: hata çoğunlukla koda hiç ulaşmadan, plan aşamasında yakalanıyordu. Mesela quiz özelliğinde tek satırlık bir açıklamayla ("çoktan seçmeli soru sayfası") spec çıkartınca AI, 5 soruluk ve kaydedilip tekrar kullanılan bir quiz tasarlamıştı. Gerçek tasarım mockup'ını ve "soru anlık olarak Gemini'den gelecek" açıklamamı verip planı tekrar gözden geçirtince, kod hiç yazılmadan tek soru + her seferinde taze üretim şekline döndü. Yani hatayı kod yazıldıktan sonra düzeltmek yerine, plan aşamasında önünü kestim.

Kod yazıldıktan sonra da direkt kabul etmedim, her seferinde okuyup review ettim, gerekçesini anlamadığım ya da proje kurallarına aykırı gördüğüm yerde geri gönderdim. Bazı örnekler:

- Reader ekranında bir tıklama olayını AI proje kuralını ("her kullanıcı hareketi bir intent") harfiyen uygulayıp gereksiz yere ViewModel içinde dallandırmıştı — aynı dokümanın başka bir kuralı bunu yasaklıyordu. İki kuralı yan yana koyup navigasyonun düz bir callback olması gerektiğine ben karar verdim.
- CLAUDE.md'deki "önemli olayları logla" kuralını AI kendi başına yorumlayıp hangi event'lerin loglanacağına kendi karar vermişti, isteğim dışında. Bunu geri aldım — kuralın var olması, detayına AI'ın karar vereceği anlamına gelmiyor.
- Gemini SDK'sı derlendi, testler geçti ama gerçek cihazda "Test" butonuna basınca uygulama çöktü. Derleyicinin göremeyeceği bir bağımlılık çakışmasıydı, sadece cihazda deneyerek yakaladım ve SDK'yı bırakıp elle REST isteği atan bir çözüme çevirttim.

Genel olarak öğrendiğim şey: planı iyi okursan çoğu hatayı kod yazılmadan önlüyorsun; kod yazıldıktan sonra kalan hatalar ise genelde derleyicinin ya da testin göremeyeceği, sadece gerçek cihazda ya da dikkatli bir review'da ortaya çıkan şeyler oluyor.

## Süreci hızlandırmak için kurduğum yapılar

Kod yazmayı hızlandırmak için birkaç şey kurdum, sıfırdan her seferinde aynı anlatımı tekrarlamamak için:

- **`CLAUDE.md`** — proje kök dizininde, her oturumun başında otomatik okunan bir kural dosyası. Modül sınırları, MVI'nin nasıl şekillendiği, "kod içinde yorum yok" gibi kurallar burada tanımlı. Bunu bir kere yazınca her seferinde tekrar anlatmak zorunda kalmadım.
- **Spec-kit komutları** (`speckit-specify`, `speckit-plan`, `speckit-tasks`, `speckit-implement`, `speckit-clarify`, `speckit-analyze`, `speckit-checklist`) — her yeni özellik önce bir `spec.md`, sonra `plan.md`/`research.md`, sonra `tasks.md` üzerinden geçti (`specs/` klasöründeki tüm alt klasörler bunun ürünü). Büyük bir özelliği ("sesli anlatım ekle" gibi) direkt kod yazdırmak yerine, önce tasarım kararlarını görüp onaylayıp sonra uygulattım.
- **`smart-commit`** — değişiklikleri anlamlı gruplara ayırıp tek tek commit'leyen bir komut, her seferinde "şunu ayrı commit'le" diye uğraşmamak için.
- **DesignSync (Claude Design MCP)** — tasarımın tek kaynağı bir Claude Design projesiydi. Ekran kodlamadan önce bu MCP üzerinden renk, spacing, tipografi gibi tokenları okutup Compose'a öyle geçirdim, elle tahmin etmek yerine.
- **Miro MCP** — mimari diyagramı (modüller arası bağımlılıklar, katmanlar) Miro board'u üzerinde oluşturup güncellerken kullandım, ekran görüntüsü alıp elle çizmek yerine.
- **Supabase MCP** — uzak ders içeriği özelliğini kurarken (proje oluşturma, tablo/şema kontrolü, seed verisinin gerçekten yazıldığını doğrulama) Supabase tarafını konsola girmeden buradan yönettim.
- **`.claude/settings.local.json` izin listesi** — `./gradlew assembleDebug`, `git commit` gibi sık kullanılan komutları her seferinde onay istemeden çalışacak şekilde izinli hale getirdim, build/test döngüsü yavaşlamasın diye.

## Bilinen eksikler / kestirmeler

- Satın alma tamamen mock — gerçek bir ödeme sağlayıcısı bağlı değil.
- Quiz soruları hiçbir yere kaydedilmiyor, her "Test" tıklamasında yeniden üretiliyor — offline çalışmıyor.
- API key'ler derlenmiş APK içinde duruyor (BuildConfig üzerinden) — gerçek bir yayın için secret management gerekir.
