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
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.ExecutionException

class Register_Pessoa : AppCompatActivity() {

    // Cria variavel do firebaseAuth:
    private lateinit var auth: FirebaseAuth

    // Cria a variavel do banco de dados:
    private lateinit var db: FirebaseFirestore

    // Criar variavel dos campos da tela:
    private lateinit var inputNome: TextInputEditText
    private lateinit var inputCPF: TextInputEditText
    private lateinit var inputEmail: TextInputEditText
    private lateinit var inputCEP: TextInputEditText
    private lateinit var inputNRua: TextInputEditText
    private lateinit var inputSenha: TextInputEditText
    private lateinit var inputRepSenha: TextInputEditText
    private lateinit var txtInfoCEP: TextView
    private lateinit var bVerMais: Button
    private lateinit var bCadastrar: Button
    private lateinit var bJTConta: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_pessoa)
        if(!verificaInternet()){
            alertaSemConexao()
        }

        // Inicializa a variavel do firebaseAuth:
        auth = Firebase.auth

        //inicializa o db:
        db = FirebaseFirestore.getInstance()

        // Inicializa as variaveis dos campos:
        inputNome = findViewById(R.id.Input_Nome_RegisterPessoa)
        inputCPF = findViewById(R.id.Input_CPF_RegisterPessoa)
        inputEmail = findViewById(R.id.Input_Email_RegisterPessoa)
        inputCEP = findViewById(R.id.Input_CEP_RegisterPessoa)
        inputNRua = findViewById(R.id.Input_Numero_RegisterPessoa)
        inputSenha = findViewById(R.id.Input_Senha_RegisterPessoa)
        inputRepSenha = findViewById(R.id.Input_RSenha_RegisterPessoa)
        txtInfoCEP = findViewById(R.id.TextView_DadosCEP_RegisterPessoa)
        bVerMais = findViewById(R.id.Button_VerMais_RegisterPessoa)
        bCadastrar = findViewById(R.id.Button_Cadastrar_RegisterPessoa)
        bJTConta = findViewById(R.id.Button_JtConta_RegisterPessoa)

        // Chama a função que fecha o teclado:
        closeKeyBoard()

        bCadastrar.setOnClickListener {
            if(!verificaInternet()){
                alertaSemConexao()
            }
            closeKeyBoard()

            // Obtem o texto das duas caixas de textos:
            val nomeS = inputNome.text.toString().trim()
            val cpfS = inputCPF.text.toString().trim()
            val emailS = inputEmail.text.toString().trim()
            val cepS = inputCEP.text.toString().trim()
            val ruaS = inputNRua.text.toString().trim()
            val senhaS = inputSenha.text.toString().trim()
            val repsenhaS = inputRepSenha.text.toString().trim()

            if(nomeS.isEmpty() || cpfS.isEmpty() || emailS.isEmpty() || cepS.isEmpty() || ruaS.isEmpty() || senhaS.isEmpty() || repsenhaS.isEmpty()){
                val descripton = getString(R.string.h_algum_campo_em_branco_nverifique_os_campos_e_tente_novamente)
                alertError(descripton)
            }else if (senhaS.length < 7){
                val descripton = getString(R.string.a_senha_digitada_muito_curta)
                alertError(descripton)
            }else if(senhaS != repsenhaS){
                val descripton = getString(R.string.as_senhas_fornecidas_s_o_diferentes)
                alertError(descripton)
            }else if (!validaCPF(cpfS)){
                val descripton = getString(R.string.o_cpf_fornecido_inv_lido_ndeve_estar_no_seguinte_formato_nxxx_xxx_xxx_xx)
                alertError(descripton)
            }else if(!validaCEP(cepS)){
                val descripton = getString(R.string.o_cep_fornecido_inv_lido_ndeve_estar_no_seguinte_formato_nxxxxx_xxx)
                alertError(descripton)
            }else if(validaCEP(cepS)){
                if (cepS.length == 9){
                    if (validaCEP(cepS)){
                        val cep = cepS.trim().replace("-", "")
                        createUser(emailS, senhaS)
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

        bVerMais.setOnClickListener {
            if(!verificaInternet()){
                alertaSemConexao()
            }
            closeKeyBoard()
            if (bVerMais.text.toString() == getString(R.string.ver_mais)){
                bVerMais.text = getString(R.string.ver_menos)
                buscaCEP()
            }else{
                bVerMais.text = getString(R.string.ver_mais)
                txtInfoCEP.text = getString(R.string.informa_es_do_cep)
            }
        }

        // Terceiro código:
        // Clique do botão já tem conta:
        bJTConta.setOnClickListener {
            if(!verificaInternet()){
                alertaSemConexao()
            }
            startActivity(Intent(this, LogIn::class.java))
            finish()
        }
    }

    fun validaCEP(cep: String): Boolean{
        if(!verificaInternet()){
            alertaSemConexao()
        }
        // 03245-040
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
        // XXX. .XXX .XXX-XX
        val padrao = Regex("^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$")
        return if(cpf.length == 14){
            padrao.matches(cpf)
        }else{
            false
        }
    }

    private fun createUser(emailS: String, senhaS: String) {
        if(!verificaInternet()){
            alertaSemConexao()
        }
        auth.createUserWithEmailAndPassword(emailS, senhaS)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    adicionarFirestore()
                }else{
                    // Erro ao realizar Cadastro da empresa:
                    try {
                        throw task.exception!!
                    }catch (e: FirebaseAuthInvalidCredentialsException) {
                        val descripton = getString(R.string.o_endere_o_de_email_est_mal_formatado)
                        alertError(descripton)
                    }catch (e: FirebaseAuthUserCollisionException){
                        val descripton = getString(R.string.o_e_mail_fornecido_j_est_cadastrado)
                        alertError(descripton)
                    }catch (e: FirebaseAuthException){
                        val descripton = getString(R.string.erro_ao_realizar_cadastro)
                        alertError(descripton)
                    }
                }
            }
    }

    private fun buscaCEP() {
        if(!verificaInternet()){
            alertaSemConexao()
        }
        // Obtem o texto das duas caixas de textos:
        var cepS = inputCEP.text.toString()

        if (cepS.length == 9){
            if (validaCEP(cepS)){
                cepS = cepS.trim().replace("-", "")
                try{
                    val retorno = HTTPService(cepS).execute().get()
                    if (retorno.getLogradouro() == null){
                        txtInfoCEP.text = getString(R.string.cep_n_encontrado)
                    }else{
                        txtInfoCEP.text = retorno.toString()
                    }
                }catch (e: InterruptedException){
                    txtInfoCEP.text = getString(R.string.cep_n_encontrado)
                    Log.i("ErroCEP", "1")
                }catch (e: ExecutionException){
                    txtInfoCEP.text = getString(R.string.cep_n_encontrado)
                    Log.i("ErroCEP", "2")
                }
            }else{
                txtInfoCEP.text = getString(R.string.o_cep_fornecido_inv_lido_ndeve_estar_no_seguinte_formato_nxxxxx_xxx)
                Log.i("ErroCEP", "3")
            }
        }else{
            txtInfoCEP.text = getString(R.string.o_cep_fornecido_inv_lido_ndeve_estar_no_seguinte_formato_nxxxxx_xxx)
            Log.i("ErroCEP", "4")
        }
    }

    private fun adicionarFirestore() {
        if(!verificaInternet()){
            alertaSemConexao()
        }
        // obtem o uid do usuário
        val uid = auth.currentUser?.uid.toString()

        // Obtem o texto das duas caixas de textos:
        val nomeS = inputNome.text.toString().trim()
        val cpfS = inputCPF.text.toString().trim()
        val emailS = inputEmail.text.toString().trim()
        val cepS = inputCEP.text.toString().trim()
        val ruaS = inputNRua.text.toString().trim()

        // cria o objeto de mapa contendo os dados que serão adicionads ao firestore:
        val pessoa = hashMapOf(
            "UID" to uid,
            "Nome" to nomeS,
            "CPF" to cpfS,
            "E-mail" to emailS,
            "CEP" to cepS,
            "Número_Rua:" to ruaS
        )
        db.collection("UsuarioPessoa")
            .add(pessoa)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, Inicial_Screen::class.java))
                    finish()
                }else{
                    val user = auth.currentUser
                    user?.delete()
                        ?.addOnSuccessListener {
                            val description: String = "Não foi possivel realizar a criação da conta!"
                            alertError(description)
                        }
                }
            }
    }

    private fun alertError(descripton: String) {
        @SuppressLint("InflateParams")
        val dialogBinding = layoutInflater.inflate(R.layout.error_in_register_pessoa, null)
        val myDialog = Dialog(this)
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(true)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()
        myDialog.findViewById<TextView>(R.id.TextView_DescriptionError_RegisterPessoa).text = descripton
        myDialog.findViewById<Button>(R.id.Button_OK_erroRegisterPessoa).setOnClickListener {
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
        startActivity(Intent(this, LogIn::class.java))
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

