package br.com.local.bakiliya

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.ExecutionException

class Add_Cliente_Pessoa : AppCompatActivity() {

    // Cria variavel do firebaseAuth:
    private lateinit var auth: FirebaseAuth

    // Cria a variavel do banco de dados:
    private lateinit var db: FirebaseFirestore

    // variaveis para adição no firestore:
    private lateinit var uid: String
    private lateinit var collectionName: String
    private lateinit var tipo: String

    // variaveis dos campos:
    private lateinit var inputNome: TextInputEditText
    private lateinit var inputCpf: TextInputEditText
    private lateinit var inputEmail: TextInputEditText
    private lateinit var inputContato: TextInputEditText
    private lateinit var inputCep: TextInputEditText
    private lateinit var inputNrua: TextInputEditText
    private lateinit var txtIcep: TextView
    private lateinit var bVerMais: Button
    private lateinit var bAdicionar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_cliente_pessoa)
        if(!verificaInternet()){
            alertaSemConexao()
        }

        // Inicializa a variavel do firebaseAuth:
        auth = Firebase.auth

        //inicializa o db:
        db = FirebaseFirestore.getInstance()

        // obtem o uid do usuário
        uid = auth.currentUser?.uid.toString()

        // Obtem o tipo de cliente:
        tipo = "Pessoa"

        // obtem o nome da coleão que tera os clientes:
        collectionName = "${uid}_clientes"
        Log.i("CollectionName", collectionName)

        // Inicializa as variaveis dos campso:
        inputNome = findViewById(R.id.Input_Nome_AddPessoa)
        inputCpf = findViewById(R.id.Input_CPF_AddPessoa)
        inputEmail = findViewById(R.id.Input_Email_AddPessoa)
        inputContato = findViewById(R.id.Input_Contato_AddPessoa)
        inputCep = findViewById(R.id.Input_CEP_AddPessoa)
        inputNrua = findViewById(R.id.Input_Numero_AddPessoa)
        txtIcep = findViewById(R.id.TextView_DadosCEP_AddPessoa)
        bVerMais = findViewById(R.id.Button_VerMais_AddPessoa)
        bAdicionar = findViewById(R.id.Button_Adicionar_AddPessoa)

        closeKeyBoard()

        bAdicionar.setOnClickListener{
            if(!verificaInternet()){
                alertaSemConexao()
            }
            closeKeyBoard()

            // Obtem o texto das caixas de texto
            val nomeS = inputNome.text.toString().trim()
            val cpfS = inputCpf.text.toString().trim()
            val emailS = inputEmail.text.toString().trim()
            val contatoS = inputContato.text.toString().trim()
            val cepS = inputCep.text.toString().trim()
            val ruaS = inputNrua.text.toString().trim()

            if (nomeS.isEmpty() || cpfS.isEmpty() || emailS.isEmpty() || contatoS.isEmpty() || cepS.isEmpty() || ruaS.isEmpty()){
                val descripton = getString(R.string.h_algum_campo_em_branco_nverifique_os_campos_e_tente_novamente)
                alertError(descripton)
            }else if(!validaCPF(cpfS)){
                val descripton = getString(R.string.o_cpf_fornecido_inv_lido_ndeve_estar_no_seguinte_formato_nxxx_xxx_xxx_xx)
                alertError(descripton)
            }else if(!validaCEP(cepS)){
                val descripton = getString(R.string.o_cep_fornecido_inv_lido_ndeve_estar_no_seguinte_formato_nxxxxx_xxx)
                alertError(descripton)
            }else if(validaCEP(cepS)){
                if (cepS.length == 9){
                    if (validaCEP(cepS)){
                        val cep = cepS.trim().replace("-", "")
                        addUserFirestore()
                        try{
                            HTTPService(cep).execute().get()
                        }catch (e: InterruptedException){
                            val descripton = getString(R.string.o_cep_fornecido_inv_lido_ndeve_estar_no_seguinte_formato_nxxxxx_xxx)
                            alertError(descripton)
                        }catch (e: ExecutionException){
                            val descripton = getString(R.string.o_cep_fornecido_inv_lido_ndeve_estar_no_seguinte_formato_nxxxxx_xxx)
                            alertError(descripton)
                        }
                    }else{
                        val descripton = getString(R.string.o_cep_fornecido_inv_lido_ndeve_estar_no_seguinte_formato_nxxxxx_xxx)
                        alertError(descripton)
                    }
                }else{
                    val descripton = getString(R.string.o_cep_fornecido_inv_lido_ndeve_estar_no_seguinte_formato_nxxxxx_xxx)
                    alertError(descripton)
                }
            }
        }

        bVerMais.setOnClickListener{
            if(!verificaInternet()){
                alertaSemConexao()
            }
            closeKeyBoard()
            if (bVerMais.text.toString() == getString(R.string.ver_mais)){
                bVerMais.text = getString(R.string.ver_menos)
                buscaCEP()
            }else{
                bVerMais.text = getString(R.string.ver_mais)
                txtIcep.text = getString(R.string.informa_es_do_cep)
            }
        }
    }

    private fun buscaCEP() {
        if(!verificaInternet()){
            alertaSemConexao()
        }
        // Obtem o texto das duas caixas de textos:
        var cepS = inputCep.text.toString()

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

    private fun addUserFirestore() {
        if(!verificaInternet()){
            alertaSemConexao()
        }
        // Obtem o texto das caixas de texto
        val nomeS = inputNome.text.toString().trim()
        val cpfS = inputCpf.text.toString().trim()
        val emailS = inputEmail.text.toString().trim()
        val contatoS = inputContato.text.toString().trim()
        val cepS = inputCep.text.toString().trim()
        val ruaS = inputNrua.text.toString().trim()

        // cria o objeto de mapa contendo os dados que serão adicionads ao firestore:
        val pessoa = hashMapOf(
            "Nome" to nomeS,
            "Codigo" to cpfS,
            "Email" to emailS,
            "Contato" to contatoS,
            "CEP" to cepS,
            "NRua" to ruaS,
            "Tipo" to tipo
        )

        db.collection(collectionName)
            .add(pessoa)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, Inicial_Screen::class.java))
                    finish()
                }else{
                    val description: String = "Não foi possivel realizar a adição de cliente!"
                    alertError(description)
                }
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

    private fun validaCPF(cpf: String): Boolean {
        if(!verificaInternet()){
            alertaSemConexao()
        }
        val padrao = Regex("^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$")
        return if(cpf.length == 14){
            padrao.matches(cpf)
        }else{
            false
        }
    }

    private fun alertError(descripton: String) {
        @SuppressLint("InflateParams")
        val dialogBinding = layoutInflater.inflate(R.layout.error_in_add_pessoa, null)
        val myDialog = Dialog(this)
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(true)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()
        myDialog.findViewById<TextView>(R.id.TextView_DescriptionError_AddPessoa).text = descripton
        myDialog.findViewById<Button>(R.id.Button_OK_erroAddPessoa).setOnClickListener {
            myDialog.dismiss()
        }

    }

    private fun closeKeyBoard() {
        val view: View? = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, Inicial_Screen::class.java))
        finish()
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