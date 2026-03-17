package com.pokemonbp.data

/**
 * Maps each Pokédex ID to a list of possible next evolutions.
 * Single entry = straight evolution.
 * Multiple entries = player chooses (split evo like Eevee, or Charizard X/Y).
 */
object EvolutionData {

    private val chains: Map<Int, List<Int>> = mapOf(
        // Gen 1
        1 to listOf(2), 2 to listOf(3),
        4 to listOf(5), 5 to listOf(6),
        7 to listOf(8), 8 to listOf(9),
        10 to listOf(11), 11 to listOf(12),
        13 to listOf(14), 14 to listOf(15),
        16 to listOf(17), 17 to listOf(18),
        19 to listOf(20), 21 to listOf(22),
        23 to listOf(24), 25 to listOf(26),
        27 to listOf(28), 29 to listOf(30), 30 to listOf(31),
        32 to listOf(33), 33 to listOf(34),
        35 to listOf(36), 37 to listOf(38), 39 to listOf(40),
        41 to listOf(42), 43 to listOf(44), 44 to listOf(45),
        46 to listOf(47), 48 to listOf(49),
        50 to listOf(51), 52 to listOf(53), 54 to listOf(55),
        56 to listOf(57), 58 to listOf(59),
        60 to listOf(61), 61 to listOf(62),
        63 to listOf(64), 64 to listOf(65),
        66 to listOf(67), 67 to listOf(68),
        69 to listOf(70), 70 to listOf(71),
        72 to listOf(73), 74 to listOf(75), 75 to listOf(76),
        77 to listOf(78), 79 to listOf(80),
        81 to listOf(82), 84 to listOf(85),
        86 to listOf(87), 88 to listOf(89), 90 to listOf(91),
        92 to listOf(93), 93 to listOf(94),
        96 to listOf(97), 98 to listOf(99),
        100 to listOf(101), 102 to listOf(103), 104 to listOf(105),
        109 to listOf(110), 111 to listOf(112),
        116 to listOf(117), 117 to listOf(230),
        118 to listOf(119), 120 to listOf(121),
        129 to listOf(130),
        133 to listOf(134, 135, 136, 196, 197, 470, 471, 700), // Eevee → all evos
        137 to listOf(233), 138 to listOf(139), 140 to listOf(141),
        147 to listOf(148), 148 to listOf(149),
        // Megas Gen 1
        3   to listOf(10033),
        6   to listOf(10034, 10035), // Charizard → Mega X / Mega Y
        9   to listOf(10036),
        65  to listOf(10037),
        80  to listOf(10071),
        94  to listOf(10038),
        115 to listOf(10039),
        127 to listOf(10040),
        130 to listOf(10041),
        142 to listOf(10042),
        150 to listOf(10043, 10044), // Mewtwo → Mega X / Mega Y

        // Gen 2
        152 to listOf(153), 153 to listOf(154),
        155 to listOf(156), 156 to listOf(157),
        158 to listOf(159), 159 to listOf(160),
        161 to listOf(162), 163 to listOf(164),
        165 to listOf(166), 167 to listOf(168),
        170 to listOf(171), 172 to listOf(25),
        173 to listOf(35), 174 to listOf(39),
        175 to listOf(176), 177 to listOf(178),
        179 to listOf(180), 180 to listOf(181),
        183 to listOf(184), 187 to listOf(188), 188 to listOf(189),
        190 to listOf(424), 191 to listOf(192), 193 to listOf(469),
        194 to listOf(195), 198 to listOf(430), 200 to listOf(429),
        207 to listOf(472), 209 to listOf(210),
        215 to listOf(461), 216 to listOf(217), 218 to listOf(219),
        220 to listOf(221), 221 to listOf(473),
        223 to listOf(224), 228 to listOf(229),
        231 to listOf(232), 233 to listOf(474),
        236 to listOf(106, 107, 237), // Tyrogue → Hitmonlee / Hitmonchan / Hitmontop
        238 to listOf(124), 239 to listOf(125), 240 to listOf(126),
        246 to listOf(247), 247 to listOf(248),
        // Megas Gen 2
        181 to listOf(10045), 208 to listOf(10072),
        212 to listOf(10046), 214 to listOf(10073),
        229 to listOf(10047), 248 to listOf(10048),

        // Gen 3
        252 to listOf(253), 253 to listOf(254),
        255 to listOf(256), 256 to listOf(257),
        258 to listOf(259), 259 to listOf(260),
        261 to listOf(262), 263 to listOf(264),
        270 to listOf(271), 271 to listOf(272),
        273 to listOf(274), 274 to listOf(275),
        276 to listOf(277), 278 to listOf(279),
        280 to listOf(281), 281 to listOf(282, 475), // Kirlia → Gardevoir / Gallade
        285 to listOf(286), 287 to listOf(288), 288 to listOf(289),
        296 to listOf(297), 304 to listOf(305), 305 to listOf(306),
        307 to listOf(308), 309 to listOf(310),
        316 to listOf(317), 318 to listOf(319),
        320 to listOf(321), 322 to listOf(323),
        325 to listOf(326), 328 to listOf(329), 329 to listOf(330),
        331 to listOf(332), 333 to listOf(334),
        339 to listOf(340), 341 to listOf(342), 343 to listOf(344),
        349 to listOf(350), 353 to listOf(354),
        355 to listOf(356), 356 to listOf(477),
        361 to listOf(362, 478), // Snorunt → Glalie / Froslass
        363 to listOf(364), 364 to listOf(365),
        371 to listOf(372), 372 to listOf(373),
        374 to listOf(375), 375 to listOf(376),
        // Megas Gen 3
        254 to listOf(10065), 257 to listOf(10049),
        260 to listOf(10066), 282 to listOf(10051),
        303 to listOf(10052), 306 to listOf(10053),
        308 to listOf(10054), 310 to listOf(10055),
        319 to listOf(10067), 323 to listOf(10068),
        334 to listOf(10080), 354 to listOf(10064),
        359 to listOf(10057), 362 to listOf(10074),
        373 to listOf(10081), 376 to listOf(10076),
        380 to listOf(10082), 381 to listOf(10083),
        384 to listOf(10079),

        // Gen 4 (Sinnoh)
        387 to listOf(388), 388 to listOf(389),
        390 to listOf(391), 391 to listOf(392),
        393 to listOf(394), 394 to listOf(395),
        396 to listOf(397), 397 to listOf(398),
        399 to listOf(400), 403 to listOf(404), 404 to listOf(405),
        406 to listOf(407), 408 to listOf(409), 410 to listOf(411),
        418 to listOf(419), 420 to listOf(421), 425 to listOf(426),
        427 to listOf(428), 431 to listOf(432), 436 to listOf(437),
        440 to listOf(113), 443 to listOf(444), 444 to listOf(445),
        446 to listOf(143), 447 to listOf(448),
        449 to listOf(450), 451 to listOf(452), 453 to listOf(454),
        459 to listOf(460),
        // Megas Gen 4
        445 to listOf(10058), 448 to listOf(10059), 460 to listOf(10060)
    )

    fun nextEvolutions(pokedexId: Int): List<Int> = chains[pokedexId] ?: emptyList()
    fun canEvolve(pokedexId: Int): Boolean = chains.containsKey(pokedexId)
}
