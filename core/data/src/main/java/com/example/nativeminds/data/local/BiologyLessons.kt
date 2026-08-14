package com.example.nativeminds.data.local

private const val SUBJECT = "Biyoloji"
private const val AUTHOR = "Dr. Elif Kaya"

/**
 * Faz A iskeleti: her konu için kısa, gerçek ancak yer tutucu Türkçe içerik. Gerçek ~500 satırlık
 * konu anlatımları Faz B'de, konu konu gözden geçirilerek yazılacak.
 */
internal object BiologyLessons {
    val all: List<LessonSeedTopic> = listOf(
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Hücre Yapısı ve Organeller",
            author = AUTHOR,
            teaser = "Zarın içindeki küçük fabrika.",
            minutes = 6,
            hasAudio = true,
            isLocked = false,
            paragraphs = listOf(
                "Hücre, canlıların yapısal ve işlevsel en küçük birimidir. Her hücre, onu dış ortamdan ayıran bir hücre zarıyla çevrilidir ve içinde sitoplazma adı verilen jel kıvamında bir sıvı bulunur. Bu sıvının içinde, hücrenin yaşamsal faaliyetlerini yürüten küçük yapılar, yani organeller yer alır.",
                "Ökaryot hücrelerde çekirdek, kalıtım maddesini taşıyan ve hücrenin yönetim merkezi sayılan organeldir. Mitokondri enerji üretiminden, ribozom protein sentezinden, Golgi aygıtı ise maddelerin paketlenip taşınmasından sorumludur. Bu organellerin her biri, bir fabrikadaki farklı departmanlar gibi uyum içinde çalışır.",
                "Bitki hücreleri, hayvan hücrelerinden farklı olarak hücre duvarı ve kloroplast içerir. Hücre duvarı bitkiye mekanik destek sağlarken, kloroplast fotosentez yaparak güneş enerjisini kimyasal enerjiye dönüştürür. Bu farklar, bitki ve hayvan hücrelerinin yaşam biçimlerindeki temel ayrımı da açıklar.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Mitoz ve Mayoz Bölünme",
            author = AUTHOR,
            teaser = "Bir hücrenin ikiye, dörde bölünme hikayesi.",
            minutes = 7,
            hasAudio = true,
            isLocked = false,
            paragraphs = listOf(
                "Mitoz, bir hücrenin kendisiyle genetik olarak özdeş iki yavru hücreye bölünmesidir. Büyüme, yara iyileşmesi ve eşeysiz üreme gibi süreçlerde görülür. Bölünme öncesinde kromozomlar eşlenir, ardından hücre çekirdeği ve sitoplazma sırayla ikiye ayrılır.",
                "Mayoz ise üreme hücrelerinin oluşumunda gerçekleşen, kromozom sayısını yarıya indiren özel bir bölünme türüdür. İki art arda bölünme evresi içerir ve sonucunda dört farklı yavru hücre oluşur. Bu çeşitlilik, türlerin genetik çeşitliliğinin temel kaynağıdır.",
                "Mitoz ve mayoz arasındaki en önemli fark, oluşan hücrelerin genetik içeriğidir: mitoz özdeş hücreler üretirken, mayoz genetik olarak birbirinden farklı üreme hücreleri üretir. Bu iki mekanizma, çok hücreli canlıların hem büyümesini hem de nesiller boyu çeşitliliğini mümkün kılar.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Kalıtım ve Mendel Genetiği",
            author = AUTHOR,
            teaser = "Bezelyelerden öğrendiğimiz kalıtım kuralları.",
            minutes = 6,
            hasAudio = true,
            isLocked = false,
            paragraphs = listOf(
                "Gregor Mendel, 19. yüzyılda bezelye bitkileri üzerinde yaptığı deneylerle kalıtımın temel kurallarını ortaya koydu. Her özelliğin, ana babadan birer tane alınan iki alel tarafından kontrol edildiğini ve bu allellerin bağımsız olarak yavru döllere aktarıldığını gözlemledi.",
                "Baskın ve çekinik alel kavramları, Mendel'in en önemli katkılarından biridir. Baskın alel, birlikte bulunduğu çekinik aleli fenotipte bastırır; çekinik özelliğin ortaya çıkması için bireyin iki çekinik alele sahip olması gerekir.",
                "Mendel'in kalıtım kuralları, günümüzde insan genetiği başta olmak üzere birçok alanda hastalıkların kalıtsal geçişini anlamada hâlâ temel bir çerçeve sunar.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "DNA ve Protein Sentezi",
            author = AUTHOR,
            teaser = "Yaşamın şifresi nasıl okunur?",
            minutes = 8,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "DNA, canlıların kalıtsal bilgisini taşıyan çift sarmal yapılı bir moleküldür. Bu bilgi, dört farklı bazın (adenin, timin, guanin, sitozin) diziliş sırasında saklanır ve hücre bölünmesinde eksiksiz biçimde kopyalanarak yeni hücrelere aktarılır.",
                "Protein sentezi, DNA'daki bilginin önce mRNA'ya kopyalanması (transkripsiyon), ardından ribozomda bu bilginin amino asit dizisine çevrilmesiyle (translasyon) gerçekleşir. Ortaya çıkan protein, hücrenin yapısal veya işlevsel bir görevini üstlenir.",
                "Bu iki aşamalı süreç, hücrenin hangi genleri ne zaman ve ne kadar kullanacağını belirleyerek canlının gelişimini ve günlük işleyişini yönetir.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "İnsan Sindirim Sistemi",
            author = AUTHOR,
            teaser = "Ağızdan başlayan uzun bir yolculuk.",
            minutes = 7,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Sindirim sistemi, alınan besinleri hücrelerin kullanabileceği küçük moleküllere ayıran organlar bütünüdür. Ağızda başlayan mekanik ve kimyasal sindirim, yemek borusu, mide ve ince bağırsak boyunca devam eder.",
                "Mide, güçlü asidik ortamı ve enzimleriyle proteinlerin sindirimine başlar. İnce bağırsak ise karaciğer ve pankreasın salgıladığı sıvıların yardımıyla sindirimin büyük kısmını tamamlar ve besin öğelerinin kana geçtiği asıl emilim bölgesidir.",
                "Kalın bağırsak, sindirilemeyen artıklardan suyun geri emilmesini sağlar ve sistemin son basamağını oluşturur. Bu uzun yolculuğun her adımı, vücudun enerji ve yapı taşı ihtiyacını karşılamak için özenle koordine edilir.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Dolaşım Sistemi ve Kalp",
            author = AUTHOR,
            teaser = "Vücudun durmaksızın çalışan pompası.",
            minutes = 6,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Dolaşım sistemi, kalp, damarlar ve kandan oluşan; oksijen, besin ve atık maddelerin vücutta taşınmasını sağlayan bir ağdır. Kalp, dört odacıklı yapısıyla kanı hem akciğerlere hem de vücudun geri kalanına pompalar.",
                "Atardamarlar kanı kalpten uzaklaştırırken, toplardamarlar kanı kalbe geri getirir. Kılcal damarlar ise oksijen ve besin alışverişinin gerçekleştiği en ince ve en yaygın damar ağını oluşturur.",
                "Kalbin düzenli ritmi, özel uyarı-ileti sistemi sayesinde sağlanır. Bu ritmin bozulması, dolaşım sisteminin tüm vücuda hizmet etme kapasitesini doğrudan etkiler.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Solunum Sistemi",
            author = AUTHOR,
            teaser = "Her nefeste gerçekleşen gaz alışverişi.",
            minutes = 5,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Solunum sistemi, vücudun oksijen almasını ve karbondioksit vermesini sağlayan organlardan oluşur. Hava burun veya ağızdan girer, gırtlak ve soluk borusundan geçerek akciğerlere ulaşır.",
                "Akciğerlerdeki milyonlarca küçük hava kesesi olan alveoller, gaz alışverişinin gerçekleştiği asıl yüzeyi oluşturur. İnce zarları sayesinde oksijen kana geçerken, karbondioksit kandan alveollere aktarılır.",
                "Diyafram kasının kasılıp gevşemesi, akciğerlerin genişleyip daralmasını sağlayarak solunumu mekanik olarak yönetir. Bu sürekli döngü, hücrelerin enerji üretimi için gerekli oksijeni asla kesintiye uğratmaz.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Boşaltım Sistemi",
            author = AUTHOR,
            teaser = "Vücudun atık yönetim merkezi.",
            minutes = 5,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Boşaltım sistemi, metabolizma sonucu oluşan atık maddelerin vücuttan uzaklaştırılmasından sorumludur. Böbrekler, bu sistemin en önemli organı olarak kanı sürekli süzer ve fazla suyu, tuzu ve üreyi ayırır.",
                "Böbreklerdeki nefron adı verilen milyonlarca küçük süzme birimi, kanı filtreleyip gerekli maddeleri geri emerken, gereksiz olanları idrar olarak toplar. Bu idrar, üreterler yoluyla mesaneye taşınır ve orada depolanır.",
                "Boşaltım sistemi yalnızca atık uzaklaştırmakla kalmaz, aynı zamanda vücuttaki su ve tuz dengesini de düzenleyerek iç ortamın kararlılığını korur.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Ekosistem ve Enerji Akışı",
            author = AUTHOR,
            teaser = "Güneşten başlayan enerji zinciri.",
            minutes = 6,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Ekosistem, belirli bir bölgedeki canlılar ile bu canlıların etkileşimde bulunduğu cansız çevrenin bütününü ifade eder. Bu sistemin işleyişi, enerjinin üreticilerden tüketicilere doğru aktarılmasına dayanır.",
                "Üreticiler, güneş enerjisini fotosentez yoluyla kimyasal enerjiye dönüştürerek besin zincirinin ilk halkasını oluşturur. Otçul tüketiciler bu enerjiyi üreticilerden, etçil tüketiciler ise diğer hayvanlardan alır.",
                "Her enerji aktarımında bir miktar enerji ısı olarak kaybolur, bu yüzden besin zincirindeki basamak sayısı sınırlıdır. Ayrıştırıcılar ise ölü organizmalardaki enerjiyi ve maddeleri tekrar ekosisteme kazandırarak döngüyü tamamlar.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Evrim ve Doğal Seçilim",
            author = AUTHOR,
            teaser = "Türlerin zaman içindeki değişim hikayesi.",
            minutes = 7,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Evrim, popülasyonların genetik yapısının nesiller boyunca değişmesi sürecidir. Charles Darwin'in ortaya koyduğu doğal seçilim kuramı, bu değişimin en temel mekanizmalarından biri olarak kabul edilir.",
                "Doğal seçilime göre, bir popülasyondaki bireyler arasında doğal olarak varyasyon bulunur. Ortama daha iyi uyum sağlayan özelliklere sahip bireyler hayatta kalma ve üreme şansını artırır, bu özellikler zamanla popülasyonda yaygınlaşır.",
                "Bu süreç, milyonlarca yıl içinde türlerin ortaya çıkmasına, değişmesine ve bazen de yok olmasına yol açar. Fosil kayıtları, karşılaştırmalı anatomi ve moleküler biyoloji bulguları, evrim sürecine dair güçlü kanıtlar sunar.",
            ),
        ),
    )
}
