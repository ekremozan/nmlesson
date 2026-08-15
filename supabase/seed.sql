-- One-time seed: run once after schema.sql, in the Supabase SQL editor, to populate the remote
-- catalog with the same 40 lessons the app used to ship as its hardcoded dummy content.
-- Generated from core/data/src/main/java/com/example/nativeminds/data/local/DummyLessonSeed.kt
-- and DummyLessonContentSeed.kt (spec 004-remote-lesson-content, FR-009).

insert into public.lessons (id, subject, title, teaser, minutes, has_audio, is_locked, image) values
  (1, 'Biyoloji', 'Hücre Yapısı ve Organeller', 'Zarın içindeki küçük fabrika.', 6, true, false, 'subject_biology'),
  (2, 'Biyoloji', 'Mitoz ve Mayoz Bölünme', 'Bir hücrenin ikiye, dörde bölünme hikayesi.', 7, true, false, 'subject_biology'),
  (3, 'Biyoloji', 'Kalıtım ve Mendel Genetiği', 'Bezelyelerden öğrendiğimiz kalıtım kuralları.', 6, true, false, 'subject_biology'),
  (4, 'Biyoloji', 'DNA ve Protein Sentezi', 'Yaşamın şifresi nasıl okunur?', 8, true, true, 'subject_biology'),
  (5, 'Biyoloji', 'İnsan Sindirim Sistemi', 'Ağızdan başlayan uzun bir yolculuk.', 7, true, true, 'subject_biology'),
  (6, 'Biyoloji', 'Dolaşım Sistemi ve Kalp', 'Vücudun durmaksızın çalışan pompası.', 6, true, true, 'subject_biology'),
  (7, 'Biyoloji', 'Solunum Sistemi', 'Her nefeste gerçekleşen gaz alışverişi.', 5, true, true, 'subject_biology'),
  (8, 'Biyoloji', 'Boşaltım Sistemi', 'Vücudun atık yönetim merkezi.', 5, true, true, 'subject_biology'),
  (9, 'Biyoloji', 'Ekosistem ve Enerji Akışı', 'Güneşten başlayan enerji zinciri.', 6, true, true, 'subject_biology'),
  (10, 'Biyoloji', 'Evrim ve Doğal Seçilim', 'Türlerin zaman içindeki değişim hikayesi.', 7, true, true, 'subject_biology'),
  (11, 'Tarih', 'Osmanlı Devleti''nin Kuruluşu', 'Küçük bir uç beyliğinden bir cihan devletine.', 6, true, false, 'subject_history'),
  (12, 'Tarih', 'İstanbul''un Fethi ve Sonuçları', 'Bir çağın kapanıp diğerinin açılması.', 7, true, false, 'subject_history'),
  (13, 'Tarih', 'Osmanlı''da Duraklama ve Gerileme', 'Bir imparatorluğun büyümesi nasıl durdu?', 7, true, false, 'subject_history'),
  (14, 'Tarih', 'Fransız İhtilali ve Etkileri', 'Bir devrimin tüm dünyaya yayılan fikirleri.', 6, true, true, 'subject_history'),
  (15, 'Tarih', 'I. Dünya Savaşı ve Osmanlı', 'Bir imparatorluğun son büyük savaşı.', 8, true, true, 'subject_history'),
  (16, 'Tarih', 'Milli Mücadele ve Kurtuluş Savaşı', 'Bir milletin yeniden doğuş mücadelesi.', 8, true, true, 'subject_history'),
  (17, 'Tarih', 'Atatürk İlke ve İnkılapları', 'Yeni bir devletin temel taşları.', 7, true, true, 'subject_history'),
  (18, 'Tarih', 'II. Dünya Savaşı', 'İnsanlık tarihinin en yıkıcı çatışması.', 8, true, true, 'subject_history'),
  (19, 'Tarih', 'Soğuk Savaş Dönemi', 'Silahsız ama gerilimli bir küresel mücadele.', 7, true, true, 'subject_history'),
  (20, 'Tarih', 'Türkiye Cumhuriyeti''nin Dış Politikası', 'Barış içinde bağımsız bir yol arayışı.', 6, true, true, 'subject_history'),
  (21, 'Coğrafya', 'Türkiye''nin Coğrafi Konumu', 'Üç kıtanın kesiştiği noktada bir ülke.', 6, true, false, 'subject_geography'),
  (22, 'Coğrafya', 'İklim Tipleri ve Türkiye İklimi', 'Aynı ülkede dört farklı iklim.', 7, true, false, 'subject_geography'),
  (23, 'Coğrafya', 'Türkiye''nin Yer Şekilleri', 'Dağlardan ovalara uzanan bir yüzey.', 6, true, false, 'subject_geography'),
  (24, 'Coğrafya', 'Nüfus ve Yerleşme', 'İnsanların nerede ve neden yaşadığı.', 6, true, true, 'subject_geography'),
  (25, 'Coğrafya', 'Göç ve Şehirleşme', 'Köyden kente uzanan büyük hareketlilik.', 6, true, true, 'subject_geography'),
  (26, 'Coğrafya', 'Tarım ve Hayvancılık', 'Toprağın ve iklimin şekillendirdiği üretim.', 6, true, true, 'subject_geography'),
  (27, 'Coğrafya', 'Sanayi ve Ekonomik Faaliyetler', 'Hammaddeden ürüne uzanan üretim zinciri.', 6, true, true, 'subject_geography'),
  (28, 'Coğrafya', 'Doğal Kaynaklar ve Enerji', 'Yer altından ve güneşten gelen güç.', 6, true, true, 'subject_geography'),
  (29, 'Coğrafya', 'Çevre Sorunları ve Sürdürülebilirlik', 'Doğayla dengeyi yeniden kurmak.', 6, true, true, 'subject_geography'),
  (30, 'Coğrafya', 'Bölgesel Kalkınma Projeleri (GAP vb.)', 'Suyla dönüşen bir bölgenin hikayesi.', 7, true, true, 'subject_geography'),
  (31, 'Kimya', 'Maddenin Yapısı ve Atom Modelleri', 'Her şeyin en küçük yapı taşı.', 6, true, false, 'subject_chemistry'),
  (32, 'Kimya', 'Periyodik Sistem', 'Elementlerin düzenli bir haritası.', 6, true, false, 'subject_chemistry'),
  (33, 'Kimya', 'Kimyasal Bağlar', 'Atomları bir arada tutan görünmez bağlar.', 7, true, false, 'subject_chemistry'),
  (34, 'Kimya', 'Mol Kavramı ve Kimyasal Hesaplamalar', 'Görünmeyen taneleri saymanın yolu.', 7, true, true, 'subject_chemistry'),
  (35, 'Kimya', 'Gazların Genel Özellikleri', 'Sınır tanımayan bir hâl.', 6, true, true, 'subject_chemistry'),
  (36, 'Kimya', 'Karışımlar ve Ayırma Teknikleri', 'Birlikte olan ama birleşmeyen maddeler.', 6, true, true, 'subject_chemistry'),
  (37, 'Kimya', 'Asit-Baz Dengesi', 'Ekşi ile acı arasındaki denge.', 6, true, true, 'subject_chemistry'),
  (38, 'Kimya', 'Kimyasal Tepkimeler ve Denge', 'İleri ve geri işleyen bir denge oyunu.', 7, true, true, 'subject_chemistry'),
  (39, 'Kimya', 'Organik Kimyaya Giriş', 'Karbonun kurduğu sonsuz çeşitlilik.', 7, true, true, 'subject_chemistry'),
  (40, 'Kimya', 'Enerji ve Kimyasal Tepkimeler', 'Her tepkimenin bir enerji hikayesi vardır.', 6, true, true, 'subject_chemistry');

insert into public.lesson_content (lesson_id, author, body) values
  (1, 'Dr. Elif Kaya', 'Hücre, canlıların yapısal ve işlevsel en küçük birimidir. Her hücre, onu dış ortamdan ayıran bir hücre zarıyla çevrilidir ve içinde sitoplazma adı verilen jel kıvamında bir sıvı bulunur. Bu sıvının içinde, hücrenin yaşamsal faaliyetlerini yürüten küçük yapılar, yani organeller yer alır.

Ökaryot hücrelerde çekirdek, kalıtım maddesini taşıyan ve hücrenin yönetim merkezi sayılan organeldir. Mitokondri enerji üretiminden, ribozom protein sentezinden, Golgi aygıtı ise maddelerin paketlenip taşınmasından sorumludur. Bu organellerin her biri, bir fabrikadaki farklı departmanlar gibi uyum içinde çalışır.

Bitki hücreleri, hayvan hücrelerinden farklı olarak hücre duvarı ve kloroplast içerir. Hücre duvarı bitkiye mekanik destek sağlarken, kloroplast fotosentez yaparak güneş enerjisini kimyasal enerjiye dönüştürür. Bu farklar, bitki ve hayvan hücrelerinin yaşam biçimlerindeki temel ayrımı da açıklar.'),
  (2, 'Dr. Elif Kaya', 'Mitoz, bir hücrenin kendisiyle genetik olarak özdeş iki yavru hücreye bölünmesidir. Büyüme, yara iyileşmesi ve eşeysiz üreme gibi süreçlerde görülür. Bölünme öncesinde kromozomlar eşlenir, ardından hücre çekirdeği ve sitoplazma sırayla ikiye ayrılır.

Mayoz ise üreme hücrelerinin oluşumunda gerçekleşen, kromozom sayısını yarıya indiren özel bir bölünme türüdür. İki art arda bölünme evresi içerir ve sonucunda dört farklı yavru hücre oluşur. Bu çeşitlilik, türlerin genetik çeşitliliğinin temel kaynağıdır.

Mitoz ve mayoz arasındaki en önemli fark, oluşan hücrelerin genetik içeriğidir: mitoz özdeş hücreler üretirken, mayoz genetik olarak birbirinden farklı üreme hücreleri üretir. Bu iki mekanizma, çok hücreli canlıların hem büyümesini hem de nesiller boyu çeşitliliğini mümkün kılar.'),
  (3, 'Dr. Elif Kaya', 'Gregor Mendel, 19. yüzyılda bezelye bitkileri üzerinde yaptığı deneylerle kalıtımın temel kurallarını ortaya koydu. Her özelliğin, ana babadan birer tane alınan iki alel tarafından kontrol edildiğini ve bu allellerin bağımsız olarak yavru döllere aktarıldığını gözlemledi.

Baskın ve çekinik alel kavramları, Mendel''in en önemli katkılarından biridir. Baskın alel, birlikte bulunduğu çekinik aleli fenotipte bastırır; çekinik özelliğin ortaya çıkması için bireyin iki çekinik alele sahip olması gerekir.

Mendel''in kalıtım kuralları, günümüzde insan genetiği başta olmak üzere birçok alanda hastalıkların kalıtsal geçişini anlamada hâlâ temel bir çerçeve sunar.'),
  (4, 'Dr. Elif Kaya', 'DNA, canlıların kalıtsal bilgisini taşıyan çift sarmal yapılı bir moleküldür. Bu bilgi, dört farklı bazın (adenin, timin, guanin, sitozin) diziliş sırasında saklanır ve hücre bölünmesinde eksiksiz biçimde kopyalanarak yeni hücrelere aktarılır.

Protein sentezi, DNA''daki bilginin önce mRNA''ya kopyalanması (transkripsiyon), ardından ribozomda bu bilginin amino asit dizisine çevrilmesiyle (translasyon) gerçekleşir. Ortaya çıkan protein, hücrenin yapısal veya işlevsel bir görevini üstlenir.

Bu iki aşamalı süreç, hücrenin hangi genleri ne zaman ve ne kadar kullanacağını belirleyerek canlının gelişimini ve günlük işleyişini yönetir.'),
  (5, 'Dr. Elif Kaya', 'Sindirim sistemi, alınan besinleri hücrelerin kullanabileceği küçük moleküllere ayıran organlar bütünüdür. Ağızda başlayan mekanik ve kimyasal sindirim, yemek borusu, mide ve ince bağırsak boyunca devam eder.

Mide, güçlü asidik ortamı ve enzimleriyle proteinlerin sindirimine başlar. İnce bağırsak ise karaciğer ve pankreasın salgıladığı sıvıların yardımıyla sindirimin büyük kısmını tamamlar ve besin öğelerinin kana geçtiği asıl emilim bölgesidir.

Kalın bağırsak, sindirilemeyen artıklardan suyun geri emilmesini sağlar ve sistemin son basamağını oluşturur. Bu uzun yolculuğun her adımı, vücudun enerji ve yapı taşı ihtiyacını karşılamak için özenle koordine edilir.'),
  (6, 'Dr. Elif Kaya', 'Dolaşım sistemi, kalp, damarlar ve kandan oluşan; oksijen, besin ve atık maddelerin vücutta taşınmasını sağlayan bir ağdır. Kalp, dört odacıklı yapısıyla kanı hem akciğerlere hem de vücudun geri kalanına pompalar.

Atardamarlar kanı kalpten uzaklaştırırken, toplardamarlar kanı kalbe geri getirir. Kılcal damarlar ise oksijen ve besin alışverişinin gerçekleştiği en ince ve en yaygın damar ağını oluşturur.

Kalbin düzenli ritmi, özel uyarı-ileti sistemi sayesinde sağlanır. Bu ritmin bozulması, dolaşım sisteminin tüm vücuda hizmet etme kapasitesini doğrudan etkiler.'),
  (7, 'Dr. Elif Kaya', 'Solunum sistemi, vücudun oksijen almasını ve karbondioksit vermesini sağlayan organlardan oluşur. Hava burun veya ağızdan girer, gırtlak ve soluk borusundan geçerek akciğerlere ulaşır.

Akciğerlerdeki milyonlarca küçük hava kesesi olan alveoller, gaz alışverişinin gerçekleştiği asıl yüzeyi oluşturur. İnce zarları sayesinde oksijen kana geçerken, karbondioksit kandan alveollere aktarılır.

Diyafram kasının kasılıp gevşemesi, akciğerlerin genişleyip daralmasını sağlayarak solunumu mekanik olarak yönetir. Bu sürekli döngü, hücrelerin enerji üretimi için gerekli oksijeni asla kesintiye uğratmaz.'),
  (8, 'Dr. Elif Kaya', 'Boşaltım sistemi, metabolizma sonucu oluşan atık maddelerin vücuttan uzaklaştırılmasından sorumludur. Böbrekler, bu sistemin en önemli organı olarak kanı sürekli süzer ve fazla suyu, tuzu ve üreyi ayırır.

Böbreklerdeki nefron adı verilen milyonlarca küçük süzme birimi, kanı filtreleyip gerekli maddeleri geri emerken, gereksiz olanları idrar olarak toplar. Bu idrar, üreterler yoluyla mesaneye taşınır ve orada depolanır.

Boşaltım sistemi yalnızca atık uzaklaştırmakla kalmaz, aynı zamanda vücuttaki su ve tuz dengesini de düzenleyerek iç ortamın kararlılığını korur.'),
  (9, 'Dr. Elif Kaya', 'Ekosistem, belirli bir bölgedeki canlılar ile bu canlıların etkileşimde bulunduğu cansız çevrenin bütününü ifade eder. Bu sistemin işleyişi, enerjinin üreticilerden tüketicilere doğru aktarılmasına dayanır.

Üreticiler, güneş enerjisini fotosentez yoluyla kimyasal enerjiye dönüştürerek besin zincirinin ilk halkasını oluşturur. Otçul tüketiciler bu enerjiyi üreticilerden, etçil tüketiciler ise diğer hayvanlardan alır.

Her enerji aktarımında bir miktar enerji ısı olarak kaybolur, bu yüzden besin zincirindeki basamak sayısı sınırlıdır. Ayrıştırıcılar ise ölü organizmalardaki enerjiyi ve maddeleri tekrar ekosisteme kazandırarak döngüyü tamamlar.'),
  (10, 'Dr. Elif Kaya', 'Evrim, popülasyonların genetik yapısının nesiller boyunca değişmesi sürecidir. Charles Darwin''in ortaya koyduğu doğal seçilim kuramı, bu değişimin en temel mekanizmalarından biri olarak kabul edilir.

Doğal seçilime göre, bir popülasyondaki bireyler arasında doğal olarak varyasyon bulunur. Ortama daha iyi uyum sağlayan özelliklere sahip bireyler hayatta kalma ve üreme şansını artırır, bu özellikler zamanla popülasyonda yaygınlaşır.

Bu süreç, milyonlarca yıl içinde türlerin ortaya çıkmasına, değişmesine ve bazen de yok olmasına yol açar. Fosil kayıtları, karşılaştırmalı anatomi ve moleküler biyoloji bulguları, evrim sürecine dair güçlü kanıtlar sunar.'),
  (11, 'Doç. Dr. Mehmet Aydın', 'Osmanlı Devleti, 13. yüzyılın sonlarında Anadolu''nun kuzeybatısında, Söğüt ve çevresinde küçük bir uç beyliği olarak Osman Bey önderliğinde kuruldu. Bizans sınırındaki bu konum, beyliğin hem askeri hem de ekonomik açıdan hızla güçlenmesine zemin hazırladı.

Orhan Bey döneminde Bursa''nın fethiyle beylik başkent kazandı ve Rumeli''ye geçişle birlikte Balkanlar''da genişlemeye başladı. Bu genişleme, sadece askeri başarılarla değil, fethedilen bölgelerdeki yönetim anlayışıyla da destekleniyordu.

14. ve 15. yüzyıllar boyunca art arda gelen padişahlar, beyliği önce bir bölgesel güce, ardından bir imparatorluğa dönüştürdü. Bu sürecin en önemli dönüm noktalarından biri, ileride İstanbul''un fethiyle taçlanacaktı.'),
  (12, 'Doç. Dr. Mehmet Aydın', 'II. Mehmed, 1453 yılında uzun bir kuşatmanın ardından Bizans''ın başkenti İstanbul''u fethetti. Bu zafer, yalnızca askeri bir başarı değil, aynı zamanda dönemin savaş teknolojisinde de bir dönüm noktasıydı; büyük toplar kuşatmanın seyrini belirledi.

Fetih, Bizans İmparatorluğu''nun sonunu getirirken Osmanlı Devleti''ni bir dünya gücü hâline getirdi. İstanbul, yeni başkent olarak imar edildi ve farklı din ile milletlerden insanların bir arada yaşadığı kozmopolit bir merkez hâline geldi.

Tarihçiler, İstanbul''un fethini genellikle Orta Çağ''ın kapanışı ve Yeni Çağ''ın başlangıcı olarak kabul eder. Bu olay, Avrupa''da ticaret yollarının değişmesine ve coğrafi keşiflere giden sürecin hızlanmasına da katkı sağladı.'),
  (13, 'Doç. Dr. Mehmet Aydın', '16. yüzyılın sonlarından itibaren Osmanlı Devleti, sınırlarının genişlemesinde önemli bir yavaşlama yaşadı. Bu döneme tarih yazımında duraklama dönemi denir; toprak kayıpları henüz başlamamış olsa da fetihlerin hızı belirgin biçimde azalmıştı.

İç isyanlar, taht kavgaları ve merkezi otoritenin zayıflaması, yönetim mekanizmasında ciddi aksaklıklara yol açtı. Aynı zamanda Avrupa''da yaşanan bilimsel ve askeri gelişmeler, Osmanlı''nın teknolojik üstünlüğünü giderek kaybetmesine neden oldu.

18. yüzyılda ise toprak kayıplarının belirginleşmesiyle gerileme dönemi başladı. Bu dönem, devletin çöküşünü durdurmaya yönelik ilk ıslahat girişimlerinin de başlangıcı oldu.'),
  (14, 'Doç. Dr. Mehmet Aydın', '1789''da patlak veren Fransız İhtilali, mutlak monarşiye karşı halkın ayaklanmasıyla başladı ve kısa sürede özgürlük, eşitlik ve kardeşlik ilkelerini merkezine alan köklü bir dönüşüme evrildi. Bu ilkeler, Avrupa''nın siyasi düşüncesini derinden etkiledi.

İhtilal, yalnızca Fransa''da değil tüm dünyada milliyetçilik, cumhuriyetçilik ve halk egemenliği gibi kavramların yayılmasına zemin hazırladı. Çok uluslu imparatorluklar, bu yeni fikir akımlarından doğrudan etkilendi.

Osmanlı Devleti gibi çok milletli bir yapıya sahip imparatorluklar için milliyetçilik fikri özellikle tehlikeliydi; zira farklı etnik gruplar arasında bağımsızlık taleplerinin doğmasına yol açtı ve 19. yüzyıl boyunca birçok isyanın ideolojik temelini oluşturdu.'),
  (15, 'Doç. Dr. Mehmet Aydın', 'I. Dünya Savaşı, 1914 yılında Avrupa''nın büyük güçleri arasında başladı ve kısa sürede küresel bir çatışmaya dönüştü. Osmanlı Devleti, İttifak Devletleri safında savaşa girerek birçok cephede aynı anda mücadele etmek zorunda kaldı.

Çanakkale Cephesi, Osmanlı ordusunun en önemli savunma başarılarından biri olarak öne çıktı ve İtilaf Devletleri''nin İstanbul''a ulaşma planını boşa çıkardı. Ancak diğer cephelerdeki kayıplar, devletin gücünü giderek zayıflattı.

Savaşın sonunda imzalanan Mondros Ateşkes Antlaşması, Osmanlı topraklarının fiilen işgaline kapı araladı. Bu süreç, Türk milletinin bağımsızlık mücadelesinin de başlangıç noktası oldu.'),
  (16, 'Doç. Dr. Mehmet Aydın', 'Mondros Ateşkesi sonrası Anadolu''nun işgale uğraması üzerine Mustafa Kemal önderliğinde başlayan Milli Mücadele, önce direniş örgütlenmeleriyle, ardından düzenli ordu birlikleriyle sürdürüldü. Amasya Genelgesi ve Erzurum-Sivas Kongreleri, bu örgütlenmenin temel taşlarıdır.

TBMM''nin 1920''de açılmasıyla milli iradeye dayanan yeni bir siyasi merkez oluşturuldu. Sakarya Meydan Muharebesi ve Büyük Taarruz gibi kritik savaşlar, işgalci kuvvetlerin Anadolu''dan çıkarılmasını sağladı.

Mücadelenin zaferle sonuçlanması, 1923''te imzalanan Lozan Antlaşması ile uluslararası alanda tescillendi ve yeni Türk devletinin bağımsızlığının önünü açtı.'),
  (17, 'Doç. Dr. Mehmet Aydın', 'Cumhuriyetin ilanının ardından Mustafa Kemal Atatürk önderliğinde siyasi, hukuki, toplumsal, eğitim ve ekonomi alanlarında köklü inkılaplar gerçekleştirildi. Saltanatın ve halifeliğin kaldırılması, yeni rejimin laik ve cumhuriyetçi karakterini pekiştirdi.

Harf İnkılabı, kadınlara seçme ve seçilme hakkının tanınması, Medeni Kanun''un kabulü gibi adımlar, toplumsal yaşamı çağdaş bir temele oturttu. Eğitimde birliğin sağlanması ise yeni neslin ortak bir müfredatla yetişmesini mümkün kıldı.

Atatürkçü düşünce sisteminin temelini oluşturan cumhuriyetçilik, milliyetçilik, halkçılık, devletçilik, laiklik ve inkılapçılık ilkeleri, bu reformların ideolojik çerçevesini oluşturdu ve Türkiye Cumhuriyeti''nin kurucu değerleri hâline geldi.'),
  (18, 'Doç. Dr. Mehmet Aydın', 'II. Dünya Savaşı, 1939''da Almanya''nın Polonya''yı işgaliyle başladı ve kısa sürede kıtalar arası bir çatışmaya dönüştü. Mihver Devletleri ile Müttefik Devletleri arasında geçen savaş, tarihin en fazla can kaybına yol açan çatışması oldu.

Savaş boyunca yaşanan Holokost, sivil bombardımanlar ve nihayetinde atom bombalarının kullanımı, uluslararası hukukta ve insan hakları anlayışında kalıcı değişimlere yol açtı.

1945''te Müttefiklerin zaferiyle sona eren savaş, dünya siyasetinde yeni bir dengeyi de beraberinde getirdi; Birleşmiş Milletler''in kuruluşu ve iki kutuplu Soğuk Savaş düzeninin temelleri bu dönemde atıldı.'),
  (19, 'Doç. Dr. Mehmet Aydın', 'II. Dünya Savaşı sonrasında ABD önderliğindeki Batı Bloku ile Sovyetler Birliği önderliğindeki Doğu Bloku arasında ideolojik ve siyasi bir rekabet başladı. Bu rekabet, doğrudan bir sıcak savaşa dönüşmediği için Soğuk Savaş olarak adlandırıldı.

Silahlanma yarışı, uzay yarışı ve vekalet savaşları, bu dönemin en belirgin özellikleriydi. NATO ve Varşova Paktı gibi askeri ittifaklar, dünyayı iki kutuplu bir güvenlik mimarisine böldü.

1991''de Sovyetler Birliği''nin dağılmasıyla Soğuk Savaş sona erdi ve dünya, tek kutuplu bir uluslararası düzene doğru evrildi. Bu dönemin mirası, günümüz uluslararası ilişkilerinde hâlâ hissedilmektedir.'),
  (20, 'Doç. Dr. Mehmet Aydın', 'Türkiye Cumhuriyeti''nin kuruluşundan itibaren dış politikası, "Yurtta Sulh, Cihanda Sulh" ilkesi çerçevesinde şekillendi. Bu yaklaşım, komşu ülkelerle barışçıl ilişkiler kurmayı ve uluslararası anlaşmazlıklardan uzak durmayı hedefledi.

Cumhuriyetin ilk yıllarında Batılı kurumlarla bütünleşme çabaları öne çıkarken, II. Dünya Savaşı sonrasında Türkiye, NATO''ya katılarak Batı ittifakının bir parçası oldu. Bu tercih, Soğuk Savaş döneminde ülkenin jeopolitik konumunu belirledi.

Günümüzde Türkiye, bölgesel ve küresel meselelerde daha çok yönlü bir dış politika izlemekte; hem Batı kurumlarıyla ilişkilerini sürdürmekte hem de komşu bölgelerde etkin bir aktör olmaya çalışmaktadır.'),
  (21, 'Dr. Ayşe Demir', 'Türkiye, Asya ve Avrupa kıtaları arasında, kuzey yarım kürede orta enlemlerde yer alan bir ülkedir. Bu konum, hem iklim çeşitliliği hem de kültürel etkileşim açısından ülkeye özgün bir kimlik kazandırır.

Matematik konum, Türkiye''nin Ekvator''a ve başlangıç meridyenine olan uzaklığını; özel konum ise komşu ülkelerle ilişkileri, ticaret yollarına yakınlığı ve jeopolitik önemi ifade eder. Bu iki konum türü birlikte değerlendirildiğinde Türkiye''nin stratejik önemi daha iyi anlaşılır.

Boğazlar üzerindeki hakimiyeti, Türkiye''yi Karadeniz ile Akdeniz arasındaki deniz ticaretinin de kilit noktalarından biri hâline getirir. Bu konum, tarih boyunca ülkenin siyasi ve ekonomik önemini artıran temel etkenlerden olmuştur.'),
  (22, 'Dr. Ayşe Demir', 'İklim, bir bölgede uzun yıllar boyunca gözlenen ortalama hava koşullarını ifade eder. Sıcaklık, yağış, nem ve rüzgâr gibi unsurların bir araya gelmesiyle farklı iklim tipleri ortaya çıkar.

Türkiye, coğrafi konumu ve yer şekillerinin çeşitliliği nedeniyle aynı anda birden fazla iklim tipini barındırır. Kıyı bölgelerinde Akdeniz ve Karadeniz iklimleri görülürken, iç kesimlerde daha sert özellikler taşıyan karasal iklim egemendir.

Dağların kıyıya paralel uzanması, nemli hava kütlelerinin iç bölgelere ulaşmasını engelleyerek iklim çeşitliliğini pekiştirir. Bu durum, tarımsal üretimden yerleşim dokusuna kadar birçok coğrafi özelliği doğrudan etkiler.'),
  (23, 'Dr. Ayşe Demir', 'Türkiye''nin yüzey şekilleri, jeolojik tarihi boyunca yaşanan kıvrımlanma, kırılma ve volkanik faaliyetler sonucunda şekillenmiştir. Ülke, ortalama yükseltisi yüksek ve engebeli bir topografyaya sahiptir.

Kuzeyde Karadeniz Dağları, güneyde Toros Dağları kıyıya paralel uzanırken, bu iki sıradağ arasında kalan İç Anadolu daha alçak ve düz bir görünüm sunar. Doğuya gidildikçe yükselti belirgin biçimde artar.

Akarsu vadileri, ovalar ve platolar, bu ana yapıyı tamamlayan diğer yer şekilleridir. Bu çeşitlilik, tarımdan ulaşıma kadar birçok beşeri faaliyetin bölgeden bölgeye farklılaşmasına yol açar.'),
  (24, 'Dr. Ayşe Demir', 'Nüfus dağılışı, bir bölgenin iklimi, yer şekilleri, ekonomik olanakları ve tarihsel gelişimi gibi birçok etkene bağlı olarak şekillenir. Türkiye''de nüfus, genellikle kıyı bölgelerde ve büyük ovalarda yoğunlaşır.

Sanayileşme ve kentleşme süreçleri, nüfusun kırsaldan kente doğru büyük bir hareketliliğe girmesine neden olmuştur. Bu göç dalgası, büyük şehirlerin hızla büyümesine ve kırsal nüfusun azalmasına yol açtı.

Yerleşmenin şekli ve yoğunluğu, arazi kullanımından altyapı planlamasına kadar birçok coğrafi ve ekonomik kararı doğrudan etkiler. Bu nedenle nüfus çalışmaları, bölgesel planlamanın temel veri kaynaklarından biridir.'),
  (25, 'Dr. Ayşe Demir', 'Göç, insanların ekonomik, sosyal veya siyasi nedenlerle yaşadıkları yeri değiştirmesi olarak tanımlanır. Türkiye''de özellikle 20. yüzyılın ikinci yarısından itibaren kırsaldan kente yoğun bir göç yaşanmıştır.

Bu göç dalgası, iş imkânlarının kentlerde yoğunlaşmasından kaynaklanmış ve büyük şehirlerin nüfusunu hızla artırmıştır. Şehirleşme oranındaki bu artış, konut, altyapı ve ulaşım gibi alanlarda yeni ihtiyaçlar doğurmuştur.

Göç ve şehirleşme süreci, yalnızca demografik değil aynı zamanda kültürel bir dönüşümü de beraberinde getirmiştir; farklı bölgelerden gelen insanlar, şehirlerde yeni bir toplumsal doku oluşturmuştur.'),
  (26, 'Dr. Ayşe Demir', 'Tarım, bir bölgenin iklim koşulları, toprak yapısı ve su kaynaklarına bağlı olarak şekillenen temel ekonomik faaliyetlerden biridir. Türkiye''nin farklı iklim bölgeleri, çok çeşitli tarım ürünlerinin yetiştirilmesine imkân tanır.

Kıyı bölgelerde turunçgil ve zeytin gibi ürünler öne çıkarken, İç Anadolu''da tahıl tarımı yaygındır. Hayvancılık ise özellikle bitkisel tarıma elverişli olmayan dağlık ve yüksek bölgelerde önemli bir geçim kaynağıdır.

Tarımsal üretimin çeşitliliği, hem iç tüketimi karşılamakta hem de ülke ekonomisine ihracat yoluyla katkı sağlamaktadır. Sulama teknolojilerindeki gelişmeler, iklimin sınırlayıcı etkisini bir ölçüde azaltmaktadır.'),
  (27, 'Dr. Ayşe Demir', 'Sanayi, hammaddelerin işlenerek katma değeri yüksek ürünlere dönüştürülmesi sürecidir. Bir bölgede sanayinin gelişmesi; hammadde kaynaklarına yakınlık, ulaşım imkânları, enerji kaynakları ve iş gücü gibi etkenlere bağlıdır.

Türkiye''de sanayi, özellikle büyük şehirler ve limanlara yakın bölgelerde yoğunlaşmıştır. Tekstil, otomotiv, gıda ve makine sanayii, ülke ekonomisinde önemli paya sahip sektörler arasında yer alır.

Sanayileşme süreci, yalnızca üretim değil aynı zamanda istihdam ve şehirleşme üzerinde de doğrudan etkilidir; sanayi bölgeleri, çevresinde yeni yerleşim alanlarının oluşmasına zemin hazırlar.'),
  (28, 'Dr. Ayşe Demir', 'Doğal kaynaklar, bir ülkenin ekonomik kalkınmasında önemli rol oynayan yer altı ve yer üstü zenginlikleridir. Madenler, su kaynakları ve ormanlar bu kaynakların başlıca örnekleridir.

Enerji üretimi açısından Türkiye, hem fosil kaynaklara hem de yenilenebilir kaynaklara sahiptir. Hidroelektrik santraller akarsulardan, güneş enerjisi santralleri güneş ışınımından, rüzgâr santralleri ise uygun rüzgâr koridorlarından yararlanır.

Yenilenebilir enerji kaynaklarının kullanımının artması, hem dışa bağımlılığı azaltmakta hem de çevresel sürdürülebilirliğe katkı sağlamaktadır. Bu nedenle enerji politikaları, günümüz coğrafyasının önemli konularından biri hâline gelmiştir.'),
  (29, 'Dr. Ayşe Demir', 'Çevre sorunları, insan faaliyetlerinin doğal dengeyi bozması sonucu ortaya çıkan hava, su ve toprak kirliliği gibi olumsuz durumları kapsar. Sanayileşme ve hızlı şehirleşme, bu sorunların başlıca nedenleri arasında yer alır.

İklim değişikliği, günümüzde en kapsamlı çevre sorunlarından biri olarak öne çıkar; sera gazı salımlarının artması, küresel sıcaklıkların yükselmesine ve doğal afetlerin sıklaşmasına yol açar.

Sürdürülebilirlik kavramı, bugünün ihtiyaçlarını karşılarken gelecek nesillerin ihtiyaçlarını da gözeten bir kalkınma anlayışını ifade eder. Yenilenebilir enerji kullanımı, geri dönüşüm ve doğal alanların korunması, bu anlayışın somut uygulamalarındandır.'),
  (30, 'Dr. Ayşe Demir', 'Bölgesel kalkınma projeleri, gelişmişlik farklarını azaltmak amacıyla belirli bölgelerde uygulanan kapsamlı yatırım programlarıdır. Güneydoğu Anadolu Projesi (GAP), Türkiye''nin en büyük bölgesel kalkınma projelerinden biridir.

GAP, Fırat ve Dicle nehirleri üzerine kurulan barajlar ve sulama sistemleriyle bölgenin tarımsal potansiyelini artırmayı, aynı zamanda enerji üretimi sağlamayı hedefler. Proje, tarım dışında sanayi ve eğitim alanlarını da kapsayan bütüncül bir yaklaşım sunar.

Bu tür projeler, yalnızca ekonomik değil aynı zamanda sosyal kalkınmayı da amaçlar; bölgedeki istihdam imkânlarını artırarak göçü azaltmayı ve yaşam standartlarını yükseltmeyi hedefler.'),
  (31, 'Doç. Dr. Can Yılmaz', 'Madde, kütlesi ve hacmi olan, evrendeki her şeyi oluşturan temel varlıktır. Maddenin en küçük yapı taşı olan atom, çekirdek ve çekirdek etrafında hareket eden elektronlardan oluşur.

Atom modelleri, bilim insanlarının atomun yapısını anlama çabasının tarihini yansıtır. Dalton''un katı küre modelinden Thomson''ın üzümlü kek modeline, Rutherford''un çekirdek modelinden Bohr''un enerji katmanlı modeline kadar her biri, önceki modelin eksiklerini gidermeye çalışmıştır.

Günümüzde kabul gören modern atom modeli, elektronların belirli yörüngelerde değil, olasılık bulutları içinde bulunduğunu öngörür. Bu model, atomun kimyasal davranışlarını açıklamada en güçlü çerçeveyi sunar.'),
  (32, 'Doç. Dr. Can Yılmaz', 'Periyodik sistem, elementlerin atom numaralarına göre sıralandığı ve benzer özellik gösteren elementlerin aynı sütunlarda toplandığı bir düzendir. Bu düzenleme, Dmitri Mendeleyev''in 19. yüzyılda geliştirdiği çalışmalara dayanır.

Sistemde yatay sıralara periyot, dikey sütunlara ise grup adı verilir. Aynı gruptaki elementler, benzer sayıda değerlik elektronuna sahip olduğu için birbirine yakın kimyasal özellikler gösterir.

Periyodik sistem yalnızca elementleri sınıflandırmakla kalmaz, aynı zamanda atom yarıçapı, iyonlaşma enerjisi ve elektronegatiflik gibi özelliklerin periyot ve gruplar boyunca nasıl değiştiğini öngörmemizi de sağlar.'),
  (33, 'Doç. Dr. Can Yılmaz', 'Kimyasal bağ, atomların daha kararlı bir elektron dizilimine ulaşmak için birbirleriyle etkileşime girmesi sonucu oluşan çekim kuvvetidir. Bu bağlar sayesinde atomlar bir araya gelerek molekülleri ve bileşikleri oluşturur.

İyonik bağ, elektron alışverişiyle oluşan zıt yüklü iyonlar arasındaki çekimle meydana gelir. Kovalent bağ ise atomların ortak elektron çiftleri paylaşmasıyla oluşur ve genellikle ametaller arasında görülür.

Metalik bağ, metal atomlarının değerlik elektronlarını ortak bir elektron denizinde paylaşmasıyla oluşur ve metallerin iletkenlik gibi karakteristik özelliklerini açıklar. Bağ türü, bir bileşiğin fiziksel ve kimyasal özelliklerini büyük ölçüde belirler.'),
  (34, 'Doç. Dr. Can Yılmaz', 'Mol, kimyada madde miktarını ifade etmek için kullanılan temel birimdir. Bir mol madde, Avogadro sayısı kadar (yaklaşık 6,02 x 10 üzeri 23) tanecik içerir; bu tanecikler atom, molekül veya iyon olabilir.

Mol kavramı, kimyasal tepkimelerdeki madde miktarlarını kütle üzerinden hesaplamayı mümkün kılar. Bir maddenin mol kütlesi, periyodik sistemdeki atom kütlelerinden yararlanılarak hesaplanır.

Kimyasal hesaplamalarda mol kavramı, tepkimeye giren ve çıkan maddelerin birbirine oranını belirleyen denklemlerin temelini oluşturur; bu sayede laboratuvarda ne kadar madde kullanılması gerektiği önceden hesaplanabilir.'),
  (35, 'Doç. Dr. Can Yılmaz', 'Gazlar, belirli bir şekli ve hacmi olmayan, bulundukları kabın tamamını dolduran madde hâlidir. Bu davranış, gaz tanecikleri arasındaki çekim kuvvetlerinin oldukça zayıf olmasından kaynaklanır.

Gazların basıncı, hacmi ve sıcaklığı arasındaki ilişki, çeşitli gaz yasalarıyla açıklanır. Boyle Yasası basınç ile hacim, Charles Yasası ise hacim ile sıcaklık arasındaki ilişkiyi sabit koşullar altında tanımlar.

İdeal gaz denklemi, bu yasaları tek bir eşitlikte birleştirerek gazların basınç, hacim, sıcaklık ve mol miktarı arasındaki ilişkiyi bütünüyle ifade eder ve birçok pratik hesaplamada kullanılır.'),
  (36, 'Doç. Dr. Can Yılmaz', 'Karışım, iki veya daha fazla maddenin kimyasal özelliklerini kaybetmeden bir araya gelmesiyle oluşan yapıdır. Karışımlar, homojen ve heterojen olmak üzere iki ana grupta incelenir.

Homojen karışımlarda bileşenler birbiri içinde eşit oranda dağılmıştır ve gözle ayırt edilemez; tuzlu su bu duruma örnektir. Heterojen karışımlarda ise bileşenler gözle veya mikroskopla ayırt edilebilir.

Karışımları oluşturan maddeleri ayırmak için süzme, damıtma, buharlaştırma ve kristallendirme gibi yöntemler kullanılır. Hangi yöntemin seçileceği, karışımdaki maddelerin fiziksel özelliklerindeki farklara bağlıdır.'),
  (37, 'Doç. Dr. Can Yılmaz', 'Asitler, sulu çözeltilerinde hidrojen iyonu (H+) veren; bazlar ise hidroksit iyonu (OH-) veren veya hidrojen iyonu alan maddelerdir. Bu tanım, günlük hayatta karşılaştığımız birçok maddenin kimyasal davranışını açıklar.

pH ölçeği, bir çözeltinin asitlik veya bazlık derecesini 0 ile 14 arasında bir sayıyla ifade eder. 7 değeri nötr kabul edilirken, 7''nin altındaki değerler asidik, üstündeki değerler bazik özellik gösterir.

Asit-baz tepkimelerinde asit ve baz birbirini nötrleştirerek tuz ve su oluşturur. Bu denge, hem laboratuvar ortamında hem de canlı organizmaların iç dengesinin korunmasında hayati öneme sahiptir.'),
  (38, 'Doç. Dr. Can Yılmaz', 'Kimyasal tepkime, bir veya daha fazla maddenin yeni özelliklere sahip başka maddelere dönüşmesi sürecidir. Tepkimeler sırasında atomlar yeniden düzenlenir, ancak toplam kütle korunur.

Bazı tepkimeler tek yönlü ilerlerken, birçok tepkime hem ileri hem de geri yönde eş zamanlı gerçekleşebilir. İleri ve geri tepkime hızlarının eşitlendiği nokta, kimyasal denge olarak adlandırılır.

Le Chatelier ilkesine göre, dengedeki bir sisteme dışarıdan bir etki (sıcaklık, basınç veya derişim değişimi) uygulandığında sistem, bu etkiyi azaltacak yönde yeni bir dengeye ulaşır. Bu ilke, endüstriyel üretim süreçlerinin verimini artırmada sıkça kullanılır.'),
  (39, 'Doç. Dr. Can Yılmaz', 'Organik kimya, karbon atomlarını temel alan bileşiklerin yapısını, özelliklerini ve tepkimelerini inceleyen kimya dalıdır. Karbon, dört bağ yapabilme özelliği sayesinde çok sayıda ve çeşitli bileşik oluşturabilir.

Hidrokarbonlar, yalnızca karbon ve hidrojen atomlarından oluşan en basit organik bileşiklerdir. Bağ türüne göre alkanlar, alkenler ve alkinler olarak sınıflandırılır ve her biri farklı kimyasal tepkime eğilimleri gösterir.

Organik kimya, yalnızca petrol ve doğal gaz gibi kaynaklarla sınırlı değildir; canlı organizmaların yapı taşları olan karbonhidratlar, proteinler ve yağlar da organik bileşiklerdir. Bu nedenle organik kimya, biyoloji ile kimya arasındaki köprüyü oluşturur.'),
  (40, 'Doç. Dr. Can Yılmaz', 'Kimyasal tepkimeler gerçekleşirken enerji ya çevreye salınır ya da çevreden soğurulur. Enerji açığa çıkaran tepkimelere ekzotermik, enerji soğuran tepkimelere ise endotermik tepkime denir.

Bir tepkimenin gerçekleşmesi için gerekli olan en küçük enerji miktarına aktivasyon enerjisi denir. Katalizörler, aktivasyon enerjisini düşürerek tepkimenin daha hızlı gerçekleşmesini sağlar, ancak tepkimenin genel enerji dengesini değiştirmez.

Enerji değişimlerinin incelenmesi, yalnızca laboratuvar deneyleri için değil, aynı zamanda enerji üretiminden gıda muhafazasına kadar birçok günlük uygulamanın temelini oluşturan pratik bir bilgi alanıdır.');
