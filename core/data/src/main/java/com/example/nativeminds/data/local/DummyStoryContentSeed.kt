package com.example.nativeminds.data.local

import com.example.nativeminds.model.StoryContent

/**
 * The text behind each story in [DummyStorySeed], keyed by the same ids.
 *
 * Kept in its own file rather than inlined into the story list: the metadata list is scanned by
 * anyone changing the catalog, and burying six full stories inside it would make that unreadable.
 *
 * Cut corner: hand-written content standing in for a real catalog until a backend exists.
 */
object DummyStoryContentSeed {
    val content = listOf(
        StoryContent(
            storyId = 1,
            author = "Marguerite Halloran",
            paragraphs = listOf(
                "Forty winters he kept the log, and forty winters the log kept him. Wind from the northwest, it said. Swell moderate. Nothing to report. The sentences were short because the weather was long, and because a man who writes too much about the sea begins to argue with it.",
                "The keeper's handwriting changed twice in those years. Once in 1931, when the light was converted and the great brass clockwork he had wound every four hours was carried down the stairs in pieces. Once in 1948, for reasons the log does not give.",
                "Between the pages of the final volume, folded into eighths and soft as cloth, the inspectors found a letter. It was addressed to a house in Galway that had been empty for a decade. It was never sent, and it was never thrown away, which are two different kinds of keeping.",
                "He wrote about the birds, mostly. How they arrived in October in numbers he had stopped trying to count, and how they left without any signal he could see. He wrote that he had grown used to being the only person who knew certain things: the exact hour the fog would lift, the name of the dog on the supply boat, the sound the lamp made in the last second before it caught.",
                "And then, near the bottom of the second page, one line with no weather in it at all.",
            ),
        ),
        StoryContent(
            storyId = 2,
            author = "Dr. Peter Vance",
            paragraphs = listOf(
                "A loaf of bread is a record of an argument between two gases and a net. The net is gluten, built out of two proteins that only become one structure once flour meets water and someone applies force. The gas is carbon dioxide, and it is produced by an organism that has no interest whatsoever in bread.",
                "Yeast eats sugar. In the absence of enough oxygen it does so inefficiently, and the waste products of that inefficiency are alcohol and carbon dioxide. Every hole in the crumb of a loaf is a bubble the dough was strong enough to hold and elastic enough not to tear around.",
                "This is why kneading matters and why over-kneading ruins things. Work the dough and the protein strands align into sheets that trap gas. Work it too long and the same sheets stiffen past the point where they can stretch, and the bubbles either escape or burst the structure they were held in.",
                "The oven does the last, strangest part. In the first minutes of baking the gas inside every bubble expands with the heat, the alcohol boils off, and the loaf rises faster than it ever did on the counter. Bakers call it oven spring. It is the dough's final expansion, happening at the exact moment the crust is setting hard enough to make it permanent.",
            ),
        ),
        StoryContent(
            storyId = 3,
            author = "Tomás Ferreiro",
            paragraphs = listOf(
                "For ninety-one years, the Admiralty chart showed an island at 44° south. Ships were told to give it a wide berth. Whalers reported its cliffs in fog. A cable company planned a station on it. There was no island.",
                "The error began, as most durable errors do, with a careful man. In 1826 a sealing captain named Ross recorded land where he had seen a bank of low cloud, and because his other measurements were unusually good, the mistake inherited his credibility.",
                "Cartographers copy each other. That is not a scandal; it is the method. Each new edition of the chart carried the island forward, and each printing made it harder to remove, until removing it required more authority than putting it there ever had.",
                "What finally undid it was not a discovery but a bookkeeper. In 1917 a hydrographic clerk in London noticed that every sighting since 1826 had been reported by a vessel that already carried the chart, and none by a vessel that did not. The island existed only where it was expected.",
                "It was struck from the record the following year, in a single line of correction that named no one. The clerk's own name survives on nothing but the memorandum, which is the fate of most people who remove things from maps.",
                "There are, at last count, still four such islands on charts in active use. Nobody is in a hurry to look for them.",
            ),
        ),
        StoryContent(
            storyId = 4,
            author = "Ines Marchetti",
            paragraphs = listOf(
                "A city crossed at speed is a set of instructions. A city walked slowly is a set of accidents, and the accidents are the point.",
                "I learned this by injury. Six weeks on a bad ankle turned my twenty-minute route into fifty, and in the extra thirty minutes I found a courtyard I had passed for four years without seeing, a man who sells one kind of cheese and nothing else, and the fact that my street runs downhill, which I had never once noticed while hurrying up it.",
                "Speed is a filter. It keeps out noise, which is the argument for it, and it keeps out everything that resembles noise, which is the argument against. A shortcut is a decision to see less, made once and then repeated for years.",
                "I am not recommending injury. I am recommending the pace of one, which you can adopt at any time, and which costs nothing but the belief that arriving is the part that matters.",
            ),
        ),
        StoryContent(
            storyId = 5,
            author = "Ada Ferreira",
            paragraphs = listOf(
                "There was a jar at the back of my grandmother's pantry that nobody opened, and everybody moved. It travelled through three kitchens and two countries, always to the back, always behind the things that were used.",
                "It held salt. Coarse, grey, damp at the bottom in the way sea salt goes when it has been somewhere humid for long enough. There was no label. There was no story attached to it that anyone in the family could repeat without disagreeing about a detail.",
                "My aunt said it came from the salt pans near where my grandmother was born, carried out in a coat pocket the week she left. My uncle said it was bought in a shop in 1974 and had simply outlived the meal it was meant for. Both of them were certain, and neither of them had ever asked.",
                "My grandmother, when I finally asked her, looked at it for a while and said she could not remember either. Then she put it back at the back of the shelf, behind the things she used.",
                "It is in my kitchen now. I have not opened it. I understand, at last, that the jar is not the point and never was — what the family kept was the habit of moving it, and I am the fourth kitchen.",
            ),
        ),
        StoryContent(
            storyId = 6,
            author = "Dr. Peter Vance",
            paragraphs = listOf(
                "The common swift lands to nest and for almost nothing else. Tagging studies have followed individual birds for ten months in continuous flight — feeding, mating, and sleeping without once touching down.",
                "The sleeping is the part that resists belief. Swifts climb to two thousand metres at dusk, and there they enter a state that looks like sleep in one half of the brain while the other half stays awake enough to hold a course.",
                "Dolphins do the same trick in water, and so do several ducks at the edge of a sleeping flock, keeping the outward eye open. It appears to be less an exotic adaptation than a common answer to an old problem: rest is not optional, and neither is watching.",
                "What the swift adds is scale. Ten months is not a nap taken in shifts; it is an entire life lived at altitude, interrupted only by the few weeks a year in which the species remembers it has feet.",
            ),
        ),
    )
}
