import com.example.retrorest.config.CustomXmlConverterFactory
import com.example.retrorest.modele.TypeCompte
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

object RetrofitClient {
    private const val BASE_URL = "http://MacBook-Pro-de-Saad.local:8085/banque/" // Remplace par l'URL du backend

    private var retrofitJson: Retrofit? = null
    private var retrofitXml: Retrofit? = null
    fun getJsonInstance(): Retrofit {
        if (retrofitJson == null) {
            val gson = GsonBuilder()
                .registerTypeAdapter(TypeCompte::class.java, object : JsonSerializer<TypeCompte> {
                    override fun serialize(src: TypeCompte, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
                        return JsonPrimitive(src.name)
                    }
                })
                .registerTypeAdapter(TypeCompte::class.java, object : JsonDeserializer<TypeCompte> {
                    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TypeCompte {
                        return TypeCompte.valueOf(json.asString)
                    }
                })
                .create()

            retrofitJson = createRetrofit(GsonConverterFactory.create(gson))
        }
        return retrofitJson!!
    }

    fun getXmlInstance(): Retrofit {
        if (retrofitXml == null) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("Accept", "application/xml")
                        .header("Content-Type", "application/xml")
                        .method(original.method, original.body)
                        .build()
                    chain.proceed(request)
                }
                .build()

            retrofitXml = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(CustomXmlConverterFactory.create())
                .build()
        }
        return retrofitXml!!
    }

    private fun createRetrofit(converterFactory: Converter.Factory): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .build()
    }



}
