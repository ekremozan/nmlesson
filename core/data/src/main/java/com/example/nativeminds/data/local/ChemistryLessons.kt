package com.example.nativeminds.data.local

private const val SUBJECT = "Kimya"
private const val AUTHOR = "Doç. Dr. Can Yılmaz"

/**
 * Faz A iskeleti: her konu için kısa, gerçek ancak yer tutucu Türkçe içerik. Gerçek ~500 satırlık
 * konu anlatımları Faz E'de, konu konu gözden geçirilerek yazılacak.
 */
internal object ChemistryLessons {
    val all: List<LessonSeedTopic> = listOf(
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Maddenin Yapısı ve Atom Modelleri",
            author = AUTHOR,
            teaser = "Her şeyin en küçük yapı taşı.",
            minutes = 6,
            hasAudio = true,
            isLocked = false,
            paragraphs = listOf(
                "Madde, kütlesi ve hacmi olan, evrendeki her şeyi oluşturan temel varlıktır. Maddenin en küçük yapı taşı olan atom, çekirdek ve çekirdek etrafında hareket eden elektronlardan oluşur.",
                "Atom modelleri, bilim insanlarının atomun yapısını anlama çabasının tarihini yansıtır. Dalton'un katı küre modelinden Thomson'ın üzümlü kek modeline, Rutherford'un çekirdek modelinden Bohr'un enerji katmanlı modeline kadar her biri, önceki modelin eksiklerini gidermeye çalışmıştır.",
                "Günümüzde kabul gören modern atom modeli, elektronların belirli yörüngelerde değil, olasılık bulutları içinde bulunduğunu öngörür. Bu model, atomun kimyasal davranışlarını açıklamada en güçlü çerçeveyi sunar.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Periyodik Sistem",
            author = AUTHOR,
            teaser = "Elementlerin düzenli bir haritası.",
            minutes = 6,
            hasAudio = true,
            isLocked = false,
            paragraphs = listOf(
                "Periyodik sistem, elementlerin atom numaralarına göre sıralandığı ve benzer özellik gösteren elementlerin aynı sütunlarda toplandığı bir düzendir. Bu düzenleme, Dmitri Mendeleyev'in 19. yüzyılda geliştirdiği çalışmalara dayanır.",
                "Sistemde yatay sıralara periyot, dikey sütunlara ise grup adı verilir. Aynı gruptaki elementler, benzer sayıda değerlik elektronuna sahip olduğu için birbirine yakın kimyasal özellikler gösterir.",
                "Periyodik sistem yalnızca elementleri sınıflandırmakla kalmaz, aynı zamanda atom yarıçapı, iyonlaşma enerjisi ve elektronegatiflik gibi özelliklerin periyot ve gruplar boyunca nasıl değiştiğini öngörmemizi de sağlar.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Kimyasal Bağlar",
            author = AUTHOR,
            teaser = "Atomları bir arada tutan görünmez bağlar.",
            minutes = 7,
            hasAudio = true,
            isLocked = false,
            paragraphs = listOf(
                "Kimyasal bağ, atomların daha kararlı bir elektron dizilimine ulaşmak için birbirleriyle etkileşime girmesi sonucu oluşan çekim kuvvetidir. Bu bağlar sayesinde atomlar bir araya gelerek molekülleri ve bileşikleri oluşturur.",
                "İyonik bağ, elektron alışverişiyle oluşan zıt yüklü iyonlar arasındaki çekimle meydana gelir. Kovalent bağ ise atomların ortak elektron çiftleri paylaşmasıyla oluşur ve genellikle ametaller arasında görülür.",
                "Metalik bağ, metal atomlarının değerlik elektronlarını ortak bir elektron denizinde paylaşmasıyla oluşur ve metallerin iletkenlik gibi karakteristik özelliklerini açıklar. Bağ türü, bir bileşiğin fiziksel ve kimyasal özelliklerini büyük ölçüde belirler.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Mol Kavramı ve Kimyasal Hesaplamalar",
            author = AUTHOR,
            teaser = "Görünmeyen taneleri saymanın yolu.",
            minutes = 7,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Mol, kimyada madde miktarını ifade etmek için kullanılan temel birimdir. Bir mol madde, Avogadro sayısı kadar (yaklaşık 6,02 x 10 üzeri 23) tanecik içerir; bu tanecikler atom, molekül veya iyon olabilir.",
                "Mol kavramı, kimyasal tepkimelerdeki madde miktarlarını kütle üzerinden hesaplamayı mümkün kılar. Bir maddenin mol kütlesi, periyodik sistemdeki atom kütlelerinden yararlanılarak hesaplanır.",
                "Kimyasal hesaplamalarda mol kavramı, tepkimeye giren ve çıkan maddelerin birbirine oranını belirleyen denklemlerin temelini oluşturur; bu sayede laboratuvarda ne kadar madde kullanılması gerektiği önceden hesaplanabilir.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Gazların Genel Özellikleri",
            author = AUTHOR,
            teaser = "Sınır tanımayan bir hâl.",
            minutes = 6,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Gazlar, belirli bir şekli ve hacmi olmayan, bulundukları kabın tamamını dolduran madde hâlidir. Bu davranış, gaz tanecikleri arasındaki çekim kuvvetlerinin oldukça zayıf olmasından kaynaklanır.",
                "Gazların basıncı, hacmi ve sıcaklığı arasındaki ilişki, çeşitli gaz yasalarıyla açıklanır. Boyle Yasası basınç ile hacim, Charles Yasası ise hacim ile sıcaklık arasındaki ilişkiyi sabit koşullar altında tanımlar.",
                "İdeal gaz denklemi, bu yasaları tek bir eşitlikte birleştirerek gazların basınç, hacim, sıcaklık ve mol miktarı arasındaki ilişkiyi bütünüyle ifade eder ve birçok pratik hesaplamada kullanılır.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Karışımlar ve Ayırma Teknikleri",
            author = AUTHOR,
            teaser = "Birlikte olan ama birleşmeyen maddeler.",
            minutes = 6,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Karışım, iki veya daha fazla maddenin kimyasal özelliklerini kaybetmeden bir araya gelmesiyle oluşan yapıdır. Karışımlar, homojen ve heterojen olmak üzere iki ana grupta incelenir.",
                "Homojen karışımlarda bileşenler birbiri içinde eşit oranda dağılmıştır ve gözle ayırt edilemez; tuzlu su bu duruma örnektir. Heterojen karışımlarda ise bileşenler gözle veya mikroskopla ayırt edilebilir.",
                "Karışımları oluşturan maddeleri ayırmak için süzme, damıtma, buharlaştırma ve kristallendirme gibi yöntemler kullanılır. Hangi yöntemin seçileceği, karışımdaki maddelerin fiziksel özelliklerindeki farklara bağlıdır.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Asit-Baz Dengesi",
            author = AUTHOR,
            teaser = "Ekşi ile acı arasındaki denge.",
            minutes = 6,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Asitler, sulu çözeltilerinde hidrojen iyonu (H+) veren; bazlar ise hidroksit iyonu (OH-) veren veya hidrojen iyonu alan maddelerdir. Bu tanım, günlük hayatta karşılaştığımız birçok maddenin kimyasal davranışını açıklar.",
                "pH ölçeği, bir çözeltinin asitlik veya bazlık derecesini 0 ile 14 arasında bir sayıyla ifade eder. 7 değeri nötr kabul edilirken, 7'nin altındaki değerler asidik, üstündeki değerler bazik özellik gösterir.",
                "Asit-baz tepkimelerinde asit ve baz birbirini nötrleştirerek tuz ve su oluşturur. Bu denge, hem laboratuvar ortamında hem de canlı organizmaların iç dengesinin korunmasında hayati öneme sahiptir.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Kimyasal Tepkimeler ve Denge",
            author = AUTHOR,
            teaser = "İleri ve geri işleyen bir denge oyunu.",
            minutes = 7,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Kimyasal tepkime, bir veya daha fazla maddenin yeni özelliklere sahip başka maddelere dönüşmesi sürecidir. Tepkimeler sırasında atomlar yeniden düzenlenir, ancak toplam kütle korunur.",
                "Bazı tepkimeler tek yönlü ilerlerken, birçok tepkime hem ileri hem de geri yönde eş zamanlı gerçekleşebilir. İleri ve geri tepkime hızlarının eşitlendiği nokta, kimyasal denge olarak adlandırılır.",
                "Le Chatelier ilkesine göre, dengedeki bir sisteme dışarıdan bir etki (sıcaklık, basınç veya derişim değişimi) uygulandığında sistem, bu etkiyi azaltacak yönde yeni bir dengeye ulaşır. Bu ilke, endüstriyel üretim süreçlerinin verimini artırmada sıkça kullanılır.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Organik Kimyaya Giriş",
            author = AUTHOR,
            teaser = "Karbonun kurduğu sonsuz çeşitlilik.",
            minutes = 7,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Organik kimya, karbon atomlarını temel alan bileşiklerin yapısını, özelliklerini ve tepkimelerini inceleyen kimya dalıdır. Karbon, dört bağ yapabilme özelliği sayesinde çok sayıda ve çeşitli bileşik oluşturabilir.",
                "Hidrokarbonlar, yalnızca karbon ve hidrojen atomlarından oluşan en basit organik bileşiklerdir. Bağ türüne göre alkanlar, alkenler ve alkinler olarak sınıflandırılır ve her biri farklı kimyasal tepkime eğilimleri gösterir.",
                "Organik kimya, yalnızca petrol ve doğal gaz gibi kaynaklarla sınırlı değildir; canlı organizmaların yapı taşları olan karbonhidratlar, proteinler ve yağlar da organik bileşiklerdir. Bu nedenle organik kimya, biyoloji ile kimya arasındaki köprüyü oluşturur.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Enerji ve Kimyasal Tepkimeler",
            author = AUTHOR,
            teaser = "Her tepkimenin bir enerji hikayesi vardır.",
            minutes = 6,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Kimyasal tepkimeler gerçekleşirken enerji ya çevreye salınır ya da çevreden soğurulur. Enerji açığa çıkaran tepkimelere ekzotermik, enerji soğuran tepkimelere ise endotermik tepkime denir.",
                "Bir tepkimenin gerçekleşmesi için gerekli olan en küçük enerji miktarına aktivasyon enerjisi denir. Katalizörler, aktivasyon enerjisini düşürerek tepkimenin daha hızlı gerçekleşmesini sağlar, ancak tepkimenin genel enerji dengesini değiştirmez.",
                "Enerji değişimlerinin incelenmesi, yalnızca laboratuvar deneyleri için değil, aynı zamanda enerji üretiminden gıda muhafazasına kadar birçok günlük uygulamanın temelini oluşturan pratik bir bilgi alanıdır.",
            ),
        ),
    )
}
