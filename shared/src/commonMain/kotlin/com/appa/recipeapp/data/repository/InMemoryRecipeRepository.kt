package com.appa.recipeapp.data.repository

import com.appa.recipeapp.data.model.Ingredient
import com.appa.recipeapp.data.model.Rating
import com.appa.recipeapp.data.model.Recipe
import com.appa.recipeapp.data.model.RecipeStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import com.appa.recipeapp.utils.fuzzyMatches
import com.appa.recipeapp.data.model.MusicTrack

/**
 * Временная реализация в памяти для разработки UI.
 * Позже заменим на Firebase или Room.
 */
class InMemoryRecipeRepository : RecipeRepository {

    private val recipesFlow = MutableStateFlow(seedRecipes())
    private var nextId = 100

    override fun getMainFeed(tags: Set<String>, query: String): Flow<List<Recipe>> =
        recipesFlow.map { list ->
            var filtered = list

            // Фильтр по тегам
            if (tags.isNotEmpty()) {
                filtered = filtered.filter { recipe -> recipe.tags.any { it in tags } }
            }

            // Фильтр по поисковой строке (название + автор)
            if (query.isNotBlank()) {
                filtered = filtered.filter { recipe ->
                    fuzzyMatches(recipe.title, query) ||
                            fuzzyMatches(recipe.authorName, query)
                }
            }

            filtered.shuffled()
        }

    override fun getByTag(tag: String): Flow<List<Recipe>> =
        recipesFlow.map { list ->
            list.filter { tag in it.tags }.shuffled()
        }

    override fun getTopRated(limit: Int): Flow<List<Recipe>> =
        recipesFlow.map { list ->
            list.sortedByDescending { recipe ->
                if (recipe.ratings.isEmpty()) 0.0
                else recipe.ratings.map { it.stars }.average()
            }.take(limit)
        }

    override fun getRecipe(id: String): Flow<Recipe?> =
        recipesFlow.map { list -> list.find { it.id == id } }

    override fun getUserRecipes(userId: String): Flow<List<Recipe>> =
        recipesFlow.map { list -> list.filter { it.authorId == userId } }

    override fun getAllTags(): Flow<List<String>> =
        recipesFlow.map { list ->
            list.flatMap { it.tags }.distinct().sorted()
        }

    override fun getFavorites(): Flow<List<Recipe>> =
        recipesFlow.map { list -> list.filter { it.isFavorite } }

    override suspend fun toggleFavorite(recipeId: String) {
        recipesFlow.value = recipesFlow.value.map { recipe ->
            if (recipe.id == recipeId) recipe.copy(isFavorite = !recipe.isFavorite)
            else recipe
        }
    }

    override suspend fun addRecipe(recipe: Recipe): String {
        val id = "recipe_${nextId++}"
        val newRecipe = recipe.copy(id = id, createdAt = nowMillis())
        recipesFlow.value = recipesFlow.value + newRecipe
        return id
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        recipesFlow.value = recipesFlow.value.map {
            if (it.id == recipe.id) recipe else it
        }
    }

    override suspend fun deleteRecipe(id: String) {
        recipesFlow.value = recipesFlow.value.filterNot { it.id == id }
    }

    override suspend fun addOrUpdateRating(recipeId: String, rating: Rating) {
        recipesFlow.value = recipesFlow.value.map { recipe ->
            if (recipe.id != recipeId) recipe
            else {
                val updatedRatings = recipe.ratings
                    .filterNot { it.userId == rating.userId } + rating
                recipe.copy(ratings = updatedRatings)
            }
        }
    }

    private fun nowMillis(): Long = 1730000000000L + nextId * 1000L

    private fun seedRecipes(): List<Recipe> = listOf(
        Recipe(
            id = "recipe_1",
            authorId = "me",
            authorName = "Вы",
            title = "Борщ",
            description = "Классический красный борщ со свёклой и говядиной. Наваристый и ароматный.",
            ingredients = listOf(
                Ingredient("Свёкла", "2", "шт"),
                Ingredient("Говядина", "500", "г"),
                Ingredient("Капуста", "300", "г"),
                Ingredient("Картофель", "4", "шт"),
                Ingredient("Лук", "1", "шт"),
                Ingredient("Морковь", "1", "шт"),
                Ingredient("Томатная паста", "2", "ст.л."),
            ),
            steps = listOf(
                RecipeStep("Сварить бульон из говядины (1.5 часа)."),
                RecipeStep("Нарезать свёклу, морковь, лук. Обжарить с томатной пастой."),
                RecipeStep("Добавить картофель в бульон, варить 10 минут."),
                RecipeStep("Добавить капусту и зажарку, варить 15 минут."),
                RecipeStep("Дать настояться 30 минут под крышкой."),
            ),
            tags = listOf("суп", "русская кухня", "горячее", "обед"),
            cookingTimeMinutes = 120,
            ratings = listOf(
                Rating("friend_1", "Аня", 5, "Лучший борщ!", 1730000001000L),
                Rating("friend_2", "Максим", 4, "Вкусно, но свёклы можно больше", 1730000002000L),
                Rating("friend_3", "Оля", 5, "Как у бабушки 🥰", 1730000003000L),
            ),
        ),
        Recipe(
            id = "recipe_2",
            authorId = "friend_1",
            authorName = "Аня",
            title = "Паста карбонара",
            description = "Итальянская классика: спагетти, гуанчале, яйца и пекорино.",
            ingredients = listOf(
                Ingredient("Спагетти", "400", "г"),
                Ingredient("Гуанчале", "200", "г"),
                Ingredient("Яйца", "4", "шт"),
                Ingredient("Пекорино", "100", "г"),
                Ingredient("Перец чёрный", "по вкусу", ""),
            ),
            steps = listOf(
                RecipeStep("Отварить спагетти до аль денте."),
                RecipeStep("Обжарить гуанчале до хруста."),
                RecipeStep("Смешать яйца с тёртым сыром и перцем."),
                RecipeStep("Соединить пасту с гуанчале, снять с огня."),
                RecipeStep("Быстро вмешать яичную смесь, постоянно помешивая."),
            ),
            tags = listOf("паста", "итальянская кухня", "быстро", "ужин"),
            cookingTimeMinutes = 25,
            ratings = listOf(
                Rating("me", "Вы", 5, "Идеально!", 1730000004000L),
                Rating("friend_2", "Максим", 5, "", 1730000005000L),
            ),
        ),
        Recipe(
            id = "recipe_3",
            authorId = "me",
            authorName = "Вы",
            title = "Оливье",
            description = "Новогодний салат, который любят все.",
            ingredients = listOf(
                Ingredient("Картофель", "4", "шт"),
                Ingredient("Морковь", "2", "шт"),
                Ingredient("Яйца", "4", "шт"),
                Ingredient("Горошек", "1", "банка"),
                Ingredient("Огурцы солёные", "3", "шт"),
                Ingredient("Майонез", "200", "г"),
            ),
            steps = listOf(
                RecipeStep("Отварить картофель, морковь и яйца. Остудить."),
                RecipeStep("Нарезать всё мелкими кубиками."),
                RecipeStep("Добавить горошек и нарезанные огурцы."),
                RecipeStep("Заправить майонезом, посолить."),
            ),
            tags = listOf("салат", "русская кухня", "праздничное", "обед"),
            cookingTimeMinutes = 60,
            ratings = listOf(
                Rating("friend_3", "Оля", 4, "Классика", 1730000006000L),
            ),
        ),
        Recipe(
            id = "recipe_4",
            authorId = "friend_2",
            authorName = "Максим",
            title = "Сырники",
            description = "Пышные сырники на завтрак со сгущёнкой.",
            ingredients = listOf(
                Ingredient("Творог", "500", "г"),
                Ingredient("Яйцо", "1", "шт"),
                Ingredient("Мука", "3", "ст.л."),
                Ingredient("Сахар", "2", "ст.л."),
                Ingredient("Соль", "щепотка", ""),
            ),
            steps = listOf(
                RecipeStep("Смешать творог, яйцо, сахар и соль."),
                RecipeStep("Добавить муку, замесить мягкое тесто."),
                RecipeStep("Сформировать сырники, обвалять в муке."),
                RecipeStep("Обжарить на среднем огне по 3 минуты с каждой стороны."),
            ),
            tags = listOf("завтрак", "десерт", "быстро"),
            cookingTimeMinutes = 20,
            ratings = listOf(
                Rating("me", "Вы", 5, "Очень вкусно", 1730000007000L),
                Rating("friend_1", "Аня", 5, "", 1730000008000L),
                Rating("friend_3", "Оля", 4, "Сладковато", 1730000009000L),
            ),
        ),
        Recipe(
            id = "recipe_5",
            authorId = "friend_3",
            authorName = "Оля",
            title = "Том Ям",
            description = "Острый тайский суп с креветками и кокосовым молоком.",
            ingredients = listOf(
                Ingredient("Креветки", "300", "г"),
                Ingredient("Кокосовое молоко", "400", "мл"),
                Ingredient("Грибы", "200", "г"),
                Ingredient("Лемонграсс", "2", "стебля"),
                Ingredient("Паста Том Ям", "2", "ст.л."),
            ),
            steps = listOf(
                RecipeStep("Вскипятить воду с лемонграссом."),
                RecipeStep("Добавить пасту Том Ям, размешать."),
                RecipeStep("Добавить грибы, варить 5 минут."),
                RecipeStep("Добавить креветки и кокосовое молоко."),
                RecipeStep("Варить 3 минуты, подавать с лаймом."),
            ),
            tags = listOf("суп", "тайская кухня", "острое", "ужин"),
            cookingTimeMinutes = 30,
            musicNote = "Включи что-нибудь тайское - лемонграсс, бамбук, всё такое.",
            musicTracks = listOf(
                MusicTrack(
                    title = "Thai Kitchen Jazz",
                    url = "https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M",
                ),
            ),
            ratings = listOf(
                Rating("friend_1", "Аня", 4, "Остро!", 1730000010000L),
                Rating("friend_2", "Максим", 5, "Топ", 1730000011000L),
            ),
        ),
        Recipe(
            id = "recipe_6",
            authorId = "me",
            authorName = "Вы",
            title = "Шарлотка",
            description = "Простой яблочный пирог из 4 ингредиентов.",
            ingredients = listOf(
                Ingredient("Яйца", "4", "шт"),
                Ingredient("Сахар", "200", "г"),
                Ingredient("Мука", "200", "г"),
                Ingredient("Яблоки", "5", "шт"),
            ),
            steps = listOf(
                RecipeStep("Взбить яйца с сахаром до пышной массы."),
                RecipeStep("Аккуратно вмешать муку."),
                RecipeStep("Нарезать яблоки, выложить в форму."),
                RecipeStep("Залить тестом, выпекать 40 минут при 180°C."),
            ),
            tags = listOf("десерт", "выпечка", "быстро", "завтрак"),
            cookingTimeMinutes = 50,
            ratings = listOf(
                Rating("friend_2", "Максим", 5, "Обожаю", 1730000012000L),
                Rating("friend_3", "Оля", 5, "", 1730000013000L),
            ),
        ),
    )
}