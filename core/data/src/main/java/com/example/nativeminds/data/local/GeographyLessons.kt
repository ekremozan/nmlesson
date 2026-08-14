package com.example.nativeminds.data.local

private const val SUBJECT = "Coğrafya"
private const val AUTHOR = "Dr. Ayşe Demir"

/**
 * Faz A iskeleti: her konu için kısa, gerçek ancak yer tutucu Türkçe içerik. Gerçek ~500 satırlık
 * konu anlatımları Faz D'de, konu konu gözden geçirilerek yazılacak.
 */
internal object GeographyLessons {
    val all: List<LessonSeedTopic> = listOf(
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Türkiye'nin Coğrafi Konumu",
            author = AUTHOR,
            teaser = "Üç kıtanın kesiştiği noktada bir ülke.",
            minutes = 6,
            hasAudio = true,
            isLocked = false,
            paragraphs = listOf(
                "Türkiye, Asya ve Avrupa kıtaları arasında, kuzey yarım kürede orta enlemlerde yer alan bir ülkedir. Bu konum, hem iklim çeşitliliği hem de kültürel etkileşim açısından ülkeye özgün bir kimlik kazandırır.",
                "Matematik konum, Türkiye'nin Ekvator'a ve başlangıç meridyenine olan uzaklığını; özel konum ise komşu ülkelerle ilişkileri, ticaret yollarına yakınlığı ve jeopolitik önemi ifade eder. Bu iki konum türü birlikte değerlendirildiğinde Türkiye'nin stratejik önemi daha iyi anlaşılır.",
                "Boğazlar üzerindeki hakimiyeti, Türkiye'yi Karadeniz ile Akdeniz arasındaki deniz ticaretinin de kilit noktalarından biri hâline getirir. Bu konum, tarih boyunca ülkenin siyasi ve ekonomik önemini artıran temel etkenlerden olmuştur.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "İklim Tipleri ve Türkiye İklimi",
            author = AUTHOR,
            teaser = "Aynı ülkede dört farklı iklim.",
            minutes = 7,
            hasAudio = true,
            isLocked = false,
            paragraphs = listOf(
                "İklim, bir bölgede uzun yıllar boyunca gözlenen ortalama hava koşullarını ifade eder. Sıcaklık, yağış, nem ve rüzgâr gibi unsurların bir araya gelmesiyle farklı iklim tipleri ortaya çıkar.",
                "Türkiye, coğrafi konumu ve yer şekillerinin çeşitliliği nedeniyle aynı anda birden fazla iklim tipini barındırır. Kıyı bölgelerinde Akdeniz ve Karadeniz iklimleri görülürken, iç kesimlerde daha sert özellikler taşıyan karasal iklim egemendir.",
                "Dağların kıyıya paralel uzanması, nemli hava kütlelerinin iç bölgelere ulaşmasını engelleyerek iklim çeşitliliğini pekiştirir. Bu durum, tarımsal üretimden yerleşim dokusuna kadar birçok coğrafi özelliği doğrudan etkiler.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Türkiye'nin Yer Şekilleri",
            author = AUTHOR,
            teaser = "Dağlardan ovalara uzanan bir yüzey.",
            minutes = 6,
            hasAudio = true,
            isLocked = false,
            paragraphs = listOf(
                "Türkiye'nin yüzey şekilleri, jeolojik tarihi boyunca yaşanan kıvrımlanma, kırılma ve volkanik faaliyetler sonucunda şekillenmiştir. Ülke, ortalama yükseltisi yüksek ve engebeli bir topografyaya sahiptir.",
                "Kuzeyde Karadeniz Dağları, güneyde Toros Dağları kıyıya paralel uzanırken, bu iki sıradağ arasında kalan İç Anadolu daha alçak ve düz bir görünüm sunar. Doğuya gidildikçe yükselti belirgin biçimde artar.",
                "Akarsu vadileri, ovalar ve platolar, bu ana yapıyı tamamlayan diğer yer şekilleridir. Bu çeşitlilik, tarımdan ulaşıma kadar birçok beşeri faaliyetin bölgeden bölgeye farklılaşmasına yol açar.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Nüfus ve Yerleşme",
            author = AUTHOR,
            teaser = "İnsanların nerede ve neden yaşadığı.",
            minutes = 6,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Nüfus dağılışı, bir bölgenin iklimi, yer şekilleri, ekonomik olanakları ve tarihsel gelişimi gibi birçok etkene bağlı olarak şekillenir. Türkiye'de nüfus, genellikle kıyı bölgelerde ve büyük ovalarda yoğunlaşır.",
                "Sanayileşme ve kentleşme süreçleri, nüfusun kırsaldan kente doğru büyük bir hareketliliğe girmesine neden olmuştur. Bu göç dalgası, büyük şehirlerin hızla büyümesine ve kırsal nüfusun azalmasına yol açtı.",
                "Yerleşmenin şekli ve yoğunluğu, arazi kullanımından altyapı planlamasına kadar birçok coğrafi ve ekonomik kararı doğrudan etkiler. Bu nedenle nüfus çalışmaları, bölgesel planlamanın temel veri kaynaklarından biridir.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Göç ve Şehirleşme",
            author = AUTHOR,
            teaser = "Köyden kente uzanan büyük hareketlilik.",
            minutes = 6,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Göç, insanların ekonomik, sosyal veya siyasi nedenlerle yaşadıkları yeri değiştirmesi olarak tanımlanır. Türkiye'de özellikle 20. yüzyılın ikinci yarısından itibaren kırsaldan kente yoğun bir göç yaşanmıştır.",
                "Bu göç dalgası, iş imkânlarının kentlerde yoğunlaşmasından kaynaklanmış ve büyük şehirlerin nüfusunu hızla artırmıştır. Şehirleşme oranındaki bu artış, konut, altyapı ve ulaşım gibi alanlarda yeni ihtiyaçlar doğurmuştur.",
                "Göç ve şehirleşme süreci, yalnızca demografik değil aynı zamanda kültürel bir dönüşümü de beraberinde getirmiştir; farklı bölgelerden gelen insanlar, şehirlerde yeni bir toplumsal doku oluşturmuştur.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Tarım ve Hayvancılık",
            author = AUTHOR,
            teaser = "Toprağın ve iklimin şekillendirdiği üretim.",
            minutes = 6,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Tarım, bir bölgenin iklim koşulları, toprak yapısı ve su kaynaklarına bağlı olarak şekillenen temel ekonomik faaliyetlerden biridir. Türkiye'nin farklı iklim bölgeleri, çok çeşitli tarım ürünlerinin yetiştirilmesine imkân tanır.",
                "Kıyı bölgelerde turunçgil ve zeytin gibi ürünler öne çıkarken, İç Anadolu'da tahıl tarımı yaygındır. Hayvancılık ise özellikle bitkisel tarıma elverişli olmayan dağlık ve yüksek bölgelerde önemli bir geçim kaynağıdır.",
                "Tarımsal üretimin çeşitliliği, hem iç tüketimi karşılamakta hem de ülke ekonomisine ihracat yoluyla katkı sağlamaktadır. Sulama teknolojilerindeki gelişmeler, iklimin sınırlayıcı etkisini bir ölçüde azaltmaktadır.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Sanayi ve Ekonomik Faaliyetler",
            author = AUTHOR,
            teaser = "Hammaddeden ürüne uzanan üretim zinciri.",
            minutes = 6,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Sanayi, hammaddelerin işlenerek katma değeri yüksek ürünlere dönüştürülmesi sürecidir. Bir bölgede sanayinin gelişmesi; hammadde kaynaklarına yakınlık, ulaşım imkânları, enerji kaynakları ve iş gücü gibi etkenlere bağlıdır.",
                "Türkiye'de sanayi, özellikle büyük şehirler ve limanlara yakın bölgelerde yoğunlaşmıştır. Tekstil, otomotiv, gıda ve makine sanayii, ülke ekonomisinde önemli paya sahip sektörler arasında yer alır.",
                "Sanayileşme süreci, yalnızca üretim değil aynı zamanda istihdam ve şehirleşme üzerinde de doğrudan etkilidir; sanayi bölgeleri, çevresinde yeni yerleşim alanlarının oluşmasına zemin hazırlar.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Doğal Kaynaklar ve Enerji",
            author = AUTHOR,
            teaser = "Yer altından ve güneşten gelen güç.",
            minutes = 6,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Doğal kaynaklar, bir ülkenin ekonomik kalkınmasında önemli rol oynayan yer altı ve yer üstü zenginlikleridir. Madenler, su kaynakları ve ormanlar bu kaynakların başlıca örnekleridir.",
                "Enerji üretimi açısından Türkiye, hem fosil kaynaklara hem de yenilenebilir kaynaklara sahiptir. Hidroelektrik santraller akarsulardan, güneş enerjisi santralleri güneş ışınımından, rüzgâr santralleri ise uygun rüzgâr koridorlarından yararlanır.",
                "Yenilenebilir enerji kaynaklarının kullanımının artması, hem dışa bağımlılığı azaltmakta hem de çevresel sürdürülebilirliğe katkı sağlamaktadır. Bu nedenle enerji politikaları, günümüz coğrafyasının önemli konularından biri hâline gelmiştir.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Çevre Sorunları ve Sürdürülebilirlik",
            author = AUTHOR,
            teaser = "Doğayla dengeyi yeniden kurmak.",
            minutes = 6,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Çevre sorunları, insan faaliyetlerinin doğal dengeyi bozması sonucu ortaya çıkan hava, su ve toprak kirliliği gibi olumsuz durumları kapsar. Sanayileşme ve hızlı şehirleşme, bu sorunların başlıca nedenleri arasında yer alır.",
                "İklim değişikliği, günümüzde en kapsamlı çevre sorunlarından biri olarak öne çıkar; sera gazı salımlarının artması, küresel sıcaklıkların yükselmesine ve doğal afetlerin sıklaşmasına yol açar.",
                "Sürdürülebilirlik kavramı, bugünün ihtiyaçlarını karşılarken gelecek nesillerin ihtiyaçlarını da gözeten bir kalkınma anlayışını ifade eder. Yenilenebilir enerji kullanımı, geri dönüşüm ve doğal alanların korunması, bu anlayışın somut uygulamalarındandır.",
            ),
        ),
        LessonSeedTopic(
            subject = SUBJECT,
            title = "Bölgesel Kalkınma Projeleri (GAP vb.)",
            author = AUTHOR,
            teaser = "Suyla dönüşen bir bölgenin hikayesi.",
            minutes = 7,
            hasAudio = true,
            isLocked = true,
            paragraphs = listOf(
                "Bölgesel kalkınma projeleri, gelişmişlik farklarını azaltmak amacıyla belirli bölgelerde uygulanan kapsamlı yatırım programlarıdır. Güneydoğu Anadolu Projesi (GAP), Türkiye'nin en büyük bölgesel kalkınma projelerinden biridir.",
                "GAP, Fırat ve Dicle nehirleri üzerine kurulan barajlar ve sulama sistemleriyle bölgenin tarımsal potansiyelini artırmayı, aynı zamanda enerji üretimi sağlamayı hedefler. Proje, tarım dışında sanayi ve eğitim alanlarını da kapsayan bütüncül bir yaklaşım sunar.",
                "Bu tür projeler, yalnızca ekonomik değil aynı zamanda sosyal kalkınmayı da amaçlar; bölgedeki istihdam imkânlarını artırarak göçü azaltmayı ve yaşam standartlarını yükseltmeyi hedefler.",
            ),
        ),
    )
}
