package br.com.local.bakiliya

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import java.util.concurrent.ExecutionException

class View_Cliente_Pessoa : AppCompatActivity() {

    // variaveis dos dados:
    private lateinit var Nome: String
    private lateinit var Codigo: String
    private lateinit var Email: String
    private lateinit var Contato: String
    private lateinit var Cep: String
    private lateinit var Nrua: String

    // campos:
    private lateinit var txtNome: TextView
    private lateinit var txtCodigo: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtContato: TextView
    private lateinit var txtCep: TextView
    private lateinit var txtNrua: TextView
    private lateinit var txtIcep: TextView
    private lateinit var bVerMais: Button
    private lateinit var bLigacao: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_cliente_pessoa)
        if(!verificaInternet()){
            alertaSemConexao()
        }

        txtNome = findViewById<TextView>(R.id.TextView_Nome_ViewClientePessoa)
        txtCodigo = findViewById<TextView>(R.id.TextView_CPF_ViewClientePessoa)
        txtEmail = findViewById<TextView>(R.id.TextView_Email_ViewClientePessoa)
        txtContato = findViewById<TextView>(R.id.TextView_Contato_ViewClientePessoa)
        txtCep = findViewById<TextView>(R.id.TextView_CEP_ViewClientePessoa)
        txtNrua = findViewById<TextView>(R.id.TextView_NRUA_ViewClientePessoa)
        txtIcep = findViewById(R.id.TextView_DadosCEP_ViewClientePessoa)
        bVerMais = findViewById(R.id.Button_VerMais_ViewClientePessoa)
        bLigacao = findViewById(R.id.efetuarLigacao_ClientePessoa)

        val extras = intent.extras
        if (extras != null){
            if(!verificaInternet()){
                alertaSemConexao()
            }
            Nome = extras.getString("Nome").toString()
            Codigo = extras.getString("Codigo").toString()
            Email = extras.getString("Email").toString()
            Contato = extras.getString("Contato").toString()
            Cep = extras.getString("Cep").toString()
            Nrua = extras.getString("NRua").toString()

            txtNome.text = Nome
            txtCodigo.text = Codigo
            txtEmail.text = Email
            txtContato.text = Contato
            txtCep.text = Cep
            txtNrua.text = Nrua
        }

        bVerMais.setOnClickListener{
            if(!verificaInternet()){
                alertaSemConexao()
            }
            if (bVerMais.text.toString() == getString(R.string.ver_mais)){
                bVerMais.text = getString(R.string.ver_menos)
                buscaCEP()
            }else{
                bVerMais.text = getString(R.string.ver_mais)
                txtIcep.text = getString(R.string.informa_es_do_cep)
            }
        }

        bLigacao.setOnClickListener{
            if(!verificaInternet()){
                alertaSemConexao()
            }
            Toast.makeText(this, "Faz a ligação!", Toast.LENGTH_LONG).show()
            val u = Uri.parse("tel:$Contato")

            // Create the intent and set the data for the
            // intent as the phone number.
            val i = Intent(Intent.ACTION_DIAL, u)
            try {
                // Launch the Phone app's dialer with a phone
                // number to dial a call.
                startActivity(i)
            } catch (s: SecurityException) {
                // show() method display the toast with
                // exception message.
                Toast.makeText(this, "Não foi possivel fazer ligação!", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //startActivity(Intent(this, Inicial_Screen::class.java))
        finish()
    }

    private fun buscaCEP() {
        if(!verificaInternet()){
            alertaSemConexao()
        }
        // Obtem o texto das duas caixas de textos:
        var cepS = Cep

        if (cepS.length == 9){
            if (validaCEP(cepS)){
                cepS = cepS.trim().replace("-", "")
                try{
                    val retorno = HTTPService(cepS).execute().get()
                    if (retorno.getLogradouro() == null){
                        txtIcep.text = getString(R.string.cep_n_encontrado)
                    }else{
                        txtIcep.text = retorno.toString()
                    }
                }catch (e: InterruptedException){
                    txtIcep.text = getString(R.string.cep_n_encontrado)
                    Log.i("ErroCEP", "1")
                }catch (e: ExecutionException){
                    txtIcep.text = getString(R.string.cep_n_encontrado)
                    Log.i("ErroCEP", "2")
                }
            }else{
                txtIcep.text = getString(R.string.o_cep_fornecido_inv_lido_ndeve_estar_no_seguinte_formato_nxxxxx_xxx)
                Log.i("ErroCEP", "3")
            }
        }else{
            txtIcep.text = getString(R.string.o_cep_fornecido_inv_lido_ndeve_estar_no_seguinte_formato_nxxxxx_xxx)
            Log.i("ErroCEP", "4")
        }
    }

    private fun validaCEP(cep: String): Boolean {
        if(!verificaInternet()){
            alertaSemConexao()
        }
        val padrao = Regex("^\\d{5}-\\d{3}$")
        return if(cep.length == 9){
            padrao.matches(cep)
        }else{
            false
        }
    }

    private fun verificaInternet(): Boolean {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        return isConnected
    }

    private fun alertaSemConexao() {
        val layout = R.layout.sem_conexao
        val dialogBinding = layoutInflater.inflate(layout, null)
        val myDialog = Dialog(this)
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(false)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()
        myDialog.findViewById<Button>(R.id.Btn_Reconectar).setOnClickListener {
            if(verificaInternet()){
                myDialog.dismiss()
            }else{
                Toast.makeText(this, "Ainda não há conexão!", Toast.LENGTH_LONG).show()
            }
        }
    }
}