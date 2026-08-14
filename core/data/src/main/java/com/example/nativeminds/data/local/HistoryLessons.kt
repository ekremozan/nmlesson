package com.example.nativeminds.data.local

private const val SUBJECT = "Tarih"
private const val AUTHOR = "Doç. Dr. Mehmet Aydın"

/**
 * Faz A iskeleti: her konu için kısa, gerçek ancak yer tutucu Türkçe içerik. Gerçek ~500 satırlık
 * konu anlatımları Faz C'de, konu konu gözden geçirilerek yazılacak.
 */
internal object HistoryLessons {
    val all: List<LessonSeedTopic> = listOf(
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Osmanlı Devleti'nin Kuruluşu",
            author = AUTHOR,
            teaser = "Küçük bir uç beyliğinden bir cihan devletine.",
            minutes = 6,
            hasAudio = true,
            isLocked = false,
            paragraphs = listOf(
                "Osmanlı Devleti, 13. yüzyılın sonlarında Anadolu'nun kuzeybatısında, Söğüt ve çevresinde küçük bir uç beyliği olarak Osman Bey önderliğinde kuruldu. Bizans sınırındaki bu konum, beyliğin hem askeri hem de ekonomik açıdan hızla güçlenmesine zemin hazırladı.",
                "Orhan Bey döneminde Bursa'nın fethiyle beylik başkent kazandı ve Rumeli'ye geçişle birlikte Balkanlar'da genişlemeye başladı. Bu genişleme, sadece askeri başarılarla değil, fethedilen bölgelerdeki yönetim anlayışıyla da destekleniyordu.",
                "14. ve 15. yüzyıllar boyunca art arda gelen padişahlar, beyliği önce bir bölgesel güce, ardından bir imparatorluğa dönüştürdü. Bu sürecin en önemli dönüm noktalarından biri, ileride İstanbul'un fethiyle taçlanacaktı.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "İstanbul'un Fethi ve Sonuçları",
            author = AUTHOR,
            teaser = "Bir çağın kapanıp diğerinin açılması.",
            minutes = 7,
            hasAudio = true,
            isLocked = false,
            paragraphs = listOf(
                "II. Mehmed, 1453 yılında uzun bir kuşatmanın ardından Bizans'ın başkenti İstanbul'u fethetti. Bu zafer, yalnızca askeri bir başarı değil, aynı zamanda dönemin savaş teknolojisinde de bir dönüm noktasıydı; büyük toplar kuşatmanın seyrini belirledi.",
                "Fetih, Bizans İmparatorluğu'nun sonunu getirirken Osmanlı Devleti'ni bir dünya gücü hâline getirdi. İstanbul, yeni başkent olarak imar edildi ve farklı din ile milletlerden insanların bir arada yaşadığı kozmopolit bir merkez hâline geldi.",
                "Tarihçiler, İstanbul'un fethini genellikle Orta Çağ'ın kapanışı ve Yeni Çağ'ın başlangıcı olarak kabul eder. Bu olay, Avrupa'da ticaret yollarının değişmesine ve coğrafi keşiflere giden sürecin hızlanmasına da katkı sağladı.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Osmanlı'da Duraklama ve Gerileme",
            author = AUTHOR,
            teaser = "Bir imparatorluğun büyümesi nasıl durdu?",
            minutes = 7,
            hasAudio = true,
            isLocked = false,
            paragraphs = listOf(
                "16. yüzyılın sonlarından itibaren Osmanlı Devleti, sınırlarının genişlemesinde önemli bir yavaşlama yaşadı. Bu döneme tarih yazımında duraklama dönemi denir; toprak kayıpları henüz başlamamış olsa da fetihlerin hızı belirgin biçimde azalmıştı.",
                "İç isyanlar, taht kavgaları ve merkezi otoritenin zayıflaması, yönetim mekanizmasında ciddi aksaklıklara yol açtı. Aynı zamanda Avrupa'da yaşanan bilimsel ve askeri gelişmeler, Osmanlı'nın teknolojik üstünlüğünü giderek kaybetmesine neden oldu.",
                "18. yüzyılda ise toprak kayıplarının belirginleşmesiyle gerileme dönemi başladı. Bu dönem, devletin çöküşünü durdurmaya yönelik ilk ıslahat girişimlerinin de başlangıcı oldu.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Fransız İhtilali ve Etkileri",
            author = AUTHOR,
            teaser = "Bir devrimin tüm dünyaya yayılan fikirleri.",
            minutes = 6,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "1789'da patlak veren Fransız İhtilali, mutlak monarşiye karşı halkın ayaklanmasıyla başladı ve kısa sürede özgürlük, eşitlik ve kardeşlik ilkelerini merkezine alan köklü bir dönüşüme evrildi. Bu ilkeler, Avrupa'nın siyasi düşüncesini derinden etkiledi.",
                "İhtilal, yalnızca Fransa'da değil tüm dünyada milliyetçilik, cumhuriyetçilik ve halk egemenliği gibi kavramların yayılmasına zemin hazırladı. Çok uluslu imparatorluklar, bu yeni fikir akımlarından doğrudan etkilendi.",
                "Osmanlı Devleti gibi çok milletli bir yapıya sahip imparatorluklar için milliyetçilik fikri özellikle tehlikeliydi; zira farklı etnik gruplar arasında bağımsızlık taleplerinin doğmasına yol açtı ve 19. yüzyıl boyunca birçok isyanın ideolojik temelini oluşturdu.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "I. Dünya Savaşı ve Osmanlı",
            author = AUTHOR,
            teaser = "Bir imparatorluğun son büyük savaşı.",
            minutes = 8,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "I. Dünya Savaşı, 1914 yılında Avrupa'nın büyük güçleri arasında başladı ve kısa sürede küresel bir çatışmaya dönüştü. Osmanlı Devleti, İttifak Devletleri safında savaşa girerek birçok cephede aynı anda mücadele etmek zorunda kaldı.",
                "Çanakkale Cephesi, Osmanlı ordusunun en önemli savunma başarılarından biri olarak öne çıktı ve İtilaf Devletleri'nin İstanbul'a ulaşma planını boşa çıkardı. Ancak diğer cephelerdeki kayıplar, devletin gücünü giderek zayıflattı.",
                "Savaşın sonunda imzalanan Mondros Ateşkes Antlaşması, Osmanlı topraklarının fiilen işgaline kapı araladı. Bu süreç, Türk milletinin bağımsızlık mücadelesinin de başlangıç noktası oldu.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Milli Mücadele ve Kurtuluş Savaşı",
            author = AUTHOR,
            teaser = "Bir milletin yeniden doğuş mücadelesi.",
            minutes = 8,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Mondros Ateşkesi sonrası Anadolu'nun işgale uğraması üzerine Mustafa Kemal önderliğinde başlayan Milli Mücadele, önce direniş örgütlenmeleriyle, ardından düzenli ordu birlikleriyle sürdürüldü. Amasya Genelgesi ve Erzurum-Sivas Kongreleri, bu örgütlenmenin temel taşlarıdır.",
                "TBMM'nin 1920'de açılmasıyla milli iradeye dayanan yeni bir siyasi merkez oluşturuldu. Sakarya Meydan Muharebesi ve Büyük Taarruz gibi kritik savaşlar, işgalci kuvvetlerin Anadolu'dan çıkarılmasını sağladı.",
                "Mücadelenin zaferle sonuçlanması, 1923'te imzalanan Lozan Antlaşması ile uluslararası alanda tescillendi ve yeni Türk devletinin bağımsızlığının önünü açtı.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Atatürk İlke ve İnkılapları",
            author = AUTHOR,
            teaser = "Yeni bir devletin temel taşları.",
            minutes = 7,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Cumhuriyetin ilanının ardından Mustafa Kemal Atatürk önderliğinde siyasi, hukuki, toplumsal, eğitim ve ekonomi alanlarında köklü inkılaplar gerçekleştirildi. Saltanatın ve halifeliğin kaldırılması, yeni rejimin laik ve cumhuriyetçi karakterini pekiştirdi.",
                "Harf İnkılabı, kadınlara seçme ve seçilme hakkının tanınması, Medeni Kanun'un kabulü gibi adımlar, toplumsal yaşamı çağdaş bir temele oturttu. Eğitimde birliğin sağlanması ise yeni neslin ortak bir müfredatla yetişmesini mümkün kıldı.",
                "Atatürkçü düşünce sisteminin temelini oluşturan cumhuriyetçilik, milliyetçilik, halkçılık, devletçilik, laiklik ve inkılapçılık ilkeleri, bu reformların ideolojik çerçevesini oluşturdu ve Türkiye Cumhuriyeti'nin kurucu değerleri hâline geldi.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "II. Dünya Savaşı",
            author = AUTHOR,
            teaser = "İnsanlık tarihinin en yıkıcı çatışması.",
            minutes = 8,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "II. Dünya Savaşı, 1939'da Almanya'nın Polonya'yı işgaliyle başladı ve kısa sürede kıtalar arası bir çatışmaya dönüştü. Mihver Devletleri ile Müttefik Devletleri arasında geçen savaş, tarihin en fazla can kaybına yol açan çatışması oldu.",
                "Savaş boyunca yaşanan Holokost, sivil bombardımanlar ve nihayetinde atom bombalarının kullanımı, uluslararası hukukta ve insan hakları anlayışında kalıcı değişimlere yol açtı.",
                "1945'te Müttefiklerin zaferiyle sona eren savaş, dünya siyasetinde yeni bir dengeyi de beraberinde getirdi; Birleşmiş Milletler'in kuruluşu ve iki kutuplu Soğuk Savaş düzeninin temelleri bu dönemde atıldı.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Soğuk Savaş Dönemi",
            author = AUTHOR,
            teaser = "Silahsız ama gerilimli bir küresel mücadele.",
            minutes = 7,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "II. Dünya Savaşı sonrasında ABD önderliğindeki Batı Bloku ile Sovyetler Birliği önderliğindeki Doğu Bloku arasında ideolojik ve siyasi bir rekabet başladı. Bu rekabet, doğrudan bir sıcak savaşa dönüşmediği için Soğuk Savaş olarak adlandırıldı.",
                "Silahlanma yarışı, uzay yarışı ve vekalet savaşları, bu dönemin en belirgin özellikleriydi. NATO ve Varşova Paktı gibi askeri ittifaklar, dünyayı iki kutuplu bir güvenlik mimarisine böldü.",
                "1991'de Sovyetler Birliği'nin dağılmasıyla Soğuk Savaş sona erdi ve dünya, tek kutuplu bir uluslararası düzene doğru evrildi. Bu dönemin mirası, günümüz uluslararası ilişkilerinde hâlâ hissedilmektedir.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Türkiye Cumhuriyeti'nin Dış Politikası",
            author = AUTHOR,
            teaser = "Barış içinde bağımsız bir yol arayışı.",
            minutes = 6,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Türkiye Cumhuriyeti'nin kuruluşundan itibaren dış politikası, \"Yurtta Sulh, Cihanda Sulh\" ilkesi çerçevesinde şekillendi. Bu yaklaşım, komşu ülkelerle barışçıl ilişkiler kurmayı ve uluslararası anlaşmazlıklardan uzak durmayı hedefledi.",
                "Cumhuriyetin ilk yıllarında Batılı kurumlarla bütünleşme çabaları öne çıkarken, II. Dünya Savaşı sonrasında Türkiye, NATO'ya katılarak Batı ittifakının bir parçası oldu. Bu tercih, Soğuk Savaş döneminde ülkenin jeopolitik konumunu belirledi.",
                "Günümüzde Türkiye, bölgesel ve küresel meselelerde daha çok yönlü bir dış politika izlemekte; hem Batı kurumlarıyla ilişkilerini sürdürmekte hem de komşu bölgelerde etkin bir aktör olmaya çalışmaktadır.",
            ),
        ),
    )
}
