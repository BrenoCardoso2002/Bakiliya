package br.com.local.bakiliya

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LogIn : AppCompatActivity() {

    // Cria variavel do firebaseAuth:
    private lateinit var auth: FirebaseAuth

    // Criar variavel dos campos da tela:
    private lateinit var inputEmail: TextInputEditText
    private lateinit var inputSenha: TextInputEditText
    private lateinit var bEsqueci: Button
    private lateinit var bConectar: Button
    private lateinit var bCriar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        if(!verificaInternet()){
            alertaSemConexao()
        }

        // Inicializa a variavel do firebaseAuth:
        auth = Firebase.auth

        // Inicializa as variaveis dos campos:
        inputEmail = findViewById(R.id.Input_Email_Login)
        inputSenha = findViewById(R.id.Input_Senha_Login)
        bEsqueci = findViewById(R.id.Button_Esqueci_Login)
        bConectar = findViewById(R.id.Button_Conectar_Login)
        bCriar = findViewById(R.id.Button_Criar_Login)

        // Chama a função que fecha o teclado:
        closeKeyBoard()

        // Primeiro código:
        // Clique do botão para se conectar:
        bConectar.setOnClickListener {
            closeKeyBoard()
            if(!verificaInternet()){
                alertaSemConexao()
            }

            // Obtem o texto das duas caixas de textos:
            val emailS = inputEmail.text.toString().trim()
            val senhaS = inputSenha.text.toString()

            if (emailS.isEmpty() || senhaS.isEmpty()){
                val descripton = getString(R.string.h_algum_campo_em_branco_nverifique_os_campos_e_tente_novamente)
                alertError(descripton)
            }else{
                // Realiza o login do usuário:
                auth.signInWithEmailAndPassword(emailS, senhaS)
                    .addOnCompleteListener(this) {task ->
                        if (task.isSuccessful){
                            // Vai para a tela inicial:
                            startActivity(Intent(this, Inicial_Screen::class.java))
                            finish()
                        }else{
                            // Caso de erro ao realizar login:
                            try{
                                throw task.exception!!
                            }catch (e: FirebaseAuthInvalidCredentialsException) {
                                val errorCode = (task.exception as FirebaseAuthException?)!!.errorCode
                                if (errorCode == "ERROR_INVALID_EMAIL"){
                                    val descripton = getString(R.string.o_endere_o_de_e_mail_fornecido_inv_lido_ntente_novamente)
                                    alertError(descripton)
                                }else if(errorCode == "ERROR_WRONG_PASSWORD"){
                                    val descripton = getString(R.string.senha_inv_lida_ntente_novamente_ou_clique_em_esqueci_a_senha)
                                    alertError(descripton)
                                }
                            }catch (e: FirebaseAuthInvalidUserException){
                                val descripton = getString(R.string.endere_o_de_e_mail_n_o_est_cadastrado_ntente_criar_uma_conta)
                                alertError(descripton)
                            }catch (e: FirebaseAuthException){
                                val descripton = getString(R.string.erro_ao_relizar_o_login)
                                alertError(descripton)
                            }
                        }
                    }
            }
        }

        // Segundo código:
        // Clique do botão para recuperar a senha:
        bEsqueci.setOnClickListener {
            closeKeyBoard()
            if(!verificaInternet()){
                alertaSemConexao()
            }

            // Obtem o texto das duas caixas de textos:
            val emailS = inputEmail.text.toString().trim()

            if(emailS.isEmpty()){
                val descripton = getString(R.string.o_campo_de_e_mail_est_em_branco)
                alertErrorRecSenha(descripton)
            }else{
                // Envia o e-mail de recuperação de senha:
                auth.sendPasswordResetEmail(emailS)
                    .addOnCompleteListener(this) {task ->
                        if (task.isSuccessful){
                            alertSucessRecSenha()
                        }else{
                            // E-mail não enviado:
                            try {
                                throw task.exception!!
                            }catch (e: FirebaseAuthInvalidCredentialsException) {
                                val descripton = getString(R.string.o_endere_o_de_e_mail_est_mal_formatado)
                                alertErrorRecSenha(descripton)
                            }catch (e: FirebaseAuthInvalidUserException){
                                val descripton = getString(R.string.endere_o_de_e_mail_n_o_est_cadastrado_ntente_criar_uma_conta)
                                alertErrorRecSenha(descripton)
                            }catch (e: FirebaseAuthException){
                                val descripton = getString(R.string.n_o_foi_possivel_enviar_e_mail_de_recupera_o_de_senha)
                                alertErrorRecSenha(descripton)
                            }
                        }
                    }
            }
        }

        // Terceiro código:
        // Clique do botão criar conta:
        bCriar.setOnClickListener {
            if(!verificaInternet()){
                alertaSemConexao()
            }
            // Chama o alerta pra escolher o tipo de cadastro:
            alertChoiceRegister()
        }

        // Quarto código:
        // Fecha o teclado ao clica no enter:
        inputEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                closeKeyBoard()
                true
            } else {
                false
            }
        }

        // Quinto código:
        // Fecha o teclado ao clica no enter:
        inputSenha.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                closeKeyBoard()
                true
            } else {
                false
            }
        }

    }

    // Função que chama a alertDialog de escolher tipo de cadastro:
    private fun alertChoiceRegister() {
        @SuppressLint("InflateParams")
        val dialogBinding = layoutInflater.inflate(R.layout.choice_register, null)
        val myDialog = Dialog(this)
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(false)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()
        myDialog.findViewById<Button>(R.id.Button_pEmpresa_choiceRegister).setOnClickListener {
            myDialog.dismiss()
            startActivity(Intent(this, Register_Empresa::class.java))
            finish()
        }
        myDialog.findViewById<Button>(R.id.Button_pPessoa_choiceRegister).setOnClickListener {
            myDialog.dismiss()
            startActivity(Intent(this, Register_Pessoa::class.java))
            finish()
        }
        myDialog.findViewById<Button>(R.id.Button_JtConta_choiceRegister).setOnClickListener {
            myDialog.dismiss()
        }
    }

    // Função que chama a alertDialog de erro no login:
    private fun alertError(descripton: String) {
        @SuppressLint("InflateParams")
        val dialogBinding = layoutInflater.inflate(R.layout.error_in_login, null)
        val myDialog = Dialog(this)
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(true)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()
        myDialog.findViewById<TextView>(R.id.TextView_DescriptionError_Login).text = descripton
        myDialog.findViewById<Button>(R.id.Button_OK_erroLogin).setOnClickListener {
            myDialog.dismiss()
        }
    }

    // Função que chama a alertDialog de erro para recuperar a senha:
    private fun alertErrorRecSenha(descripton: String) {
        @SuppressLint("InflateParams")
        val dialogBinding = layoutInflater.inflate(R.layout.error_recuperar_senha, null)
        val myDialog = Dialog(this)
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(true)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()
        myDialog.findViewById<TextView>(R.id.TextView_DescriptionError_recSenha).text = descripton
        myDialog.findViewById<Button>(R.id.Button_OK_erro_recSenha).setOnClickListener {
            myDialog.dismiss()
        }
    }

    // Função que chama a alertDialog de sucesso para recuperar a senha:
    private fun alertSucessRecSenha() {
        @SuppressLint("InflateParams")
        val dialogBinding = layoutInflater.inflate(R.layout.sucess_recuperar_senha, null)
        val myDialog = Dialog(this)
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(true)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()
        myDialog.findViewById<Button>(R.id.Button_OK_sucess_recSenha).setOnClickListener {
            myDialog.dismiss()
        }
    }

    // Função que fecha o teclado:
    private fun closeKeyBoard() {
        val view: View? = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
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