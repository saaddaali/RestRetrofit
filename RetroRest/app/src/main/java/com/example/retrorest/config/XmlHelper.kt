import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import java.io.StringWriter

object XmlHelper {
    private val serializer: Serializer = Persister()

    fun compteToXml(compte: Compte): String {
        return try {
            val writer = StringWriter()
            serializer.write(compte, writer)
            writer.toString()
        } catch (e: Exception) {
            throw Exception("Failed to convert Compte to XML: ${e.message}")
        }
    }

    fun xmlToCompte(xml: String): Compte {
        return try {
            serializer.read(Compte::class.java, xml)
        } catch (e: Exception) {
            throw Exception("Failed to parse XML to Compte: ${e.message}")
        }
    }
}