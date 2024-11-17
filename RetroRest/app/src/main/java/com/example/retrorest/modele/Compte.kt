import com.example.retrorest.modele.TypeCompte
import com.google.gson.annotations.SerializedName
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "compte", strict = false)
data class Compte(
    @field:Element(name = "id", required = false)
    @SerializedName("id")
    var id: Long? = null,
    @field:Element(name = "solde")
    @SerializedName("solde")
    var solde: Double,
    @field:Element(name = "dateCreation")
    @SerializedName("dateCreation")
    var dateCreation: String,
    @field:Element(name = "typeCompte")
    @SerializedName("typeCompte")
    var typeCompte: TypeCompte


)
//no arg
{
    constructor() : this(null, 0.0, "", TypeCompte.COURANT)
}