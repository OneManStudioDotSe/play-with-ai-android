package se.onemanstudio.playaroundwithai.data.agents.data.tools

import se.onemanstudio.playaroundwithai.core.network.dto.FunctionDeclaration
import se.onemanstudio.playaroundwithai.core.network.dto.FunctionParameters
import se.onemanstudio.playaroundwithai.core.network.dto.PropertySchema
import se.onemanstudio.playaroundwithai.core.network.dto.Tool

fun buildToolDeclarations(): Tool = Tool(
    functionDeclarations = listOf(
        FunctionDeclaration(
            name = "search_places",
            description = "Search for interesting places, restaurants, cafes, or attractions near a location",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "query" to PropertySchema(type = "STRING", description = "What to search for"),
                    "latitude" to PropertySchema(type = "NUMBER", description = "Center latitude"),
                    "longitude" to PropertySchema(type = "NUMBER", description = "Center longitude"),
                    "count" to PropertySchema(type = "INTEGER", description = "Number of results (1-10)"),
                ),
                required = listOf("query", "latitude", "longitude"),
            ),
        ),
        FunctionDeclaration(
            name = "calculate_route",
            description = "Calculate the optimal walking route between a list of places, returning the best order and total distance",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "places" to PropertySchema(
                        type = "ARRAY",
                        description = "List of place objects with name, latitude, longitude",
                        items = PropertySchema(
                            type = "OBJECT",
                            description = "A place",
                            properties = mapOf(
                                "name" to PropertySchema(type = "STRING", description = "Name of the place"),
                                "latitude" to PropertySchema(type = "NUMBER", description = "Latitude"),
                                "longitude" to PropertySchema(type = "NUMBER", description = "Longitude"),
                            ),
                            required = listOf("name", "latitude", "longitude"),
                        ),
                    ),
                ),
                required = listOf("places"),
            ),
        ),
    ),
)
