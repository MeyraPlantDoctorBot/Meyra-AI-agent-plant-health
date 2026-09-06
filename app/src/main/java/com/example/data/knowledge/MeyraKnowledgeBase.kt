package com.example.data.knowledge

data class EthiopianCropInfo(
    val name: String,
    val localNames: String, // Amharic & Afaan Oromo
    val keyDiseases: List<String>,
    val ipmStrategy: String,
    val postHarvestTip: String,
    val valueAddition: String
)

data class OutbreakAlert(
    val id: String,
    val title: String,
    val titleOromo: String,
    val titleAmharic: String,
    val cropAffected: String,
    val regionsAffected: String,
    val riskLevel: String, // Critical, High, Moderate
    val description: String,
    val actionRequired: String,
    val datePosted: String
)

object MeyraKnowledgeBase {

    val ethiopianCrops = listOf(
        EthiopianCropInfo(
            name = "Teff (ጤፍ / allaa)",
            localNames = "Amharic: ጤፍ (Teff) | Afaan Oromo: allaa / Taffii",
            keyDiseases = listOf("Teff Rust (Uromyces eragrostidis)", "Damping off", "Head Blight"),
            ipmStrategy = "Crop rotation with chickpea or faba bean. Sowing early with proper seeding density. Removing infected wild grass species around field borders.",
            postHarvestTip = "Thresh cleanly when grain moisture is below 12%. Store in clean, airtight PICS bags or traditional clay containers lined with ash.",
            valueAddition = "High-protein gluten-free teff flour packaging, Injera bakery aggregation, teff snack production for export."
        ),
        EthiopianCropInfo(
            name = "Coffee (ቡና / Bunna)",
            localNames = "Amharic: ቡና (Bunna) | Afaan Oromo: Bunna",
            keyDiseases = listOf("Coffee Berry Disease (CBD - Colletotrichum)", "Coffee Leaf Rust (Hemileia vastatrix)", "Coffee Wilt Disease (Tracheomycosis)"),
            ipmStrategy = "Pruning old coffee branches to improve ventilation. Mulching with organic crop residues. Planting shade trees (like Acacia, Cordia africana). Removing CBD mummified berries before flowering.",
            postHarvestTip = "Harvest only bright red ripe cherries. Use clean stream water for wet processing or sun-dry on raised African beds for 12-15 days until 11% moisture.",
            valueAddition = "Specialty grade sorting, micro-lot traceability branding, local roasting & eco-pulp composting."
        ),
        EthiopianCropInfo(
            name = "Maize (በቆሎ / Boqollo)",
            localNames = "Amharic: በቆሎ (Bequolo) | Afaan Oromo: Boqolloo",
            keyDiseases = listOf("Fall Armyworm (Spodoptera frugiperda)", "Gray Leaf Spot", "Maize Lethal Necrosis Disease (MLND)"),
            ipmStrategy = "Push-pull technology with Desmodium and Napier grass. Hand-picking egg masses and caterpillars. Applying neem extract or wood ash in maize whorls.",
            postHarvestTip = "Dry cobs thoroughly on clean tarpaulins. Shell gently without cracking kernels. Store in hermetic PICS bags to prevent Prostephanus grain borer.",
            valueAddition = "Balanced animal feed milling, cornstarch small enterprise, roasted maize packaging."
        ),
        EthiopianCropInfo(
            name = "Avocado (አቮካዶ / Avokaadoo)",
            localNames = "Amharic: አቮካዶ (Avocado) | Afaan Oromo: Avokaadoo",
            keyDiseases = listOf("Root Rot (Phytophthora cinnamomi)", "Anthracnose", "Canker"),
            ipmStrategy = "Ensure soil drainage on gentle slopes. Avoid waterlogging around root crown. Apply compost with Trichoderma bio-fungicide.",
            postHarvestTip = "Harvest with stems attached using clip pruners. Use padded plastic crates instead of sacks to eliminate internal bruising.",
            valueAddition = "Cold-pressed virgin avocado oil extraction, avocado butter cosmetic line, pulp freezing for local agro-processors."
        ),
        EthiopianCropInfo(
            name = "Wheat (ስንዴ / Kamadii)",
            localNames = "Amharic: ስንዴ (Sinde) | Afaan Oromo: Kamadii",
            keyDiseases = listOf("Stem Rust (Ug99 strain)", "Yellow / Stripe Rust", "Fusarium Head Blight"),
            ipmStrategy = "Plant rust-resistant certified varieties (e.g., Kingbird, Ogolcho). Rotate with legumes. Early field scouting during high humidity.",
            postHarvestTip = "Dry to 12.5% moisture. Remove chaff and weed seeds using mechanical sieves before storage.",
            valueAddition = "Whole wheat flour, pasta & bread cooperative bakeries, straw briquettes."
        ),
        EthiopianCropInfo(
            name = "Sorghum (ማሽላ / Masingaa)",
            localNames = "Amharic: ማሽላ (Mashilla) | Afaan Oromo: Masingaa",
            keyDiseases = listOf("Sorghum Anthracnose", "Striga Weed Infestation", "Grain Mold"),
            ipmStrategy = "Intercropping with Striga-suppressive legumes. Applying organic manure to enrich soil nitrogen. Using clean certified seeds.",
            postHarvestTip = "Store in dry ventilated stores. Treat with natural botanicals like neem leaf powder or ash.",
            valueAddition = "Sorghum-based malt beverages, traditional Areki/Tella base grain, high-fiber flour blends."
        )
    )

    val activeOutbreaks = listOf(
        OutbreakAlert(
            id = "ALERT_001",
            title = "Fall Armyworm High Risk Warning",
            titleOromo = "Akeekkachiisa Waraana Waraabaa Boqolloo",
            titleAmharic = "የመኸር ሰራዊት ትል ከፍተኛ ማስጠንቀቂያ",
            cropAffected = "Maize, Sorghum",
            regionsAffected = "Oromia (Jimma, West Shoa), Amhara (East Gojjam), SNNPR",
            riskLevel = "Critical",
            description = "Favorable humid conditions have triggered rapid egg hatching of Fall Armyworm in young maize fields.",
            actionRequired = "Inspect young whorls twice weekly. Apply neem leaf extract or targeted biorational treatment early morning.",
            datePosted = "August 2026"
        ),
        OutbreakAlert(
            id = "ALERT_002",
            title = "Coffee Leaf Rust Wet Season Alert",
            titleOromo = "Akeekkachiisa Waawwaatama Balleessaa Bunnaa",
            titleAmharic = "የቡና ቅጠል ዝገት የዝናብ ወቅት ማስጠንቀቂያ",
            cropAffected = "Coffee",
            regionsAffected = "Sidama, Oromia (Guji, Keffa), SNNPR",
            riskLevel = "High",
            description = "High rainfall in southern coffee belts has increased fungal spore dispersal of Coffee Leaf Rust.",
            actionRequired = "Prune excess shade branches to increase sunlight penetration. Apply copper-based organic protectant if symptoms appear.",
            datePosted = "August 2026"
        ),
        OutbreakAlert(
            id = "ALERT_003",
            title = "Desert Locust Surveillance Update",
            titleOromo = "Odoo Guutuufi Hawaasaa Hawwaannisaa",
            titleAmharic = "የበረሀ አንበጣ ክትትል መረጃ",
            cropAffected = "Teff, Sorghum, Pasture",
            regionsAffected = "Afar, Somali, Lowland Hararghe",
            riskLevel = "Moderate",
            description = "Swarm movements reported across eastern lowlands. Local scouting teams deployed.",
            actionRequired = "Report unusual hopper band sightings immediately to Woreda Agriculture Office.",
            datePosted = "August 2026"
        )
    )
}
