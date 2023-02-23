package br.com.local.bakiliya

import android.os.AsyncTask
import com.google.gson.Gson
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class HTTPService(cepS: String) : AsyncTask<Void, Void, CEP>() {
    private var cep: String? = cepS

    override fun doInBackground(vararg void: Void?): CEP {

        val resposta = StringBuilder()

        if (cep != null && cep!!.length == 8) {
            try {
                val url = URL("https://viacep.com.br/ws/$cep/json/")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Content-type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 5000
                connection.connect()
                val scanner = Scanner(url.openStream())
                while (scanner.hasNext()) {
                    resposta.append(scanner.next())
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return Gson().fromJson(resposta.toString(), CEP::class.java)
    }
}
