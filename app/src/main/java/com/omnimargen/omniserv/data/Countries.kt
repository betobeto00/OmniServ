package com.omnimargen.omniserv.data

data class Country(val code: String, val name: String)

object Countries {
    val list = listOf(
        Country("VE", "Venezuela"),
        Country("US", "Estados Unidos"),
        Country("CO", "Colombia"),
        Country("MX", "México"),
        Country("AR", "Argentina"),
        Country("CL", "Chile"),
        Country("PE", "Perú"),
        Country("EC", "Ecuador"),
        Country("BR", "Brasil"),
        Country("PA", "Panamá"),
        Country("CR", "Costa Rica"),
        Country("DO", "República Dominicana"),
        Country("HN", "Honduras"),
        Country("SV", "El Salvador"),
        Country("GT", "Guatemala"),
        Country("NI", "Nicaragua"),
        Country("BO", "Bolivia"),
        Country("PY", "Paraguay"),
        Country("UY", "Uruguay"),
        Country("ES", "España"),
        Country("PR", "Puerto Rico"),
        Country("CU", "Cuba"),
        Country("DE", "Alemania"),
        Country("FR", "Francia"),
        Country("IT", "Italia"),
        Country("PT", "Portugal"),
        Country("GB", "Reino Unido"),
        Country("CA", "Canadá"),
        Country("AU", "Australia"),
        Country("CN", "China"),
        Country("JP", "Japón"),
        Country("IN", "India"),
        Country("KR", "Corea del Sur"),
        Country("TR", "Turquía"),
        Country("RU", "Rusia"),
        Country("SA", "Arabia Saudita"),
        Country("AE", "Emiratos Árabes Unidos"),
        Country("IL", "Israel"),
        Country("ZA", "Sudáfrica"),
        Country("NG", "Nigeria"),
        Country("GH", "Ghana"),
        Country("KE", "Kenia"),
        Country("PH", "Filipinas"),
        Country("TH", "Tailandia"),
        Country("VN", "Vietnam"),
        Country("ID", "Indonesia"),
        Country("MY", "Malasia"),
        Country("SG", "Singapur"),
        Country("NZ", "Nueva Zelanda"),
    )

    fun getNameByCode(code: String): String {
        return list.find { it.code == code }?.name ?: code
    }
}
