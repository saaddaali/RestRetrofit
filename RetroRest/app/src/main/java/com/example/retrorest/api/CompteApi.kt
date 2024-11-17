
import retrofit2.Call
import retrofit2.http.*


interface CompteApi {
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @GET("comptes")
    fun getAllComptes(): Call<List<Compte>>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @GET("comptes/{id}")
    fun getCompteById(@Path("id") id: Long): Call<Compte>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("comptes")
    fun createCompteJson(@Body compte: Compte): Call<Compte>

    @Headers(
        "Accept: application/xml",
        "Content-Type: application/xml"
    )
    @POST("comptes")
    fun createCompteXml(@Body xmlString: Compte): Call<Compte>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @PUT("comptes/{id}")
    fun updateCompte(@Path("id") id: Long, @Body compte: Compte): Call<Compte>

    @DELETE("comptes/{id}")
    fun deleteCompte(@Path("id") id: Long): Call<Void>
}
