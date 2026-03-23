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
        445 to listOf(10058), 448 to listOf(10059), 460 to listOf(10060),

        // Gen 5 (Unova)
        495 to listOf(496), 496 to listOf(497),   // Snivy
        498 to listOf(499), 499 to listOf(500),   // Tepig
        501 to listOf(502), 502 to listOf(503),   // Oshawott
        504 to listOf(505),                        // Patrat
        506 to listOf(507), 507 to listOf(508),   // Lillipup
        509 to listOf(510),                        // Purrloin
        511 to listOf(512),                        // Pansage
        513 to listOf(514),                        // Pansear
        515 to listOf(516),                        // Panpour
        517 to listOf(518),                        // Munna
        519 to listOf(520), 520 to listOf(521),   // Pidove
        522 to listOf(523),                        // Blitzle
        524 to listOf(525), 525 to listOf(526),   // Roggenrola
        527 to listOf(528),                        // Woobat
        529 to listOf(530),                        // Drilbur
        532 to listOf(533), 533 to listOf(534),   // Timburr
        535 to listOf(536), 536 to listOf(537),   // Tympole
        540 to listOf(541), 541 to listOf(542),   // Sewaddle
        543 to listOf(544), 544 to listOf(545),   // Venipede
        546 to listOf(547),                        // Cottonee
        548 to listOf(549),                        // Petilil
        551 to listOf(552), 552 to listOf(553),   // Sandile
        554 to listOf(555),                        // Darumaka
        557 to listOf(558),                        // Dwebble
        559 to listOf(560),                        // Scraggy
        562 to listOf(563),                        // Yamask
        564 to listOf(565),                        // Tirtouga
        566 to listOf(567),                        // Archen
        568 to listOf(569),                        // Trubbish
        570 to listOf(571),                        // Zorua
        572 to listOf(573),                        // Minccino
        574 to listOf(575), 575 to listOf(576),   // Gothita
        577 to listOf(578), 578 to listOf(579),   // Solosis
        580 to listOf(581),                        // Ducklett
        582 to listOf(583), 583 to listOf(584),   // Vanillite
        585 to listOf(586),                        // Deerling
        588 to listOf(589),                        // Karrablast
        590 to listOf(591),                        // Foongus
        592 to listOf(593),                        // Frillish
        595 to listOf(596),                        // Joltik
        597 to listOf(598),                        // Ferroseed
        599 to listOf(600), 600 to listOf(601),   // Klink
        602 to listOf(603), 603 to listOf(604),   // Tynamo
        605 to listOf(606),                        // Elgyem
        607 to listOf(608), 608 to listOf(609),   // Litwick
        610 to listOf(611), 611 to listOf(612),   // Axew
        613 to listOf(614),                        // Cubchoo
        616 to listOf(617),                        // Shelmet
        619 to listOf(620),                        // Mienfoo
        622 to listOf(623),                        // Golett
        624 to listOf(625),                        // Pawniard
        627 to listOf(628),                        // Rufflet
        629 to listOf(630),                        // Vullaby
        633 to listOf(634), 634 to listOf(635),   // Deino
        636 to listOf(637),                        // Larvesta

        // Gen 6 (Kalos)
        650 to listOf(651), 651 to listOf(652),   // Chespin
        653 to listOf(654), 654 to listOf(655),   // Fennekin
        656 to listOf(657), 657 to listOf(658),   // Froakie
        659 to listOf(660),                        // Bunnelby
        661 to listOf(662), 662 to listOf(663),   // Fletchling
        664 to listOf(665), 665 to listOf(666),   // Scatterbug
        667 to listOf(668),                        // Litleo
        669 to listOf(670), 670 to listOf(671),   // Flabébé
        672 to listOf(673),                        // Skiddo
        674 to listOf(675),                        // Pancham
        677 to listOf(678),                        // Espurr
        679 to listOf(680), 680 to listOf(681),   // Honedge
        682 to listOf(683),                        // Spritzee
        684 to listOf(685),                        // Swirlix
        686 to listOf(687),                        // Inkay
        688 to listOf(689),                        // Clauncher
        690 to listOf(691),                        // Skrelp
        692 to listOf(693),                        // Helioptile
        694 to listOf(695),                        // Tyrunt
        696 to listOf(697),                        // Amaura
        704 to listOf(705), 705 to listOf(706),   // Goomy
        708 to listOf(709),                        // Phantump
        710 to listOf(711),                        // Pumpkaboo
        712 to listOf(713),                        // Bergmite

        // Gen 7 (Alola)
        722 to listOf(723), 723 to listOf(724),   // Rowlet
        725 to listOf(726), 726 to listOf(727),   // Litten
        728 to listOf(729), 729 to listOf(730),   // Popplio
        731 to listOf(732), 732 to listOf(733),   // Pikipek
        734 to listOf(735),                        // Yungoos
        736 to listOf(737), 737 to listOf(738),   // Grubbin
        742 to listOf(743),                        // Cutiefly
        744 to listOf(745),                        // Rockruff
        747 to listOf(748),                        // Mareanie
        749 to listOf(750),                        // Mudbray
        751 to listOf(752),                        // Dewpider
        753 to listOf(754),                        // Fomantis
        755 to listOf(756),                        // Morelull
        757 to listOf(758),                        // Salandit
        759 to listOf(760),                        // Stufful
        761 to listOf(762), 762 to listOf(763),   // Bounsweet
        767 to listOf(768),                        // Wimpod
        769 to listOf(770),                        // Sandygast
        772 to listOf(773),                        // Type: Null
        782 to listOf(783), 783 to listOf(784),   // Jangmo-o

        // Gen 8 (Galar)
        810 to listOf(811), 811 to listOf(812),   // Grookey
        813 to listOf(814), 814 to listOf(815),   // Scorbunny
        816 to listOf(817), 817 to listOf(818),   // Sobble
        819 to listOf(820),                        // Skwovet
        821 to listOf(822), 822 to listOf(823),   // Rookidee
        824 to listOf(825), 825 to listOf(826),   // Blipbug
        827 to listOf(828),                        // Nickit
        829 to listOf(830),                        // Gossifleur
        831 to listOf(832),                        // Wooloo
        833 to listOf(834),                        // Chewtle
        835 to listOf(836),                        // Yamper
        837 to listOf(838), 838 to listOf(839),   // Rolycoly
        840 to listOf(841, 842),                   // Applin → Flapple / Appletun
        843 to listOf(844),                        // Silicobra
        846 to listOf(847),                        // Arrokuda
        848 to listOf(849),                        // Toxel
        850 to listOf(851),                        // Sizzlipede
        852 to listOf(853),                        // Clobbopus
        854 to listOf(855),                        // Sinistea
        856 to listOf(857), 857 to listOf(858),   // Hatenna
        859 to listOf(860), 860 to listOf(861),   // Impidimp
        868 to listOf(869),                        // Milcery
        872 to listOf(873),                        // Snom
        878 to listOf(879),                        // Cufant
        885 to listOf(886), 886 to listOf(887),   // Dreepy

        // Gen 9 (Paldea)
        906 to listOf(907), 907 to listOf(908),   // Sprigatito
        909 to listOf(910), 910 to listOf(911),   // Fuecoco
        912 to listOf(913), 913 to listOf(914),   // Quaxly
        915 to listOf(916),                        // Lechonk
        917 to listOf(918),                        // Tarountula
        919 to listOf(920),                        // Nymble
        921 to listOf(922), 922 to listOf(923),   // Pawmi
        924 to listOf(925),                        // Tandemaus
        926 to listOf(927),                        // Fidough
        928 to listOf(929), 929 to listOf(930),   // Smoliv
        932 to listOf(933), 933 to listOf(934),   // Nacli
        935 to listOf(936, 937),                   // Charcadet → Armarouge / Ceruledge
        938 to listOf(939),                        // Tadbulb
        940 to listOf(941),                        // Wattrel
        942 to listOf(943),                        // Maschiff
        944 to listOf(945),                        // Shroodle
        946 to listOf(947),                        // Bramblin
        948 to listOf(949),                        // Toedscool
        951 to listOf(952),                        // Capsakid
        953 to listOf(954),                        // Rellor
        955 to listOf(956),                        // Flittle
        957 to listOf(958), 958 to listOf(959),   // Tinkatink
        963 to listOf(964),                        // Finizen
        965 to listOf(966),                        // Varoom
        969 to listOf(970),                        // Glimmet
        971 to listOf(972),                        // Greavard
        974 to listOf(975),                        // Cetoddle
        // Cross-gen evolutions (Gen 9)
        625 to listOf(983),                        // Bisharp → Kingambit
        // Frigibax line
        996 to listOf(997), 997 to listOf(998)    // Frigibax → Arctibax → Baxcalibur
    )

    // Reverse map: evolution ID → pre-evolution ID (built lazily from chains)
    private val reverseChains: Map<Int, Int> by lazy {
        val map = mutableMapOf<Int, Int>()
        chains.forEach { (parent, evos) -> evos.forEach { evo -> map[evo] = parent } }
        map
    }

    fun nextEvolutions(pokedexId: Int): List<Int> = chains[pokedexId] ?: emptyList()
    fun canEvolve(pokedexId: Int): Boolean = chains.containsKey(pokedexId)

    fun previousEvolution(pokedexId: Int): Int? = reverseChains[pokedexId]
    fun canDevolve(pokedexId: Int): Boolean = reverseChains.containsKey(pokedexId)
}
